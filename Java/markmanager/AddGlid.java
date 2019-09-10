package markmanager;

import brmHandlers.PinLoadGlid;
import config.Parameters;
import cust.Glid;
import cust.Utils;
import exceptions.ExceptionNotPermited;
import log.Logger;

/**
 * Punto de acceso a creaci\u00F3n de GLIDs para proveedores de contenido
 * 
 * @author Adrian Rocha
 */
public class AddGlid {

    /**
     * @param args Requiere GLID, 
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddGlid");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO ADD GLID ***");
        Utils.removeSpecialChar(args);
        String newGlid = args[0];
        String newDescription = args[1];
        try {
            Glid myGlid = new Glid(newGlid, newDescription);
            if (PinLoadGlid.existsGlid(myGlid.getCode())) {
                Logger.screen(Logger.Error, "El c\u00F3digo de GLID indicado ya existe en BRM");
                System.exit(Parameters.ERR_INVALID_VALUE);
            }
            else {
                Logger.screen(Logger.Debug, "Se verific\u00F3 que el c\u00F3digo de GLID indicado NO existe...");
                Logger.screen(Logger.Debug, "Generando la nueva configuraci\u00F3n:");
                Logger.screen(Logger.Debug, "GLID: "+newGlid);
                Logger.screen(Logger.Debug, "Descripci\u00F3n: "+newDescription);
                PinLoadGlid plg = new PinLoadGlid(false);
                plg.addGlid(myGlid);
                plg.loadToDB();
                Logger.screen(Logger.Debug, "GLID agregado.");
            }
        } catch (ExceptionNotPermited e) {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddGlid");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }        
    }
}
