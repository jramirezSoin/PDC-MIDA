package markmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.DateFormat;
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



public class ModifyMarkMIDA {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ModifyMarkMIDA");
		Logger.log(Logger.Debug, "Modificacion de destino internacional");
		if (args.length < 5) {
            Logger.screen(Logger.Error, "Parametros insuficientes");
            System.exit(Parameters.ERR_INVALID_VALUE);
		}

		if(Modality.getModality(args[0]) == null){
		Logger.screen(Logger.Error, "La modalidad " + args[0] + " es invalida.");
  		System.exit(Parameters.ERR_INVALID_VALUE);
		}

		Modality modality = Modality.getModality(args[0]);
		HashMap<String, List<ZoneItem>>  zoneDestins = XmlUtilsByModality.getAllZoneItems(modality, ServiceType.TEL, true);
		List<String> keys= new ArrayList<String>(zoneDestins.keySet());
		Statement stmt;
		String destinationPrefix = args[1]; //number
		//String pais_code = args[2];
		String validFrom = args[2];
		String price = args[3];
		String priceAd = args[4];
		String zoneName = "PRE_IC_MIDA_" + destinationPrefix;
		String rateType="DUR";
		Date today = new Date();

        try {

        	stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;


			//Obtener zoneName en base al destinationPrefix
			boolean founded = false;
			for(String key:keys){
				String keyWithoutCode = key.substring(0, 12) + key.substring(16);
				if(keyWithoutCode.equals(zoneName)){
					zoneName = key;
					founded = true;
					break;
				 }
				}

				if(!founded){
				Logger.screen(Logger.Error, "La Marcacion destino "+destinationPrefix+" que intenta configurar no existe para el servicio TEL");
				System.exit(Parameters.ERR_INVALID_VALUE);
				}

			//validar validFrom >= a dia actual
			Date validFrom_DATE = DateFormater.stringToDate(validFrom.replace("T", ""), new SimpleDateFormat("yyyyMMddHHmmss"));
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

			if(validFrom_DATE.compareTo(today) <= 0){
				Logger.screen(Logger.Error, "La fecha de inicio especificada debe ser posterior al dia actual (" + df.format(today) + ")");
			}else{
			//crear ZM y IC
				Logger.onlyScreen("Inicio de modificacion");
				XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, zoneName, validFrom, "inf", "00"+destinationPrefix, null,true);
			    String glid = "1600200" + (modality.equals(Modality.HYBRID) ? "3":"1" );
				List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();

	    		if (rateType.equals("DUR")) {
		    	listOfPriceTierRange.add(new PriceTierRange("60", "840", "", Double.parseDouble(price), "MINUTES", 60.0, glid));
		    	listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "840", "", Double.parseDouble(priceAd), "MINUTES", 1.0, glid));
	    		}
	    	
	    		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
	    		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
				HashMap<String, PriceTier> priceTiers= XmlUtilsByModality.getPriceTiers(modality, ServiceType.TEL, zoneName, true);
            if(priceTiers.size()==0)
            	Logger.screenOnly("No existe priceTiers para la marcacion actual");
            	//XmlUtilsByModality.addPriceTier(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+zoneName, mapPriceTierRange, rateType),null,true);
            else
            	XmlUtilsByModality.addPriceTierRange(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+zoneName, mapPriceTierRange, rateType),null,true, true);
		    }
		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
