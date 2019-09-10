package markmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import log.Logger;
import brmHandlers.ItemTypeDeploy;
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
import cust.Validations;
import cust.Modality.ResultName;

/**
 *
 * @author Adrian Rocha
 * @modifiedBy Roger Masis
 */
public class AddMarkDual {
	// Analizar tema de alta cuando el item type ya existe
	// Una posibilidad es avanzar si es que las fechas no se superponen

	/**
	 * @param args Requiere Campa\u00F1a, precio, nombre, marcaci\u00F3n y fechas,
	 */
	public static void main(String[] args) {
		Parameters.loadParameters();
		Logger.setName("AddMarkDual");
		Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO ADD CAMPAINGN SMS DUAL ***");
		Utils.removeSpecialChar(args);
		Integer i = 0;
		Modality modality = Modality.getModality(args[i]);
		if (args.length >= 11 && modality == null) {
        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[i] + "] no ha sido implemantada");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (modality != null)
			i++;
		else
			modality = Modality.POSPAID;
		String campaign = args[i++];
		String campaignName = args[i++].replace(' ', '_');
		String validFrom = args[i++];
		String validTo = args[i++];
		String glidMO = args[i++];
		String glidMT = args[i++];
		Double priceMO = Double.parseDouble(args[i++]);
		Double priceMT = Double.parseDouble(args[i++]);
		String taxCodeMO = args[i++];
		String taxCodeMT = args[i++];
		DateFormater.stringToDate(validFrom);
		DateFormater.stringToDate(validTo);
		ItemTypeDeploy.validateCM();
		if (validFrom.compareTo(validTo) > 0) {
			Logger.screen(Logger.Error,
					"Valor inv\u00E1lido en Fechas de campa\u00F1a, el inicio no puede ser posterior al final.");
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
		if (validFrom.compareTo(Parameters.START_DATE_FROM) < 0) {
			Logger.screen(Logger.Error, "Valor inv\u00E1lido en Fechas de campa\u00F1a, el inicio no puede ser menor a "
					+ Parameters.START_DATE_FROM);
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
		Validations.validateCampaignID(campaign, "SMS");
		if (!PinLoadGlid.existsGlid(glidMO)) {
			Logger.screen(Logger.Error, "El GLID indicado no existe: " + glidMO);
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
		if (!PinLoadGlid.existsGlid(glidMO)) {
			Logger.screen(Logger.Error, "El GLID indicado no existe: " + glidMT);
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
		Logger.log(Logger.Debug, "******Price MO: " + priceMO);
		Logger.log(Logger.Debug, "******Price MT: " + priceMT);
		try {
			if (modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID)) {
				campaignSms(modality, campaign, campaignName, validFrom, validTo, glidMO, priceMO, taxCodeMO, glidMT,
						priceMT, taxCodeMT);
			} else {
				String zoneMO = "E_S" + campaign;
				String zoneMT = "E_S" + campaign + "MT";
				if (XmlUtils.isZoneValidAtDate(zoneMO, validFrom) || XmlUtils.isZoneValidAtDate(zoneMT, validFrom)
						|| XmlUtils.isZoneValidAtDate(zoneMO, validTo) || XmlUtils.isZoneValidAtDate(zoneMT, validTo)) {
					dateNotValid();
				}
				Logger.log(Logger.Debug, "Agregando Zone Item");
				XmlUtils.addZoneItem("TelcoGsmSms", zoneMO, validFrom, validTo,
						"00777" + Integer.toString(campaign.length()) + campaign, campaignName);
				XmlUtils.addZoneItem("TelcoGsmSms", zoneMT, validFrom, validTo,
						"00888" + Integer.toString(campaign.length()) + campaign, campaignName);
				// Busco si es que ya existe el Item
				String itemDesc = XmlUtils.getItemSmsDescription(campaign);
				if (itemDesc == null) {
					Logger.log(Logger.Debug, "Creando nuevo Item");
					ItemTypeDeploy.AddItemType("E_S" + campaign);
					XmlUtils.addItemRuleSpecSms(campaign, campaignName);
				} else {// Caso en el que el item ya existe, solo actualizo la descripcion si es
						// necesario
					if (!itemDesc.equals(campaignName)) {
						Logger.log(Logger.Debug, "Modificando Item Desc");
						XmlUtils.modItemSmsDescription(campaign, campaignName);
					}
				}
				PriceTierRange ptrMO = new PriceTierRange("NO_MAX", "188", taxCodeMO, priceMO, "NONE", 1.0, glidMO);
				PriceTierRange ptrMT = new PriceTierRange("NO_MAX", "188", taxCodeMT, priceMT, "NONE", 1.0, glidMT);
				HashMap<String, List<PriceTierRange>> ptrHashMO = new HashMap<String, List<PriceTierRange>>();
				List<PriceTierRange> lMO = new ArrayList<PriceTierRange>();
				lMO.add(ptrMO);
				HashMap<String, List<PriceTierRange>> ptrHashMT = new HashMap<String, List<PriceTierRange>>();
				if (validFrom.compareTo(Parameters.START_DATE_FROM) > 0) {// Siempre el price tier range debe iniciar en
																			// esta fecha, se inicia con cargo 0
					PriceTierRange ptr0 = new PriceTierRange("NO_MAX", "188", taxCodeMO, 0.0, "NONE", 1.0, glidMO);
					List<PriceTierRange> l0 = new ArrayList<PriceTierRange>();
					l0.add(ptr0);
					ptrHashMO.put(Parameters.START_DATE_FROM, l0);
					ptrHashMT.put(Parameters.START_DATE_FROM, l0);
				}
				ptrHashMO.put(validFrom, lMO);
				List<PriceTierRange> lMT = new ArrayList<PriceTierRange>();
				lMT.add(ptrMT);
				ptrHashMT.put(validFrom, lMT);
				// Configurando Price Tier por duracion
				if (XmlUtils.getSmsPriceTier(zoneMO) == null) {// Busco si es que ya existe el PriceTier
					PriceTier ptMO = new PriceTier(zoneMO, ptrHashMO, "EVT");
					XmlUtils.addSmsPriceTier("E_S" + campaign, ptMO);
				} else {
					XmlUtils.addSmsPriceTierRange(zoneMO, validFrom, ptrMO);
				}
				if (XmlUtils.getSmsPriceTier(zoneMT) == null) {// Busco si es que ya existe el PriceTier
					PriceTier ptMT = new PriceTier(zoneMT, ptrHashMT, "EVT");
					XmlUtils.addSmsPriceTier("E_S" + campaign + "MT", ptMT);
				} else {
					XmlUtils.addSmsPriceTierRange(zoneMT, validFrom, ptrMT);
				}
				XmlUtils.updateHistoryXml("E_S" + campaign);
				XmlUtils.updateHistoryXml("E_S" + campaign + "MT");
				Utils.notifyNewCampaign("SMS", campaign, validFrom + " a " + validTo,
						Double.toString(priceMO) + " / " + Double.toString(priceMT), glidMO + " / " + glidMT,
						campaignName, false);
			}
			Logger.screen(Logger.Debug, "Nueva campa\u00F1a creada!");
		} catch (Exception e) {
			Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddMarkDual");
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
	}

	private static void campaignSms(Modality modality, String campaign, String campaignName, String validFrom,
			String validTo, String glidMO, Double priceMO, String taxCodeMO, String glidMT, Double priceMT,
			String taxCodeMT) {
		ServiceType serviceType = modality.SMS;
		for (ResultName resultName : modality.getResultsNames(serviceType)) {
			String zoneName = resultName.getZoneName(campaign);
			if (XmlUtilsByModality.isZoneValidAtDate(modality, serviceType, zoneName, validFrom)
					|| XmlUtilsByModality.isZoneValidAtDate(modality, serviceType, zoneName, validTo))
				dateNotValid();
		}
		Logger.log(Logger.Debug, "Agregando Zone Item");
		// Agregar MO y MT
		XmlUtilsByModality.addZoneItem(modality, serviceType, campaign, validFrom, validTo,
				serviceType.prefix + campaign, null);

		List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
		listOfPriceTierRange
				.add(new PriceTierRange("NO_MAX", "188", taxCodeMT, priceMT, "NONE", 1.0, glidMT, true, "MT"));
		listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", null, Parameters.DEFAULT_PRICE_SMS, "NONE", 1.0,
				glidMO, false, "MO"));
		listOfPriceTierRange
				.add(new PriceTierRange("NO_MAX", "188", taxCodeMO, priceMO, "NONE", 1.0, glidMO, true, "MO"));

		HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
		mapPriceTierRange.put(Parameters.START_DATE_FROM_PRE, listOfPriceTierRange);
		mapPriceTierRange.put(validFrom, listOfPriceTierRange);
		for (ResultName resultName : modality.getResultsNames(serviceType)) {
			if (!XmlUtilsByModality.existsTelephonyPriceTier(modality, serviceType, resultName.getZoneName(campaign))) {// Busco si es que ya existe
				XmlUtilsByModality.addPriceTier(modality, serviceType,
						new PriceTier(resultName.getZoneName(campaign), mapPriceTierRange, null), resultName);
			} else {
				XmlUtilsByModality.addPriceTierRange(modality, serviceType,
						new PriceTier(resultName.getZoneName(campaign), mapPriceTierRange, null), resultName);
			}
			XmlUtilsByModality.updateHistoryXml(modality, serviceType, resultName.getZoneName(campaign));
		}

		Utils.notifyNewCampaign("SMS", campaign, validFrom + " a " + validTo,
				Double.toString(priceMO) + " / " + Double.toString(priceMT), glidMO + " / " + glidMT, campaignName, true);
	}

	private static void dateNotValid() {
		Logger.screen(Logger.Error,
				"Existe una campa\u00F1a vigente con el mismo ID dentro del rango de fechas indicado!");
		Logger.screen(Logger.Error, "Saliendo.");
		System.exit(Parameters.ERR_INVALID_VALUE);
	}
}
