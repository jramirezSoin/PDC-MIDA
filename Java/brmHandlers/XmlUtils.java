package brmHandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.StAXEventBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import config.Parameters;
import cust.DateFormater;
import cust.Modality;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Xml;
import cust.ZoneItem;
import exceptions.ExceptionImportExportPricing;
import log.Logger;

/**
*
* @author Adrian Rocha
* @modifiedBy Roger Masis
*/
public class XmlUtils {
	
	private static Document jdomDocTelephonyPrice = null;
	private static Document jdomDocSmsPrice = null;
	private static Document jdomDocTelephonyItem = null;
	private static Document jdomDocSmsItem = null;
	private static Document jdomDocUscSelector = null;
	private static Document jdomDocZonning = null;
	private static Document jdomDocZoneResultConfiguration = null;
	private static Document jdomDocHistory = null;

	public static boolean modItemTelephonyDescription(String idCampaign, String newDesc) {
		Logger.log(Logger.Debug, "modItemTelephonyDescription() - Inicio");
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		Element desc = null;
		try {
			if (jdomDocTelephonyItem == null) {
				reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyItem = builder.build(reader);
			}
			Element root = jdomDocTelephonyItem.getRootElement();
			List<Element> itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			String itemType = "/item/ice/E_N" + idCampaign;
			for (Element element : itemSpecs) {
				if (element.getChildText("type").equals(itemType)) {
					desc = element.getChild("description");
					if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
						Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
						element.setAttribute(a);
					}
					break;
				}
			}
			if (desc == null) {
				Logger.log(Logger.Debug, "No se encontro el item");
				return false;
			}
			Logger.log(Logger.Debug, "Descripci\u00F3n anterior encontrada: " + desc.getText());
			Logger.log(Logger.Debug, "Aplicando nueva descripci\u00F3: " + newDesc);
			desc.setText(newDesc);
			Logger.log(Logger.Debug, "Guardando XML de items");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocTelephonyItem,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar descripcion de item en XML de Telephony: "
					+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL
							+ "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		ItemTypeDeploy.notifyNewItem(false);
		Logger.log(Logger.Debug, "modItemTelephonyDescription() - Fin");
		return true;
	}

