package markmanager;

import brmHandlers.XmlUtils;
import log.Logger;
import config.Parameters;
import cust.Utils;

/**
 * Punto de acceso para modificaci\u00F3n de descripciones de Items
 * 
 * @author Adrian Rocha
 */
public class ModItemDescription {

    /**
     * @param args Requiere 
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ModItemDescription");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO MOD ITEM DESCR ***");
        Utils.removeSpecialChar(args);
        String evtType = args[0];
        String campana = args[1];
        String newDescr = args[2].replace(' ', '_');
        if (!evtType.equals("900") && !evtType.equals("SMS")) {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en Tipo de campa\u00F1a: "+evtType);
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
    	Logger.screen(Logger.Debug, "Modificando descripcion del item correspondiente a "+campana);
        Logger.screen(Logger.Debug, "Nueva descripci\u00F3n: "+newDescr);
        if (evtType.equals("SMS")) {
        	if (!XmlUtils.modItemSmsDescription(campana, newDescr)) {
        		Logger.screen(Logger.Debug, "NO se encontr\u00F3 la campa\u00F1a SMS indicada!");
        		System.exit(Parameters.ERR_INVALID_VALUE);
        	}
        }
        else {
        	if (!XmlUtils.modItemTelephonyDescription(campana, newDescr)) {
        		Logger.screen(Logger.Debug, "NO se encontr\u00F3 la campa\u00F1a 900 indicada!");
        		System.exit(Parameters.ERR_INVALID_VALUE);
        	}
        }
		Logger.screen(Logger.Debug, "Descripci\u00F3n de item modificada");
    }
}
