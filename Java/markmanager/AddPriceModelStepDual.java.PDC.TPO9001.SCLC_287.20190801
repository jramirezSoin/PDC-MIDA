package markmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import brmHandlers.PinLoadGlid;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.DateFormater;
import cust.Modality;
import cust.PriceTier;
import cust.PriceTierRange;
import cust.ServiceType;
import cust.Utils;
import cust.Modality.ResultName;
import log.Logger;

/**
 *
 * @author Adrian Rocha
 * @modifiedBy Roger Masis
 */
public class AddPriceModelStepDual {

    /**
     * @param args Requiere Campa\u00F1a y Nuevos precios, 
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("AddPriceModelStep");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO ADD DUAL PRICE MODEL STEP ***");
        Utils.removeSpecialChar(args);
        Integer i = 0;
		Modality modality = Modality.getModality(args[i]);
		if (args.length >= 9 && modality == null) {
        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[i] + "] no ha sido implemantada");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (modality != null)
			i++;
		else
			modality = Modality.POSPAID;
        String campaign = args[i++];
        double newChargeMO = Double.parseDouble(args[i++]);
        double newChargeMT = Double.parseDouble(args[i++]);
        String newValidFrom = args[i++];
        String glidMO = args[i++];
        String glidMT = args[i++];
        String taxCodeMO = args[i++];
        String taxCodeMT = args[i++];
        DateFormater.stringToDate(newValidFrom);
        String zoneMO = "E_S"+campaign;
        String zoneMT = "E_S"+campaign+"MT";
    	if (!PinLoadGlid.existsGlid(glidMO)) {
            Logger.screen(Logger.Error, "El GLID indicado no existe: "+glidMO);
            System.exit(Parameters.ERR_INVALID_VALUE);    		
    	}
    	if (!PinLoadGlid.existsGlid(glidMT)) {
            Logger.screen(Logger.Error, "El GLID indicado no existe: "+glidMT);
            System.exit(Parameters.ERR_INVALID_VALUE);    		
    	}
        try {
        	Logger.log(Logger.Debug, "Verificando existencia de la campa\u00F1a...");
        	if (modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID)) {
        		campaignSms(modality, campaign, newValidFrom, glidMO, newChargeMO, taxCodeMO, glidMT, newChargeMT, taxCodeMT);
        	}else {
				if (!XmlUtils.isZoneValidAtDate(zoneMO, newValidFrom) || !XmlUtils.isZoneValidAtDate(zoneMT, newValidFrom)) {
	                Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
	                Logger.screen(Logger.Error, "Saliendo.");
	                System.exit(Parameters.ERR_INVALID_VALUE);        		
	    		}
	    		PriceTier ptMO, ptMT;
	    		ptMO = XmlUtils.getSmsPriceTier(zoneMO);
	            if (ptMO == null) {
	                Logger.screen(Logger.Error, "La campa\u00F1a indicada no existe!");
	                Logger.screen(Logger.Error, "Saliendo.");
	                System.exit(Parameters.ERR_INVALID_VALUE);
	            }
	            ptMT = XmlUtils.getSmsPriceTier(zoneMT);
	            if (ptMT == null) {
	                Logger.screen(Logger.Error, "La campa\u00F1a indicada no tiene tarifa MT, no es posible proceder. Consulte al administrador");
	                Logger.screen(Logger.Error, "Saliendo.");
	                System.exit(Parameters.ERR_INTEGRATE);
	            }
	            if (ptMO.existePriceModelStepPosterior(newValidFrom)) {
	            	Logger.screen(Logger.Warning, "Se advierte que existe una tarifa posterior para esta misma campa\u00F1a!");
	                Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
	            }
	        	Logger.log(Logger.Debug, "configurando tarifas");
	            PriceTierRange ptrMO = new PriceTierRange("NO_MAX", "188", taxCodeMO, newChargeMO, "NONE", 1.0, glidMO);
	            PriceTierRange ptrMT = new PriceTierRange("NO_MAX", "188", taxCodeMT, newChargeMT, "NONE", 1.0, glidMT);
	        	Logger.log(Logger.Debug, "Aplicando tarifa MO al XML");
	            XmlUtils.addSmsPriceTierRange(zoneMO, newValidFrom, ptrMO);
	        	Logger.log(Logger.Debug, "Aplicando tarifa MT al XML");
	            XmlUtils.addSmsPriceTierRange(zoneMT, newValidFrom, ptrMT);
			}
        	Logger.screen(Logger.Debug, "Nuevo precio agregado.");
        } catch (Exception e) {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddPriceModelStep");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
    }
    
    private static void campaignSms(Modality modality, String campaign, String validFrom, String glidMO, Double priceMO, String taxCodeMO, String glidMT, Double priceMT, String taxCodeMT) {
    	ServiceType serviceType = modality.SMS;
    	//Validaciones
		for (ResultName resultName : modality.getResultsNames(serviceType)) {
			if (!XmlUtilsByModality.isZoneValidAtDate(modality, serviceType, resultName.getZoneName(campaign), validFrom)) {
				Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}
			if (new PriceTier(resultName.getZoneName(campaign),
					XmlUtilsByModality.getPriceTier(modality, serviceType, resultName.getZoneName(campaign)), null).existePriceModelStepPosterior(validFrom)) {
				Logger.screen(Logger.Warning,
						"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a! :" + resultName
								+ campaign);
				Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
			}
		}
		
		List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
		listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", taxCodeMT, priceMT, "NONE", 1.0, glidMT, true, "MT"));
		listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", null, Parameters.DEFAULT_PRICE_SMS, "NONE", 1.0, glidMO, false, "MO"));
		listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", taxCodeMO, priceMO, "NONE", 1.0, glidMO, true, "MO"));
        
    	Logger.log(Logger.Debug, "configurando tarifas");
    	//TODO Validar si aplica
    	//XmlUtilsByModality.modifyZoneItem(modality, serviceType, campaign, validFrom, null);
		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
		for (ResultName resultName : modality.getResultsNames(serviceType)) {
			String zoneName = resultName.getZoneName(campaign);
			if (!XmlUtilsByModality.addPriceTierRange(modality, serviceType,
					new PriceTier(zoneName, mapPriceTierRange, null), resultName))
				XmlUtilsByModality.addPriceTier(modality, serviceType,
						new PriceTier(zoneName, mapPriceTierRange, null), resultName);
		}
    }
}
