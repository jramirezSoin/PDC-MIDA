package groupUpdater;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import log.Logger;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;

import config.Parameters;
import cust.ZoneItem;
import database.DBManager;
import Modality;

public class AddMarkMIDA {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddMarkMIDA");
		Logger.log(Logger.Debug, "Creacion de destino internacional");
		if (args.length < 6) {
            Logger.screen(Logger.Error, "Parametros insuficientes");
            System.exit(Parameters.ERR_INVALID_VALUE);
		}
		HashMap<String, List<ZoneItem>>  zoneDestins = XmlUtils.getAllZoneInternationalDestins();
		Statement stmt;
		
		Modality modality = Modality.getModality(args[0]);
		String destinationPrefix = args[1];
		String pais_code = args[2];
		String validFrom = args[3];
		String price = args[4];
		String priceAd = args[5];
		String zoneName = pais_code + "_" + destinationPrefix;

        try {
			stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;
			if (zoneDestins.containsKey(destinationPrefix)) {
				Logger.screen(Logger.Error, "La Marcacion destino "+ destinationPrefix +" que intenta configurar ya existe para el servicio TEL, proceso cancelado");
	            System.exit(Parameters.ERR_INVALID_VALUE);
			}
			destinsIfw = stmt.executeQuery("select NAME from IFW_STANDARD_ZONE where servicecode = 'TEL' AND ZONE_RT LIKE '%MI%' and destin_areacode = '00"+destinationPrefix+"'");
			destinsIfw.next();
			String description = destinsIfw.getString(1);
			XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, zoneName, validFrom, "inf", destinationPrefix, null);
		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
