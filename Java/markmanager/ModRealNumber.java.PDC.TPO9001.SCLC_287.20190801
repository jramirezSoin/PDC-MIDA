package markmanager;

import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.DateFormater;
import cust.Modality;
import cust.Utils;
import cust.Validations;
import log.Logger;

/**
*
* @author Adrian Rocha
* @modifiedBy Roger Masis
*/
public class ModRealNumber {

    /**
     * @param args Requiere Campaña, fecha y Nuevo número real 
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ModRealNumber");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO MOD NUMERO REAL ***");
        Utils.removeSpecialChar(args);
        Integer i = 0;
        Modality modality = Modality.getModality(args[i]);
        if (args.length >= 4 && modality == null) {
        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[i] + "] no ha sido implemantada");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (modality != null)
			i++;
		else
			modality = Modality.POSPAID;
        String campaign = args[i++];
        String newNumber = args[i++];
        String date = args[i++];
        Validations.validateCampaignID(campaign, "900");
        Validations.validateRealNumber(newNumber, "900", campaign);
        DateFormater.stringToDate(date);
        if (modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID) || modality.equals(Modality.HORARY))
        	campaignTel(modality, campaign, newNumber, date);
        else {
	        newNumber = "00999" + newNumber;
	        //TODO hay que verificar en tasacion si las fechas de from y to son inclusivas
	        if (!XmlUtils.modZoneItemsDestinationPrefix("E_NE"+campaign, date, newNumber) && !XmlUtils.modZoneItemsDestinationPrefix("E_ND"+campaign, date, newNumber)) {
	        	Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
	        	Logger.screen(Logger.Error, "Saliendo.");
	        	System.exit(Parameters.ERR_INVALID_VALUE);
	        }
	        XmlUtils.updateHistoryXml("E_N"+campaign);
        }
        Logger.screen(Logger.Debug, "La configuracion se aplico satisfactoriamente en la campa\u00F1a: "+campaign);
    }
    
    private static void campaignTel(Modality modality, String campaign, String newNumber, String date) {
    	if (!XmlUtilsByModality.modifyZoneItemsDestinationPrefix(modality, modality.TEL, campaign, date, newNumber, null)) {
        	Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
        	Logger.screen(Logger.Error, "Saliendo.");
        	System.exit(Parameters.ERR_INVALID_VALUE);
        }
        XmlUtils.updateHistoryXml(campaign);
    }
}
