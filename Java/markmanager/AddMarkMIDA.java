package markmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import brmHandlers.PinLoadGlid;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.ZoneItem;
import database.DBManager;
import cust.Modality;
import cust.Modality.ResultName;
import cust.DateFormater;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Utils;
import cust.DateFormater;
import log.Logger;



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
		List<String> keys= new ArrayList<String>(zoneDestins.keySet());
		Statement stmt;
		String destinationPrefix = args[1];
		String pais_code = args[2];
		String validFrom = args[3];
		String price = args[4];
		String priceAd = args[5];
		String zoneName = pais_code + "_" + destinationPrefix;
		String rateType="DUR";
		Date today = new Date();

        try {

			stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;

			//validar validFrom >= a dia actual
			Date validFrom_DATE = DateFormater.stringToDate(validFrom.replace("T", ""), new SimpleDateFormat("yyyyMMddHHmmss"));

			if(validFrom_DATE.compareTo(today) <= 0){
				Logger.screen(Logger.Error, "La fecha de inicio especificada debe ser posterior al dia actual (" + today.toString() + ")");
			}else{
			//crear ZM y IC
				if (zoneDestins.containsKey("PRE_IC_MIDA_"+zoneName)) {
				Logger.screen(Logger.Error, "La Marcacion destino "+destinationPrefix+" que intenta configurar ya existe para el servicio TEL");
				}else{
				XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, zoneName, validFrom, "inf", "00"+destinationPrefix, null);
			    }

				List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
	    		if (rateType.equals("DUR")) {
		    	listOfPriceTierRange.add(new PriceTierRange("60", "840", "", 100.0, "MINUTES", 60.0, "16002001"));
		    	listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "840", "", 10.0, "MINUTES", 1.0, "16002001"));
	    		}
	    	
	    		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
	    		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
				HashMap<String, PriceTier> priceTiers= XmlUtilsByModality.getPriceTiers(modality, ServiceType.TEL, zoneName, true);
            if(priceTiers.size()==0)
                XmlUtilsByModality.addPriceTier(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+zoneName, mapPriceTierRange, rateType),null,true);
            else
            	XmlUtilsByModality.addPriceTierRange(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+zoneName, mapPriceTierRange, rateType),null,true);
		    }

		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
