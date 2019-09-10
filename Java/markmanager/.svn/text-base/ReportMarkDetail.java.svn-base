package markmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import brmHandlers.PinLoadGlid;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import log.Logger;
import config.Parameters;
import cust.DetailPrint;
import cust.Modality;
import cust.Modality.ResultName;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Utils;
import cust.ZoneItem;

public class ReportMarkDetail {

    /**
     * @param args ID Campa\u00F1a
     */
	public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ConsMarkDetail");
        if (args.length > 0) {
		    Logger.log(Logger.Debug, "Consulta de campa\u00F1a espec\u00EDfica");
        	Integer i = 0;
        	Modality modality = null;
    		if(args != null && args.length > 1) {
    			modality = Modality.getModality(args[i]);
    	        if (modality == null) {
    	        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[0] + "] no ha sido implemantada");
    	            System.exit(Parameters.ERR_INVALID_VALUE);
    	        }
    	        i++;
            }else
            	modality = Modality.POSPAID;
    		String campaign = args[i];
    		if(!modality.equals(Modality.POSPAID))
    			detail(modality, (campaign.length() > 6 ? ServiceType.TEL : ServiceType.SMS), campaign);
    		else {
	    		HashMap<String, List<ZoneItem>> zones = XmlUtils.getAllZoneItems();
	        	if (campaign.length() > 6) {
	        		Logger.log(Logger.Debug, "Buscando la campa\u00F1a como tipo 900...");
	        		String zone = "E_N" + campaign;
	        		List<ZoneItem> zoneCampaign = null;
	        		if (zones.containsKey(zone)) {
	        			zoneCampaign = zones.get(zone);
	        			Collections.sort(zoneCampaign, Collections.reverseOrder());
	        			Logger.onlyScreen("\nDescripci\u00F3n de la campa\u00F1a:\n"+XmlUtils.getItemTelephonyDescription(campaign)+"\n");
	        			Logger.onlyScreen("\nConfiguraci\u00F3n de la marcaci\u00F3n:");
	    				Logger.onlyScreen("Impact Category |Valid From |Valid To   |Product Name      |N\u00FAmero Real");
	    				//TODO mejorar presentacion de fechas a YYYY-MM-DD agregando metodo en DateFormater
	    				//TODO ordenar el listado de zonning y precios implementando la interfaz Comparable en ZoneItems y PriceTierRange
	        			for (ZoneItem zoneItem : zoneCampaign) {
	        				Logger.onlyScreen(Utils.fixedLenthString(zoneItem.getName(), 16)+"|"+Utils.fixedLenthString(zoneItem.getValidFrom(), 11)+
	        						"|"+Utils.fixedLenthString(zoneItem.getValidTo(), 11)+"|"+Utils.fixedLenthString(zoneItem.getProductName(), 18)+
	        						"|"+zoneItem.getDestinationPrefix());
						}
	        			PriceTier ptD = XmlUtils.getTelephonyPriceTier("E_ND"+campaign);
	        			PriceTier ptE = XmlUtils.getTelephonyPriceTier("E_NE"+campaign);
	        			Logger.onlyScreen("\nConfiguraci\u00F3n de precios:");
	        			Logger.onlyScreen("Price Model |Valid From |RUM Name |Price  |Impuestos|GLID          |Descr. GLID");
	        			HashMap<String, List<PriceTierRange>> rangesD = null, rangesE = null;
	        			List<String> listDates = new ArrayList<String>();
	        			if (ptD != null) {
	        				rangesD = ptD.getPriceTierRanges();
		        			for (String validFrom : rangesD.keySet()) {
		        				listDates.add(validFrom);
							}
	        			}
	        			if (ptE != null) {
	        				rangesE = ptE.getPriceTierRanges();
		        			for (String validFrom : rangesE.keySet()) {
		        				if (!listDates.contains(validFrom)) {
		        					listDates.add(validFrom);
		        				}
							}
	        			}
	        			Collections.sort(listDates, Collections.reverseOrder());
	        			for (String validFrom: listDates) {
							PriceTierRange range;
							if (rangesD != null && rangesD.get(validFrom) != null) {
								range = rangesD.get(validFrom).get(0);
								Logger.onlyScreen(Utils.fixedLenthString(ptD.getName(), 12)+"|"+Utils.fixedLenthString(validFrom, 11)+"|"+
										Utils.fixedLenthString(ptD.getRum(), 9)+"|"+Utils.fixedLenthString(Double.toString(range.getPrice()), 7)+"|"+Utils.fixedLenthString(range.getTaxCode(), 9)+"|"+
										Utils.fixedLenthString(range.getGlid(), 14)+"|"+Utils.fixedLenthString(PinLoadGlid.getGlidDescription(range.getGlid()), 30));
							}
							if (rangesE != null && rangesE.get(validFrom) != null) {
								range = rangesE.get(validFrom).get(0);
								Logger.onlyScreen(Utils.fixedLenthString(ptE.getName(), 12)+"|"+Utils.fixedLenthString(validFrom, 11)+"|"+
										Utils.fixedLenthString(ptE.getRum(), 9)+"|"+Utils.fixedLenthString(Double.toString(range.getPrice()), 7)+"|"+Utils.fixedLenthString(range.getTaxCode(), 9)+"|"+
										Utils.fixedLenthString(range.getGlid(), 14)+"|"+Utils.fixedLenthString(PinLoadGlid.getGlidDescription(range.getGlid()), 30));
							}
						}
	        		}
	        		else {
	        			Logger.screen(Logger.Debug, "No se encontr\u00F3 la campa\u00F1a indicada.");
	        		}
	        	}
	        	else {
	        		Logger.log(Logger.Debug, "Buscando la campa\u00F1a como tipo SMS...");
	        		String zone = "E_S" + campaign;
	        		List<ZoneItem> zoneCampaign = null;
	        		if (zones.containsKey(zone)) {
	        			zoneCampaign = zones.get(zone);
	        			zoneCampaign.addAll(zones.get(zone+"MT"));
	        			Collections.sort(zoneCampaign, Collections.reverseOrder());
	        			Logger.onlyScreen("\nDescripci\u00F3n de la campa\u00F1a:\n"+XmlUtils.getItemSmsDescription(campaign)+"\n");
	        			Logger.onlyScreen("\nConfiguraci\u00F3n de la marcaci\u00F3n:");
	    				Logger.onlyScreen("Impact Category |Valid From |Valid To   |Product Name      |N\u00FAmero Real");
	    				//TODO mejorar presentacion de fechas a YYYY-MM-DD agregando metodo en DateFormater
	    				//TODO ordenar el listado de zonning y precios implementando la interfaz Comparable en ZoneItems y PriceTierRange
	        			for (ZoneItem zoneItem : zoneCampaign) {
	        				Logger.onlyScreen(Utils.fixedLenthString(zone, 16)+"|"+Utils.fixedLenthString(zoneItem.getValidFrom(), 11)+
	        						"|"+Utils.fixedLenthString(zoneItem.getValidTo(), 11)+"|"+Utils.fixedLenthString(zoneItem.getProductName(), 18)+
	        						"|"+zoneItem.getDestinationPrefix());
						}
	        			PriceTier pt = XmlUtils.getSmsPriceTier("E_S"+campaign);
	        			PriceTier ptmt = XmlUtils.getSmsPriceTier("E_S"+campaign+"MT");
	        			Logger.onlyScreen("\nConfiguraci\u00F3n de precios:");
	        			Logger.onlyScreen("Price Model |Valid From |RUM Name |Price  |Impuestos|GLID          |Descr. GLID");
	        			HashMap<String, List<PriceTierRange>> ranges = pt.getPriceTierRanges();
	        			HashMap<String, List<PriceTierRange>> rangesmt = ptmt.getPriceTierRanges();
	        			List<String> listDates = new ArrayList<String>();
	        			for (String validFrom : ranges.keySet()) {
	        				listDates.add(validFrom);
	        			}
	        			Collections.sort(listDates, Collections.reverseOrder());
	        			for (String validFrom : listDates) {
							PriceTierRange range = ranges.get(validFrom).get(0);
							Logger.onlyScreen(Utils.fixedLenthString(pt.getName(), 12)+"|"+Utils.fixedLenthString(validFrom, 11)+"|"+
									Utils.fixedLenthString(pt.getRum(), 9)+"|"+Utils.fixedLenthString(Double.toString(range.getPrice()), 7)+"|"+Utils.fixedLenthString(range.getTaxCode(), 9)+"|"+
									Utils.fixedLenthString(range.getGlid(), 14)+"|"+Utils.fixedLenthString(PinLoadGlid.getGlidDescription(range.getGlid()), 30));
							range = rangesmt.get(validFrom).get(0);
							Logger.onlyScreen(Utils.fixedLenthString(ptmt.getName(), 12)+"|"+Utils.fixedLenthString(validFrom, 11)+"|"+
									Utils.fixedLenthString(ptmt.getRum(), 9)+"|"+Utils.fixedLenthString(Double.toString(range.getPrice()), 7)+"|"+Utils.fixedLenthString(range.getTaxCode(), 9)+"|"+
									Utils.fixedLenthString(range.getGlid(), 14)+"|"+Utils.fixedLenthString(PinLoadGlid.getGlidDescription(range.getGlid()), 30));
						}
	        		}
	        		else {
	        			Logger.screen(Logger.Debug, "No se encontr\u00F3 la campa\u00F1a indicada.");
	        		}        		
	        	}    	
	        }
        }
	}
	
	private static List<ZoneItem> getZoneItems(String campaign, HashMap<String, List<ZoneItem>> zones, ServiceType serviceType){
		List<ZoneItem> myZones = new LinkedList<ZoneItem>();
		if (zones == null)
			return myZones;
		for(ResultName resultName : ResultName.values()) {
			if(!resultName.applyServiceType(serviceType))
				continue;
			String name = resultName.getZoneName(campaign);
			if(zones.containsKey(name))
					myZones.addAll(zones.get(name));
		}
		
		return myZones;
	}
	
	private static void detail(Modality modality, ServiceType serviceType, String campaign) {
    	if (campaign.length() > 6) {
    		Logger.log(Logger.Debug, "Buscando la campa\u00F1a como tipo " + serviceType);
    		List<ZoneItem> zoneCampaign = getZoneItems(campaign, XmlUtilsByModality.getAllZoneItems(modality, serviceType), serviceType);
    		if(!zoneCampaign.isEmpty()) {
    			Collections.sort(zoneCampaign, Collections.reverseOrder());
    			Logger.onlyScreen("\nConfiguraci\u00F3n de la marcaci\u00F3n:");
				Logger.onlyScreen("Impact Category                              |Valid From |Valid To   |Product Name      |N\u00FAmero Real");
				for (ZoneItem zoneItem : zoneCampaign) {
    				Logger.onlyScreen(Utils.fixedLenthString(zoneItem.getName(), 45)+"|"+Utils.fixedLenthString(zoneItem.getValidFrom(), 11)+
    						"|"+Utils.fixedLenthString(zoneItem.getValidTo(), 11)+"|"+Utils.fixedLenthString(zoneItem.getProductName(), 18)+
    						"|"+zoneItem.getDestinationPrefix());
				}
				HashMap<String, PriceTier> priceTiers = XmlUtilsByModality.getPriceTiers(modality, serviceType, campaign);
				Logger.onlyScreen("\nConfiguraci\u00F3n de precios:");
    			Logger.onlyScreen("Price Model                                  |Valid From |RUM Name |Price  |Impuestos|GLID          |Descr. GLID");
    			
    			List<DetailPrint> detailPrints = new LinkedList<DetailPrint>();
    			for(String key : priceTiers.keySet()) {
    				PriceTier priceTier = priceTiers.get(key);
    				HashMap<String, List<PriceTierRange>> mapOfPriceTierRanges = priceTier.getPriceTierRanges();
    				for(String _key : mapOfPriceTierRanges.keySet()) {
    					if(_key.equals("0"))
    						continue;
    					List<PriceTierRange> listOfpriceTierRanges = mapOfPriceTierRanges.get(_key);
    					for(PriceTierRange priceTierRange : listOfpriceTierRanges) {
    						if(priceTierRange.getUpperBound().equals("NO_MAX"))
    							detailPrints.add(new DetailPrint(Utils.fixedLenthString(priceTier.getName(), 45), Utils.fixedLenthString(_key, 11), Utils.fixedLenthString(priceTier.getRum(), 9),
    									Utils.fixedLenthString(Double.toString(priceTierRange.getPrice()), 7), Utils.fixedLenthString(priceTierRange.getTaxCode(), 9),
    									Utils.fixedLenthString(priceTierRange.getGlid(), 14), Utils.fixedLenthString(PinLoadGlid.getGlidDescription(priceTierRange.getGlid()), 30)));
    					}
    						
    				}
    			}
    			Collections.sort(detailPrints, Collections.reverseOrder());
    			for(DetailPrint detailPrint : detailPrints) 
    				Logger.onlyScreen(detailPrint.getPriceModel() + "|" + detailPrint.getValidFrom() + "|" + detailPrint.getRunName() + "|" + detailPrint.getPrice() 
    				+ "|" + detailPrint.getTax() + "|" + detailPrint.getGlid() + "|" + detailPrint.getDescGlid());
    		}
    		else
    			Logger.screen(Logger.Debug, "No se encontr\u00F3 la campa\u00F1a indicada.");
    	}   
	}
}
