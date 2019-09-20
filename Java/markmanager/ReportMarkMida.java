package markmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import log.Logger;
import brmHandlers.PinLoadGlid;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.Modality;
import cust.Modality.ResultName;
import cust.DateFormater;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Utils;
import cust.ZoneItem;

public class ReportMarkMida {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ReportMarkMida");
		Logger.log(Logger.Debug, "Consulta de marcacion MIDA");
		ArrayList<Modality> modalities = new ArrayList<>();
		Boolean isDestination=false;
		String destinationPrefix="00";
		if(args != null && args.length > 0) {
			modalities.add(Modality.getModality(args[0]));
	        if (modalities.get(0) == null && !(Utils.isNumeric(args[0]))) {
	        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[0] + "] no ha sido implemantada, ingrese una modalidad correcta o un numero de destino internacional");
	            System.exit(Parameters.ERR_INVALID_VALUE);
	        }else if(Utils.isNumeric(args[0])){
	        	modalities.clear();
	        	modalities.add(Modality.PREPAID);
	        	modalities.add(Modality.HYBRID);
	        	modalities.add(Modality.CCM);
	        	modalities.add(Modality.CCF);
	        	isDestination=true;
	        	destinationPrefix+=args[0];	
	        }
        }else{
        	modalities.add(Modality.PREPAID);
        	modalities.add(Modality.HYBRID);
        	modalities.add(Modality.CCM);
        	modalities.add(Modality.CCF);
        }
        ServiceType serviceType= ServiceType.TEL;	
		Logger.onlyScreen("Codigo |Impact Category                |Tipo|Paso|Tarifa  |Moneda |ValidoDesde");
		Logger.onlyScreen("");
		Boolean ingresa=false;
		for(Modality modality: modalities){
			HashMap<String, List<ZoneItem>> zones = XmlUtilsByModality.getAllZoneItems(modality, serviceType, true);
			HashMap<String, PriceTier> priceTiers = XmlUtilsByModality.getPriceTiers(modality, serviceType, null, true); 
			HashMap<String, String> history = XmlUtilsByModality.getHistory(modality, serviceType);
			List<String> zoneNames =  new ArrayList<String>(zones.keySet());
			Collections.sort(zoneNames);
			for(String zoneName : zoneNames) {
				String name = zoneName;
				List<ZoneItem> zoneItems = zones.get(zoneName);
				Collections.sort(zoneItems);
				for (ZoneItem zoneItem : zoneItems) {
					PriceTier priceTier = priceTiers.get(zoneItem.getName()); 
					if (priceTier != null) {
						HashMap<String, List<PriceTierRange>> priceTierRanges= priceTier.getPriceTierRanges();
						List<String> dates = new ArrayList<String>(priceTierRanges.keySet());
						Collections.sort(dates, Collections.reverseOrder());
						for(String date: dates){
							List<PriceTierRange> listPtr = priceTierRanges.get(date);
							int step=1;
							for (int index=listPtr.size()-1; index>=0; index--) {
								if(!isDestination || (isDestination && zoneItem.getDestinationPrefix().equals(destinationPrefix))){
									ingresa=true;
									Logger.onlyScreen(
											Utils.fixedLenthString(zoneItem.getDestinationPrefix(), 7)+"|" +
											Utils.fixedLenthString(zoneName, 31)+"|"+
											Utils.fixedLenthString(modality+"", 3)+" |"+Utils.fixedLenthString(step+"",4)+"|"+
											Utils.fixedLenthString(Double.toString(listPtr.get(index).getPrice()), 8)+"|USD    |"+((date.equals("0"))?"0":DateFormater.shortDateFormat(date)));	
								}
								step++;
							}
						}
						if(!isDestination)
							Logger.onlyScreen("");	
					}
				}
			}
		}
		if(!ingresa)
			Logger.onlyScreen("No existen tarifas para "+((isDestination)?"ese destino internacional":"esa modalidad"));
	}
		
}
