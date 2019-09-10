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
		Modality modality = null;
		if(args != null && args.length > 0) {
			modality = Modality.getModality(args[0]);
	        if (modality == null) {
	        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[0] + "] no ha sido implemantada");
	            System.exit(Parameters.ERR_INVALID_VALUE);
	        }
        }else
        	modality = Modality.POSPAID;
		HashMap<String, String> glidDescs = PinLoadGlid.getGlidDescriptions();

		Logger.onlyScreen("Codigo|ImpactCategory|Tipo|Paso|Tarifa|Moneda|ValidoDesde");
		ServiceType serviceType= ServiceType.TEL; 
		HashMap<String, List<ZoneItem>> zones = XmlUtilsByModality.getAllZoneItems(modality, serviceType,true);
		HashMap<String, PriceTier> priceTiers = XmlUtilsByModality.getPriceTiers(modality, serviceType, null,true); 
		HashMap<String, String> history = XmlUtilsByModality.getHistory(modality, serviceType);
		List<String> zoneNames =  new ArrayList<String>(zones.keySet());
		Collections.sort(zoneNames);
		for(String zoneName : zoneNames) {
			String name = zoneName;
			for(ResultName rn : ResultName.values()) {
				if(!rn.applyServiceType(serviceType))
					continue;
				String prefix = rn.name().replace("XXXX", "");;
				if(rn.group.length() > 0) {
					name = name.replace(rn.group, "");
					prefix = prefix.replace(rn.group, "");
				}
				name = name.replace(prefix, "");
			}
			List<ZoneItem> zoneItems = zones.get(zoneName);
			Collections.sort(zoneItems);
			for (ZoneItem zoneItem : zoneItems) {
				PriceTier priceTier = priceTiers.get(zoneItem.getName()); 
				if (priceTier != null) {
					List<PriceTierRange> priceTierRanges= getMoreRecentPriceByDate(zoneItem.getName(), zoneItem.getValidTo(), priceTier);
					PriceTierRange priceTierRange = priceTierRanges.get(0); 
					String taxCode = priceTierRange.getTaxCode();
					if(taxCode == null)
						taxCode = "<NO DEFINIDO>";
					
					Logger.onlyScreen(Utils.fixedLenthString(zoneName, 40)+"|"+
							Utils.fixedLenthString(zoneItem.getProductName(), 18)+"|"+
							// TODO Validar si aplica
							// Utils.fixedLenthString(mapItemDescs.getOrDefault(zoneName, "<<ERROR AL BUSCAR DESCRIPCION>>"), 80)+"|"+
							Utils.fixedLenthString("<ERROR AL BUSCAR DESCRIPCION>", 80) + "|" +
							Utils.fixedLenthString(zoneItem.getDestinationPrefix(), 15)+"|" +
							Utils.fixedLenthString(zoneItem.getValidFrom(), 12)+"|" +
							Utils.fixedLenthString(zoneItem.getValidTo(), 12)+"|" +
							Utils.fixedLenthString(history.getOrDefault(zoneName, "<ERROR ULT. MODICACION>"), 23)+"|" +
							Utils.fixedLenthString(Double.toString(priceTierRange.getPrice()), 10)+"|" +
							Utils.fixedLenthString(taxCode, 9)+"|" +
							Utils.fixedLenthString(priceTierRange.getGlid(), 10)+"|" +
							Utils.fixedLenthString(glidDescs.getOrDefault(priceTierRange.getGlid(), "<NO encontrado>"), 30));
				}
			}
		}
		
	}
	
	private static List<PriceTierRange> getMoreRecentPriceByDate(String campaign, String date, PriceTier pt) {
		for (String d : pt.getPriceTierRanges().keySet()) {
			if (date.compareTo(d) >= 0)
				return pt.getPriceTierRanges().get(d);
		}
		return pt.getPriceTierRanges().get("0");
	}
	
	private static List<PriceTierRange> getMoreRecentPrice(String campaign, String date, PriceTier pt) {
		String moreRecent = "";
		for (String d : pt.getPriceTierRanges().keySet()) {
			if ((moreRecent.equals("") || moreRecent.compareTo(d) < 0) && (d.compareTo(date) <= 0)) {
				moreRecent = d;
			}
		}
		return pt.getPriceTierRanges().get(moreRecent);
	}
		
}