	public static boolean addUscSelectorOper(String oper, String zone) {
		Logger.log(Logger.Debug, "addUscSelectorOper(" + zone + ", " + oper + ") - Inicio");
		XMLInputFactory factory = XMLInputFactory.newFactory();
		try {
			if (jdomDocUscSelector == null) {
				XMLEventReader reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocUscSelector = builder.build(reader);
			}
			List<Element> validPeriods = jdomDocUscSelector.getRootElement().getChild("uscSelector")
					.getChildren("validityPeriod");
			Element ruleValid = null;
			Element validFieldValue = null;
			Element period = null;
			int length = 0;
			boolean exists = false;
			boolean hasSpace = false;
			String maxValidFrom = "";
			for (Element v : validPeriods) {
				String validFrom = v.getChildText("validFrom");
				if (validFrom.compareTo(maxValidFrom) > 0) {
					maxValidFrom = validFrom;
					period = v;
				}
			}
			List<Element> rules = period.getChildren("rule");
			for (Element element : rules) {
				String eZoneName = element.getChild("result").getChildText("resultName");
				if (eZoneName.equals(zone)) {
					exists = true;
					List<Element> fieldExpressions = element.getChildren("fieldToValueExpression");
					for (Element element2 : fieldExpressions) {
						if (element2.getChildText("fieldName").equals("AnyEvent.USAGE_CLASS")) {
							validFieldValue = element2.getChild("fieldValue");
							String fieldValue = validFieldValue.getText();
							if (fieldValue.length() < 420) {
								ruleValid = element;
								hasSpace = true;
							}
							break;
						}
					}
					if (hasSpace) {
						break;
					}
					String name = element.getChildText("name");
					if (name.length() > length) {
						length = name.length();
						ruleValid = element;
					}
				}
			}
			if (exists == false) {
				Logger.log(Logger.Debug, "No se encontro el zone name");
				return false;
			}
			if (hasSpace) {
				Logger.log(Logger.Debug, "Agregando operador a regla existente");
				String newText = validFieldValue.getText() + "|" + oper;
				Logger.log(Logger.Debug, "Nuevo valor: " + newText);
				validFieldValue.setText(newText);
				if (ruleValid.getAttribute(Parameters.XML_TAG_FLAG) == null) {
					Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
					ruleValid.setAttribute(a);
				}
			} else {// Caso donde hay que agregar un nuevo rule por falta de espacio
				Logger.log(Logger.Debug, "Creando nueva regla para el operador");
				Element newRule = ruleValid.clone();
				String newName = newRule.getChildText("name") + ".";
				newRule.getChild("name").setText(newName);
				List<Element> fieldExpressions = newRule.getChildren("fieldToValueExpression");
				for (Element element2 : fieldExpressions) {
					if (element2.getChildText("fieldName").equals("AnyEvent.USAGE_CLASS")) {
						validFieldValue = element2.getChild("fieldValue");
						validFieldValue.setText(oper);
						break;
					}
				}
				period.addContent(newRule);
			}
			Logger.log(Logger.Debug, "Nueva operadora registrada: " + oper);
			Logger.log(Logger.Debug, "Guardando XML de USC Selector");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocUscSelector,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar USC Selector en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_USC_SELECTOR + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de USC Selector, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addUscSelectorOper() - Fin");
		return true;
	}

	/**
	 * 
	 * @return Retorna un HashMap cuya clave es el código de operador, el valor es
	 *         un HashMap con clave fecha validFrom y valor lista de zone name + ";"
	 *         + service code + ";" + service class Retorna además por referencia
	 *         las descripciones de las zonas Voz MO
	 */
	public static HashMap<String, HashMap<String, List<String>>> getAllUscSelectorByOper(
			HashMap<String, String> zoneDescriptions) {
		Logger.log(Logger.Debug, "getAllUscSelectorByOper() - Inicio");
		HashMap<String, HashMap<String, List<String>>> opers = new HashMap<String, HashMap<String, List<String>>>();
		XMLInputFactory factory = XMLInputFactory.newFactory();
		try {
			if (jdomDocUscSelector == null) {
				Logger.log(Logger.Debug, "Parseando XML de USC Selector");
				XMLEventReader reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocUscSelector = builder.build(reader);
			}
			Logger.log(Logger.Debug, "Cargando USC Rules por Operador");
			List<Element> validPeriods = jdomDocUscSelector.getRootElement().getChild("uscSelector")
					.getChildren("validityPeriod");
			for (Element validPeriod : validPeriods) {
				String validFrom = validPeriod.getChildText("validFrom");
				List<Element> rules = validPeriod.getChildren("rule");
				for (Element rule : rules) {
					String zoneName = rule.getChild("result").getChildText("resultName");
					if (zoneName.startsWith("TAPIN") || zoneName.startsWith("ZTAPIN")) {
						String usageClass = null;
						String serviceClass = null;
						String serviceCode = null;
						if (!zoneDescriptions.containsKey(zoneName)) {
							String name = rule.getChildText("name");
							zoneDescriptions.put(zoneName, name);
						}
						List<Element> fieldExpressions = rule.getChildren("fieldToValueExpression");
						for (Element field : fieldExpressions) {
							String fName = field.getChildText("fieldName");
							if (fName.equals("AnyEvent.USAGE_CLASS") && usageClass == null) {
								usageClass = field.getChildText("fieldValue");
							} else if (fName.equals("AnyEvent.SERVICE_CLASS")) {
								serviceClass = field.getChildText("fieldValue");
							} else if (fName.equals("AnyEvent.SERVICE_CODE")) {
								serviceCode = field.getChildText("fieldValue");
							}
						}
						if (usageClass != null && serviceClass != null && serviceCode != null) {// Me aseguro que esté
																								// consistente
							String[] opersArray = usageClass.split("\\|");
							for (String o : opersArray) {
								String value = zoneName + ";" + serviceCode + ";" + serviceClass;
								HashMap<String, List<String>> operPeriods;
								if (opers.containsKey(o)) {// 1-Reviso si el operador ya tiene datos
									operPeriods = opers.get(o);
									if (!operPeriods.containsKey(validFrom)) {
										String maxPrevDate = "";
										for (String date : operPeriods.keySet()) {
											if (date.compareTo(maxPrevDate) > 0) {
												maxPrevDate = date;
											}
										}
										if (!maxPrevDate.equals("")) {
											if (operPeriods.get(maxPrevDate).contains(value)) {// Si la configuracion de
																								// tarifa es identica a
																								// la de la fecha
																								// anterior se omite
																								// para mostrar la misma
																								// salida que en el
																								// reporte de IFW
												break;
											}
										}
									}
								} else {// Si no tiene datos le creo un nuevo hash
									operPeriods = new HashMap<String, List<String>>();
									opers.put(o, operPeriods);
								}
								List<String> values;
								if (!operPeriods.containsKey(validFrom)) {// 2-Reviso si tiene datos para la fecha y
																			// sino creo una nueva lista
									values = new ArrayList<String>();
									operPeriods.put(validFrom, values);
								} else {
									values = operPeriods.get(validFrom);
								}
								if (!values.contains(value)) {// Agrego el valor a la lista
									values.add(value);
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar operadores en USC Selector XMLs, Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getAllUscSelectorByOper() - Fin");
		return opers;
	}

	public static boolean addItemRuleSpecTelephony(String idCampaign, String desc) {
		Logger.log(Logger.Debug, "addItemRuleSpecTelephony(" + idCampaign + ", " + desc + ") - Inicio");
		try {
			if (jdomDocTelephonyItem == null) {
				XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyItem = builder.build(reader);
			}
			Element itemTypeSelectors = jdomDocTelephonyItem.getRootElement().getChild("itemTypeSelectors");
			Logger.log(Logger.Debug, "addItemRuleSpecTelephony - calculando maxOrder");
			List<Element> rules = itemTypeSelectors.getChildren("rule");
			Integer maxOrder = 0;
			for (Element element : rules) {
				try {
					Integer order = Integer.parseInt(element.getChildText("ruleOrder"));
					if (order > maxOrder) {
						maxOrder = order;
					}
				} catch (NumberFormatException e) {

				}
			}
			maxOrder++;
			List<Element> itemSpecs = itemTypeSelectors.getChildren("itemSpec");
			List<Element> cloneItemSpecs = new ArrayList<Element>();
			for (Element element : itemSpecs) {
				cloneItemSpecs.add(element.clone());
			}
			itemTypeSelectors.removeChildren("itemSpec");

			Logger.log(Logger.Debug, "Configurando nuevo Rule");
			Element rule = new Element("rule");
			rule.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
			rule.addContent(new Element("ruleName").setText("Regla_ItemTypeSelector_Por_Item_TAG_N" + idCampaign));
			rule.addContent(new Element("ruleOrder").setText(maxOrder.toString()));
			Element expression = new Element("expression");
			expression.addContent(new Element("separator").setText(";"));
			expression.addContent(new Element("operation").setText("EQUAL_TO"));
			expression.addContent(new Element("fieldName").setText("EventDelayedSessionTelcoGsm.PROVIDER_DESCR"));
			expression.addContent(new Element("fieldKind").setText("EVENT_SPEC_FIELD"));
			expression.addContent(new Element("fieldValue").setText("N" + idCampaign));
			rule.addContent(expression);
			rule.addContent(new Element("specName").setText("item-ice-E_N" + idCampaign));
			itemTypeSelectors.addContent(rule);

			itemTypeSelectors.addContent(cloneItemSpecs);

			Logger.log(Logger.Debug, "Configurando nuevo Item Spec");
			Element itemSpec = new Element("itemSpec");
			itemSpec.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
			itemSpec.addContent(new Element("name").setText("item-ice-E_N" + idCampaign));
			itemSpec.addContent(new Element("description").setText(desc));
			itemSpec.addContent(new Element("priceListName").setText("Default"));
			itemSpec.addContent(new Element("obsolete").setText("false"));
			itemSpec.addContent(new Element("type").setText("/item/ice/E_N" + idCampaign));
			itemSpec.addContent(new Element("default").setText("false"));
			itemSpec.addContent(new Element("aggregationType").setText("CUMULATIVE_NONPRECREATE"));
			itemSpec.addContent(new Element("category").setText("usage"));
			itemTypeSelectors.addContent(itemSpec);

			Logger.log(Logger.Debug, "Guardando XML de items");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocTelephonyItem,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer items en XML de Telephony: " + Parameters.XML_DB_DIR
					+ Parameters.XML_ITEM_SELECTORS_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL
							+ "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addItemRuleSpecTelephony() - Fin");
		return true;
	}

	public static boolean addItemRuleSpecSms(String idCampaign, String desc) {
		Logger.log(Logger.Debug, "addItemRuleSpecSms(" + idCampaign + ", " + desc + ") - Inicio");
		try {
			if (jdomDocSmsItem == null) {
				XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsItem = builder.build(reader);
			}
			Element itemTypeSelectors = jdomDocSmsItem.getRootElement().getChild("itemTypeSelectors");
			Logger.log(Logger.Debug, "addItemRuleSpecSms - calculando maxOrder");
			List<Element> rules = itemTypeSelectors.getChildren("rule");
			Integer maxOrder = 0;
			for (Element element : rules) {
				try {
					Integer order = Integer.parseInt(element.getChildText("ruleOrder"));
					if (order > maxOrder) {
						maxOrder = order;
					}
				} catch (NumberFormatException e) {

				}
			}
			maxOrder++;
			List<Element> itemSpecs = itemTypeSelectors.getChildren("itemSpec");
			List<Element> cloneItemSpecs = new ArrayList<Element>();
			for (Element element : itemSpecs) {
				cloneItemSpecs.add(element.clone());
			}
			itemTypeSelectors.removeChildren("itemSpec");

			Logger.log(Logger.Debug, "Configurando nuevo Rule");
			Element rule = new Element("rule");
			rule.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
			rule.addContent(new Element("ruleName").setText("Regla_ItemTypeSelector_Por_Item_TAG_S" + idCampaign));
			rule.addContent(new Element("ruleOrder").setText(maxOrder.toString()));
			Element expression = new Element("expression");
			expression.addContent(new Element("separator").setText(";"));
			expression.addContent(new Element("operation").setText("EQUAL_TO"));
			expression.addContent(new Element("fieldName").setText("EventDelayedSessionTelcoGsm.PROVIDER_DESCR"));
			expression.addContent(new Element("fieldKind").setText("EVENT_SPEC_FIELD"));
			expression.addContent(new Element("fieldValue").setText("S" + idCampaign));
			rule.addContent(expression);
			rule.addContent(new Element("specName").setText("item-ice-E_S" + idCampaign));
			itemTypeSelectors.addContent(rule);

			itemTypeSelectors.addContent(cloneItemSpecs);

			Logger.log(Logger.Debug, "Configurando nuevo Item Spec");
			Element itemSpec = new Element("itemSpec");
			itemSpec.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
			itemSpec.addContent(new Element("name").setText("item-ice-E_S" + idCampaign));
			itemSpec.addContent(new Element("description").setText(desc));
			itemSpec.addContent(new Element("priceListName").setText("Default"));
			itemSpec.addContent(new Element("obsolete").setText("false"));
			itemSpec.addContent(new Element("type").setText("/item/ice/E_S" + idCampaign));
			itemSpec.addContent(new Element("default").setText("false"));
			itemSpec.addContent(new Element("aggregationType").setText("CUMULATIVE_NONPRECREATE"));
			itemSpec.addContent(new Element("category").setText("usage"));
			itemTypeSelectors.addContent(itemSpec);

			Logger.log(Logger.Debug, "Guardando XML de items");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocSmsItem,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer items en XML de SMS: " + Parameters.XML_DB_DIR
					+ Parameters.XML_ITEM_SELECTORS_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS
							+ "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addItemRuleSpecSms() - Fin");
		return true;
	}

	public static String getItemTelephonyDescription(String idCampaign) {
		Logger.log(Logger.Debug, "getItemTelephonyDescription() - Inicio");
		String desc = null;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		try {
			if (jdomDocTelephonyItem == null) {
				XMLEventReader reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyItem = builder.build(reader);
			}
			Element root = jdomDocTelephonyItem.getRootElement();
			List<Element> itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			String itemType = "/item/ice/E_N" + idCampaign;
			for (Element element : itemSpecs) {
				if (element.getChildText("type").equals(itemType)) {
					desc = element.getChildText("description");
					Logger.log(Logger.Debug, "Descripcion encontrada: " + desc);
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar descripcion de item en XML de telefonia: "
					+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getItemTelephonyDescription() - Fin");
		return desc;
	}

	public static HashMap<String, String> getAllItemDescriptions() {
		Logger.log(Logger.Debug, "getAllItemDescriptions() - Inicio");
		HashMap<String, String> desc = new HashMap<String, String>();
		XMLInputFactory factory = XMLInputFactory.newFactory();

		XMLEventReader reader;
		try {
			if (jdomDocTelephonyItem == null) {
				Logger.log(Logger.Debug, "Parseando XML de items TEL");
				reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyItem = builder.build(reader);
			}
			Logger.log(Logger.Debug, "Cargando descripciones TEL");
			Element root = jdomDocTelephonyItem.getRootElement();
			List<Element> itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			for (Element element : itemSpecs) {
				String itemType = element.getChildText("type");
				if (itemType.startsWith("/item/ice/E_N")) {
					String idCampaign = itemType.substring(13);
					desc.put(idCampaign, element.getChildText("description"));
				}
			}
			if (jdomDocSmsItem == null) {
				Logger.log(Logger.Debug, "Parseando XML de items SMS");
				reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsItem = builder.build(reader);
			}
			Logger.log(Logger.Debug, "Cargando descripciones SMS");
			root = jdomDocSmsItem.getRootElement();
			itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			for (Element element : itemSpecs) {
				String itemType = element.getChildText("type");
				if (itemType.startsWith("/item/ice/E_S")) {
					String idCampaign = itemType.substring(13);
					desc.put(idCampaign, element.getChildText("description"));
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar descripciones de items en XMLs, Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getAllItemDescriptions() - Fin");
		return desc;
	}

	public static boolean modItemSmsDescription(String idCampaign, String newDesc) {
		Logger.log(Logger.Debug, "modItemSmsDescription() - Inicio");
		XMLInputFactory factory = XMLInputFactory.newFactory();
		Element desc = null;
		try {
			if (jdomDocSmsItem == null) {
				XMLEventReader reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsItem = builder.build(reader);
			}
			Element root = jdomDocSmsItem.getRootElement();
			List<Element> itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			String itemType = "/item/ice/E_S" + idCampaign;
			for (Element element : itemSpecs) {
				if (element.getChildText("type").equals(itemType)) {
					desc = element.getChild("description");
					if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
						Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
						element.setAttribute(a);
					}
					break;
				}
			}
			if (desc == null) {
				Logger.log(Logger.Debug, "No se encontro el item");
				return false;
			}
			Logger.log(Logger.Debug, "Descripci\u00F3n anterior encontrada: " + desc.getText());
			Logger.log(Logger.Debug, "Aplicando nueva descripci\u00F3: " + newDesc);
			desc.setText(newDesc);
			Logger.log(Logger.Debug, "Guardando XML de items");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocSmsItem,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar descripcion de item en XML de SMS: " + Parameters.XML_DB_DIR
					+ Parameters.XML_ITEM_SELECTORS_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS
							+ "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modItemSmsDescription() - Fin");
		return true;
	}

	public static String getItemSmsDescription(String idCampaign) {
		Logger.log(Logger.Debug, "getItemSmsDescription() - Inicio");
		String desc = null;
		XMLInputFactory factory = XMLInputFactory.newFactory();

		XMLEventReader reader;
		try {
			if (jdomDocSmsItem == null) {
				reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsItem = builder.build(reader);
			}
			Element root = jdomDocSmsItem.getRootElement();
			List<Element> itemSpecs = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			String itemType = "/item/ice/E_S" + idCampaign;
			for (Element element : itemSpecs) {
				if (element.getChildText("type").equals(itemType)) {
					desc = element.getChildText("description");
					Logger.log(Logger.Debug, "Descripcion encontrada: " + desc);
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar descripcion de item en XML de SMS: " + Parameters.XML_DB_DIR
					+ Parameters.XML_ITEM_SELECTORS_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getItemSmsDescription() - Fin");
		return desc;
	}

	public static HashMap<String, PriceTier> getSmsPriceTiers() {
		Logger.log(Logger.Debug, "getSmsPriceTiers() - Inicio");
		HashMap<String, PriceTier> map = new HashMap<String, PriceTier>();
		XMLInputFactory factory = XMLInputFactory.newFactory();

		XMLEventReader reader;
		try {
			if (jdomDocSmsPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsPrice = builder.build(reader);
			}
			// System.out.println(jdomDoc.getRootElement().getName()); // prints "rss"
			// System.out.println(jdomDoc.getRootElement().getNamespacesIntroduced().get(1).getURI());
			// // prints "http://search.yahoo.com/mrss/"
			Element root = jdomDocSmsPrice.getRootElement();
			Element enhacedZoneModel = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChild("applicableRum").getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesSmsDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesSmsDoc) {
				String zoneName = element.getChildText("name");
				if (zoneName.startsWith("E_S")) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					String modelName = model.getChildText("name");
					HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
					List<Element> validityPeriods = model.getChild("usageChargePopModel").getChild("priceTier")
							.getChildren("priceTierValidityPeriod");
					for (Element range : validityPeriods) {
						String validFrom = range.getChildText("validFrom");
						Element priceTierRange = range.getChild("priceTierRange");
						Element charges = priceTierRange.getChild("scaledCharge");
						PriceTierRange ptr = new PriceTierRange(priceTierRange.getChildText("upperBound"), "188",
								charges.getChildText("taxCode"), Double.parseDouble(charges.getChildText("price")),
								charges.getChildText("unitOfMeasure"),
								Double.parseDouble(charges.getChildText("incrementStep")),
								charges.getChildText("glid"));
						List<PriceTierRange> a = new ArrayList<PriceTierRange>();
						a.add(ptr);
						priceTierRanges.put(validFrom, a);
					}
					PriceTier pt = new PriceTier(modelName, priceTierRanges, "EVT");
					map.put(zoneName, pt);
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer XML de charges SMS: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getSmsPriceTiers() - Fin");
		return map;
	}

	public static PriceTier getSmsPriceTier(String zoneName) {
		Logger.log(Logger.Debug, "getSmsPriceTier() - Inicio");
		PriceTier pt = null;
		XMLInputFactory factory = XMLInputFactory.newFactory();

		XMLEventReader reader;
		try {
			if (jdomDocSmsPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsPrice = builder.build(reader);
			}
			// System.out.println(jdomDoc.getRootElement().getName()); // prints "rss"
			// System.out.println(jdomDoc.getRootElement().getNamespacesIntroduced().get(1).getURI());
			// // prints "http://search.yahoo.com/mrss/"
			Element root = jdomDocSmsPrice.getRootElement();
			Element enhacedZoneModel = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChild("applicableRum").getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesSmsDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesSmsDoc) {
				if (element.getChildText("name").equals(zoneName)) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					String modelName = model.getChildText("name");
					HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
					List<Element> validityPeriods = model.getChild("usageChargePopModel").getChild("priceTier")
							.getChildren("priceTierValidityPeriod");
					for (Element range : validityPeriods) {
						String validFrom = range.getChildText("validFrom");
						Element priceTierRange = range.getChild("priceTierRange");
						Element charges = priceTierRange.getChild("scaledCharge");
						PriceTierRange ptr = new PriceTierRange(priceTierRange.getChildText("upperBound"), "188",
								charges.getChildText("taxCode"), Double.parseDouble(charges.getChildText("price")),
								charges.getChildText("unitOfMeasure"),
								Double.parseDouble(charges.getChildText("incrementStep")),
								charges.getChildText("glid"));
						List<PriceTierRange> a = new ArrayList<PriceTierRange>();
						a.add(ptr);
						priceTierRanges.put(validFrom, a);
					}
					pt = new PriceTier(modelName, priceTierRanges, "EVT");
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar SMS charge en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getSmsPriceTier() - Fin");
		return pt;
	}

	public static boolean addSmsPriceTierRange(String zoneName, String validFrom, PriceTierRange priceTierRange) {
		Logger.log(Logger.Debug, "addSmsPriceTier(" + zoneName + ", " + validFrom + ", " + priceTierRange.getPrice()
				+ ", " + priceTierRange.getGlid() + ", " + priceTierRange.getTaxCode() + ") - Inicio");
		boolean updated = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocSmsPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsPrice = builder.build(reader);
			}
			Element root = jdomDocSmsPrice.getRootElement();
			Element enhacedZoneModel = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChild("applicableRum").getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesSmsDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesSmsDoc) {
				if (element.getChildText("name").equals(zoneName)) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					Element priceTier = model.getChild("usageChargePopModel").getChild("priceTier");
					List<Element> validityPeriods = priceTier.getChildren("priceTierValidityPeriod");
					boolean replace = false;
					List<Element> cloneValidityPeriods = new ArrayList<Element>();
					for (Element range : validityPeriods) {
						String validitiPeriodValidFrom = range.getChildText("validFrom");
						if (validFrom.equals(validitiPeriodValidFrom)) {
							replace = true;
							Logger.log(Logger.Debug,
									"getSmsPriceTier - ValidFrom coincide con uno ya configurado, se modifica el precio");
							Element charges = range.getChild("priceTierRange").getChild("scaledCharge");
							charges.getChild("taxCode").setText(priceTierRange.getTaxCode());
							charges.getChild("glid").setText(priceTierRange.getGlid());
							charges.getChild("price").setText(priceTierRange.getPrice().toString());
							break;
						} else if (validitiPeriodValidFrom.compareTo(validFrom) > 0) {
							cloneValidityPeriods.add(range.clone());
							priceTier.removeContent(range);
						}
					}
					if (replace == false) {
						Logger.log(Logger.Debug,
								"getSmsPriceTier - ValidFrom NO coincide con uno ya configurado, se crea un nuevo Tier");
						Element newElem = new Element("priceTierValidityPeriod");
						newElem.addContent(new Element("lowerBound").setText("0"));
						newElem.addContent(new Element("validFrom").setText(validFrom));
						Element newPriceTierRange = new Element("priceTierRange");
						newPriceTierRange.addContent(new Element("upperBound").setText("NO_MAX"));
						Element newScaledCharge = new Element("scaledCharge");
						newScaledCharge.addContent(new Element("price").setText(priceTierRange.getPrice().toString()));
						newScaledCharge.addContent(new Element("unitOfMeasure").setText("NONE"));
						newScaledCharge.addContent(new Element("balanceElementNumCode").setText("188"));
						newScaledCharge.addContent(new Element("discountable").setText("false"));
						newScaledCharge.addContent(new Element("priceType").setText("CONSUMPTION"));
						newScaledCharge.addContent(new Element("taxTime").setText("BILLING_TIME"));
						newScaledCharge.addContent(new Element("taxCode").setText(priceTierRange.getTaxCode()));
						newScaledCharge.addContent(new Element("glid").setText(priceTierRange.getGlid()));
						newScaledCharge.addContent(new Element("incrementStep").setText("1.0"));
						newScaledCharge.addContent(new Element("incrementRounding").setText("UP"));
						newPriceTierRange.addContent(newScaledCharge);
						newElem.addContent(newPriceTierRange);
						Element applicableQuantity = priceTier.getChild("applicableQuantity");
						priceTier.removeChild("applicableQuantity");
						priceTier.addContent(newElem);
						for (Element element2 : cloneValidityPeriods) {
							priceTier.addContent(element2);
						}
						priceTier.addContent(applicableQuantity);
					}
					if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
						Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
						element.setAttribute(a);
					}
					Logger.log(Logger.Debug, "getSmsPriceTier() - Fin");
					updated = true;
					Logger.log(Logger.Debug, "Guardando XML de charges SMS");
					XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
					xmlOut.output(jdomDocSmsPrice,
							new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar SMS charge en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de SMS charge: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getSmsPriceTier() - Fin");
		return updated;
	}

	public static boolean delTelephonyPriceTierRange(String zoneName) {
		Logger.log(Logger.Debug, "delTelephonyPriceTierRange(" + zoneName + ") - Inicio");
		boolean deleted = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocTelephonyPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyPrice = builder.build(reader);
			}
			Element root = jdomDocTelephonyPrice.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			for (Element ar : aplicableRums) {
				Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
				List<Element> chargesTelephonyDoc = enhacedZoneModel.getChildren("results");
				for (Element element : chargesTelephonyDoc) {
					String name = element.getChildText("name");
					if (name.equals(zoneName)) {
						Logger.log(Logger.Debug, "Borrando elemento");
						enhacedZoneModel.removeContent(element);
						deleted = true;
						break;
					}
				}
			}
			if (deleted) {
				Logger.log(Logger.Debug, "Guardando XML de charges TEL");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocTelephonyPrice,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al depurar cargo no utilizado TEL " + zoneName + " en XML: "
					+ Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML al depurar cargo no utilizado TEL " + zoneName
					+ " charge: " + Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "delTelephonyPriceTierRange() - Fin");
		return deleted;
	}

	public static boolean addTelephonyPriceTierRange(String zoneName, String validFrom, PriceTierRange priceTierRange) {
		Logger.log(Logger.Debug,
				"addTelephonyPriceTierRange(" + zoneName + ", " + validFrom + ", " + priceTierRange.getPrice() + ", "
						+ priceTierRange.getGlid() + ", " + priceTierRange.getTaxCode() + ") - Inicio");
		boolean durationPrice = zoneName.startsWith("E_ND");
		boolean updated = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocTelephonyPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyPrice = builder.build(reader);
			}
			Element root = jdomDocTelephonyPrice.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			Element ar = null;
			String rum = "EVT";
			if (durationPrice) {
				rum = "DUR";
			}
			for (Element element : aplicableRums) {
				if (element.getChildText("applicableRumName").equals(rum)) {
					ar = element;
					break;
				}
			}
			Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesTelephonyDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesTelephonyDoc) {
				String name = element.getChildText("name");
				if (name.equals(zoneName)) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					Element priceTier = model.getChild("usageChargePopModel").getChild("priceTier");
					List<Element> validityPeriods = priceTier.getChildren("priceTierValidityPeriod");
					boolean replace = false;
					List<Element> cloneValidityPeriods = new ArrayList<Element>();
					for (Element range : validityPeriods) {
						String validitiPeriodValidFrom = range.getChildText("validFrom");
						if (validFrom.equals(validitiPeriodValidFrom)) {
							replace = true;
							Logger.log(Logger.Debug,
									"getTelephonyPriceTier - ValidFrom coincide con uno ya configurado, se modifica el precio");
							Element charges = range.getChild("priceTierRange").getChild("scaledCharge");
							charges.getChild("taxCode").setText(priceTierRange.getTaxCode());
							charges.getChild("glid").setText(priceTierRange.getGlid());
							charges.getChild("price").setText(priceTierRange.getPrice().toString());
							break;
						} else if (validitiPeriodValidFrom.compareTo(validFrom) > 0) {
							cloneValidityPeriods.add(range.clone());
							priceTier.removeContent(range);
						}
					}
					if (replace == false) {
						Logger.log(Logger.Debug,
								"getTelephonyPriceTier - ValidFrom NO coincide con uno ya configurado, se crea un nuevo Tier");
						Element newElem = new Element("priceTierValidityPeriod");
						newElem.addContent(new Element("lowerBound").setText("0"));
						newElem.addContent(new Element("validFrom").setText(validFrom));
						Element newPriceTierRange = new Element("priceTierRange");
						newPriceTierRange.addContent(new Element("upperBound").setText("NO_MAX"));
						Element newScaledCharge = new Element("scaledCharge");
						newScaledCharge.addContent(new Element("price").setText(priceTierRange.getPrice().toString()));
						newScaledCharge.addContent(new Element("unitOfMeasure").setText("MINUTES"));
						newScaledCharge.addContent(new Element("balanceElementNumCode").setText("188"));
						newScaledCharge.addContent(new Element("discountable").setText("false"));
						newScaledCharge.addContent(new Element("priceType").setText("CONSUMPTION"));
						newScaledCharge.addContent(new Element("taxTime").setText("BILLING_TIME"));
						newScaledCharge.addContent(new Element("taxCode").setText(priceTierRange.getTaxCode()));
						newScaledCharge.addContent(new Element("glid").setText(priceTierRange.getGlid()));
						newScaledCharge.addContent(new Element("incrementStep").setText("1.0"));
						newScaledCharge.addContent(new Element("incrementRounding").setText("UP"));
						newPriceTierRange.addContent(newScaledCharge);
						newElem.addContent(newPriceTierRange);
						Element applicableQuantity = priceTier.getChild("applicableQuantity");
						priceTier.removeChild("applicableQuantity");
						priceTier.addContent(newElem);
						for (Element element2 : cloneValidityPeriods) {
							priceTier.addContent(element2);
						}
						priceTier.addContent(applicableQuantity);
					}
					if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
						Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
						element.setAttribute(a);
					}
					Logger.log(Logger.Debug, "getTelephonyPriceTier() - Updated");
					updated = true;
					Logger.log(Logger.Debug, "Guardando XML de charges TEL");
					XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
					xmlOut.output(jdomDocTelephonyPrice,
							new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar TEL charge en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de TEL charge: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addTelephonyPriceTierRange() - Fin");
		return updated;
	}

	private static Element makeResultElement(String name, PriceTier pt, String compositeSufix) {
		Logger.log(Logger.Debug, "makeResultElement(" + name + ", " + pt.getName() + ", " + pt.getRum() + ", "
				+ compositeSufix + ") - Inicio");
		Element result = new Element("results");
		result.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
		result.addContent(new Element("name").setText(name));
		Element timeConfiguration = new Element("timeConfiguration");
		timeConfiguration.addContent(new Element("timeModelName").setText("TMUNICA"));
		Element tags = new Element("tags");
		tags.addContent(new Element("name").setText("BUNICA"));
		Element crpCompositePopModel = new Element("crpCompositePopModel");
		crpCompositePopModel.addContent(new Element("name").setText(pt.getName() + compositeSufix));
		Element usageChargePopModel = new Element("usageChargePopModel");
		Element priceTier = new Element("priceTier");
		priceTier.addContent(new Element("distributionMethod").setText("FROM_BAL_IMPACT"));
		priceTier.addContent(new Element("tierBasis").addContent(new Element("rumTierExpression")));
		priceTier.addContent(new Element("enforceCreditLimit").setText("false"));
		priceTier.addContent(new Element("rumName").setText(pt.getRum()));
		List<String> keys = new ArrayList<String>();
		for (String date : pt.getPriceTierRanges().keySet()) {
			keys.add(date);
		}
		Collections.sort(keys);
		for (String date : keys) {
			PriceTierRange ptr = pt.getPriceTierRanges().get(date).get(0);
			Logger.log(Logger.Debug,
					"makeResultElement - price tier range - " + ptr.getBalanceElementNumCode() + ", " + ptr.getGlid()
							+ ", " + ptr.getTaxCode() + ", " + ptr.getUnitOfMeasure() + ", " + ptr.getIncrementStep()
							+ ", " + ptr.getPrice());
			Element priceTierValidityPeriod = new Element("priceTierValidityPeriod");
			priceTierValidityPeriod.addContent(new Element("lowerBound").setText("0"));
			priceTierValidityPeriod.addContent(new Element("validFrom").setText(date));
			Element newPriceTierRange = new Element("priceTierRange");
			if (pt.getRum().equals("DUR")) {
				newPriceTierRange.addContent(new Element("upperBound").setText("NO_MAX"));
			} else {
				newPriceTierRange.addContent(new Element("upperBound").setText("1"));
			}
			Element newScaledCharge = new Element("scaledCharge");
			newScaledCharge.addContent(new Element("price").setText(ptr.getPrice().toString()));
			newScaledCharge.addContent(new Element("unitOfMeasure").setText(ptr.getUnitOfMeasure()));
			newScaledCharge.addContent(new Element("balanceElementNumCode").setText("188"));
			newScaledCharge.addContent(new Element("discountable").setText("false"));
			newScaledCharge.addContent(new Element("priceType").setText("CONSUMPTION"));
			newScaledCharge.addContent(new Element("taxTime").setText("BILLING_TIME"));
			newScaledCharge.addContent(new Element("taxCode").setText(ptr.getTaxCode()));
			newScaledCharge.addContent(new Element("glid").setText(ptr.getGlid()));
			newScaledCharge.addContent(new Element("incrementStep").setText("1.0"));
			newScaledCharge.addContent(new Element("incrementRounding").setText("UP"));
			newPriceTierRange.addContent(newScaledCharge);
			priceTierValidityPeriod.addContent(newPriceTierRange);
			priceTier.addContent(priceTierValidityPeriod);
		}
		priceTier.addContent(new Element("applicableQuantity").setText("ORIGINAL"));
		usageChargePopModel.addContent(priceTier);
		crpCompositePopModel.addContent(usageChargePopModel);
		tags.addContent(crpCompositePopModel);
		timeConfiguration.addContent(tags);
		result.addContent(timeConfiguration);
		Logger.log(Logger.Debug, "makeResultElement() - Fin");
		return result;
	}

	public static boolean addTelephonyPriceTier(PriceTier priceTierDuration, PriceTier priceTierEvent) {
		boolean updated = false;
		Logger.log(Logger.Debug, "addTelephonyPriceTier(" + priceTierDuration.getName() + ", "
				+ priceTierEvent.getName() + ") - Inicio");
		try {
			if (jdomDocTelephonyPrice == null) {
				XMLEventReader reader = XMLInputFactory.newFactory()
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyPrice = builder.build(reader);
			}
			Element root = jdomDocTelephonyPrice.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			Element arDur = null, arEvt = null;
			for (Element element : aplicableRums) {
				if (element.getChildText("applicableRumName").equals("DUR")) {
					arDur = element;
					;
				} else if (element.getChildText("applicableRumName").equals("EVT")) {
					arEvt = element;
				}
			}
			Element enhacedZoneModelDur = arDur.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			Element enhacedZoneModelEvt = arEvt.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			// Se agregan results para duración y evento
			enhacedZoneModelDur.addContent(makeResultElement("E_" + priceTierDuration.getName(), priceTierDuration,
					"_Rule_PMS_TEL_DEF_" + priceTierDuration.getName()));
			enhacedZoneModelEvt.addContent(makeResultElement("E_" + priceTierEvent.getName(), priceTierEvent, ""));

			Logger.log(Logger.Debug, "Guardando XML de charges TEL");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocTelephonyPrice,
					new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
			updated = true;
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar TEL charge en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de TEL charge: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addTelephonyPriceTier() - Fin");
		return updated;
	}

	public static void addSmsPriceTier(String name, PriceTier priceTier) {
		Logger.log(Logger.Debug, "addSmsPriceTier(" + priceTier.getName() + ") - Inicio");
		try {
			if (jdomDocSmsPrice == null) {
				XMLEventReader reader = XMLInputFactory.newFactory()
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocSmsPrice = builder.build(reader);
			}
			Element root = jdomDocSmsPrice.getRootElement();
			Element aplicableRum = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChild("applicableRum");

			Element enhacedZoneModel = aplicableRum.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			// Se agregan el nuevo result
			enhacedZoneModel.addContent(makeResultElement(name, priceTier, ""));

			Logger.log(Logger.Debug, "Guardando XML de charges SMS");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocSmsPrice, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al buscar SMS charge en XML: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de SMS charge: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_SMS + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addSmsPriceTier() - Fin");
	}

	public static HashMap<String, PriceTier> getTelephonyPriceTiers() {
		Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Inicio");
		HashMap<String, PriceTier> map = new HashMap<String, PriceTier>();
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocTelephonyPrice == null) {
				reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyPrice = builder.build(reader);
			}
			Element root = jdomDocTelephonyPrice.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			Element ar = null;
			for (Element element : aplicableRums) {
				if (element.getChildText("applicableRumName").equals("DUR")) {
					ar = element;
					break;
				}
			}
			Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Cargando PriceTiers por duracion");
			Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesTelephonyDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesTelephonyDoc) {
				String zoneName = element.getChildText("name");
				if (zoneName.startsWith("E_ND")) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					String modelName = model.getChildText("name");
					HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
					List<Element> validityPeriods = model.getChild("usageChargePopModel").getChild("priceTier")
							.getChildren("priceTierValidityPeriod");
					for (Element range : validityPeriods) {
						String validFrom = range.getChildText("validFrom");
						Element priceTierRange = range.getChild("priceTierRange");
						Element charges = priceTierRange.getChild("scaledCharge");
						PriceTierRange ptr = new PriceTierRange(priceTierRange.getChildText("upperBound"), "188",
								charges.getChildText("taxCode"), Double.parseDouble(charges.getChildText("price")),
								charges.getChildText("unitOfMeasure"),
								Double.parseDouble(charges.getChildText("incrementStep")),
								charges.getChildText("glid"));
						List<PriceTierRange> a = new ArrayList<PriceTierRange>();
						a.add(ptr);
						priceTierRanges.put(validFrom, a);
					}

					PriceTier pt = new PriceTier(modelName, priceTierRanges, "DUR");
					map.put(zoneName, pt);
				}
			}
			ar = null;
			for (Element element : aplicableRums) {
				if (element.getChildText("applicableRumName").equals("EVT")) {
					ar = element;
					break;
				}
			}
			Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Cargando PriceTiers por evento");
			enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			chargesTelephonyDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesTelephonyDoc) {
				String zoneName = element.getChildText("name");
				if (zoneName.startsWith("E_NE")) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					String modelName = model.getChildText("name");
					HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
					List<Element> validityPeriods = model.getChild("usageChargePopModel").getChild("priceTier")
							.getChildren("priceTierValidityPeriod");
					for (Element range : validityPeriods) {
						String validFrom = range.getChildText("validFrom");
						Element priceTierRange = range.getChild("priceTierRange");
						Element charges = priceTierRange.getChild("scaledCharge");
						PriceTierRange ptr = new PriceTierRange(priceTierRange.getChildText("upperBound"), "188",
								charges.getChildText("taxCode"), Double.parseDouble(charges.getChildText("price")),
								charges.getChildText("unitOfMeasure"),
								Double.parseDouble(charges.getChildText("incrementStep")),
								charges.getChildText("glid"));
						List<PriceTierRange> l = new ArrayList<PriceTierRange>();
						l.add(ptr);
						priceTierRanges.put(validFrom, l);
					}
					PriceTier pt = new PriceTier(modelName, priceTierRanges, "EVT");
					map.put(zoneName, pt);
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer XML de charges Telephony: " + Parameters.XML_DB_DIR
					+ Parameters.XML_CHARGES_TEL + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Fin");
		return map;
	}

	public static PriceTier getTelephonyPriceTier(String zoneName) {
		Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Inicio");
		PriceTier pt = null;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		String xmlCharges = Parameters.XML_CHARGES_TEL;
		try {
			if (jdomDocTelephonyPrice == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + xmlCharges));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocTelephonyPrice = builder.build(reader);
			}
			Element root = jdomDocTelephonyPrice.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			Element ar = null;
			String rum = "EVT";
			if (zoneName.startsWith("E_ND")) {
				rum = "DUR";
			}
			for (Element element : aplicableRums) {
				if (element.getChildText("applicableRumName").equals(rum)) {
					ar = element;
					break;
				}
			}
			Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
			List<Element> chargesTelephonyDoc = enhacedZoneModel.getChildren("results");
			for (Element element : chargesTelephonyDoc) {
				String name = element.getChildText("name");
				if (name.equals(zoneName)) {
					Element model = element.getChild("timeConfiguration").getChild("tags")
							.getChild("crpCompositePopModel");
					String modelName = model.getChildText("name");
					HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
					List<Element> validityPeriods = model.getChild("usageChargePopModel").getChild("priceTier")
							.getChildren("priceTierValidityPeriod");
					for (Element range : validityPeriods) {
						String validFrom = range.getChildText("validFrom");
						Element priceTierRange = range.getChild("priceTierRange");
						Element charges = priceTierRange.getChild("scaledCharge");// Se toma como supuesto que solo sera
																					// un priceTierRange por cada
																					// priceTierValidityPeriod
						PriceTierRange ptr = new PriceTierRange(priceTierRange.getChildText("upperBound"), "188",
								charges.getChildText("taxCode"), Double.parseDouble(charges.getChildText("price")),
								charges.getChildText("unitOfMeasure"),
								Double.parseDouble(charges.getChildText("incrementStep")),
								charges.getChildText("glid"));
						List<PriceTierRange> l = new ArrayList<PriceTierRange>();
						l.add(ptr);
						priceTierRanges.put(validFrom, l);
					}
					pt = new PriceTier(modelName, priceTierRanges, rum);
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de charges Telephony: " + Parameters.XML_DB_DIR + xmlCharges + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getTelephonyPriceTier() - Fin");
		return pt;
	}

	/**
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws JDOMException
	 * @author Adrian
	 * @return Retorna un hashmap (ZoneName -> (list <ZoneItem>)) ya que un ZoneName
	 *         puede corresponder a varios zone items, en los casos que la campaña
	 *         ha tenido mas de una vigencia
	 */
	public static HashMap<String, List<ZoneItem>> getAllZoneItems() {
		Logger.log(Logger.Debug, "getAllZoneItems() - Inicio");
		HashMap<String, List<ZoneItem>> map = new HashMap<String, List<ZoneItem>>();
		XMLInputFactory factory = XMLInputFactory.newFactory();

		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			// System.out.println(jdomDoc.getRootElement().getName()); // prints "rss"
			// System.out.println(jdomDoc.getRootElement().getNamespacesIntroduced().get(1).getURI());
			// // prints "http://search.yahoo.com/mrss/"
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zoneName = zoneResult.getChildText("zoneName");
				String productName = element.getChildText("productName");
				if ((zoneName.startsWith("E_S") && productName.equals("TelcoGsmSms"))
						|| (zoneName.startsWith("E_N") && productName.equals("TelcoGsmTelephony"))) {
					String ic = zoneName;
					if (productName.equals("TelcoGsmTelephony")) {
						ic = "E_N" + zoneName.substring(4);
					}
					ZoneItem zm = new ZoneItem(element.getChildText("destinationPrefix"), zoneName, productName,
							element.getChildText("validFrom"), element.getChildText("validTo"));
					List<ZoneItem> l;
					if (map.containsKey(ic)) {
						l = map.get(ic);
						l.add(zm);
					} else {
						l = new ArrayList<ZoneItem>();
						l.add(zm);
						map.put(ic, l);
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getAllZoneItems() - Fin");
		return map;
	}

	public static HashMap<String, List<ZoneItem>> getAllZoneInternationalDestins() {
		Logger.log(Logger.Debug, "getAllZoneInternationalDestins() - Inicio");
		HashMap<String, List<ZoneItem>> map = new HashMap<String, List<ZoneItem>>();
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zoneName = zoneResult.getChildText("zoneName");
				String productName = element.getChildText("productName");
				String destinationPrefix = element.getChildText("destinationPrefix");
				if (zoneName.contains("MI") && productName.equals("TelcoGsmTelephony")) {
					ZoneItem zm = new ZoneItem(destinationPrefix, zoneName, productName,
							element.getChildText("validFrom"), element.getChildText("validTo"));
					List<ZoneItem> l;
					if (map.containsKey(destinationPrefix)) {
						l = map.get(destinationPrefix);
						l.add(zm);
					} else {
						l = new ArrayList<ZoneItem>();
						l.add(zm);
						map.put(destinationPrefix, l);
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getAllZoneInternationalDestins() - Fin");
		return map;
	}

	public static boolean modZoneItemResult(String productName, String newZoneName, String destinationPrefix) {
		Logger.log(Logger.Debug, "modZoneItemResult() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Logger.screen(Logger.Debug, "XML de zonning cargado");
			List<Element> zoneItemsDoc = jdomDocZonning.getRootElement().getChild("standardZoneModel")
					.getChildren("zoneItem");
			Logger.screen(Logger.Debug, "Buscando destino a modificar");
			for (Element element : zoneItemsDoc) {
				String itemProductName = element.getChildText("productName");
				String itemDestinationPrefix = element.getChildText("destinationPrefix");
				if (itemProductName.equals(productName) && itemDestinationPrefix.equals(destinationPrefix)) {
					Element zoneName = element.getChild("zoneResult").getChild("zoneName");
					String actualName = zoneName.getText();
					if (actualName.equals(newZoneName)) {
						Logger.log(Logger.Debug, "El zone name indicado es igual al que se tiene configurado");
					} else {
						zoneName.setText(newZoneName);
						Logger.log(Logger.Debug, "Se actualiza el zone name");
						if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
							Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
							element.setAttribute(a);
						}
						found = true;
					}
				}
			}
			if (found) {
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocZonning, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONNING + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modZoneItemResult() - Fin");
		return found;
	}

	public static boolean addZoneResult(String zoneName, String description) {
		Logger.log(Logger.Debug, "addZoneResult(" + zoneName + ", " + description + ") - Inicio");
		boolean notFound = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZoneResultConfiguration == null) {
				reader = factory.createXMLEventReader(
						new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZoneResultConfiguration = builder.build(reader);
			}
			List<Element> zoneResults = jdomDocZoneResultConfiguration.getRootElement()
					.getChildren("zoneResultConfiguration");
			for (Element element : zoneResults) {
				String name = element.getChildText("name");
				if (name.equals(zoneName)) {
					Logger.log(Logger.Debug, "El Zone Result ya existe, se continua la configuracion");
					notFound = true;
					break;
				}
			}
			if (notFound == false) {
				Logger.log(Logger.Debug, "Se agrega un nuevo registro Zone Result");
				Element newElem = zoneResults.get(1).clone();
				newElem.removeChild("internalId");
				newElem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
				newElem.getChild("name").setText(zoneName);
				newElem.getChild("description").setText(description);
				newElem.getChild("result").setText(zoneName);
				jdomDocZoneResultConfiguration.getRootElement().addContent(newElem);
				Logger.log(Logger.Debug, "Guardando XML de Zone Result Configuration");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocZoneResultConfiguration,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer XML de Zone Result Configuration: " + Parameters.XML_DB_DIR
					+ Parameters.XML_ZONE_RESULT_CONFIGURATION + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de Zone Result Configuration, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION
							+ "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addZoneResult() - Fin");
		return notFound;
	}

	public static boolean addZoneItem(String productName, String zoneName, String validFrom, String validTo,
			String destinationPrefix, String description) {
		Logger.log(Logger.Debug, "addZoneItem() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element standardZoneModel = jdomDocZonning.getRootElement().getChild("standardZoneModel");
			Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
			Element newElem = new Element("zoneItem");
			newElem.addContent(new Element("productName").setText(productName));
			newElem.addContent(new Element("originPrefix").setText("00"));
			newElem.addContent(new Element("destinationPrefix").setText(destinationPrefix));
			newElem.addContent(new Element("validFrom").setText(validFrom));
			newElem.addContent(new Element("validTo").setText(validTo));
			newElem.addContent(new Element("zoneResult").addContent(new Element("zoneName").setText(zoneName)));
			Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
			newElem.setAttribute(a);
			standardZoneModel.addContent(newElem);
			Logger.log(Logger.Debug, "Guardando XML de zonning");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocZonning, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			addZoneResult(zoneName, description);
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONNING + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addZoneItem() - Fin");
		return found;
	}

	public static boolean modZoneItemsValidTo(String zoneName, String newValidTo, String validToActual,
			boolean showMessage) {
		Logger.log(Logger.Debug, "modZoneItemsValidTo() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			List<Element> newElems = new ArrayList<Element>();
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zone = zoneResult.getChildText("zoneName");
				if (zoneName.equals(zone)) {
					Element validTo = element.getChild("validTo");
					if (validToActual.equals(validTo.getText())) {
						found = true;
						if (newValidTo.compareTo(validTo.getText()) > 0) {
							String msg = "La fecha ingresada es mayor a la configurada actualmente - " + newValidTo
									+ " > " + validTo.getText();
							if (showMessage) {
								Logger.screen(Logger.Debug, msg);
							} else {
								Logger.log(Logger.Debug, msg);
							}
							Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
							String newValidFrom = DateFormater.sumDays(validTo.getText(), 1);
							Element newElem = new Element("zoneItem");
							newElem.addContent(new Element("productName").setText(element.getChildText("productName")));
							newElem.addContent(
									new Element("originPrefix").setText(element.getChildText("originPrefix")));
							newElem.addContent(new Element("destinationPrefix")
									.setText(element.getChildText("destinationPrefix")));
							newElem.addContent(new Element("validFrom").setText(newValidFrom));
							newElem.addContent(new Element("validTo").setText(newValidTo));
							newElem.addContent(
									new Element("zoneResult").addContent(new Element("zoneName").setText(zone)));
							Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
							newElem.setAttribute(a);
							newElems.add(newElem);
						} else {
							String msg = "La fecha ingresada es menor a la configurada actualmente - " + newValidTo
									+ " < " + validTo.getText();
							if (showMessage) {
								Logger.screen(Logger.Debug, msg);
								Logger.screen(Logger.Debug, "Se modifica el registro Zone Item actual");
							} else {
								Logger.log(Logger.Debug, msg);
							}
							if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
								Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
								element.setAttribute(a);
							}
							validTo.setText(newValidTo);
						}
						break;
					}
				}
			}
			if (found) {
				if (newElems.size() > 0) {
					Logger.log(Logger.Debug, "Agregando nuevos zoneItems generados");
					zoneItemsDoc.addAll(newElems);
				}
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocZonning, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONNING + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modZoneItemsValidTo() - Fin");
		return found;
	}

	/**
	 * Método para cambiar el tipo de cobro en campaña 900 existente, permite
	 * pasar de cobro por Duración a cobro por Evento y viceversa
	 * 
	 * @param zoneName zone actual que se debe modificar/dividir
	 * @param dateFrom fecha desde la cual aplicar el cambio
	 * @return retorna true si se pudo encontrar el zone y aplicar el cambio
	 */
	public static boolean modZoneItemsRum900(String zoneName, String dateFrom) {
		Logger.log(Logger.Debug, "modZoneItemsRum900() - Inicio");
		boolean found = false;
		try {
			if (jdomDocZonning == null) {
				XMLInputFactory factory = XMLInputFactory.newFactory();
				XMLEventReader reader = factory
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			String newZone;
			String resultDescription;
			if (zoneName.startsWith("E_ND")) {
				newZone = "E_NE" + zoneName.substring(4);
				resultDescription = "ECE_EVT";
			} else {
				newZone = "E_ND" + zoneName.substring(4);
				resultDescription = "ECE_DUR";
			}
			Element newElem = null;
			Element root = jdomDocZonning.getRootElement();
			Element standardZoneModel = root.getChild("standardZoneModel");
			List<Element> zoneItemsDoc = standardZoneModel.getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				Element zone = zoneResult.getChild("zoneName");
				if (zoneName.equals(zone.getText())) {
					Element validTo = element.getChild("validTo");
					Element validFrom = element.getChild("validFrom");
					if ((dateFrom.compareTo(validFrom.getText()) >= 0)
							&& (dateFrom.compareTo(validTo.getText()) <= 0)) {
						Logger.log(Logger.Debug,
								"modZoneItemsRum900() - Rango encontrado: " + validFrom + " - " + validTo);
						found = true;
						if (dateFrom.equals(validFrom.getText())) {// En caso de coincidir con el inicio del rango
																	// actual, directamente hay que reemplazarlo
							Logger.log(Logger.Debug, "modZoneItemsRum900() - Se reemplaza el zone result: " + newZone);
							zone.setText(newZone);
						} else {
							Logger.log(Logger.Debug, "modZoneItemsRum900() - Se crea un nuevo zone result: " + newZone);
							String validToActual = validTo.getText();
							String newValidToActual = DateFormater.sumDays(dateFrom, -1);
							Logger.log(Logger.Debug,
									"modZoneItemsRum900() - Modificando vigencia del actual: " + newValidToActual);
							element.getChild("validTo").setText(newValidToActual);
							Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
							newElem = new Element("zoneItem");
							newElem.addContent(new Element("productName").setText(element.getChildText("productName")));
							newElem.addContent(
									new Element("originPrefix").setText(element.getChildText("originPrefix")));
							newElem.addContent(new Element("destinationPrefix")
									.setText(element.getChildText("destinationPrefix")));
							newElem.addContent(new Element("validFrom").setText(dateFrom));
							newElem.addContent(new Element("validTo").setText(validToActual));
							newElem.addContent(
									new Element("zoneResult").addContent(new Element("zoneName").setText(newZone)));
							Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
							newElem.setAttribute(a);
						}
						if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
							Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
							element.setAttribute(a);
						}
						break;
					}
					// else if (found && (dateFrom.compareTo(validFrom.getText()) < 0) &&
					// (dateFrom.compareTo(validTo.getText()) < 0)) { //En caso de que haya otros
					// rangos posteriores tambien se cambian
					// zone.setText(newZone);
					// }Para el caso que se tengan que modificar los zonning posteriores
				}
			}
			if (found) {
				if (newElem != null) {
					standardZoneModel.addContent(newElem);
				}
				addZoneResult(newZone, resultDescription);
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocZonning, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONNING + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modZoneItemsRum900() - Fin");
		return found;
	}

	public static boolean isZoneDestinValidAtDate(String destin, String date) {
		Logger.log(Logger.Debug, "isZoneDestinValidAtDate() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				String destination = element.getChildText("destinationPrefix");
				if (destination.equals(destin)) {
					String validTo = element.getChildText("validTo");
					String validFrom = element.getChildText("validFrom");
					if ((date.compareTo(validFrom) >= 0) && (date.compareTo(validTo) <= 0)) {
						found = true;
						break;
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "isZoneDestinValidAtDate() - Fin");
		return found;
	}

	public static boolean isZoneValidAtDate(String zoneName, String date) {
		Logger.log(Logger.Debug, "isZoneValidAtDate() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zone = zoneResult.getChildText("zoneName");
				if (zoneName.equals(zone)) {
					String validTo = element.getChildText("validTo");
					String validFrom = element.getChildText("validFrom");
					if ((date.compareTo(validFrom) >= 0) && (date.compareTo(validTo) <= 0)) {
						found = true;
						break;
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "isZoneValidAtDate() - Fin");
		return found;
	}

	public static boolean isZoneValid(String zoneName) {
		Logger.log(Logger.Debug, "isZoneValid() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zone = zoneResult.getChildText("zoneName");
				if (zoneName.equals(zone)) {
					found = true;
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "isZoneValid() - Fin");
		return found;
	}

	public static boolean modZoneItemsDestinationPrefix(String zoneName, String date, String destinationPrefix) {
		Logger.log(Logger.Debug, "modZoneItemsDestinationPrefix() - Inicio");
		boolean found = false;
		XMLInputFactory factory = XMLInputFactory.newFactory();
		XMLEventReader reader;
		try {
			if (jdomDocZonning == null) {
				reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocZonning = builder.build(reader);
			}
			Element root = jdomDocZonning.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			List<Element> newElems = new ArrayList<Element>();
			for (Element element : zoneItemsDoc) {
				Element zoneResult = element.getChild("zoneResult");
				String zone = zoneResult.getChildText("zoneName");
				if (zoneName.equals(zone)) {
					String validFrom = element.getChildText("validFrom");
					String validTo = element.getChildText("validTo");
					// TODO analizar que se hace con los casos de fechas posteriores
					if (date.compareTo(validFrom) >= 0 && date.compareTo(validTo) < 0) {
						Logger.log(Logger.Debug,
								"Vigencia de campa\u00F1a encontrada - " + validFrom + " - " + validTo);
						found = true;
						Element destin = element.getChild("destinationPrefix");
						if (destin.getText().equals(destinationPrefix)) {
							Logger.screen(Logger.Error,
									"El n\u00FAmero indicado ya se encuentra configurado en la campa\u00F1a, Saliendo!");
							System.exit(Parameters.ERR_INVALID_VALUE);
						}
						if (date.equals(validFrom)) {
							Logger.log(Logger.Debug,
									"Como la fecha indicada coincide con el inicio se modifica el zoneItem y no se crea uno nuevo");
							destin.setText(destinationPrefix);
						} else {
							Logger.log(Logger.Debug,
									"Como la fecha indicada NO coincide con el inicio se modifica la vigencia del zoneItem actual y se crea uno nuevo");
							String newValidTo = DateFormater.sumDays(date, -1);
							Element newElem = new Element("zoneItem");
							newElem.addContent(new Element("productName").setText(element.getChildText("productName")));
							newElem.addContent(
									new Element("originPrefix").setText(element.getChildText("originPrefix")));
							newElem.addContent(new Element("destinationPrefix").setText(destinationPrefix));
							newElem.addContent(new Element("validFrom").setText(date));
							newElem.addContent(new Element("validTo").setText(element.getChildText("validTo")));
							newElem.addContent(
									new Element("zoneResult").addContent(new Element("zoneName").setText(zone)));
							element.getChild("validTo").setText(newValidTo);
							if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) {
								Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
								element.setAttribute(a);
							}
							Attribute a = new Attribute(Parameters.XML_TAG_FLAG, "update");
							newElem.setAttribute(a);
							newElems.add(newElem);
						}
					}
				}
			}
			if (found) {
				if (newElems.size() > 0) {
					Logger.log(Logger.Debug, "Agregando nuevos zoneItems generados");
					zoneItemsDoc.addAll(newElems);
				}
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDocZonning, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			}
		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ Parameters.XML_DB_DIR + Parameters.XML_ZONNING + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "getAllZoneItems() - Fin");
		return found;
	}

	public static HashMap<String, String> getHistory() {
		Logger.log(Logger.Debug, "XmlUtils.getHistory() - Inicio");
		HashMap<String, String> map = new HashMap<String, String>();
		if (jdomDocHistory == null) {
			XMLEventReader reader;
			try {
				reader = XMLInputFactory.newFactory()
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_HISTORY));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocHistory = builder.build(reader);
			} catch (FileNotFoundException | XMLStreamException | JDOMException | FactoryConfigurationError e) {
				Logger.screen(Logger.Error, e.toString());
				Logger.screen(Logger.Error,
						"Error al leer XML de historial: " + Parameters.XML_DB_DIR + Parameters.XML_HISTORY);
				System.exit(Parameters.ERR_FILESYSTEM);
			}
		}
		Element root = jdomDocHistory.getRootElement();
		for (Element e : root.getChildren()) {
			String key = e.getName();
			String value = e.getAttributeValue("modifyDate");
			map.put(key, value);
		}
		Logger.log(Logger.Debug, "XmlUtils.getHistory() - Fin");
		return map;
	}

	public static void updateHistoryXml(String zoneName) {
		Logger.screen(Logger.Debug, "Generando fecha en el historial");
		if (jdomDocHistory == null) {
			XMLEventReader reader;
			try {
				reader = XMLInputFactory.newFactory()
						.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_HISTORY));
				StAXEventBuilder builder = new StAXEventBuilder();
				jdomDocHistory = builder.build(reader);
			} catch (FileNotFoundException | XMLStreamException | JDOMException | FactoryConfigurationError e) {
				Logger.screen(Logger.Error, e.toString());
				Logger.screen(Logger.Error,
						"Error al leer XML de historial: " + Parameters.XML_DB_DIR + Parameters.XML_HISTORY);
				return;
			}
		}
		Element root = jdomDocHistory.getRootElement();
		Element zone = root.getChild(zoneName);
		if (zone == null) {
			Logger.log(Logger.Debug, "No se encontro el zoneName, se agregara uno nuevo");
			Element newZone = new Element(zoneName);
			newZone.setAttribute("modifyDate", DateFormater.dateToString(new Date()));
			root.addContent(newZone);
		} else {
			Logger.log(Logger.Debug, "Se actualiza el zoneName encontrado");
			zone.setAttribute("modifyDate", DateFormater.dateToString(new Date()));
		}
		try {
			Logger.log(Logger.Debug, "Guardando XML de historial");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(jdomDocHistory, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_HISTORY));
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al guardar el cambio en el XML de historial: " + Parameters.XML_DB_DIR
					+ Parameters.XML_HISTORY + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "updateHistoryXml() - Fin");
	}

	public static boolean publishChanges(boolean force) {
		Logger.log(Logger.Debug, "publishChanges() - Inicio");
		boolean changesZoneResult = false;
		boolean changesZonning = false;
		boolean changesUscSelector = false;
		boolean changesItemsTel = false;
		boolean changesItemsSms = false;
		boolean changesChargesTel = false;
		boolean changesChargesTelUsd = false;
		boolean changesChargesSms = false;
		boolean refreshNeededZoneResult = false;
		boolean refreshNeededZonning = false;
		boolean refreshNeededItemsTel = false;
		boolean refreshNeededItemsSms = false;
		boolean refreshNeededChargesTel = false;
		boolean refreshNeededChargesSms = false;
		boolean changesZonningPreSms = false;
		boolean changesZonningPreTel = false;
		boolean changesZonningHibSms = false;
		boolean changesZonningHibTel = false;
		boolean changesZonningHibHorTel = false;
		boolean changesZonningPreMida = false;
		boolean changesZonningHibMida = false;
		boolean changesZonningCCFMida = false;
		boolean changesZonningCCMMida = false;
		boolean changesChargesPreTel = false;
		boolean changesChargesPreSms = false;
		boolean changesChargesHibTel = false;
		boolean changesChargesHibSms = false;
		boolean changesChargesHibHorTel = false;
		boolean changesChargesPreMida = false;
		boolean changesChargesHibMida = false;
		boolean changesChargesCCFMida = false;
		boolean changesChargesCCMMida = false;
		
		XMLInputFactory factory = XMLInputFactory.newFactory();
		try {
			Logger.screen(Logger.Debug, "Analizando Zone Result Configuration");
			XMLEventReader reader = factory.createXMLEventReader(
					new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION));
			StAXEventBuilder builder = new StAXEventBuilder();
			Document jdomDoc = builder.build(reader);
			reader.close();
			Element root = jdomDoc.getRootElement();
			List<Element> zoneResultsDoc = root.getChildren();
			for (Element element : zoneResultsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesZoneResult == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML de Zone Result Configuration");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
								+ Parameters.XML_ZONE_RESULT_CONFIGURATION + ".bk" + DateFormater.dateToShortString()));
					}
					changesZoneResult = true;
					if (a.getValue().equals("new")) {
						refreshNeededZoneResult = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en zonning, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			if (changesZoneResult || force) {
				Logger.log(Logger.Debug, "Guardando XML de Zone Result Configuration");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc, new FileOutputStream(
						Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Zone Result Configuration");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION + ".process", "-config", args);
				jdomDocZoneResultConfiguration = null;
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION);
				if (refreshNeededZoneResult) {
					ImportExportPricing.genZoneResultConfigurationXml(true);
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Zone Result Configuration");
			}
			zoneResultsDoc = null;

			Logger.screen(Logger.Debug, "Analizando Zonning");
			reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ZONNING));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			reader.close();
			root = jdomDoc.getRootElement();
			List<Element> zoneItemsDoc = root.getChild("standardZoneModel").getChildren("zoneItem");
			for (Element element : zoneItemsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesZonning == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML de zonning");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING
								+ ".bk" + DateFormater.dateToShortString()));
					}
					changesZonning = true;
					if (a.getValue().equals("new")) {
						refreshNeededZonning = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en zonning, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			if (changesZonning || force) {
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en zonning");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_ZONNING + ".process", "-config", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_ZONNING);
				jdomDocZonning = null;
				if (refreshNeededZonning) {
					ImportExportPricing.genZonningBaseXml(true);
					;
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Zonning");
			}
			zoneItemsDoc = null;

			Logger.screen(Logger.Debug, "Analizando USC Map");
			reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			reader.close();
			root = jdomDoc.getRootElement();
			List<Element> uscValidityDoc = root.getChild("uscSelector").getChildren("validityPeriod");
			for (Element uscValidity : uscValidityDoc) {
				List<Element> uscSelectorDoc = uscValidity.getChildren("rule");
				for (Element element : uscSelectorDoc) {
					Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
					if (a != null) {
						if (changesUscSelector == false) {
							Logger.log(Logger.Debug, "Guardando backup de XML de USC Selector");
							XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
							xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
									+ Parameters.XML_USC_SELECTOR + ".bk" + DateFormater.dateToShortString()));
						}
						changesUscSelector = true;
						Logger.log(Logger.Debug, "Cambio encontrado en USC Selector, removiendo atributo");
						element.removeAttribute(Parameters.XML_TAG_FLAG);
					}
				}
			}
			if (changesUscSelector || force) {
				Logger.log(Logger.Debug, "Guardando XML de USC Selector");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en USC Selector");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR + ".process", "-pricing", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_USC_SELECTOR);
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en USC Selector");
			}
			uscValidityDoc = null;

			Logger.screen(Logger.Debug, "Analizando Item Selectors Telephony");
			reader = factory
					.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			root = jdomDoc.getRootElement();
			List<Element> itemSpecsDoc = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			for (Element element : itemSpecsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesItemsTel == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML Items telefonia");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
								+ Parameters.XML_ITEM_SELECTORS_TEL + ".bk" + DateFormater.dateToShortString()));
					}
					changesItemsTel = true;
					if (a.getValue().equals("new")) {
						refreshNeededItemsTel = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en Item Selectors Telephony, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			itemSpecsDoc = root.getChild("itemTypeSelectors").getChildren("rule");
			for (Element element : itemSpecsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesItemsTel == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML Items telefonia");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
								+ Parameters.XML_ITEM_SELECTORS_TEL + ".bk" + DateFormater.dateToShortString()));
					}
					changesItemsTel = true;
					if (a.getValue().equals("new")) {
						refreshNeededItemsTel = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en Item Selectors Telephony, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			if (changesItemsTel || force) {
				Logger.log(Logger.Debug, "Guardando XML de Selectors Telephony");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Item Selectors Telephony");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL + ".process", "-config", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL);
				jdomDocTelephonyItem = null;
				if (refreshNeededItemsTel) {
					ImportExportPricing.genItemsTelephonyBaseXml(true);
					;
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Item Selectors Telephony");
			}
			itemSpecsDoc = null;

			Logger.screen(Logger.Debug, "Analizando Item Selectors SMS");
			reader = factory
					.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			root = jdomDoc.getRootElement();
			itemSpecsDoc = root.getChild("itemTypeSelectors").getChildren("itemSpec");
			for (Element element : itemSpecsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesItemsSms == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML Items SMS");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
								+ Parameters.XML_ITEM_SELECTORS_SMS + ".bk" + DateFormater.dateToShortString()));
					}
					changesItemsSms = true;
					if (a.getValue().equals("new")) {
						refreshNeededItemsSms = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en Item Selectors SMS, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			itemSpecsDoc = root.getChild("itemTypeSelectors").getChildren("rule");
			for (Element element : itemSpecsDoc) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesItemsSms == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML Items SMS");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
								+ Parameters.XML_ITEM_SELECTORS_SMS + ".bk" + DateFormater.dateToShortString()));
					}
					changesItemsSms = true;
					if (a.getValue().equals("new")) {
						refreshNeededItemsSms = true;
					}
					Logger.log(Logger.Debug, "Cambio encontrado en Item Selectors SMS, removiendo atributo");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
			if (changesItemsSms || force) {
				Logger.log(Logger.Debug, "Guardando XML de Selectors SMS");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Item Selectors SMS");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS + ".process", "-config", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_SMS);
				jdomDocSmsItem = null;
				if (refreshNeededItemsSms) {
					ImportExportPricing.genItemsSmsBaseXml(true);
					;
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Item Selectors SMS");
			}
			itemSpecsDoc = null;

			Logger.screen(Logger.Debug, "Analizando Charges Telephony");
			reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			root = jdomDoc.getRootElement();
			List<Element> aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency")
					.getChildren("applicableRum");
			List<Element> chargesDoc;
			for (Element ar : aplicableRums) {
				Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
				chargesDoc = enhacedZoneModel.getChildren("results");
				for (Element element : chargesDoc) {
					Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
					if (a != null) {
						if (changesChargesTel == false) {
							Logger.log(Logger.Debug, "Guardando backup de XML Charges telefonia");
							XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
							xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
									+ Parameters.XML_CHARGES_TEL + ".bk" + DateFormater.dateToShortString()));
						}
						changesChargesTel = true;
						if (a.getValue().equals("new")) {
							refreshNeededChargesTel = true;
						}
						Logger.log(Logger.Debug, "Cambio encontrado en Charges Telephony, removiendo atributo");
						element.removeAttribute(Parameters.XML_TAG_FLAG);
					}
				}
			}
			if (changesChargesTel || force) {
				Logger.log(Logger.Debug, "Guardando XML de Charges Telephony");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Charges Telephony");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL + ".process", "-pricing", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL);
				jdomDocTelephonyPrice = null;
				if (refreshNeededChargesTel) {
					ImportExportPricing.genChargesTelephonyBaseXml(true);
					;
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Charges Telephony");
			}
			chargesDoc = null;

			Logger.screen(Logger.Debug, "Analizando Charges Telephony USD");
			reader = factory
					.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL_USD));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			root = jdomDoc.getRootElement();
			aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency").getChildren("applicableRum");
			for (Element ar : aplicableRums) {
				Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
				chargesDoc = enhacedZoneModel.getChildren("results");
				for (Element element : chargesDoc) {
					Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
					if (a != null) {
						if (changesChargesTelUsd == false) {
							Logger.log(Logger.Debug, "Guardando backup de XML Charges telefonia USD");
							XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
							xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
									+ Parameters.XML_CHARGES_TEL_USD + ".bk" + DateFormater.dateToShortString()));
						}
						changesChargesTelUsd = true;
						Logger.log(Logger.Debug, "Cambio encontrado en Charges Telephony USD, removiendo atributo");
						element.removeAttribute(Parameters.XML_TAG_FLAG);
					}
				}
			}
			if (changesChargesTelUsd || force) {
				Logger.log(Logger.Debug, "Guardando XML de Charges Telephony USD");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL_USD + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Charges Telephony USD");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL_USD + ".process", "-pricing", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL_USD);
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Charges Telephony USD");
			}
			chargesDoc = null;

			Logger.screen(Logger.Debug, "Analizando Charges SMS");
			reader = factory.createXMLEventReader(new FileReader(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS));
			builder = new StAXEventBuilder();
			jdomDoc = builder.build(reader);
			root = jdomDoc.getRootElement();
			aplicableRums = root.getChild("chargeRatePlan").getChild("subscriberCurrency").getChildren("applicableRum");
			for (Element ar : aplicableRums) {
				Element enhacedZoneModel = ar.getChild("crpRelDateRange").getChild("enhancedZoneModel");
				chargesDoc = enhacedZoneModel.getChildren("results");
				for (Element element : chargesDoc) {
					Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
					if (a != null) {
						if (changesChargesSms == false) {
							Logger.log(Logger.Debug, "Guardando backup de XML Charges SMS");
							XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
							xmlOut.output(jdomDoc, new FileOutputStream(Parameters.XML_DB_DIR
									+ Parameters.XML_CHARGES_SMS + ".bk" + DateFormater.dateToShortString()));
						}
						changesChargesSms = true;
						if (a.getValue().equals("new")) {
							refreshNeededChargesSms = true;
						}
						Logger.log(Logger.Debug, "Cambio encontrado en Charges SMS, removiendo atributo");
						element.removeAttribute(a);
					}
				}
			}
			if (changesChargesSms || force) {
				Logger.log(Logger.Debug, "Guardando XML de Charges SMS");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(jdomDoc,
						new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS + ".process"));
				Logger.screen(Logger.Debug, "Subiendo cambios en Charges SMS");
				List<String> args = new ArrayList<String>();
				args.add("-ow");
				ImportExportPricing.runImportExportPricing("-import",
						Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS + ".process", "-pricing", args);
				moveProcessedFile(Parameters.XML_DB_DIR + Parameters.XML_CHARGES_SMS);
				jdomDocSmsPrice = null;
				if (refreshNeededChargesSms) {
					ImportExportPricing.genChargesSmsBaseXml(true);
					;
				}
			} else {
				Logger.screen(Logger.Debug, "No hay cambios por aplicar en Charges SMS");
			}
			chargesDoc = null;
			
			changesZonningPreSms = XmlUtilsByModality.zonning(force, Modality.PREPAID, ServiceType.SMS);
			changesZonningPreTel = XmlUtilsByModality.zonning(force, Modality.PREPAID, ServiceType.TEL);
			changesZonningHibSms = XmlUtilsByModality.zonning(force, Modality.HYBRID, ServiceType.SMS);
			changesZonningHibTel = XmlUtilsByModality.zonning(force, Modality.HYBRID, ServiceType.TEL);
			changesZonningHibHorTel = XmlUtilsByModality.zonning(force, Modality.HORARY, ServiceType.TEL);
			changesZonningPreMida = XmlUtilsByModality.zonning(force, Modality.PREPAID, ServiceType.TEL,true);
			changesZonningHibMida = XmlUtilsByModality.zonning(force, Modality.HYBRID, ServiceType.TEL,true);
			changesZonningCCFMida = XmlUtilsByModality.zonning(force, Modality.CCF, ServiceType.TEL,true);
			changesZonningCCMMida = XmlUtilsByModality.zonning(force, Modality.CCM, ServiceType.TEL,true);
			changesChargesPreTel = XmlUtilsByModality.charging(force, Modality.PREPAID, ServiceType.TEL);
			changesChargesPreSms = XmlUtilsByModality.charging(force, Modality.PREPAID, ServiceType.SMS);
			changesChargesHibTel = XmlUtilsByModality.charging(force, Modality.HYBRID, ServiceType.TEL);
			changesChargesHibSms = XmlUtilsByModality.charging(force, Modality.HYBRID, ServiceType.SMS);
			changesChargesHibHorTel = XmlUtilsByModality.charging(force, Modality.HORARY, ServiceType.TEL);
			changesChargesPreMida = XmlUtilsByModality.charging(force, Modality.PREPAID, ServiceType.TEL,true);
			changesChargesHibMida = XmlUtilsByModality.charging(force, Modality.HYBRID, ServiceType.TEL,true);
			changesChargesCCFMida = XmlUtilsByModality.charging(force, Modality.CCF, ServiceType.TEL,true);
			changesChargesCCMMida = XmlUtilsByModality.charging(force, Modality.HORARY, ServiceType.TEL,true);
			
			

		} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al leer XML de entrada, Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (ExceptionImportExportPricing | InterruptedException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error en la ejecucion de ImportExportPricing, puede ocurrir perdida de datos, consulte al administrador!");
			System.exit(Parameters.ERR_IOERROR);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al escribir XML, puede ocurrir perdida de datos, consulte al administrador!");
			System.exit(Parameters.ERR_IOERROR);
		}
		Logger.log(Logger.Debug, "publishChanges() - Fin");
		return changesZonning || changesChargesSms || changesChargesTel || changesItemsSms || changesItemsTel || changesChargesTelUsd
				|| changesZonningPreSms || changesZonningPreTel || changesZonningHibSms || changesZonningHibTel
				|| changesChargesPreTel || changesChargesPreSms || changesChargesHibTel || changesChargesHibSms
				|| changesChargesHibHorTel || changesZonningHibHorTel || changesZonningCCMMida || changesChargesCCMMida ||
				changesZonningCCFMida || changesChargesCCFMida || changesZonningPreMida || changesChargesPreMida || changesZonningHibMida || changesChargesHibMida;
	}

	protected static void moveProcessedFile(String fileName) {
		try {
			Logger.log(Logger.Debug, "Renombrando archivo procesado: " + fileName);
			ProcessBuilder pb = new ProcessBuilder("mv", fileName + ".process", fileName);
			pb.directory(new File(Parameters.GLID_LOADER_PATH));
			Process process = pb.start();
			InputStream iop = process.getInputStream();
			BufferedInputStream bif = new BufferedInputStream(iop);
			process.waitFor();
			if (process.exitValue() != 0) {
				Logger.log(Logger.Warning, "No se pudo renombrar el archivo: " + fileName + ".process");
				Logger.log(Logger.Warning, IOUtils.toString(bif, "UTF-8"));
				iop = process.getErrorStream();
				Logger.log(Logger.Warning, IOUtils.toString(new BufferedInputStream(iop), "UTF-8"));
			} else {
				Logger.log(Logger.Debug, "Archivo renombrado a: " + fileName);
				Logger.log(Logger.Debug, IOUtils.toString(bif, "UTF-8"));
			}
		} catch (Exception ioe) {
			Logger.screen(Logger.Error, ioe.toString());
			Logger.screen(Logger.Error, "Error al renombrar el archivo: " + fileName);
		}
	}
}
