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
		if(args != null && args.length > 0) {
			modalities.add(Modality.getModality(args[0]));
	        if (modalities.get(0) == null) {
	        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[0] + "] no ha sido implemantada");
	            System.exit(Parameters.ERR_INVALID_VALUE);
	        }
        }else{
        	modalities.add(Modality.PREPAID);
        	modalities.add(Modality.HYBRID);
        	modalities.add(Modality.CCM);
        	modalities.add(Modality.CCF);
        }
        ServiceType serviceType= ServiceType.TEL;	
		Logger.onlyScreen("Codigo          |ImpactCategory                    |Tipo|Paso|Tarifa     |Moneda|ValidoDesde");

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
						for(String date: dates){
							List<PriceTierRange> listPtr = priceTierRanges.get(date);
							int step=1;
							for (int index=listPtr.size()-1; index>=0; index--) { 
							Logger.onlyScreen(
									Utils.fixedLenthString(zoneItem.getDestinationPrefix(), 15)+"|" +
									Utils.fixedLenthString(zoneName, 40)+"|"+
									Utils.fixedLenthString(modality+"", 3)+"|"+step+"|"+
									Utils.fixedLenthString(Double.toString(listPtr.get(index).getPrice()), 10)+"|USD|"+DateFormater.shortDateFormat(date));	
								step++;
							}
						}	
					}
				}
			}
		}
	}
		
}
