package groupUpdater;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import log.Logger;
import brmHandlers.XmlUtils;

import config.Parameters;
import cust.ZoneItem;
import database.DBManager;

public class AddMarkMIDA {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddMarkMIDA");
		Logger.log(Logger.Debug, "Creacion de destino internacional");
		if (args.length < 4) {
            Logger.screen(Logger.Error, "Parametros insuficientes");
            System.exit(Parameters.ERR_INVALID_VALUE);
		}
		HashMap<String, List<ZoneItem>>  zoneDestins = XmlUtils.getAllZoneInternationalDestins();
		Statement stmt;
		String destin = args[0];
		String zoneName = args[3];
        try {
			stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;
			if (zoneDestins.containsKey(destin)) {
				Logger.screen(Logger.Error, "La Marcacion destino "+destin+" que intenta configurar ya existe para el servicio TEL, proceso cancelado");
	            System.exit(Parameters.ERR_INVALID_VALUE);
			}
			destinsIfw = stmt.executeQuery("select NAME from IFW_STANDARD_ZONE where servicecode = 'TEL' AND ZONE_RT LIKE '%MI%' and destin_areacode = '00"+destin+"'");
			destinsIfw.next();
			String description = destinsIfw.getString(1);
			//cambiar
			XmlUtils.addZoneItem("TelcoGsmTelephony", zoneName, "19800101", "inf", "00"+destin, description);
		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
