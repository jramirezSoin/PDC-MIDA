package markmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import log.Logger;

/**
 *
 * @author Adrian Rocha
 * @modifiedBy Roger Masis
 */
public class AddPriceModelStep {

	/**
	 * @param args Requiere Campa\u00F1a y Nuevo precio,
	 */
	public static void main(String[] args) {
		Parameters.loadParameters();
		Logger.setName("AddPriceModelStep");
		Logger.log(Logger.Debug, "*** Inicio Ejecuci\u00F3n - MODO ADD PRICE MODEL STEP ***");
		Utils.removeSpecialChar(args);
		boolean updated = false;
		Integer i = 0;
		Modality modality = Modality.getModality(args[i]);
		if (args.length >= 8 && modality == null) {
        	Logger.screen(Logger.Error, "Tipo de modalidad [" + args[i] + "] no ha sido implemantada");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (modality != null)
			i++;
		else
			modality = Modality.POSPAID;
		String evtType = args[i++];// 900-SMS
		String rateType = args[i++];// MO-MT-DUR-EVT
		String campaign = args[i++];
		double newCharge = Double.parseDouble(args[i++]);
		String newValidFrom = args[i++];
		String taxCode = args[i++];
		String glid = args[i++];
		String zone = "";
		Validations.validateEvtAndRateType(evtType, rateType);
		ServiceType serviceType = ServiceType.getServiceType(evtType);
		if (args.length >= 11 && modality.equals(Modality.HORARY) && !serviceType.equals(ServiceType.TEL)) {
        	Logger.screen(Logger.Error, "La campa\\u00F1a [" + evtType + "] no ha sido implemantada para esta modalidad [" + modality + "].");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
		if (!PinLoadGlid.existsGlid(glid)) {
			Logger.screen(Logger.Error, "El GLID indicado no existe: " + glid);
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
		if (evtType.equals("900")) {
			zone = "E_ND" + campaign;
		} else if (evtType.equals("SMS")) {
			zone = "E_S" + campaign;
		}
		DateFormater.stringToDate(newValidFrom);
		try {
			Logger.log(Logger.Debug, "Verificando existencia de la campa\u00F1a...");
			if ((modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID) || modality.equals(Modality.HORARY)) && serviceType.equals(ServiceType.TEL))
				campaignTel(modality, rateType, campaign, newCharge, newValidFrom, taxCode, glid);
			else if ((modality.equals(Modality.PREPAID) || modality.equals(Modality.HYBRID)) && serviceType.equals(ServiceType.SMS))
				campaignSms(modality, rateType, campaign, newCharge, newValidFrom, taxCode, glid);
			else if (evtType.equals("SMS")) {
				if (evtType.equals("SMS")) {
					String zoneMT = "E_S" + campaign + "MT";
					if (!XmlUtils.isZoneValidAtDate(zone, newValidFrom)) {
						Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
						Logger.screen(Logger.Error, "Saliendo.");
						System.exit(Parameters.ERR_INVALID_VALUE);
					}
					PriceTier pt, ptMT;
					pt = XmlUtils.getSmsPriceTier(zone);
					if (pt == null) {
						Logger.screen(Logger.Error, "La campa\u00F1a indicada no existe!");
						Logger.screen(Logger.Error, "Saliendo.");
						System.exit(Parameters.ERR_INVALID_VALUE);
					}
					ptMT = XmlUtils.getSmsPriceTier(zoneMT);
					if (ptMT == null) {
						Logger.screen(Logger.Error,
								"La campa\u00F1a indicada no tiene tarifa MT, no es posible proceder. Consulte al administrador");
						Logger.screen(Logger.Error, "Saliendo.");
						System.exit(Parameters.ERR_INTEGRATE);
					}
					if (pt.existePriceModelStepPosterior(newValidFrom)) {
						Logger.screen(Logger.Warning,
								"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a!");
						Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
					}
					PriceTierRange ptrMO, ptrMT;
					if (rateType.equals("MO")) {
						Logger.log(Logger.Debug, "configurando tarifa MO");
						ptrMO = new PriceTierRange("NO_MAX", "188", taxCode, newCharge, "NONE", 1.0, glid);
						ptrMT = new PriceTierRange("NO_MAX", "188", taxCode, 0.0, "NONE", 1.0, glid);
					} else {
						Logger.log(Logger.Debug, "configurando tarifa MT");
						ptrMO = new PriceTierRange("NO_MAX", "188", taxCode, 0.0, "NONE", 1.0, glid);
						ptrMT = new PriceTierRange("NO_MAX", "188", taxCode, newCharge, "NONE", 1.0, glid);
					}
					Logger.log(Logger.Debug, "Aplicando tarifa MO al XML");
					XmlUtils.addSmsPriceTierRange(zone, newValidFrom, ptrMO);
					Logger.log(Logger.Debug, "Aplicando tarifa MT al XML");
					XmlUtils.addSmsPriceTierRange(zoneMT, newValidFrom, ptrMT);
				} else {// 900
					String zoneEVT = "E_NE" + campaign;
					PriceTier ptDur, ptEvt;
					ptDur = XmlUtils.getTelephonyPriceTier(zone);
					ptEvt = XmlUtils.getTelephonyPriceTier(zoneEVT);
					double chargeEvt = 0.0;
					double chargeDur = 0.0;
					if (rateType.equals("EVT")) {
						chargeEvt = newCharge;
					} else {
						chargeDur = newCharge;
					}
					PriceTierRange ptrDur = new PriceTierRange("NO_MAX", "188", taxCode, chargeDur, "MINUTES", 1.0, glid);
					PriceTierRange ptrEvt = new PriceTierRange("1", "188", taxCode, chargeEvt, "NONE", 1.0, glid);
					HashMap<String, List<PriceTierRange>> ptrHashPriceDur = new HashMap<String, List<PriceTierRange>>();
					HashMap<String, List<PriceTierRange>> ptrHashPriceEvt = new HashMap<String, List<PriceTierRange>>();
					List<PriceTierRange> lPriceInitialDur = new ArrayList<PriceTierRange>();
					List<PriceTierRange> lPriceDur = new ArrayList<PriceTierRange>();
					List<PriceTierRange> lPriceInitialEvt = new ArrayList<PriceTierRange>();
					List<PriceTierRange> lPriceEvt = new ArrayList<PriceTierRange>();
					lPriceInitialDur.add(new PriceTierRange("NO_MAX", "188", taxCode, 0.0, "MINUTES", 1.0, glid));
					ptrHashPriceDur.put(Parameters.START_DATE_FROM, lPriceInitialDur);
					lPriceDur.add(ptrDur);
					ptrHashPriceDur.put(newValidFrom, lPriceDur);
					lPriceInitialEvt.add(new PriceTierRange("1", "188", taxCode, 0.0, "NONE", 1.0, glid));
					ptrHashPriceEvt.put(Parameters.START_DATE_FROM, lPriceInitialEvt);
					lPriceEvt.add(ptrEvt);
					ptrHashPriceEvt.put(newValidFrom, lPriceEvt);
					if (XmlUtils.isZoneValidAtDate(zone, newValidFrom)) {// Para 900 hay que revisar por duration y por
																			// event, y si hay cambio de tarifa es necesario
																			// actualizar el zonning
						// Conf vigente para la fecha por Duracion
						if (((ptDur != null) && ptDur.existePriceModelStepPosterior(newValidFrom))
								|| ((ptEvt != null) && ptEvt.existePriceModelStepPosterior(newValidFrom))) {
							Logger.screen(Logger.Warning,
									"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a!");
							Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
						}
						Logger.log(Logger.Debug, "Aplicando nueva tarifa 900");
						if (rateType.equals("EVT")) {
							XmlUtils.modZoneItemsRum900(zone, newValidFrom);
							updated = XmlUtils.addTelephonyPriceTierRange(zoneEVT, newValidFrom, ptrEvt);
							if (!updated) {// Si no existe el zone result a nivel evento, entonces lo creo
								String zoneName = "NE" + campaign;
								ptEvt = new PriceTier(zoneName, ptrHashPriceEvt, "EVT");
								PriceTier pt0 = new PriceTier(zoneName, ptrHashPriceDur, "DUR");
								updated = XmlUtils.addTelephonyPriceTier(pt0, ptEvt);
							}
							if (!XmlUtils.isZoneValid(zone)) {
								Logger.log(Logger.Warning,
										"Se detecta que el " + zone + " ya no esta en uso, se borrara el charge");
								XmlUtils.delTelephonyPriceTierRange(zone);
							}
						} else {
							updated = XmlUtils.addTelephonyPriceTierRange(zone, newValidFrom, ptrEvt);
						}
					} else if (XmlUtils.isZoneValidAtDate(zoneEVT, newValidFrom)) {
						// Conf vigente para la fecha por Evento
						if (((ptDur != null) && ptDur.existePriceModelStepPosterior(newValidFrom))
								|| ((ptEvt != null) && ptEvt.existePriceModelStepPosterior(newValidFrom))) {
							Logger.screen(Logger.Warning,
									"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a!");
							Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
						}
						if (rateType.equals("DUR")) {
							XmlUtils.modZoneItemsRum900(zoneEVT, newValidFrom);
							updated = XmlUtils.addTelephonyPriceTierRange(zone, newValidFrom, ptrDur);
							if (!updated) {
								String zoneName = "ND" + campaign;
								ptDur = new PriceTier(zoneName, ptrHashPriceDur, "DUR");
								PriceTier pt0 = new PriceTier(zoneName, ptrHashPriceEvt, "EVT");
								updated = XmlUtils.addTelephonyPriceTier(ptDur, pt0);
							}
							if (!XmlUtils.isZoneValid(zoneEVT)) {
								Logger.log(Logger.Warning,
										"Se detecta que el " + zoneEVT + " ya no esta en uso, se borrara el charge");
								XmlUtils.delTelephonyPriceTierRange(zoneEVT);
							}
						} else {
							updated = XmlUtils.addTelephonyPriceTierRange(zoneEVT, newValidFrom, ptrEvt);
						}
					} else {
						updated = false;
					}
					if (!updated) {
						Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
						Logger.screen(Logger.Error, "Saliendo.");
						System.exit(Parameters.ERR_INVALID_VALUE);
					}
				}
			}
			Logger.screen(Logger.Debug, "Nuevo precio agregado.");
		} catch (Exception e) {
			Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddPriceModelStep");
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
	}

	private static void campaignSms(Modality modality, String rateType, String campaign, Double price, String validFrom,
			String taxCode, String glid) {
		try {
			ServiceType serviceType = modality.SMS;
			// Validaciones
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				if (!resultName.group.equals(rateType))
					continue;
				if (!XmlUtilsByModality.isZoneValidAtDate(modality, serviceType, resultName.getZoneName(campaign),
						validFrom)) {
					Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
					Logger.screen(Logger.Error, "Saliendo.");
					System.exit(Parameters.ERR_INVALID_VALUE);
				}
				if (new PriceTier(resultName.getZoneName(campaign),
						XmlUtilsByModality.getPriceTier(modality, serviceType, resultName.getZoneName(campaign)),
						rateType).existePriceModelStepPosterior(validFrom)) {
					Logger.screen(Logger.Warning,
							"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a! :" + resultName.getZoneName(campaign));
					Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
				}
			}
			List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
			if (rateType.equals("MT"))
				listOfPriceTierRange
						.add(new PriceTierRange("NO_MAX", "188", taxCode, price, "NONE", 1.0, glid, true, "MT"));
			else {
				listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", null, Parameters.DEFAULT_PRICE_SMS, "NONE",
						1.0, glid, false, "MO"));
				listOfPriceTierRange
						.add(new PriceTierRange("NO_MAX", "188", taxCode, price, "NONE", 1.0, glid, true, "MO"));
			}
			Logger.log(Logger.Debug, "Aplicando nueva tarifa");
			XmlUtilsByModality.modifyZoneItem(modality, serviceType, campaign, validFrom, rateType);
			HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
			mapPriceTierRange.put(validFrom, listOfPriceTierRange);
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				if (!resultName.group.equals(rateType))
					continue;
				String zoneName = resultName.getZoneName(campaign);
				if (!XmlUtilsByModality.addPriceTierRange(modality, serviceType,
						new PriceTier(zoneName, mapPriceTierRange, rateType), resultName))
					XmlUtilsByModality.addPriceTier(modality, serviceType,
							new PriceTier(zoneName, mapPriceTierRange, rateType), resultName);
				if (!XmlUtilsByModality.isZoneValid(modality, serviceType, zoneName)) {
					Logger.log(Logger.Warning,
							"Se detecta que el " + zoneName + " ya no esta en uso, se borrara el charge");
					XmlUtilsByModality.delPriceTierRange(modality, serviceType, zoneName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddPriceModelStep");
			System.exit(Parameters.ERR_INVALID_VALUE);
		}
	}

	private static void campaignTel(Modality modality, String rateType, String campaign, Double price, String validFrom,
			String taxCode, String glid) {
		try {
			ServiceType serviceType = modality.TEL;
			List<PriceTierRange> listOfPriceTierRange = new ArrayList<PriceTierRange>();
			if (rateType.equals("DUR")) {
				listOfPriceTierRange.add(new PriceTierRange("2", "188", taxCode, 0.0, "MINUTES", 1.0, glid));
				listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", taxCode, Parameters.DEFAULT_PRICE_MINUTE,
						"MINUTES", 1.0, glid));
				listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", taxCode, price, "MINUTES", 1.0, glid));
			} else {
				listOfPriceTierRange.add(new PriceTierRange("2", "188", taxCode, 0.0, "MINUTES", 1.0, glid));
				listOfPriceTierRange.add(new PriceTierRange("NO_MAX", "188", taxCode, Parameters.DEFAULT_PRICE_MINUTE,
						"MINUTES", 1.0, glid));
				listOfPriceTierRange
						.add(new PriceTierRange("NO_MAX", "188", taxCode, price, "MINUTES", 1.0, glid, false));
			}

			// Validaciones
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				if (!XmlUtilsByModality.isZoneValidAtDate(modality, serviceType, resultName.getZoneName(campaign),
						validFrom)) {
					Logger.screen(Logger.Error, "La campa\u00F1a no est\u00E1 vigente en la fecha indicada!");
					Logger.screen(Logger.Error, "Saliendo.");
					System.exit(Parameters.ERR_INVALID_VALUE);
				}
				if (new PriceTier(resultName + campaign,
						XmlUtilsByModality.getPriceTier(modality, serviceType, resultName + campaign), rateType)
								.existePriceModelStepPosterior(validFrom)) {
					Logger.screen(Logger.Warning,
							"Se advierte que existe una tarifa posterior para esta misma campa\u00F1a! :" + resultName
									+ campaign);
					Logger.screen(Logger.Warning, "Continuando con la configuraci\u00F3n...");
				}
			}
			Logger.log(Logger.Debug, "Aplicando nueva tarifa 900");
			XmlUtilsByModality.modifyZoneItem(modality, serviceType, campaign, validFrom, null);
			HashMap<String, List<PriceTierRange>> mapPriceTierRange = new HashMap<String, List<PriceTierRange>>();
			mapPriceTierRange.put(validFrom, listOfPriceTierRange);
			for (ResultName resultName : modality.getResultsNames(serviceType)) {
				String zoneName = resultName.getZoneName(campaign);
				if (!XmlUtilsByModality.addPriceTierRange(modality, serviceType,
						new PriceTier(zoneName, mapPriceTierRange, rateType)))
					XmlUtilsByModality.addPriceTier(modality, serviceType,
							new PriceTier(zoneName, mapPriceTierRange, rateType));
				if (!XmlUtilsByModality.isZoneValid(modality, serviceType, zoneName)) {
					Logger.log(Logger.Warning,
							"Se detecta que el " + zoneName + " ya no esta en uso, se borrara el charge");
					XmlUtilsByModality.delPriceTierRange(modality, serviceType, zoneName);
				}
			}
		} catch (Exception e) {
			Logger.screen(Logger.Error, "Valor inv\u00E1lido en AddPriceModelStep");
			System.exit(Parameters.ERR_INVALID_VALUE);
		}

	}
}
