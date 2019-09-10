package markmanager;

import brmHandlers.XmlUtils;
import log.Logger;
import config.Parameters;

public class CommitChanges {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("InitialSetup");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO COMMIT CHANGES ***");
        boolean force = false;
        force = args.length > 0 && args[0].equals("-f");
        XmlUtils.publishChanges(force);
        Logger.log(Logger.Debug, "CommitChanges - Fin");
	}
}
