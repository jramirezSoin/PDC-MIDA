package markmanager;

import brmHandlers.PinLoadGlid;
import config.Parameters;
import cust.Glid;
import cust.Utils;
import exceptions.ExceptionNotPermited;
import log.Logger;

/**
 *
 * @author Adrian Rocha
 */
public class ModGlid {
    
    /**
     * @param args Requiere GLID, Descripci\u00F3n, Tax code, servicio ICE
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ModGlid");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO MOD GLID ***");
        Utils.removeSpecialChar(args);
        String newGlid = args[0];
        String newDescription = args[1];
        try {
            Logger.screen(Logger.Debug, "Buscando GLID...");
            Glid myGlid = new Glid(newGlid, newDescription);
            if (!PinLoadGlid.existsGlid(myGlid.getCode())) {
                Logger.screen(Logger.Error, "El c\u00F3digo de GLID indicado NO existe en BRM - No es posible la modificaci\u00F3n.");
                Logger.screen(Logger.Error, "Saliendo.");
                System.exit(Parameters.ERR_INVALID_VALUE);
            }
            else {
                Logger.screen(Logger.Debug, "Se encontr\u00F3 el c\u00F3digo de GLID indicado en BRM...");
                Logger.screen(Logger.Debug, "Aplicando modificaci\u00F3n:");
                Logger.screen(Logger.Debug, "GLID: "+newGlid);
                Logger.screen(Logger.Debug, "Descripci\u00F3n: "+newDescription);
                Logger.screen(Logger.Debug, "Modificando Glid en BRM...");
                PinLoadGlid plg = new PinLoadGlid(false);
                if (plg.modGlid(myGlid)) {
                    plg.loadToDB();
                }
                else {
                	plg.modGlidIfw(myGlid);
                }
                Logger.screen(Logger.Debug, "Modificaci\u00F3n completada.");
            }
        } catch (ExceptionNotPermited e) {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en ModGlid");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
    }
}
