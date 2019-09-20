package markmanager;

import brmHandlers.ImportExportPricing;
import log.Logger;
import config.Parameters;
import cust.Modality;
import cust.ServiceType;
import cust.Utils;

public class InitialSetup {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("InitialSetup");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO INITIAL SETUP ***");
        Utils.removeSpecialChar(args);
        switch (args[0]) {
        case "-usc":
        	ImportExportPricing.genUscSelectorBaseXml(false);
        	break;
        case "-zonning":
        	ImportExportPricing.genZoneResultConfigurationXml(false);
        	ImportExportPricing.genZonningBaseXml(false);
        	break;
        case "-charges":
        	ImportExportPricing.genChargesSmsBaseXml(false);
        	ImportExportPricing.genChargesSmsUsdBaseXml(false);
        	ImportExportPricing.genChargesGprsUsdBaseXml(false);
			ImportExportPricing.genChargesTelephonyBaseXml(false);
			ImportExportPricing.genChargesTelephonyUsdBaseXml(false);
			break;
        case "-items":
        	ImportExportPricing.genItemsSmsBaseXml(false);
        	ImportExportPricing.genItemsTelephonyBaseXml(false);
        	break;
        case "-history":
        	ImportExportPricing.genHistoryBaseXml();
        	break;
        case "-prepaid":
        	prepaid();
        	break;
        case "-hybrid":
        	hybrid();
        	break;
        case "-horary":
        	horary();
        	break;
        case "-mida":
            mida();
            break;    
        case "-all":
        	ImportExportPricing.genZoneResultConfigurationXml(false);
        	ImportExportPricing.genZonningBaseXml(false);
        	ImportExportPricing.genUscSelectorBaseXml(false);
        	ImportExportPricing.genItemsSmsBaseXml(false);
        	ImportExportPricing.genItemsTelephonyBaseXml(false);
        	ImportExportPricing.genChargesSmsBaseXml(false);
        	ImportExportPricing.genChargesSmsUsdBaseXml(false);
        	ImportExportPricing.genChargesGprsUsdBaseXml(false);
        	ImportExportPricing.genChargesTelephonyBaseXml(false);
        	ImportExportPricing.genChargesTelephonyUsdBaseXml(false);
        	ImportExportPricing.genHistoryBaseXml();
        	prepaid();
        	hybrid();
        	horary();
            mida();
        	break;
        case "-prices":
        	prices();
        	break;
        default:
        	Logger.screen(Logger.Error, "Parametro no valido!");
        	System.exit(Parameters.ERR_IOERROR);
        }
        Logger.screen(Logger.Debug, "Configuraci\u00F3n Finalizada");
	}
	
	private static void prices() {
		Logger.onlyScreen("PRECIOS POR DEFECTO\n");
		Logger.onlyScreen("Minuto (default.price.minute.pre): " + Parameters.DEFAULT_PRICE_MINUTE);
		Logger.onlyScreen("SMS (default.price.sms.pre): " + Parameters.DEFAULT_PRICE_SMS);
		Logger.onlyScreen("");
    	//TODO validar si se debe aplicar, se requieren datos del properties
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.HORARY, ServiceType.TEL);
	}
	
	private static void horary() {
		ImportExportPricing.genZoneResultConfigurationXml(false);
		ImportExportPricing.genZonningBaseXml(false, Modality.HORARY, ServiceType.TEL);
    	ImportExportPricing.genChargesBaseXml(false, Modality.HORARY, ServiceType.TEL);
    	//TODO validar si se debe aplicar, se requieren datos del properties
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.HORARY, ServiceType.TEL);
	}
	
	private static void hybrid() {
		ImportExportPricing.genZoneResultConfigurationXml(false);
		ImportExportPricing.genZonningBaseXml(false, Modality.HYBRID, ServiceType.TEL);
    	ImportExportPricing.genZonningBaseXml(false, Modality.HYBRID, ServiceType.SMS);
    	ImportExportPricing.genChargesBaseXml(false, Modality.HYBRID, ServiceType.TEL);
    	ImportExportPricing.genChargesBaseXml(false, Modality.HYBRID, ServiceType.SMS);
    	//TODO validar si se debe aplicar, se requieren datos del properties
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.HYBRID, ServiceType.TEL);
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.HYBRID, ServiceType.SMS);
	}
	
	private static void prepaid() {
		ImportExportPricing.genZoneResultConfigurationXml(false);
		ImportExportPricing.genZonningBaseXml(false, Modality.PREPAID, ServiceType.TEL);
    	ImportExportPricing.genZonningBaseXml(false, Modality.PREPAID, ServiceType.SMS);
    	ImportExportPricing.genChargesBaseXml(false, Modality.PREPAID, ServiceType.TEL);
    	ImportExportPricing.genChargesBaseXml(false, Modality.PREPAID, ServiceType.SMS);
    	//TODO validar si se debe aplicar, se requieren datos del properties
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.PREPAID, ServiceType.TEL);
    	//ImportExportPricing.genItemSelectorBaseXml(false, Modality.PREPAID, ServiceType.SMS);
	}

    private static void mida() {
        ImportExportPricing.genZoneResultConfigurationXml(false);
        ImportExportPricing.genZonningBaseXml(false, Modality.PREPAID, ServiceType.TEL,true);
        ImportExportPricing.genZonningBaseXml(false, Modality.HYBRID, ServiceType.TEL,true);
        ImportExportPricing.genZonningBaseXml(false, Modality.CCM, ServiceType.TEL,true);
        ImportExportPricing.genZonningBaseXml(false, Modality.CCF, ServiceType.TEL,true);
        ImportExportPricing.genChargesBaseXml(false, Modality.PREPAID, ServiceType.TEL,true);
        ImportExportPricing.genChargesBaseXml(false, Modality.HYBRID, ServiceType.TEL,true);
        ImportExportPricing.genChargesBaseXml(false, Modality.CCM, ServiceType.TEL,true);
        ImportExportPricing.genChargesBaseXml(false, Modality.CCF, ServiceType.TEL,true);
        //TODO validar si se debe aplicar, se requieren datos del properties
        //ImportExportPricing.genItemSelectorBaseXml(false, Modality.PREPAID, ServiceType.TEL);
        //ImportExportPricing.genItemSelectorBaseXml(false, Modality.PREPAID, ServiceType.SMS);
    }

}
