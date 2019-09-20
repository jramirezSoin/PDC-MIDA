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
import brmHandlers.ItemTypeDeploy;
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
		String validFrom = args[3];
		String price = args[4];
		String priceAd = args[5];
		String rateType="DUR";
		Date today = new Date();
		Double dPrice=0.0;
		Double dPriceAd=0.0;
		Date validFrom_DATE= new Date();
		String zoneName= "PRE_IC_MIDA_"+((modality.equals(Modality.CCF))?"FIJO_CC_":((modality.equals(Modality.CCM))?"MOVIL_CC_":""))+destinationPrefix;
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dfi = new SimpleDateFormat("yyyyMMdd");
        try {

        	if(!destinationPrefix.matches("[A-Z]{3}[_][0-9]{1,}")){
				Logger.screen(Logger.Error,"El codigo de destino debe ser XXX_# donde XXX es el nombre del pais en 3 digitos en mayuscula y # el codigo de pais");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}

			try{
				int aux= Integer.parseInt(pais_code);
			}
			catch(Exception e){
				Logger.screen(Logger.Error,"El codigo de pais debe ser un valor numerico");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}

			if(!pais_code.matches(destinationPrefix.split("_")[1]+"[0-9]{0,}")){
				Logger.screen(Logger.Error,"El codigo ["+pais_code+"] debe inciar con el codigo de pais dado en ["+destinationPrefix+"]");
				System.exit(Parameters.ERR_INVALID_VALUE);	
			}

        	try{
				validFrom_DATE = DateFormater.stringToDate(validFrom, dfi);
				if(!((dfi.format(validFrom_DATE)).equals(validFrom)))
					throw new Exception();
			}
			catch(Exception e){
				Logger.screen(Logger.Error,"La fecha ["+validFrom+"] ingresada es invalida");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}
			try{
				dPrice = Double.parseDouble(price);
				dPriceAd = Double.parseDouble(priceAd);
			}
			catch(Exception e){
				Logger.screen(Logger.Error,"Los precios ingresados deben ser valores numericos");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}

			

			//validar validFrom >= a dia actual
			if(validFrom_DATE.compareTo(today) <= 0){
				Logger.screen(Logger.Error, "La fecha de inicio especificada debe ser posterior al dia actual (" + df.format(today) + ")");
				System.exit(Parameters.ERR_INTEGRATE);
			}else{
			//crear ZM y IC
				if (zoneDestins.containsKey(zoneName)) {
					List<ZoneItem> zItems = zoneDestins.get(zoneName);
					boolean founded=false;
					for(ZoneItem item : zItems){ if(item.getDestinationPrefix().equals("00"+pais_code)){founded=true;}}
					if(founded)	
						Logger.onlyScreen("La Marcacion destino ["+pais_code+"] del codigo de destino ["+destinationPrefix+"] ya se encuentra configurado como zoneItem");
					else{

						XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, destinationPrefix, "0", "inf", "00"+pais_code, null,true);
						//String itemDesc = XmlUtils.getItemTelephonyDescription(zoneName);
		            	//if (itemDesc == null) {
		                	//Logger.screen(Logger.Debug, "Creando nuevo Item");
		            		//ItemTypeDeploy.AddItemType("E_N"+zoneName);
		            		//XmlUtils.addItemRuleSpecTelephony(zoneName, zoneName);
		            	//}
					}
				}else{
				XmlUtilsByModality.addZoneItem(modality, ServiceType.TEL, destinationPrefix, "0", "inf", "00"+pais_code, null,true);
						String itemDesc = XmlUtils.getItemTelephonyDescription(zoneName);
		            	//if (itemDesc == null) {
		                	//Logger.screen(Logger.Debug, "Creando nuevo Item");
		            		//ItemTypeDeploy.AddItemType("E_N"+zoneName);
		            		//XmlUtils.addItemRuleSpecTelephony(zoneName, zoneName);
		            	//}
			    }
			    String glid = "16002003" + (modality.equals(Modality.HYBRID) ? "3":"1" );
				List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();

		    	listOfPriceTierRange.add(new PriceTierRange("60", "840", null, dPrice, "MINUTES", 60.0, glid));
		    	listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "840", null, dPriceAd, "MINUTES", 1.0, glid));
	    		
	    	
	    		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
	    		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
				HashMap<String, PriceTier> priceTiers= XmlUtilsByModality.getPriceTiers(modality, ServiceType.TEL, destinationPrefix, true);
				Boolean updated=false;
	            if(priceTiers.size()==0){
					List<PriceTierRange> listOfPriceTierRange2 = new ArrayList<PriceTierRange>();	            	
	            	mapPriceTierRange.put("0", listOfPriceTierRange2);
	            	listOfPriceTierRange2.add(new PriceTierRange("60", "840", null, 0.0, "MINUTES", 60.0, glid));
	            	listOfPriceTierRange2.add(new PriceTierRange("NO_MAX", "840", null, 0.0, "MINUTES", 1.0, glid));
	                updated= XmlUtilsByModality.addPriceTier(modality, ServiceType.TEL, new PriceTier(zoneName, mapPriceTierRange, rateType),null,true);
	            }
	            else
	            	updated= XmlUtilsByModality.addPriceTierRange(modality, ServiceType.TEL, new PriceTier(zoneName, mapPriceTierRange, rateType),null,true, false);
			    if(updated) Logger.screen(Logger.Debug,"Tarifa ingresada satisfactoriamente");
			    else Logger.screen(Logger.Error,"Error al ingresar la tarifa");
			    }
			    
		} catch (Exception e) {
            Logger.screen(Logger.Error, e.toString());
            e.printStackTrace();
            Logger.screen(Logger.Error, "Error en conexi\u00F3, Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
		}
	}
}
