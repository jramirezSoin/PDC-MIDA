package brmHandlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

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
import cust.Modality.ResultName;
import exceptions.ExceptionImportExportPricing;
import cust.ServiceType;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.Xml;
import cust.ZoneItem;
import log.Logger;

/**
 * 
 * @autor Roger Masis
 */
public class XmlUtilsByModality {

	private static Map<String, Map<String, Object>> jdomDocZonnings = new HashMap<String, Map<String, Object>>();
	private static Map<String, Map<String, Object>> jdomDocChargings = new HashMap<String, Map<String, Object>>();
	private static Map<String, Map<String, Object>> jdomDocSelectors = new HashMap<String, Map<String, Object>>();
	private static Map<String, Map<String, Object>> jdomDocHistories = new HashMap<String, Map<String, Object>>();

	protected static String FILE = "file", DOCUMENT = "document";

	public static Boolean existsTelephonyPriceTier(Modality modality, ServiceType serviceType, String zoneName) {
		Logger.log(Logger.Debug, "getTelephonyPriceTiers() - Inicio");
		Map<String, Object> jdomDocCharging = getChargesFile(modality, serviceType);
		Element root = ((Document) jdomDocCharging.get(XmlUtilsByModality.DOCUMENT)).getRootElement();
		Element aplicableRums = root.getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY)
				.getChild(Xml.APPLICABLERUM);
		for (Element element : aplicableRums.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL)
				.getChildren(Xml.RESULTS)) {
			if (element.getChildText(Xml.NAME).equals(zoneName)) {
				Logger.log(Logger.Debug, "getTelephonyPriceTier() - Existe");
				return true;
			}
		}
		Logger.log(Logger.Debug, "getTelephonyPriceTier() - Fin");
		return false;
	}
	
	public static boolean addPriceTier(Modality modality, ServiceType serviceType, PriceTier priceTier) {
		return addPriceTier(modality, serviceType, priceTier, null,false);
	}

	public static boolean addPriceTier(Modality modality, ServiceType serviceType, PriceTier priceTier, ResultName resultName) {
		return addPriceTier(modality,serviceType,priceTier,resultName,false);
	}	

	public static boolean addPriceTier(Modality modality, ServiceType serviceType, PriceTier priceTier, ResultName resultName, Boolean mida) {
		boolean updated = false;
		Map<String, Object> jdomDocCharging = getChargesFile(modality, serviceType,false,mida);
		try {
			Document document = (Document) jdomDocCharging.get(XmlUtilsByModality.DOCUMENT);
			Element root = document.getRootElement();
			List<Element> aplicableRums = root.getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY)
					.getChildren(Xml.APPLICABLERUM);
			@SuppressWarnings("unused")
			Element ar = null;
			for (Element element : aplicableRums) {
				if (!serviceType.equals(ServiceType.SMS) && element.getChildText(Xml.APPLICABLERUMNAME).equals("DUR")) {
					ar = element;
					priceTier.setRum("DUR");
					break;
				} else if (element.getChildText(Xml.APPLICABLERUMNAME).equals("EVT")) {
					ar = element;
					priceTier.setRum("EVT");
					break;
				}
			}

			Element enhacedZoneModelDur = ar.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL);
			Logger.log(Logger.Debug, "addPriceTier(" + priceTier.getName() + ") - Inicio");
			enhacedZoneModelDur.addContent(makeResultElement(modality, serviceType, priceTier.getName(), priceTier, resultName));
			Logger.log(Logger.Debug, "addPriceTier(" + priceTier.getName() + ") - Fin");
			Logger.log(Logger.Debug, "Guardando XML de charges");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream((String) jdomDocCharging.get(XmlUtilsByModality.FILE)));
			updated = true;
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de charge: "
					+ (String) jdomDocCharging.get(XmlUtilsByModality.FILE) + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addPriceTier() - Fin");
		return updated;
	}
	private static Element makeResultElement(Modality modality, ServiceType serviceType, String name, PriceTier pt, ResultName resultName) {
		return makeResultElement(modality,serviceType,name,pt,resultName,false);
	}
	private static Element makeResultElement(Modality modality, ServiceType serviceType, String name, PriceTier pt, ResultName resultName, Boolean mida) {
		Logger.log(Logger.Debug, "makeResultElement(" + modality + ", " + serviceType + ", " + name + ", " + pt.getName() + ", " + pt.getRum() + ") - Inicio");
		Element result = new Element(Xml.RESULTS);
		result.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
		result.addContent(new Element(Xml.NAME).setText(name));
		Element crpCompositePopModel = new Element(Xml.CRPCOMPOSITEPOPMODEL);
		crpCompositePopModel.addContent(new Element(Xml.NAME).setText("Precios"));
		Element usageChargePopModel = new Element(Xml.USAGECHARGEPOPMODEL);
		Element priceTier = new Element(Xml.PRICETIER);
		priceTier.addContent(new Element(Xml.DISTRIBUTIONMETHOD).setText(serviceType.distributionMethod));
		priceTier.addContent(new Element(Xml.TIERBASIS).addContent(new Element(Xml.RUMTIEREXPRESSION)));
		priceTier.addContent(new Element(Xml.ENFORCECREDITLIMIT).setText("false"));
		priceTier.addContent(new Element(Xml.RUMNAME).setText(pt.getRum()));
		priceTier.addContent(new Element(Xml.CURRENCYCODE).setText(((!mida)?"CRC":"USD")));

		List<String> keys = new ArrayList<String>();
		for (String date : pt.getPriceTierRanges().keySet()) {
			keys.add(date);
		}
		Collections.sort(keys);
		for (String date : keys) {
			priceTier.addContent(getPriceTierValidityPeriod(date, pt.getPriceTierRanges().get(date), resultName));
		}
		priceTier.addContent(new Element(Xml.APPLICABLEQUANTITY).setText("ORIGINAL"));
		usageChargePopModel.addContent(priceTier);
		crpCompositePopModel.addContent(usageChargePopModel);
		if(modality.equals(Modality.HORARY)) {
			Element timeConfiguration = new Element(Xml.TIMECONFIGURATION).addContent(new Element(Xml.TIMEMODELNAME).setText("HIB_TM_HORARIO"));
			timeConfiguration.addContent(new Element(Xml.TAGS).addContent(new Element(Xml.NAME).setText("HIB_TP_POSPAGO")).addContent(crpCompositePopModel.clone()));
			timeConfiguration.addContent(new Element(Xml.TAGS).addContent(new Element(Xml.NAME).setText("HIB_TP_PREPAGO")).addContent(crpCompositePopModel.clone()));
			result.addContent(timeConfiguration);
		}else
			result.addContent(crpCompositePopModel);
		Logger.log(Logger.Debug, "makeResultElement() - Fin");
		return result;
	}

	public static boolean addZoneItem(Modality modality, ServiceType serviceType, String zoneName,
			String validFrom, String validTo, String destinationPrefix, String rateType) {
		return addZoneItem(modality,serviceType,zoneName,validFrom,validTo,destinationPrefix,rateType,false);
	}

	public static boolean addZoneItem(Modality modality, ServiceType serviceType, String zoneName,
			String validFrom, String validTo, String destinationPrefix, String rateType, Boolean mida) {
		Logger.log(Logger.Debug, "addZoneItem() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType,false,mida);
		try {
			Document document = (Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT);
			Element standardZoneModel = document.getRootElement().getChild(Xml.STANDARDZONEMODEL);
			Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
			String oPrefix="02";
			if(mida){
				oPrefix= ((modality.equals(Modality.CCM))?"08":((modality.equals(Modality.CCM))?"07":"02"));
			}
			for (ResultName resultName : modality.getResultsNames(serviceType,mida)) {
				if(rateType != null && !resultName.group.equals(rateType))
					continue;
				Element newElem = new Element(Xml.ZONEITEM);
				newElem.addContent(new Element(Xml.PRODUCTNAME).setText(resultName.productName));
				newElem.addContent(new Element(Xml.ORIGINPREFIX).setText(((!mida)?resultName.originPrefix:oPrefix)));
				newElem.addContent(new Element(Xml.DESTINATIONPREFIX).setText(destinationPrefix));
				newElem.addContent(new Element(Xml.VALIDFROM).setText(validFrom));
				newElem.addContent(new Element(Xml.VALIDTO).setText(validTo));
				newElem.addContent(new Element(Xml.ZONERESULT)
						.addContent(new Element(Xml.ZONENAME).setText(resultName.getZoneName(zoneName))));
				newElem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
				standardZoneModel.addContent(newElem);
			}
			Logger.log(Logger.Debug, "Guardando XML de zonning");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)));
			
			addZoneResult(modality, serviceType, zoneName, rateType,mida);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ jdomDocZonning.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addZoneItem() - Fin");
		return found;
	}
	
	public static boolean modifyZoneItemsDestinationPrefix(Modality modality, ServiceType serviceType, String zoneName, String date, String destinationPrefix, String rateType) {
		Logger.log(Logger.Debug, "modifyZoneItemsDestinationPrefix() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT);
			Element standardZoneModel = document.getRootElement().getChild(Xml.STANDARDZONEMODEL);
			List<Element> zoneItems = standardZoneModel.getChildren(Xml.ZONEITEM);
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				if(rateType!= null && !rateType.equals(resultName.group))
					continue;
				for (Element zoneItem : zoneItems) {
					String _zoneName = resultName.getZoneName(zoneName);
					if(zoneItem.getChild(Xml.ZONERESULT).getChildText(Xml.ZONENAME).equals(_zoneName)) {
						String validFrom = zoneItem.getChildText(Xml.VALIDFROM);
						String validTo = zoneItem.getChildText(Xml.VALIDTO);
						if (date.compareTo(validFrom) >= 0 && date.compareTo(validTo) < 0) {
							Logger.log(Logger.Debug, "Vigencia de campa\u00F1a encontrada - " + validFrom + " - " + validTo);
							found = true;
							Element destin = zoneItem.getChild(Xml.DESTINATIONPREFIX);
							if (destin.getText().equals(destinationPrefix)) {
								Logger.screen(Logger.Error,
										"El n\u00FAmero indicado ya se encuentra configurado en la campa\u00F1a, Saliendo!");
								System.exit(Parameters.ERR_INVALID_VALUE);
							}
							if (date.equals(validFrom)) {
								Logger.log(Logger.Debug, "Como la fecha indicada coincide con el inicio se modifica el zoneItem y no se crea uno nuevo");
								destin.setText(destinationPrefix);
							} else {
								Logger.log(Logger.Debug,
										"Como la fecha indicada NO coincide con el inicio se modifica la vigencia del zoneItem actual y se crea uno nuevo");
								Element newZoneItem = new Element(Xml.ZONEITEM);
								String newValidTo = DateFormater.sumDays(date, -1);
								Logger.log(Logger.Debug, "modifyZoneItem() - Modificando vigencia del actual: " + validTo);
								zoneItem.getChild(Xml.VALIDTO).setText(newValidTo);
								if (zoneItem.getAttribute(Parameters.XML_TAG_FLAG) == null) 
									zoneItem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
								Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
								newZoneItem = new Element(Xml.ZONEITEM);
								newZoneItem.addContent(new Element(Xml.PRODUCTNAME).setText(zoneItem.getChildText(Xml.PRODUCTNAME)));
								newZoneItem.addContent(new Element(Xml.ORIGINPREFIX).setText(zoneItem.getChildText(Xml.ORIGINPREFIX)));
								newZoneItem.addContent(new Element(Xml.DESTINATIONPREFIX).setText(zoneItem.getChildText(Xml.DESTINATIONPREFIX)));
								newZoneItem.addContent(new Element(Xml.VALIDFROM).setText(date));
								newZoneItem.addContent(new Element(Xml.VALIDTO).setText(validTo));
								newZoneItem.addContent(new Element(Xml.ZONERESULT).addContent(new Element(Xml.ZONENAME).setText(_zoneName)));
								newZoneItem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
								standardZoneModel.addContent(newZoneItem);
							}
							break;
						}
					
					}
				}
			}
			if(found) {
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)));
			}
		} catch (FileNotFoundException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al leer XML de zonning: " + (String) jdomDocZonning.get(XmlUtilsByModality.FILE) + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ (String) jdomDocZonning.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modifyZoneItemsDestinationPrefix() - Fin");
		return found;
	}
	
	public static boolean modifyZoneItem(Modality modality, ServiceType serviceType, String zoneName, String newValidFrom, String rateType) {
		Logger.log(Logger.Debug, "modifyZoneItem() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT);
			Element standardZoneModel = document.getRootElement().getChild(Xml.STANDARDZONEMODEL);
			List<Element> zoneItems = standardZoneModel.getChildren(Xml.ZONEITEM);
			List<Element> newZoneItems = zoneItems.stream().collect(Collectors.toList());
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				if(rateType!= null && !rateType.equals(resultName.group))
					continue;
				Integer i = 0;
				for(Element zoneItem : zoneItems) {
					i++;
					String _zoneName = resultName.getZoneName(zoneName);
					if(zoneItem.getChild(Xml.ZONERESULT).getChildText(Xml.ZONENAME).equals(_zoneName) 
							&& newValidFrom.compareTo(zoneItem.getChildText(Xml.VALIDFROM)) >= 0
							&& newValidFrom.compareTo(zoneItem.getChildText(Xml.VALIDTO)) <= 0) {
						if(!zoneItem.getChildText(Xml.VALIDFROM).equals(newValidFrom)) {
							found = true;
							Element newZoneItem = new Element(Xml.ZONEITEM);
							String newValidTo = DateFormater.sumDays(newValidFrom, -1);
							String validTo = zoneItem.getChildText(Xml.VALIDTO);
							if (zoneItem.getAttribute(Parameters.XML_TAG_FLAG) == null)
								zoneItem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
							Logger.log(Logger.Debug, "modifyZoneItem() - Modificando vigencia del actual: " + validTo);
							zoneItem.getChild(Xml.VALIDTO).setText(newValidTo);
							Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
							newZoneItem = new Element(Xml.ZONEITEM);
							newZoneItem.addContent(new Element(Xml.PRODUCTNAME).setText(zoneItem.getChildText(Xml.PRODUCTNAME)));
							newZoneItem.addContent(new Element(Xml.ORIGINPREFIX).setText(zoneItem.getChildText(Xml.ORIGINPREFIX)));
							newZoneItem.addContent(new Element(Xml.DESTINATIONPREFIX).setText(zoneItem.getChildText(Xml.DESTINATIONPREFIX)));
							newZoneItem.addContent(new Element(Xml.VALIDFROM).setText(newValidFrom));
							newZoneItem.addContent(new Element(Xml.VALIDTO).setText(validTo));
							newZoneItem.addContent(new Element(Xml.ZONERESULT).addContent(new Element(Xml.ZONENAME).setText(_zoneName)));
							newZoneItem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
							newZoneItems.add(i, newZoneItem);
						}
						break;
					}
				}
			}
			if(found) {
				standardZoneModel.removeChildren(Xml.ZONEITEM);
				standardZoneModel.addContent(newZoneItems);
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)));
				addZoneResult(modality, serviceType, zoneName, rateType);
			}
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ jdomDocZonning.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modifyZoneItem() - Fin");
		return found;
	}

	public static boolean isZoneDestinValidAtDate(Modality modality, ServiceType serviceType, String destin,
			String date) {
		Logger.log(Logger.Debug, "isZoneDestinValidAtDate() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		Element root = ((Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT)).getRootElement();
		List<Element> zoneItemsDoc = root.getChild(Xml.STANDARDZONEMODEL).getChildren(Xml.ZONEITEM);
		for (Element element : zoneItemsDoc) {
			String destination = element.getChildText(Xml.DESTINATIONPREFIX);
			if (destination.equals(destin)) {
				String validTo = element.getChildText(Xml.VALIDTO);
				String validFrom = element.getChildText(Xml.VALIDFROM);
				if ((date.compareTo(validFrom) >= 0) && (date.compareTo(validTo) <= 0)) {
					found = true;
					break;
				}
			}
		}
		Logger.log(Logger.Debug, "isZoneDestinValidAtDate() - Fin");
		return found;
	}

	public static boolean isZoneValidAtDate(Modality modality, ServiceType serviceType, String zoneName, String date) {
		Logger.log(Logger.Debug, "isZoneValidAtDate() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		Element root = ((Document) jdomDocZonning.get(Xml.DOCUMENT)).getRootElement();
		List<Element> zoneItemsDoc = root.getChild(Xml.STANDARDZONEMODEL).getChildren(Xml.ZONEITEM);
		for (Element element : zoneItemsDoc) {
			Element zoneResult = element.getChild(Xml.ZONERESULT);
			String zone = zoneResult.getChildText(Xml.ZONENAME);
			if (zoneName.equals(zone)) {
				String validTo = element.getChildText(Xml.VALIDTO);
				String validFrom = element.getChildText(Xml.VALIDFROM);
				if ((date.compareTo(validFrom) >= 0) && (date.compareTo(validTo) <= 0)) {
					found = true;
					break;
				}
			}
		}
		Logger.log(Logger.Debug, "isZoneValidAtDate() - Fin");
		return found;
	}

	public static String getItemTelephonyDescription(Modality modality, ServiceType serviceType, String idCampaign) {
		Logger.log(Logger.Debug, "getItemTelephonyDescription() - Inicio");
		String desc = null;
		Map<String, Object> jdomDocSelecting = getSelectorFile(modality, serviceType);
		Element root = ((Document) jdomDocSelecting.get(Xml.DOCUMENT)).getRootElement();
		List<Element> itemSpecs = root.getChild(Xml.ITEMTYPESELECTORS).getChildren(Xml.ITEMSPEC);
		String itemType = "/item/ice/E_N" + idCampaign;
		for (Element element : itemSpecs) {
			if (element.getChildText(Xml.TYPE).equals(itemType)) {
				desc = element.getChildText(Xml.DESCRIPTION);
				Logger.log(Logger.Debug, "Descripcion encontrada: " + desc);
				break;
			}
		}
		Logger.log(Logger.Debug, "getItemTelephonyDescription() - Fin");
		return desc;
	}

	public static boolean modItemTelephonyDescription(Modality modality, ServiceType serviceType, String idCampaign,
			String newDesc) {
		Logger.log(Logger.Debug, "modItemTelephonyDescription() - Inicio");
		Element desc = null;
		Map<String, Object> jdomDocSelecting = getSelectorFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocSelecting.get(XmlUtilsByModality.DOCUMENT);
			Element root = document.getRootElement();
			List<Element> itemSpecs = root.getChild(Xml.ITEMTYPESELECTORS).getChildren(Xml.ITEMSPEC);
			String itemType = "/item/ice/E_N" + idCampaign;
			for (Element element : itemSpecs) {
				if (element.getChildText(Xml.TYPE).equals(itemType)) {
					desc = element.getChild(Xml.DESCRIPTION);
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
			xmlOut.output(document, new FileOutputStream((String) jdomDocSelecting.get(XmlUtilsByModality.FILE)));
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ (String) jdomDocSelecting.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		ItemTypeDeploy.notifyNewItem(false);
		Logger.log(Logger.Debug, "modItemTelephonyDescription() - Fin");
		return true;
	}

	public static boolean addItemRuleSpecTelephony(Modality modality, ServiceType serviceType, String idCampaign,
			String desc) {
		Logger.log(Logger.Debug, "addItemRuleSpecTelephony(" + idCampaign + ", " + desc + ") - Inicio");
		Map<String, Object> jdomDocSelecting = getSelectorFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocSelecting.get(XmlUtilsByModality.DOCUMENT);
			Element itemTypeSelectors = document.getRootElement().getChild(Xml.ITEMTYPESELECTORS);
			Logger.log(Logger.Debug, "addItemRuleSpecTelephony - calculando maxOrder");
			List<Element> rules = itemTypeSelectors.getChildren(Xml.RULE);
			Integer maxOrder = 0;
			for (Element element : rules) {
				try {
					Integer order = Integer.parseInt(element.getChildText(Xml.RULEORDER));
					if (order > maxOrder) {
						maxOrder = order;
					}
				} catch (NumberFormatException e) {

				}
			}
			maxOrder++;
			List<Element> itemSpecs = itemTypeSelectors.getChildren(Xml.ITEMSPEC);
			List<Element> cloneItemSpecs = new ArrayList<Element>();
			for (Element element : itemSpecs) {
				cloneItemSpecs.add(element.clone());
			}
			itemTypeSelectors.removeChildren(Xml.ITEMSPEC);

			Logger.log(Logger.Debug, "Configurando nuevo Rule");
			Element rule = new Element(Xml.RULE);
			rule.addContent(new Element(Xml.RULENAME).setText("Regla_ItemTypeSelector_Por_Item_TAG_PRE" + idCampaign));
			rule.addContent(new Element(Xml.RULEORDER).setText(maxOrder.toString()));
			Element expression = new Element(Xml.EXPRESSION);
			expression.addContent(new Element(Xml.SEPARATOR).setText(";"));
			expression.addContent(new Element(Xml.OPERATION).setText("EQUAL_TO"));
			expression.addContent(
					new Element(Xml.FIELDNAME).setText("EventDelayedSessionTelcoIcePrepaidGsm.PROVIDER_DESCR"));
			expression.addContent(new Element(Xml.FIELDKIND).setText("EVENT_SPEC_FIELD"));
			expression.addContent(new Element(Xml.FIELDVALUE).setText("N" + idCampaign));
			rule.addContent(expression);
			rule.addContent(new Element(Xml.SPECNAME).setText("item-ice-E_N" + idCampaign));
			itemTypeSelectors.addContent(rule);

			itemTypeSelectors.addContent(cloneItemSpecs);

			Logger.log(Logger.Debug, "Configurando nuevo Item Spec");
			Element itemSpec = new Element(Xml.ITEMSPEC);
			itemSpec.addContent(new Element(Xml.NAME).setText("item-ice-E_N" + idCampaign));
			itemSpec.addContent(new Element(Xml.DESCRIPTION).setText(desc));
			itemSpec.addContent(new Element(Xml.PRICELISTNAME).setText("Default"));
			itemSpec.addContent(new Element(Xml.OBSOLETE).setText("false"));
			itemSpec.addContent(new Element(Xml.TYPE).setText("/item/ice/E_N" + idCampaign));
			itemSpec.addContent(new Element(Xml._DEFAULT).setText("false"));
			itemSpec.addContent(new Element(Xml.AGGREGATIONTYPE).setText("CUMULATIVE_NONPRECREATE"));
			itemSpec.addContent(new Element(Xml.CATEGORY).setText("usage_prepaid"));
			itemTypeSelectors.addContent(itemSpec);

			Logger.log(Logger.Debug, "Guardando XML de items");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream((String) jdomDocSelecting.get(XmlUtilsByModality.FILE)));
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de items, es posible que haya perdida de datos: "
							+ (String) jdomDocSelecting.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addItemRuleSpecTelephony() - Fin");
		return true;
	}

	private static Element getPriceTierValidityPeriod(String date, List<PriceTierRange> listOfPriceTierRange, ResultName resultName) {
		Element priceTierValidityPeriod = new Element(Xml.PRICETIERVALIDITYPERIOD);
		priceTierValidityPeriod.addContent(new Element(Xml.LOWERBOUND).setText("0"));
		priceTierValidityPeriod.addContent(new Element(Xml.VALIDFROM).setText(date));
		List<Element> listOfpriceTierRanges = new LinkedList<Element>();
		for (PriceTierRange ptr : listOfPriceTierRange) {
			if(resultName != null && !resultName.group.equalsIgnoreCase(ptr.getGroup()))
				continue;
			Logger.log(Logger.Debug,
					"addPriceTierValidityPeriod - price tier range - " + ptr.getBalanceElementNumCode() + ", "
							+ ptr.getGlid() + ", " + ptr.getTaxCode() + ", " + ptr.getUnitOfMeasure() + ", "
							+ ptr.getIncrementStep() + ", " + ptr.getPrice());
			Element scaledFixedCharge = null;
			if (ptr.isScaledCharge())
				scaledFixedCharge = new Element(Xml.SCALEDCHARGE);
			else
				scaledFixedCharge = new Element(Xml.FIXEDCHARGE);
			scaledFixedCharge.addContent(new Element(Xml.PRICE).setText(ptr.getPrice().toString()));
			scaledFixedCharge.addContent(new Element(Xml.UNITOFMEASURE).setText(ptr.getUnitOfMeasure()));
			scaledFixedCharge.addContent(new Element(Xml.BALANCEELEMENTNUMCODE).setText("188"));
			scaledFixedCharge.addContent(new Element(Xml.DISCOUNTABLE).setText("true"));
			scaledFixedCharge.addContent(new Element(Xml.PRICETYPE).setText("CONSUMPTION"));
			if(ptr.getTaxCode() != null) {
				scaledFixedCharge.addContent(new Element(Xml.TAXTIME).setText("EVENT_TIME"));
				scaledFixedCharge.addContent(new Element(Xml.TAXCODE).setText(ptr.getTaxCode()));
			}
			scaledFixedCharge.addContent(new Element(Xml.GLID).setText(ptr.getGlid()));
			if (ptr.isScaledCharge()) {
				scaledFixedCharge.addContent(new Element(Xml.INCREMENTSTEP).setText("1.0"));
				scaledFixedCharge.addContent(new Element(Xml.INCREMENTROUNDING).setText("NONE"));
			}
			Element priceTierRange = null;
			for (Element _priceTierRange : listOfpriceTierRanges) {
				if (_priceTierRange.getChildText(Xml.UPPERBOUND).equals(ptr.getUpperBound())) {
					priceTierRange = _priceTierRange;
					break;
				}
			}
			if (priceTierRange == null) {
				priceTierRange = new Element(Xml.PRICETIERRANGE);
				priceTierRange.addContent(new Element(Xml.UPPERBOUND).setText(ptr.getUpperBound()));
				listOfpriceTierRanges.add(priceTierRange);
			}
			priceTierRange.addContent(scaledFixedCharge);

		}
		priceTierValidityPeriod.addContent(listOfpriceTierRanges);
		return priceTierValidityPeriod;
	}
	
	public static boolean addPriceTierRange(Modality modality, ServiceType serviceType, PriceTier priceTier) {
		return addPriceTierRange(modality, serviceType, priceTier, null);
	}

	public static boolean addPriceTierRange(Modality modality, ServiceType serviceType, PriceTier priceTier, ResultName resultName) {
		return addPriceTierRange(modality,serviceType,priceTier,resultName,false);
	}

	public static boolean addPriceTierRange(Modality modality, ServiceType serviceType, PriceTier priceTier, ResultName resultName,Boolean mida) {
		Logger.log(Logger.Debug, "addPriceTierRange() - Inicio");
		boolean updated = false;
		Map<String, Object> jdomDocSelecting = getChargesFile(modality, serviceType, false, mida);
		try {
			Document document = (Document) jdomDocSelecting.get(XmlUtilsByModality.DOCUMENT);
			Element root = document.getRootElement();
			Element aplicableRum = root.getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY)
					.getChild(Xml.APPLICABLERUM);
			Element enhacedZoneModel = aplicableRum.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL);
			List<Element> results = enhacedZoneModel.getChildren(Xml.RESULTS);
			for (Element result : results) {
				String name = result.getChildText(Xml.NAME);
				if (name.equals(priceTier.getName())) {
					Element search = null;
					Element timeConfiguration = null;
					if(modality.equals(Modality.HORARY)) {
						timeConfiguration = result.getChild(Xml.TIMECONFIGURATION);
						List<Element> tags = timeConfiguration.getChildren(Xml.TAGS);
						for(Element tag : tags) {
							if(tag.getChildText(Xml.NAME).equals("HIB_TP_POSPAGO"))
								search = tag;
							else
								timeConfiguration.removeContent(tag);
						}
						if(search == null) {
							Logger.screen(Logger.Error, "Nodo de tags no encontrado, Saliendo!");
							System.exit(Parameters.ERR_FILESYSTEM);
						}
					}else
						search = result;
					Element _priceTier = search.getChild(Xml.CRPCOMPOSITEPOPMODEL).getChild(Xml.USAGECHARGEPOPMODEL)
							.getChild(Xml.PRICETIER);
					List<Element> validityPeriods = _priceTier.getChildren(Xml.PRICETIERVALIDITYPERIOD);
					List<String> keys = new ArrayList<String>();
					for (String date : priceTier.getPriceTierRanges().keySet()) {
						keys.add(date);
					}
					Collections.sort(keys);
					for (String date : keys) {
						Integer i = 0;
						for (Element range : validityPeriods) {
							i++;
							String validFrom = range.getChildText(Xml.VALIDFROM);
							if(validFrom.equals(Parameters.START_DATE_FROM_PRE) && date.equals(Parameters.START_DATE_FROM_PRE)) {
								break;
							}
							else if (date.equals(validFrom)) {
								if(!mida){
									updated = true;
									validityPeriods.remove(i);
								}else{
									Logger.screen(Logger.Error,"La fecha seleccionada, ya existe, ejecute el comando de modificacion respectivo");
								}
								break;
							}else if (date.compareTo(validFrom) < 0) {
								i--;
								updated = true;
								break;
							}else if (validityPeriods.size() <= i) {
								updated = true;
								break;
							}
						}
						if(updated)
							validityPeriods.add(i, getPriceTierValidityPeriod(date, priceTier.getPriceTierRanges().get(date), resultName));
					}
					if(updated) {
						String applicableQuantity = _priceTier.getChildText(Xml.APPLICABLEQUANTITY);
						if(applicableQuantity != null) {
							_priceTier.removeChild(Xml.APPLICABLEQUANTITY);
							_priceTier.addContent(new Element(Xml.APPLICABLEQUANTITY).setText("ORIGINAL"));
						}
						if(modality.equals(Modality.HORARY)) {
							Element tag = new Element(Xml.TAGS);
							tag.addContent(search.cloneContent());
							tag.getChild(Xml.NAME).setText("HIB_TP_PREPAGO");
							timeConfiguration.addContent(tag);
						}
						if (result.getAttribute(Parameters.XML_TAG_FLAG) == null)
							result.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
					}
				}
			}
			if(updated) {
				Logger.log(Logger.Debug, "getPriceTier() - Updated");
				Logger.log(Logger.Debug, "Guardando XML de charges TEL");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocSelecting.get(XmlUtilsByModality.FILE)));
			}
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML de TEL charge: "
					+ (String) jdomDocSelecting.get(XmlUtilsByModality.FILE) + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addPriceTierRange() - Fin");
		return updated;
	}
	
	public static boolean isZoneValid(Modality modality, ServiceType serviceType, String zoneName) {
		Logger.log(Logger.Debug, "isZoneValid() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		Element root = ((Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT)).getRootElement();
		List<Element> zoneItemsDoc = root.getChild(Xml.STANDARDZONEMODEL).getChildren(Xml.ZONEITEM);
		for (Element element : zoneItemsDoc) {
			Element zoneResult = element.getChild(Xml.ZONERESULT);
			String zone = zoneResult.getChildText(Xml.ZONENAME);
			if (zoneName.equals(zone)) {
				found = true;
				break;
			}
		}
		Logger.log(Logger.Debug, "isZoneValid() - Fin");
		return found;
	}

	public static void updateHistoryXml(Modality modality, ServiceType serviceType, String zoneName) {
		Logger.screen(Logger.Debug, "Generando fecha en el historial");
		Map<String, Object> jdomDocSelecting = getHistoryFile(modality, serviceType);
		if (jdomDocSelecting == null)
			return;
		Document document = (Document) jdomDocSelecting.get(XmlUtilsByModality.DOCUMENT);
		Element root = document.getRootElement();
		Element zone = root.getChild(zoneName);
		if (zone == null) {
			Logger.log(Logger.Debug, "No se encontro el zoneName, se agregara uno nuevo");
			Element newZone = new Element(zoneName);
			newZone.setAttribute(Xml.MODIFYDATE, DateFormater.dateToString(new Date()));
			root.addContent(newZone);
		} else {
			Logger.log(Logger.Debug, "Se actualiza el zoneName encontrado");
			zone.setAttribute("modifyDate", DateFormater.dateToString(new Date()));
		}
		try {
			Logger.log(Logger.Debug, "Guardando XML de historial");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream(Parameters.XML_DB_DIR + Parameters.XML_HISTORY));
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al guardar el cambio en el XML de historial: "
					+ (String) jdomDocSelecting.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "updateHistoryXml() - Fin");
	}

	public static void addZoneResult(Modality modality, ServiceType serviceType, String zoneName, String rateType) {
			addZoneResult(modality,serviceType,zoneName,rateType,false);
	}
	public static void addZoneResult(Modality modality, ServiceType serviceType, String zoneName, String rateType,Boolean mida) {
		Logger.log(Logger.Debug, "addZoneResult(" + zoneName + ") - Inicio");
		Map<String, Object> jdomDocZoneResult = getZoneResultFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocZoneResult.get(XmlUtilsByModality.DOCUMENT);
			List<Element> zoneResults = document.getRootElement().getChildren(Xml.ZONERESULTCONFIGURATION);
			List<Element> listOfElements = new LinkedList<Element>();
			for (ResultName resultName : modality.getResultsNames(serviceType,mida)) {
				if(rateType != null && !resultName.group.equals(rateType))
					continue;
				Boolean notFound = false;
				String _zoneName = resultName.getZoneName(zoneName);
				for (Element element : zoneResults) {
					String name = element.getChildText(Xml.NAME);
					if (name.equals(_zoneName)) {
						Logger.log(Logger.Debug, "El Zone Result ya existe, se continua la configuracion");
						notFound = true;
						break;
					}
				}
				if (notFound == false) {
					Logger.log(Logger.Debug, "Se agrega un nuevo registro Zone Result");
					Element element = zoneResults.get(1).clone();
					element.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "new"));
					element.removeChild(Xml.INTERNALID);
					element.getChild(Xml.NAME).setText(_zoneName);
					element.getChild(Xml.DESCRIPTION).setText(_zoneName);
					element.getChild(Xml.RESULT).setText(_zoneName);
					listOfElements.add(element);
				}
			}
			if (!listOfElements.isEmpty()) {
				document.getRootElement().addContent(listOfElements);
				Logger.log(Logger.Debug, "Guardando XML de Zone Result Configuration");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocZoneResult.get(XmlUtilsByModality.FILE)));
			}
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de Zone Result Configuration, es posible que haya perdida de datos: "
							+ (String) jdomDocZoneResult.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "addZoneResult() - Fin");
	}

	public static HashMap<String, List<PriceTierRange>> getPriceTier(Modality modality, ServiceType serviceType, String zoneName) {
		Logger.log(Logger.Debug, "getPriceTier() - Inicio");
		Map<String, Object> jdomDocCharging = getChargesFile(modality, serviceType);
		HashMap<String, List<PriceTierRange>> priceTierPeriods = new HashMap<String, List<PriceTierRange>>();
		Element root = ((Document) jdomDocCharging.get(XmlUtilsByModality.DOCUMENT)).getRootElement();
		List<Element> aplicableRums = root.getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY)
				.getChildren(Xml.APPLICABLERUM);
		Element ar = null;
		for (Element element : aplicableRums) {
			if (element.getChildText(Xml.APPLICABLERUMNAME).equals("DUR")) {
				ar = element;
				break;
			} else if (element.getChildText(Xml.APPLICABLERUMNAME).equals("EVT")) {
				ar = element;
				break;
			}
		}
		if(ar == null)
			return priceTierPeriods;
		Element enhacedZoneModel = ar.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL);
		List<Element> chargesTelephonyDoc = enhacedZoneModel.getChildren(Xml.RESULTS);
		for (Element element : chargesTelephonyDoc) {
			String name = element.getChildText(Xml.NAME);
			if (name.equals(zoneName)) {
				Element model = element.getChild(Xml.CRPCOMPOSITEPOPMODEL);
				List<PriceTierRange> priceTierRanges = new LinkedList<PriceTierRange>();
				List<Element> validityPeriods = model.getChild(Xml.USAGECHARGEPOPMODEL).getChild(Xml.PRICETIER)
						.getChildren(Xml.PRICETIERVALIDITYPERIOD);
				for (Element range : validityPeriods) {
					for (Element priceTier : range.getChildren(Xml.PRICETIERRANGE)) {
						List<Element> charges = priceTier.getChildren(Xml.FIXEDCHARGE);
						for (Element charge : charges)
							priceTierRanges.add(new PriceTierRange(priceTier.getChildText(Xml.UPPERBOUND),
									charge.getChildText(Xml.BALANCEELEMENTNUMCODE),
									charge.getChildText(Xml.TAXCODE), Double.parseDouble(charge.getChildText(Xml.PRICE)),
									charge.getChildText(Xml.UNITOFMEASURE), null, charge.getChildText(Xml.GLID), false));
						charges = priceTier.getChildren(Xml.SCALEDCHARGE);
						for (Element charge : charges)
							priceTierRanges.add(new PriceTierRange(priceTier.getChildText(Xml.UPPERBOUND),
									charge.getChildText(Xml.BALANCEELEMENTNUMCODE),
									charge.getChildText(Xml.TAXCODE), Double.parseDouble(charge.getChildText(Xml.PRICE)),
									charge.getChildText(Xml.UNITOFMEASURE), Double.parseDouble(charge.getChildText(Xml.INCREMENTSTEP)), charge.getChildText(Xml.GLID), true));

					}
					if(!priceTierRanges.isEmpty()) 
						priceTierPeriods.put(range.getChildText(Xml.VALIDFROM), priceTierRanges);
				}
				
			}
		}
		Logger.log(Logger.Debug, "getPriceTier() - Fin");
		return priceTierPeriods;
	}
	
	public static boolean delPriceTierRange(Modality modality, ServiceType serviceType, String zoneName) {
		Logger.log(Logger.Debug, "delPriceTierRange(" + zoneName + ") - Inicio");
		boolean deleted = false;
		Map<String, Object> jdomDocCharging = getChargesFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocCharging.get(XmlUtilsByModality.DOCUMENT);
			Element root = document.getRootElement();
			for (Element ar : root.getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY).getChildren(Xml.APPLICABLERUM)) {
				Element zoneModel = ar.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL);
				for (Element element : zoneModel.getChildren(Xml.RESULTS)) {
					String name = element.getChildText(Xml.NAME);
					if (name.equals(zoneName)) {
						Logger.log(Logger.Debug, "Borrando elemento");
						zoneModel.removeContent(element);
						deleted = true;
						break;
					}
				}
			}
			if (deleted) {
				Logger.log(Logger.Debug, "Guardando XML de charges TEL");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocCharging.get(XmlUtilsByModality.FILE)));
			}
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Error al escribir XML al depurar cargo no utilizado " + zoneName + " charge: " + (String) jdomDocCharging.get(XmlUtilsByModality.FILE) + ", Saliendo!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "delPriceTierRange() - Fin");
		return deleted;
	}

	public static HashMap<String, List<ZoneItem>> getAllZoneItems(Modality modality, ServiceType serviceType) {
		return getAllZoneItems(modality,serviceType,false);
	}

	public static HashMap<String, List<ZoneItem>> getAllZoneItems(Modality modality, ServiceType serviceType, Boolean mida) {
		Logger.log(Logger.Debug, "getAllZoneItems() - Inicio");
		HashMap<String, List<ZoneItem>> map = new HashMap<String, List<ZoneItem>>();
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType,false, mida);
		for (Element element : ((Document) jdomDocZonning.get(Xml.DOCUMENT)).getRootElement().getChild(Xml.STANDARDZONEMODEL).getChildren(Xml.ZONEITEM)) {
			String zoneName = element.getChild(Xml.ZONERESULT).getChildText(Xml.ZONENAME);
			ZoneItem zoneItem = new ZoneItem(element.getChildText(Xml.DESTINATIONPREFIX), zoneName, element.getChildText(Xml.PRODUCTNAME), element.getChildText(Xml.VALIDFROM), element.getChildText(Xml.VALIDTO));
			List<ZoneItem> zoneItems;
			if (map.containsKey(zoneName)) {
				zoneItems = map.get(zoneName);
				zoneItems.add(zoneItem);
			} else {
				zoneItems = new ArrayList<ZoneItem>();
				zoneItems.add(zoneItem);
				map.put(zoneName, zoneItems);
			}
		}
		Logger.log(Logger.Debug, "getAllZoneItems() - Fin");
		return map;
	}
	
	public static boolean modifyZoneItemsValidTo(Modality modality, ServiceType serviceType, String zoneName, String newValidTo, String validToActual, boolean showMessage) {
		Logger.log(Logger.Debug, "modZoneItemsValidTo() - Inicio");
		boolean found = false;
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		try {
			Document document = (Document) jdomDocZonning.get(Xml.DOCUMENT);
			Element zoneItemsDoc = document.getRootElement().getChild(Xml.STANDARDZONEMODEL);
			for (Element element : zoneItemsDoc.getChildren(Xml.ZONEITEM)) {
				String zone = element.getChild(Xml.ZONERESULT).getChildText(Xml.ZONENAME);
				if (zoneName.equals(zone)) {
					Element validTo = element.getChild(Xml.VALIDTO);
					if (validToActual.equals(validTo.getText())) {
						found = true;
						if (newValidTo.compareTo(validTo.getText()) > 0) {
							String msg = "La fecha ingresada es mayor a la configurada actualmente - " + newValidTo + " > " + validTo.getText();
							if (showMessage)
								Logger.screen(Logger.Debug, msg);
							else
								Logger.log(Logger.Debug, msg);
							Logger.screen(Logger.Debug, "Se agrega un nuevo registro Zone Item");
							String newValidFrom = DateFormater.sumDays(validTo.getText(), 1);
							Element newElem = new Element(Xml.ZONEITEM);
							newElem.addContent(new Element(Xml.PRODUCTNAME).setText(element.getChildText(Xml.PRODUCTNAME)));
							newElem.addContent(new Element(Xml.ORIGINPREFIX).setText(element.getChildText(Xml.ORIGINPREFIX)));
							newElem.addContent(new Element(Xml.DESTINATIONPREFIX).setText(element.getChildText(Xml.DESTINATIONPREFIX)));
							newElem.addContent(new Element(Xml.VALIDFROM).setText(newValidFrom));
							newElem.addContent(new Element(Xml.VALIDTO).setText(newValidTo));
							newElem.addContent(new Element(Xml.ZONERESULT).addContent(new Element(Xml.ZONENAME).setText(zone)));
							newElem.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));
							zoneItemsDoc.addContent(newElem);
						} else {
							String msg = "La fecha ingresada es menor a la configurada actualmente - " + newValidTo + " < " + validTo.getText();
							if (showMessage) {
								Logger.screen(Logger.Debug, msg);
								Logger.screen(Logger.Debug, "Se modifica el registro Zone Item actual");
							} else
								Logger.log(Logger.Debug, msg);
							if (element.getAttribute(Parameters.XML_TAG_FLAG) == null) 
								element.setAttribute(new Attribute(Parameters.XML_TAG_FLAG, "update"));

							validTo.setText(newValidTo);
						}
						break;
					}
				}
			}
			if (found) {
				Logger.log(Logger.Debug, "Guardando XML de zonning");
				XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
				xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)));
			}
		} catch (IOException e) {
			Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error,
					"Error al guardar el cambio en el XML de zonning, es posible que haya perdida de datos: "
							+ (String) jdomDocZonning.get(XmlUtilsByModality.FILE) + "\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
		Logger.log(Logger.Debug, "modZoneItemsValidTo() - Fin");
		return found;
	}
	
	protected static Boolean zonning(Boolean force, Modality modality, ServiceType serviceType) throws FileNotFoundException, IOException, ExceptionImportExportPricing, InterruptedException {
		
		Logger.screen(Logger.Debug, "Analizando Zonning [" +  modality + ","+serviceType + "]");
		Map<String, Object> jdomDocZonning = getZoneFile(modality, serviceType);
		Boolean changesZonning = false, refreshNeededZonning = false;
		Document document = (Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT);
		List<Element> zoneItemsDoc = document.getRootElement().getChild(Xml.STANDARDZONEMODEL).getChildren(Xml.ZONEITEM);
		for (Element element : zoneItemsDoc) {
			Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
			if (a != null) {
				if (changesZonning == false) {
					Logger.log(Logger.Debug, "Guardando backup de XML de zonning [" +  modality + "," + serviceType + "]");
					XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
					xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)
							+ ".bk" + DateFormater.dateToShortString()));
				}
				changesZonning = true;
				if (a.getValue().equals("new"))
					refreshNeededZonning = true;
				Logger.log(Logger.Debug, "Cambio encontrado en zonning, removiendo atributo [" +  modality + "," + serviceType + "]");
				element.removeAttribute(Parameters.XML_TAG_FLAG);
			}
		}
		if (changesZonning || force) {
			Logger.log(Logger.Debug, "Guardando XML de zonning[" +  modality + "," + serviceType + "]");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE) + ".process"));
			Logger.screen(Logger.Debug, "Subiendo cambios en zonning[" +  modality + "," + serviceType + "]");
			List<String> args = new ArrayList<String>();
			args.add("-ow");
			ImportExportPricing.runImportExportPricing("-import",
					(String) jdomDocZonning.get(XmlUtilsByModality.FILE) + ".process", "-config", args);
			XmlUtils.moveProcessedFile((String) jdomDocZonning.get(XmlUtilsByModality.FILE));
			if (refreshNeededZonning)
				ImportExportPricing.genZonningBaseXml(true);
		} else
			Logger.screen(Logger.Debug, "No hay cambios por aplicar en Zonning [" +  modality + ","+serviceType + "]");
		return true;
	}
	
	protected static Boolean charging(Boolean force, Modality modality, ServiceType serviceType) throws FileNotFoundException, IOException, ExceptionImportExportPricing, InterruptedException {
		
		Logger.screen(Logger.Debug, "Analizando Charges [" +  modality + ","+serviceType + "]");
		Map<String, Object> jdomDocZonning = getChargesFile(modality, serviceType);
		Boolean changesZonning = false, refreshNeededZonning = false;
		Document document = (Document) jdomDocZonning.get(XmlUtilsByModality.DOCUMENT);
		List<Element> rums = document.getRootElement().getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY).getChildren(Xml.APPLICABLERUM);
		for(Element rum : rums) {
			for (Element element : rum.getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL).getChildren(Xml.RESULTS)) {
				Attribute a = element.getAttribute(Parameters.XML_TAG_FLAG);
				if (a != null) {
					if (changesZonning == false) {
						Logger.log(Logger.Debug, "Guardando backup de XML de Charges [" +  modality + "," + serviceType + "]");
						XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
						xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE)
								+ ".bk" + DateFormater.dateToShortString()));
					}
					changesZonning = true;
					if (a.getValue().equals("new"))
						refreshNeededZonning = true;
					Logger.log(Logger.Debug, "Cambio encontrado en Charges, removiendo atributo [" +  modality + "," + serviceType + "]");
					element.removeAttribute(Parameters.XML_TAG_FLAG);
				}
			}
		}
		if (changesZonning || force) {
			Logger.log(Logger.Debug, "Guardando XML de Charges[" +  modality + "," + serviceType + "]");
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, new FileOutputStream((String) jdomDocZonning.get(XmlUtilsByModality.FILE) + ".process"));
			Logger.screen(Logger.Debug, "Subiendo cambios en Charges[" +  modality + "," + serviceType + "]");
			List<String> args = new ArrayList<String>();
			args.add("-ow");
			ImportExportPricing.runImportExportPricing("-import",
					(String) jdomDocZonning.get(XmlUtilsByModality.FILE) + ".process", "-pricing", args);
			XmlUtils.moveProcessedFile((String) jdomDocZonning.get(XmlUtilsByModality.FILE));
			if (refreshNeededZonning)
				ImportExportPricing.genZonningBaseXml(true);
		} else
			Logger.screen(Logger.Debug, "No hay cambios por aplicar en Charges [" +  modality + ","+serviceType + "]");
		return true;
	}
	
	public static HashMap<String, PriceTier> getPriceTiers(Modality modality, ServiceType serviceType, String campaign) {
		return getPriceTiers(modality,serviceType,campaign,false);
	}

	public static HashMap<String, PriceTier> getPriceTiers(Modality modality, ServiceType serviceType, String campaign, Boolean mida) {
		Logger.log(Logger.Debug, "getPriceTiers(" + modality + ", " + serviceType + ") - Inicio");
		HashMap<String, PriceTier> map = new HashMap<String, PriceTier>();
		
		Map<String, Object> jdomDocCharges = getChargesFile(modality, serviceType,false, mida);
		List<Element> results = ((Document) jdomDocCharges.get(XmlUtilsByModality.DOCUMENT)).getRootElement().getChild(Xml.CHARGERATEPLAN).getChild(Xml.SUBSCRIBERCURRENCY).getChild(Xml.APPLICABLERUM).getChild(Xml.CRPRELDATERANGE).getChild(Xml.ZONEMODEL).getChildren(Xml.RESULTS);
		for (Element result : results) {
			Boolean isvalid =  true;
			String zoneName = result.getChildText(Xml.NAME);
			if(campaign != null) {
				isvalid = false;
				for(ResultName resultName : ResultName.values()) {
					if(resultName.applyServiceType(serviceType) && zoneName.equals(resultName.getZoneName(campaign))) {
						isvalid = true;
						break;
					}
				}
			}
			if (isvalid) {
				Element model = result.getChild(Xml.CRPCOMPOSITEPOPMODEL);
				HashMap<String, List<PriceTierRange>> priceTierRanges = new HashMap<String, List<PriceTierRange>>();
				Element priceTier = model.getChild(Xml.USAGECHARGEPOPMODEL).getChild(Xml.PRICETIER);
				for (Element period : priceTier.getChildren(Xml.PRICETIERVALIDITYPERIOD)) {
					String validFrom = period.getChildText(Xml.VALIDFROM);
					List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
					for(Element priceTierRange : period.getChildren(Xml.PRICETIERRANGE)) {
						List<Element> charges = priceTierRange.getChildren(Xml.FIXEDCHARGE);
						for(Element charge : charges) {
							listOfPriceTierRange.add(0, new PriceTierRange(priceTierRange.getChildText(Xml.UPPERBOUND), charge.getChildText(Xml.BALANCEELEMENTNUMCODE),
									charge.getChildText(Xml.TAXCODE), Double.parseDouble(charge.getChildText(Xml.PRICE)),
									charge.getChildText(Xml.UNITOFMEASURE),
									(charge.getChildText(Xml.INCREMENTSTEP)!= null ? Double.parseDouble(charge.getChildText(Xml.INCREMENTSTEP)) : null),
									charge.getChildText(Xml.GLID), false));
						}
						charges = priceTierRange.getChildren(Xml.SCALEDCHARGE);
						for(Element charge : charges) {
							listOfPriceTierRange.add(0, new PriceTierRange(priceTierRange.getChildText(Xml.UPPERBOUND), charge.getChildText(Xml.BALANCEELEMENTNUMCODE),
									charge.getChildText(Xml.TAXCODE), Double.parseDouble(charge.getChildText(Xml.PRICE)),
									charge.getChildText(Xml.UNITOFMEASURE),
									Double.parseDouble(charge.getChildText(Xml.INCREMENTSTEP)),
									charge.getChildText(Xml.GLID), true));
						}
					}
					if(!listOfPriceTierRange.isEmpty())
						priceTierRanges.put(validFrom, listOfPriceTierRange);
				}
				if(!priceTierRanges.isEmpty())
					map.put(zoneName, new PriceTier(zoneName, priceTierRanges, priceTier.getChildText(Xml.RUMNAME)));
			}
		}
		Logger.log(Logger.Debug, "getPriceTiers(" + modality + ", " + serviceType +") - Fin");
		return map;
	}
	
	public static HashMap<String, String> getHistory(Modality modality, ServiceType serviceType) {
		Logger.log(Logger.Debug, "XmlUtils.getHistory(" + modality + ", " + serviceType + ") - Inicio");
		HashMap<String, String> map = new HashMap<String, String>();
		Map<String, Object> jdomDocHistory = getHistoryFile(modality, serviceType);
		for (Element e : ((Document)jdomDocHistory.get(XmlUtilsByModality.DOCUMENT)).getRootElement().getChildren())
			map.put(e.getName(), e.getAttributeValue(Xml.MODIFYDATE));
		Logger.log(Logger.Debug, "XmlUtils.getHistory(" + modality + ", " + serviceType + ") - Fin");
		return map;
	}
	
	private static Map<String, Object> getHistoryFile(Modality modality, ServiceType serviceType){
		return getHistoryFile(modality, serviceType, false);
	}
	
	protected static Map<String, Object> getHistoryFile(Modality modality, ServiceType serviceType, Boolean onlyFileName) {
		Map<String, Object> jdomDocHistory = jdomDocHistories.get(modality + "." + serviceType);
		if (jdomDocHistory == null) {
			jdomDocHistory = new HashMap<String, Object>();
			String file = Parameters.XML_DB_DIR + Parameters.XML_HISTORY;
			jdomDocHistory.put(XmlUtilsByModality.FILE, file);
			if(!onlyFileName) {
				try {
					XMLInputFactory factory = XMLInputFactory.newFactory();
					XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
					StAXEventBuilder builder = new StAXEventBuilder();
					jdomDocHistory.put(XmlUtilsByModality.DOCUMENT, builder.build(reader));
					jdomDocHistories.put(modality + "." + serviceType, jdomDocHistory);
				} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
					Logger.screen(Logger.Error, e.toString());
					Logger.screen(Logger.Error, "Error al leer XML de historial" + file);
					return null;
				}
			}
		}
		return jdomDocHistory;
	}
	
	private static Map<String, Object> getSelectorFile(Modality modality, ServiceType serviceType){
		return  getSelectorFile(modality, serviceType, false);
	}

	protected static Map<String, Object> getSelectorFile(Modality modality, ServiceType serviceType, Boolean onlyFileName) {
		Map<String, Object> jdomDocSelector = jdomDocSelectors.get(modality + "." + serviceType);
		if (jdomDocSelector == null) {
			jdomDocSelector = new HashMap<String, Object>();
			String file = Parameters.XML_DB_DIR + Parameters.XML_ITEM_SELECTORS_TEL;
			if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.TEL))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_SELECTOR_TEL_PRE;
			else if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.SMS))
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_SELECTOR_SMS_PRE;
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.TEL))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_SELECTOR_TEL_HYB;
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.SMS))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_SELECTOR_SMS_HYB;
			else {
				Logger.screen(Logger.Error, "Configuracion invalida: {" + modality + " | " + serviceType +"}, Saliendo!");
				System.exit(Parameters.ERR_FILESYSTEM);
			}
			jdomDocSelector.put(XmlUtilsByModality.FILE, file);
			if(!onlyFileName) {
				try {
					XMLInputFactory factory = XMLInputFactory.newFactory();
					XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
					StAXEventBuilder builder = new StAXEventBuilder();
					jdomDocSelector.put(XmlUtilsByModality.DOCUMENT, builder.build(reader));
					jdomDocSelectors.put(modality + "." + serviceType, jdomDocSelector);
				} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
					Logger.screen(Logger.Error, e.toString());
					Logger.screen(Logger.Error,
							"Error al buscar descripcion de item en XML de telefonia: " + file + ", Saliendo!");
					System.exit(Parameters.ERR_FILESYSTEM);
				}
			}
		}
		return jdomDocSelector;
	}
	
	private  static Map<String, Object> getZoneFile(Modality modality, ServiceType serviceType){
		return  getZoneFile(modality, serviceType, false,false);
	}

	protected static Map<String, Object> getZoneFile(Modality modality, ServiceType serviceType, Boolean onlyFileName) {
		return  getZoneFile(modality, serviceType, onlyFileName,false);
	}
	
	protected static Map<String, Object> getZoneFile(Modality modality, ServiceType serviceType, Boolean onlyFileName, Boolean mida) {
		Map<String, Object> jdomDocZonning = jdomDocZonnings.get(modality + "." + serviceType);
		if (jdomDocZonning == null) {
			jdomDocZonning = new HashMap<String, Object>();
			String file = Parameters.XML_DB_DIR + Parameters.XML_ZONNING;
			if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.TEL)){
				if(mida)
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_PRE_MIDA;
				else
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_PRE;}
			else if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.SMS))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_SMS_PRE;
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.TEL)){
				if(mida)
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_HIB_MIDA;
				else
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_HYB;}
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.SMS))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_SMS_HYB;
			else if (modality.equals(Modality.HORARY) && serviceType.equals(ServiceType.TEL) && (!mida))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_HYB_HORARIO;
			else if (modality.equals(Modality.CCF) && serviceType.equals(ServiceType.TEL) && mida)
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_CCF_MIDA;
			else if (modality.equals(Modality.CCM) && serviceType.equals(ServiceType.TEL) && mida)
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_ZONNING_TEL_CCM_MIDA;
			else {
				Logger.screen(Logger.Error, "Configuracion invalida: {" + modality + " | " + serviceType +"}, Saliendo!");
				System.exit(Parameters.ERR_FILESYSTEM);
			}
			jdomDocZonning.put(XmlUtilsByModality.FILE, file);
			if(!onlyFileName) {
				try {
					XMLInputFactory factory = XMLInputFactory.newFactory();
					XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
					StAXEventBuilder builder = new StAXEventBuilder();
					jdomDocZonning.put(XmlUtilsByModality.DOCUMENT, builder.build(reader));
					jdomDocZonnings.put(modality + "." + serviceType, jdomDocZonning);
				} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
					Logger.screen(Logger.Error, e.toString());
					Logger.screen(Logger.Error, "Error al leer XML de zonning: " + file + ", Saliendo!");
					System.exit(Parameters.ERR_FILESYSTEM);
				}
			}
		}
		return jdomDocZonning;
	}
	
	private static Map<String, Object> getChargesFile(Modality modality, ServiceType serviceType){
		return getChargesFile(modality, serviceType, false);
	}

	protected static Map<String, Object> getChargesFile(Modality modality, ServiceType serviceType, boolean onlyFileName){
		return getChargesFile(modality, serviceType, onlyFileName,false);
	}

	protected static Map<String, Object> getChargesFile(Modality modality, ServiceType serviceType, Boolean onlyFileName, Boolean mida) {
		Map<String, Object> jdomDocCharging = jdomDocChargings.get(modality + "." + serviceType);
		if (jdomDocCharging == null) {
			jdomDocCharging = new HashMap<String, Object>();
			String file = Parameters.XML_DB_DIR + Parameters.XML_CHARGES_TEL;
			if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.TEL))
				if(!mida)
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_PRE;
				else
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_PRE_MIDA;
			else if (modality.equals(Modality.PREPAID) && serviceType.equals(ServiceType.SMS))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_SMS_PRE;
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.TEL))
				if(!mida)
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_HYB;
				else
					file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_HIB_MIDA;
			else if (modality.equals(Modality.HYBRID) && serviceType.equals(ServiceType.SMS))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_SMS_HYB;
			else if (modality.equals(Modality.HORARY) && serviceType.equals(ServiceType.TEL))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_HYB_HORARIO;
			else if (modality.equals(Modality.CCF) && serviceType.equals(ServiceType.TEL))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_CCF_MIDA;
			else if (modality.equals(Modality.CCM) && serviceType.equals(ServiceType.TEL))
				file = Parameters.XML_DB_DIR + Parameters.XML_CONFIG_CHARGES_TEL_CCM_MIDA;
			else {
				Logger.screen(Logger.Error, "Configuracion invalida: {" + modality + " | " + serviceType +"}, Saliendo!");
				System.exit(Parameters.ERR_FILESYSTEM);
			}
			jdomDocCharging.put(XmlUtilsByModality.FILE, file);
			if(!onlyFileName) {
				try {
					XMLInputFactory factory = XMLInputFactory.newFactory();
					XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
					StAXEventBuilder builder = new StAXEventBuilder();
					jdomDocCharging.put(XmlUtilsByModality.DOCUMENT, builder.build(reader));
					jdomDocChargings.put(modality + "." + serviceType, jdomDocCharging);
				} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
					Logger.screen(Logger.Error, e.toString());
					Logger.screen(Logger.Error, "Error al leer XML de charges Telephony: " + file + ", Saliendo!");
					System.exit(Parameters.ERR_FILESYSTEM);
				}
			}
		}
		return jdomDocCharging;
	}
	
	private static Map<String, Object> getZoneResultFile(Modality modality, ServiceType serviceType){
		return getZoneResultFile(modality, serviceType, false);
	}

	protected static Map<String, Object> getZoneResultFile(Modality modality, ServiceType serviceType, Boolean onlyFileName) {
		Map<String, Object> jdomDocHistory = jdomDocHistories.get(modality + "." + serviceType);
		if (jdomDocHistory == null) {
			jdomDocHistory = new HashMap<String, Object>();
			String file = Parameters.XML_DB_DIR + Parameters.XML_ZONE_RESULT_CONFIGURATION;
			jdomDocHistory.put(XmlUtilsByModality.FILE, file);
			if(!onlyFileName) {
				try {
					XMLInputFactory factory = XMLInputFactory.newFactory();
					XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
					StAXEventBuilder builder = new StAXEventBuilder();
					jdomDocHistory.put(XmlUtilsByModality.DOCUMENT, builder.build(reader));
					jdomDocHistories.put(modality + "." + serviceType, jdomDocHistory);
				} catch (FileNotFoundException | XMLStreamException | JDOMException e) {
					Logger.screen(Logger.Error, e.toString());
					Logger.screen(Logger.Error, "Error al leer XML de Zone Result Configuration " + file);
					return null;
				}
			}
		}
		return jdomDocHistory;
	}

}