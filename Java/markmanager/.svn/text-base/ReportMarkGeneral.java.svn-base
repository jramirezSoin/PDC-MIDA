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

public class ReportMarkGeneral {

	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ReportMarkGeneral");
		Logger.log(Logger.Debug, "Consulta de campa\u00F1as general");
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
		if(!modality.equals(Modality.POSPAID)) {
			Logger.onlyScreen("ITEM                                    |PRODUCT NAME      |NOMBRE_FACTURA                                                                  |NUMERO_REAL    |FECHA_DESDE |FECHA_HASTA |FECHA_MODIF            |    PRECIO|IMPUESTOS|GLID      |DESCRIPCION_GLID");
			for(ServiceType serviceType : ServiceType.values()) {
				HashMap<String, List<ZoneItem>> zones = XmlUtilsByModality.getAllZoneItems(modality, serviceType);
				HashMap<String, PriceTier> priceTiers = XmlUtilsByModality.getPriceTiers(modality, serviceType, null); 
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
		}else {
			HashMap<String, List<ZoneItem>> zones = XmlUtils.getAllZoneItems();
			List<String> zoneNames =  new ArrayList<String>(zones.keySet());
			Collections.sort(zoneNames);
			Logger.onlyScreen("ITEM      |PRODUCT NAME      |NOMBRE_FACTURA                                                                  |NUMERO_REAL    |FECHA_DESDE |FECHA_HASTA |FECHA_MODIF           |    PRECIO|IMPUESTOS|GLID      |DESCRIPCION_GLID");
			HashMap<String, String> mapItemDescs = XmlUtils.getAllItemDescriptions();
			HashMap<String, PriceTier> priceTiersSms = XmlUtils.getSmsPriceTiers();
			HashMap<String, PriceTier> priceTiersTelephony = XmlUtils.getTelephonyPriceTiers();
			HashMap<String, String> history = XmlUtils.getHistory();
			for (String zoneName : zoneNames) {
				List<ZoneItem> zoneItems = zones.get(zoneName);
				Collections.sort(zoneItems);
				for (ZoneItem zoneItem : zoneItems) {
					String idCampaign = zoneName.substring(3);
					PriceTier pt;
					if (zoneName.startsWith("E_N")) {
						pt = priceTiersTelephony.get(zoneItem.getName());
					}
					else {
						pt = priceTiersSms.get(zoneItem.getName());
					}
					if (pt != null) {//Asegura consistencia de la campaña
						List<PriceTierRange> priceList = getMoreRecentPrice(zoneItem.getName(), zoneItem.getValidTo(), pt); 
						if (priceList != null) {//Asegura consistencia de la campaña
							if (idCampaign.endsWith("MT")) {
								idCampaign = idCampaign.substring(0, idCampaign.length()-2);
							}
							String glidDesc = glidDescs.get(priceList.get(0).getGlid());
							if (glidDesc == null) {
								glidDesc = "<NO encontrado>";
							}
							Logger.onlyScreen(Utils.fixedLenthString(zoneName, 10)+"|"+
									Utils.fixedLenthString(zoneItem.getProductName(), 18)+"|"+
									Utils.fixedLenthString(mapItemDescs.getOrDefault(idCampaign, "<<ERROR AL BUSCAR DESCRIPCION>>"), 80)+"|"+
									Utils.fixedLenthString(zoneItem.getDestinationPrefix(), 15)+"|"+
									Utils.fixedLenthString(zoneItem.getValidFrom(), 12)+"|"+
									Utils.fixedLenthString(zoneItem.getValidTo(), 12)+"|"+
									Utils.fixedLenthString(history.get(zoneName), 22)+"|"+
									Utils.fixedLenthString(Double.toString(priceList.get(0).getPrice()), 10)+"|"+
									Utils.fixedLenthString(priceList.get(0).getTaxCode(), 9)+"|"+
									Utils.fixedLenthString(priceList.get(0).getGlid(), 10)+"|"+
									Utils.fixedLenthString(glidDesc, 30));
						}
					}
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
