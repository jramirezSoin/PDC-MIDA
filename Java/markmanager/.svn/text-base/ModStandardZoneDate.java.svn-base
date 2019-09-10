package markmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import log.Logger;
import brmHandlers.XmlUtils;
import brmHandlers.XmlUtilsByModality;
import config.Parameters;
import cust.DateFormater;
import cust.Modality;
import cust.ZoneItem;
import cust.Modality.ResultName;
import cust.ServiceType;
import cust.Utils;
import cust.Validations;

/**
*
* @author Adrian Rocha
* @modifiedBy Roger Masis
*/
public class ModStandardZoneDate {

    /**
     * @param args Requiere Tipo Campa\u00F1a, ID y Nueva vigencia, solo se puede modificar la campaña mas reciente
     */
    public static void main(String[] args) {
        Parameters.loadParameters();
        Logger.setName("ModStandardZoneDate");
        Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO MOD VIGENCIA ***");
        Utils.removeSpecialChar(args);
        Integer i = 0;
        Modality modality = Modality.getModality(args[i]);
        if (args.length >= 4 && modality == null) {
        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[i] + "] no ha sido implemantada");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (modality != null)
			i++;
		else
			modality = Modality.POSPAID;
        String evtType = args[i++];
        String campaign = args[i++];
        String newValidTo = args[i++];
        DateFormater.stringToDate(newValidTo);
        Validations.validateCampaignID(campaign, evtType);
        ServiceType serviceType = ServiceType.getServiceType(evtType);
        if (serviceType == null) {
            Logger.screen(Logger.Error, "Valor inv\u00E1lido en Tipo de campa\u00F1a: "+ evtType);
            System.exit(Parameters.ERR_INVALID_VALUE);        	
        }
		HashMap<String, List<ZoneItem>> zones = null;
		if(modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID) || modality.equals(Modality.HORARY)) {
			zones = XmlUtilsByModality.getAllZoneItems(modality, serviceType);
			//Se generan validaciones
			Integer error = 0;
			Boolean isOK = false;
			//Se valida que exista la campana, en caso de SMS solo valida que exista al menos una campana MO o MT, para TEL debe existir todas
			for(ResultName resultName : modality.getResultsNames(serviceType)) {
				List<ZoneItem> zoneItems = zones.get(resultName.getZoneName(campaign));
				if (zoneItems == null || zoneItems.isEmpty()) {
					error = 1;
					if(serviceType.equals(ServiceType.SMS))
						continue;
					else
						break;
				}
				Collections.sort(zoneItems);
				ZoneItem zoneItem = zoneItems.get(zoneItems.size()-1);
				if (zoneItem.getValidFrom().compareTo(newValidTo) >= 0) {
					error = 2;
					if(serviceType.equals(ServiceType.SMS))
						continue;
					else
						break;
				}else if (zoneItem.getValidTo().equals(newValidTo)) { 
					error = 3;
					if(serviceType.equals(ServiceType.SMS))
						continue;
					else
						break;
				}
				isOK = isOK || true;
			}
			if(((serviceType.equals(ServiceType.SMS) && !isOK) || !serviceType.equals(ServiceType.SMS)) && error == 1) {
				Logger.screen(Logger.Error, "No se pudo encontrar la campa\u00F1a indicada!");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);  
			}else if(((serviceType.equals(ServiceType.SMS) && !isOK) || !serviceType.equals(ServiceType.SMS)) && error == 2) {
				Logger.screen(Logger.Error, "La nueva fecha de la campa\u00F1a debe ser posterior al inicio de la vigencia actual!");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}else if(((serviceType.equals(ServiceType.SMS) && !isOK) || !serviceType.equals(ServiceType.SMS)) && error == 3) {
				Logger.screen(Logger.Error, "La fecha ingresada es igual a la ya configurada! Saliendo");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}
			//Se procede con la actualizacion del XML
			for(ResultName resultName : modality.getResultsNames(serviceType)) {
				List<ZoneItem> zoneItems = zones.get(resultName.getZoneName(campaign));
				if(serviceType.equals(ServiceType.SMS) && zoneItems == null)
					continue;
				Collections.sort(zoneItems);
				ZoneItem zoneItem = zoneItems.get(zoneItems.size()-1);
				if (!XmlUtilsByModality.modifyZoneItemsValidTo(modality, serviceType, resultName.getZoneName(campaign), newValidTo, zoneItem.getValidTo(), true)) {
		        	Logger.screen(Logger.Error, "Ocurri\u00F1 un error al modificar la campa\u00F1a: "+campaign);
		        	System.exit(Parameters.ERR_UNKNOWN);
				}
				XmlUtilsByModality.updateHistoryXml(modality, serviceType, resultName.getZoneName(campaign));
			}
			Logger.screen(Logger.Debug, "La configuracion se aplico satisfactoriamente en campa\u00F1a: "+ campaign);
		}else {
			zones = XmlUtils.getAllZoneItems();
			String zone = "E_S" + campaign;
			if (evtType.equals("900")) {
				zone = "E_N" + campaign;
			}
			List<ZoneItem> zoneItems = zones.get(zone);
			if (zoneItems == null) {
				Logger.screen(Logger.Error, "No se pudo encontrar la campa\u00F1a indicada!");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);  		
			}
			Collections.sort(zoneItems);
			ZoneItem actual = zoneItems.get(zoneItems.size()-1);
			if (actual.getValidFrom().compareTo(newValidTo) >= 0) {
				Logger.screen(Logger.Error, "La nueva fecha de la campa\u00F1a debe ser posterior al inicio de la vigencia actual!");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}
			if (actual.getValidTo().equals(newValidTo)) {
				Logger.screen(Logger.Error, "La fecha ingresada es igual a la ya configurada! Saliendo");
				Logger.screen(Logger.Error, "Saliendo.");
				System.exit(Parameters.ERR_INVALID_VALUE);
			}
			if (  (evtType.equals("900") && XmlUtils.modZoneItemsValidTo(actual.getName(), newValidTo, actual.getValidTo(), true)) || 
					(evtType.equals("SMS") && XmlUtils.modZoneItemsValidTo(actual.getName(), newValidTo, actual.getValidTo(), true) && XmlUtils.modZoneItemsValidTo(actual.getName()+"MT", newValidTo, actual.getValidTo(), false))  ) {
	        	Logger.screen(Logger.Debug, "La configuracion se aplico satisfactoriamente en campa\u00F1a: "+campaign);
			}
			else {
	        	Logger.screen(Logger.Error, "Ocurri\u00F1 un error al modificar la campa\u00F1a: "+campaign);
	        	System.exit(Parameters.ERR_UNKNOWN);
			}
			if (evtType.equals("900")) {
				XmlUtils.updateHistoryXml("E_N"+campaign);
			}
			else {
		    	XmlUtils.updateHistoryXml("E_S"+campaign);
		    	XmlUtils.updateHistoryXml("E_S"+campaign+"MT");
			}
		}
    }
}
