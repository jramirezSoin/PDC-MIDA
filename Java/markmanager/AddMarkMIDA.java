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



public class AddMarkMIDA {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddMarkMIDA");
		Logger.log(Logger.Debug, "Creacion de destino internacional");
		if (args.length < 6) {
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
		String destinationPrefix = args[1]; //Codigo_destino USA_1
		String pais_code = args[2]; //Codigo pais 1707
		String validFrom = args[3]+"T000000";
		String price = args[4];
		String priceAd = args[5];
		String rateType="DUR";
		Date today = new Date();

        try {

			stmt = DBManager.getConnectionIfw().createStatement();
			ResultSet destinsIfw;

			//validar validFrom >= a dia actual
			Date validFrom_DATE = DateFormater.stringToDate(validFrom.replace("T", ""), new SimpleDateFormat("yyyyMMddHHmmss"));
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

			if(validFrom_DATE.compareTo(today) <= 0){
				Logger.screen(Logger.Error, "La fecha de inicio especificada debe ser posterior al dia actual (" + df.format(today) + ")");
				System.exit(Parameters.ERR_INTEGRATE);
			}else{
			//crear ZM y IC
				if (zoneDestins.containsKey("PRE_IC_MIDA_"+destinationPrefix)) {
					List<ZoneItem> zItems = zoneDestins.get("PRE_IC_MIDA_"+destinationPrefix);
					boolean founded=false;
					for(ZoneItem item : zItems){ if(item.getDestinationPrefix().equals("00"+pais_code)){founded=true;}}
					if(founded)	
						Logger.onlyScreen("La Marcacion destino ["+pais_code+"] del codigo de destino ["+destinationPrefix+"] ya se encuentra configurado como zoneItem");
					else
						XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, destinationPrefix, validFrom, "inf", "00"+pais_code, null,true);
				}else{
				XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, destinationPrefix, validFrom, "inf", "00"+pais_code, null,true);
			    }

			    String glid = "1600200" + (modality.equals(Modality.HYBRID) ? "3":"1" );
				List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();

		    	listOfPriceTierRange.add(new PriceTierRange("60", "840", null, Double.parseDouble(price), "MINUTES", 60.0, glid));
		    	listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "840", null, Double.parseDouble(priceAd), "MINUTES", 1.0, glid));
	    		
	    	
	    		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
	    		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
				HashMap<String, PriceTier> priceTiers= XmlUtilsByModality.getPriceTiers(modality, ServiceType.TEL, destinationPrefix, true);
            if(priceTiers.size()==0)
                XmlUtilsByModality.addPriceTier(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+destinationPrefix, mapPriceTierRange, rateType),null,true);
            else
            	XmlUtilsByModality.addPriceTierRange(modality, ServiceType.TEL, new PriceTier("PRE_IC_MIDA_"+destinationPrefix, mapPriceTierRange, rateType),null,true, false);
		    }
		} catch (SQLException e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
