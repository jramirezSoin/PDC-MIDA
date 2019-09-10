package cust;

import log.Logger;
import config.Parameters;

/**
*
* @author Adrian Rocha
* Esta clase centraliza las distintas validaciones realizadas a los datos de entrada
*/
public class Validations {
	
	public static void validateEvtAndRateType (String evtType, String rateType) {
		if (evtType.equals("900")) {
        	if (!rateType.equals("DUR") && !rateType.equals("EVT")) {
                Logger.screen(Logger.Error, "Valor inv\u00E1lido en Tipo de tasaci\u00F3n, en 900 solo se permiten los tipos DUR o EVT: "+rateType);
                System.exit(Parameters.ERR_INVALID_VALUE);        		
        	}
        }
        else if (evtType.equals("SMS")) {
        	if (!rateType.equals("MO") && !rateType.equals("MT")) {
                Logger.screen(Logger.Error, "Valor inv\u00E1lido en Tipo de tasaci\u00F3n, en SMS solo se permiten los tipos MO o MT: "+rateType);
                System.exit(Parameters.ERR_INVALID_VALUE);        		
        	}
        }
        else {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en Tipo de campa\u00F1a: "+evtType);
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
	}
	
	public static void validateCampaignID(String campaign, String evtType) {
		if (evtType.equals("900") && (campaign.length() != 7)) {
            Logger.screen(Logger.Error, "El identificador de la campa\u00F1a debe tener entre 7 caracteres.");
            System.exit(Parameters.ERR_INVALID_VALUE);			
		}
		else if (evtType.equals("SMS") && ((campaign.length() < 3) || (campaign.length() > 6))) {
            Logger.screen(Logger.Error, "El identificador de la campa\u00F1a debe tener entre 3 y 6 caracteres.");
            System.exit(Parameters.ERR_INVALID_VALUE);			
		}
	}
	
	public static void validateRealNumber(String realNumber, String evtType, String campaign) {
		if (evtType.equals("900")) {
        	if (realNumber.length() != 8) {
                Logger.screen(Logger.Error, "El n\u00FAmero real en 900 debe tener 8 caracteres.");
                System.exit(Parameters.ERR_INVALID_VALUE);        		
        	}
        }
		else {
			if (!realNumber.equals(campaign)) {
                Logger.screen(Logger.Error, "En SMS el n\u00FAmero real debe ser igual al ID de la campa\u00F1a.");
                System.exit(Parameters.ERR_INVALID_VALUE);				
			}
		}
	}

}
