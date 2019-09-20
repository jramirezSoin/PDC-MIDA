package brmHandlers;

import config.Parameters;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.Logger;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import cust.DateFormater;
import cust.Modality;
import cust.ServiceType;
import cust.Xml;
import cust.ZoneItem;
import exceptions.ExceptionImportExportPricing;

/**
 *
 * @author Adrian Rocha
 */
public class ImportExportPricing {
    
    public static final String SUFIX_CONFIG = "_config.xml";
    public static final String SUFIX_PRICING = "_pricing.xml";
    private static final String TMP_FILENAME = "proveedores_export";
    
    public static void genUscSelectorBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de USC Selector");
    	File xmlUscBase = new File(Parameters.XML_DB_DIR+Parameters.XML_USC_SELECTOR);
    	if (xmlUscBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para USC Selector, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML USC Selector");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("USC_MAP");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_USC_SELECTOR);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlUscBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de USC Selector generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en USC Selector");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genUscSelectorBaseXml() - Fin");
    }
    
    public static void genZoneResultConfigurationXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Zone Result Configuration");
    	File xmlZonningBase = new File(Parameters.XML_DB_DIR+Parameters.XML_ZONE_RESULT_CONFIGURATION);
    	if (xmlZonningBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Zone Result Configuration, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Zone Result Configuration");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("ZONE_RESULT_CONFIGURATION");
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlZonningBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Zone Result Configuration generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Zone Result Configuration");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genZoneResultConfigurationXml() - Fin");
    }
    
    public static void genZonningBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Zonning");
    	File xmlZonningBase = new File(Parameters.XML_DB_DIR+Parameters.XML_ZONNING);
    	if (xmlZonningBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Zonning, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Zonning");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("STANDARD_ZONE_MODEL");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_ZONNING);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlZonningBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Zonning generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Zonning");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genZonningBaseXml() - Fin");
    }
    
    public static void genItemsSmsBaseXml(boolean forced) {
    	//TODO verificar los XMLs generados
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Items SMS");
    	File xmlItemsSmsBase = new File(Parameters.XML_DB_DIR+Parameters.XML_ITEM_SELECTORS_SMS);
    	if (xmlItemsSmsBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Items SMS, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Items SMS");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("ITEM_TYPE_SELECTOR");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_ITEM_SMS);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlItemsSmsBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Items SMS generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en SMS Items");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genItemsSmsBaseXml() - Fin");
    }
    
    public static void genItemsTelephonyBaseXml(boolean forced) {
    	//TODO verificar los XMLs generados
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Items Telephony");
    	File xmlItemsTelephonyBase = new File(Parameters.XML_DB_DIR+Parameters.XML_ITEM_SELECTORS_TEL);
    	if (xmlItemsTelephonyBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Items TEL, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Items TEL");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("ITEM_TYPE_SELECTOR");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_ITEM_TEL);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlItemsTelephonyBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Items Telephony generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Telephony Items");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genItemsTelephonyBaseXml() - Fin");
    }
    
    public static void genChargesTelephonyBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges Telephony");
    	File xmlChargesTelephonyBase = new File(Parameters.XML_DB_DIR+Parameters.XML_CHARGES_TEL);
    	if (xmlChargesTelephonyBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges TEL, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges TEL");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_CHARGES_TEL);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlChargesTelephonyBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de charges TEL generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Telephony Charges");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesTelephonyBaseXml() - Fin");
    }
    
    public static void genChargesSmsUsdBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges SMS USD");
    	File xmlChargesSmsUsdBase = new File(Parameters.XML_DB_DIR+Parameters.XML_CHARGES_SMS_USD);
    	if (xmlChargesSmsUsdBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges SMS USD, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges SMS USD");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_CHARGES_SMS_USD);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlChargesSmsUsdBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de charges SMS USD generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en SMS Charges USD");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesSmsUsdBaseXml() - Fin");
    }
    
    public static void genChargesGprsUsdBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges GPRS USD");
    	File xmlChargesGprsUsdBase = new File(Parameters.XML_DB_DIR+Parameters.XML_CHARGES_GPRS_USD);
    	if (xmlChargesGprsUsdBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges GPRS USD, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges GPRS USD");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_CHARGES_GPRS_USD);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlChargesGprsUsdBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de charges GPRS USD generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en GPRS Charges USD");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesGprsUsdBaseXml() - Fin");
    }
    
    public static void genChargesTelephonyUsdBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges Telephony USD");
    	File xmlChargesTelephonyUsdBase = new File(Parameters.XML_DB_DIR+Parameters.XML_CHARGES_TEL_USD);
    	if (xmlChargesTelephonyUsdBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges TEL USD, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges TEL USD");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_CHARGES_TEL_USD);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlChargesTelephonyUsdBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de charges TEL USD generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Telephony Charges USD");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesTelephonyUsdBaseXml() - Fin");
    }
    
    public static void genChargesSmsBaseXml(boolean forced) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges SMS");
    	File xmlChargesSmsBase = new File(Parameters.XML_DB_DIR+Parameters.XML_CHARGES_SMS);
    	if (xmlChargesSmsBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges SMS, desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges SMS");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH+TMP_FILENAME+SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	args.add(Parameters.PDC_CONFIG_CHARGES_SMS);
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlChargesSmsBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de charges SMS generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en SMS Charges");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesSmsBaseXml() - Fin");
    }
    
    public static void genHistoryBaseXml() {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de History");
    	File xmlHistory = new File(Parameters.XML_DB_DIR+Parameters.XML_HISTORY);
    	if (xmlHistory.exists()) {
	    	Logger.screen(Logger.Error, "Ya existe XML con la información de cambios. Se cancel\u00F3 la creaci\u00F3n de XML, saliendo!");
			System.exit(Parameters.ERR_IOERROR);
    	}
    	HashMap<String, List<ZoneItem>> zones = XmlUtils.getAllZoneItems();
    	Element history = new Element("history");
    	Document jdomDocHistory = new Document(history);
    	String today = DateFormater.dateToString(new Date());
    	for (String z : zones.keySet()) {
			Element zoneName = new Element(z);
			zoneName.setAttribute("modifyDate", today);
			history.addContent(zoneName);
		}
    	try {
	        Logger.log(Logger.Debug, "Guardando XML de historial");
	        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
	        xmlOut.output(jdomDocHistory, new FileOutputStream(Parameters.XML_DB_DIR+Parameters.XML_HISTORY));
    	} catch (IOException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al guardar el cambio en el XML de historial: "+
            Parameters.XML_DB_DIR+Parameters.XML_HISTORY+"\nConsulte al administrador!");
			System.exit(Parameters.ERR_FILESYSTEM);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genHistoryBaseXml() - Fin");
    }
    
    public static void runImportExportPricing(String mode, String fileName, String dataType, List<String> args) throws ExceptionImportExportPricing, InterruptedException {
        Logger.log(Logger.Debug, "runImportExportPricing("+mode+", "+fileName+", "+dataType+", "+args.toString()+") - Inicio");
        Process process;
        try {
        	args.add(0, Parameters.PDC_LOADER_COMMAND);
        	args.add(1, mode);
        	if (mode.equals("-import")) {//En este caso hay que invertir los parametros para que funcione
	        	args.add(2, dataType);
	        	args.add(3, fileName);
        	}
        	else {
	        	args.add(2, fileName);
	        	args.add(3, dataType);
        	}
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(Parameters.PDC_LOADER_PATH));
            Logger.log(Logger.Debug, "Iniciando ImportExportPricing");
            process = pb.start();
            Thread.sleep(2000);
            OutputStream stdin = process.getOutputStream();
            InputStream iop = process.getInputStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            InputStream ier = process.getErrorStream();
            BufferedInputStream bie = new BufferedInputStream(ier);
            writer.write(Parameters.PDC_LOADER_PASSWORD);
            writer.write("\n");
            writer.flush();
            process.waitFor();
            Logger.log(Logger.Debug, "Loader ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            Logger.log(Logger.Debug, salida);
            writer.close();
            //TODO el ImportExportPricing no esta devolviendo codigo de error cuando termina mal
            if (process.exitValue() != 0) {
            	Logger.screen(Logger.Error, IOUtils.toString(bie, "UTF-8")); 
            	throw new ExceptionImportExportPricing(salida);
            }
        } catch (IOException ioe) {
                Logger.screen(Logger.Error, ioe.toString());
                Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing!");
                Logger.screen(Logger.Error, "Saliendo");
                System.exit(Parameters.ERR_IOERROR);
        }
    }
    
    public static void genItemSelectorBaseXml(boolean forced, Modality modality, ServiceType serviceType) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Item " + serviceType);
    	Map<String, Object> jdomDocCharges = XmlUtilsByModality.getZoneFile(modality, serviceType, true);
    	
    	
    	File xmlZonningBase = new File((String) jdomDocCharges.get(XmlUtilsByModality.FILE));
    	if (xmlZonningBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Item [" + serviceType + "," + modality + "], desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Item [" + serviceType + "," + modality + "]");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH + TMP_FILENAME + SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("ITEM_TYPE_SELECTOR");
    	args.add( "-n");
    	if(serviceType.equals(ServiceType.TEL))
    		args.add(modality.TEL.getPDCConfigItem());
    	else
    		args.add(modality.SMS.getPDCConfigItem());
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlZonningBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Item [" + serviceType + "," + modality + "] generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Charges [" + serviceType + "," + modality + "]");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genItemSelectorBaseXml() - Fin");
    }
    //CAMBIO
    public static void genChargesBaseXml(boolean forced, Modality modality, ServiceType serviceType) {
        genChargesBaseXml(forced,modality,serviceType,false);
    }
    public static void genChargesBaseXml(boolean forced, Modality modality, ServiceType serviceType,Boolean mida) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Charges [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
    	Map<String, Object> jdomDocCharges = XmlUtilsByModality.getChargesFile(modality, serviceType, true,mida);
    	
    	
    	File xmlZonningBase = new File((String) jdomDocCharges.get(XmlUtilsByModality.FILE));
    	if (xmlZonningBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Charges [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "], desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Charges [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH + TMP_FILENAME + SUFIX_PRICING);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("CHARGE_RATE_PLAN");
    	args.add( "-n");
    	if(serviceType.equals(ServiceType.TEL)){
    		args.add(modality.TEL.getPDCConfigCharges(modality,mida));
        }
    	else
    		args.add(modality.SMS.getPDCConfigCharges(modality));
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-pricing", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlZonningBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Charges [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "] generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Charges [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genChargesBaseXml() - Fin");
    }
    
    public static void genZonningBaseXml(boolean forced, Modality modality, ServiceType serviceType) {
        genZonningBaseXml(forced,modality,serviceType,false);
    }    
    public static void genZonningBaseXml(boolean forced, Modality modality, ServiceType serviceType,Boolean mida) {
    	Logger.screen(Logger.Debug, "Generando configuraci\u00F3n base de Zonning [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
    	Map<String, Object> jdomDocZonning = XmlUtilsByModality.getZoneFile(modality, serviceType, true,mida);
    	
    	File xmlZonningBase = new File((String) jdomDocZonning.get(XmlUtilsByModality.FILE));
    	if (xmlZonningBase.exists() && !forced) {
    		String confirm = System.console().readLine("Existe ya un archivo de configuraci\u00F3n para Zonning [" + serviceType + ", " + modality + ((mida)?",MIDA":"")+ "], desea sobreescribirlo (si/no)?");
    		if (!confirm.toLowerCase().equals("si")) {
    	    	Logger.screen(Logger.Error, "Se cancel\u00F3 la creaci\u00F3n de XML Zonning [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
    			return;
    		}
    	}
    	File fileExport = new File(Parameters.PDC_LOADER_PATH + TMP_FILENAME + SUFIX_CONFIG);
    	if (fileExport.exists() && !fileExport.delete()) {
            Logger.screen(Logger.Error, "Error. No se pudo borrar el archivo temporal en ImportExportPricing. Saliendo!");
            System.exit(Parameters.ERR_FILESYSTEM);    		
    	}
    	List<String> args = new ArrayList<String>();
    	args.add("STANDARD_ZONE_MODEL");
    	args.add( "-n");
    	if(serviceType.equals(ServiceType.TEL))
    		args.add(ServiceType.TEL.getPDCConfigZonning(modality,mida));
    	else
    		args.add(ServiceType.SMS.getPDCConfigZonning(modality));
    	try {
    		Logger.log(Logger.Debug, "Ejecutando ImportExportPricing");
			runImportExportPricing("-export", TMP_FILENAME, "-config", args);
    		Logger.log(Logger.Debug, "Moviendo archivo generado");
			if (!fileExport.renameTo(xmlZonningBase)) {
				throw new ExceptionImportExportPricing("No se pudo mover el archivo generado!");
			}
    		Logger.log(Logger.Debug, "Configuraci\u00F3n de Zonning [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "] generada");
		} catch (ExceptionImportExportPricing | InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al ejecutar ImportExportPricing en Zonning [" + serviceType + "," + modality + ((mida)?",MIDA":"")+ "]");
            System.exit(Parameters.ERR_IOERROR);
		}
    	Logger.log(Logger.Debug, "ImportExportPricing.genZonningBaseXml() - Fin");
    }
}
