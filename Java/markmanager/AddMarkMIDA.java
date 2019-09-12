package markmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import brmHandlers.PinLoadGlid;
import brmHandlers.XmlUtils;
<<<<<<< HEAD
import brmHandlers.XmlUtilsByModality;

import config.Parameters;
import cust.ZoneItem;
import database.DBManager;
import Modality;
=======
import config.Parameters;
import cust.Modality;
import cust.Modality.ResultName;
import cust.DateFormater;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Utils;
import log.Logger;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.ZoneItem;
import database.DBManager;
import java.util.ArrayList;
import java.util.Collections;
>>>>>>> d4eacd302929396ebe10b5b6af952add01f82189

public class AddMarkMIDA {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddMarkMIDA");
		Logger.log(Logger.Debug, "Creacion de destino internacional");
		if (args.length < 6) {
            Logger.screen(Logger.Error, "Parametros insuficientes");
            System.exit(Parameters.ERR_INVALID_VALUE);
		}
		Modality modality = Modality.getModality(args[0]);
		HashMap<String, List<ZoneItem>>  zoneDestins = XmlUtilsByModality.getAllZoneItems(modality, ServiceType.TEL, true);
		Statement stmt;
<<<<<<< HEAD
		
		Modality modality = Modality.getModality(args[0]);
		String destinationPrefix = args[1];
		String pais_code = args[2];
		String validFrom = args[3];
		String price = args[4];
		String priceAd = args[5];
		String zoneName = pais_code + "_" + destinationPrefix;

=======
		String destinationPrefix = args[1];
		String codigo_pais = args[2];
		String validFrom = args[3];
		String precio = args[4];
		String precioAd = args[5];
>>>>>>> d4eacd302929396ebe10b5b6af952add01f82189
        try {
			stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;
			if (zoneDestins.containsKey(destinationPrefix)) {
<<<<<<< HEAD
				Logger.screen(Logger.Error, "La Marcacion destino "+ destinationPrefix +" que intenta configurar ya existe para el servicio TEL, proceso cancelado");
	            System.exit(Parameters.ERR_INVALID_VALUE);
			}
			destinsIfw = stmt.executeQuery("select NAME from IFW_STANDARD_ZONE where servicecode = 'TEL' AND ZONE_RT LIKE '%MI%' and destin_areacode = '00"+destinationPrefix+"'");
			destinsIfw.next();
			String description = destinsIfw.getString(1);
			XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, zoneName, validFrom, "inf", destinationPrefix, null);
=======
				Logger.screen(Logger.Error, "La Marcacion destino "+destinationPrefix+" que intenta configurar ya existe para el servicio TEL");
			}else{
			destinsIfw = stmt.executeQuery("select NAME from IFW_STANDARD_ZONE where servicecode = 'TEL' AND ZONE_RT LIKE '%MI%' and destin_areacode = '00"+destinationPrefix+"'");
			destinsIfw.next();
			String description = destinsIfw.getString(1);
			XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, codigo_pais+"_"+destinationPrefix, validFrom, "inf", "00"+destinationPrefix, null);}
>>>>>>> d4eacd302929396ebe10b5b6af952add01f82189
		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
