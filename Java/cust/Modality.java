package cust;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @autor Roger Masis
 */
public enum Modality {

	POSPAID(), PREPAID(), HYBRID(), HORARY(), CCM(), CCF;

	public ServiceType SMS, TEL;

	static {
		for (Modality modality : Modality.class.getEnumConstants()) {
			modality.SMS = ServiceType.SMS;
			modality.TEL = ServiceType.TEL;
		}
	}

	private Modality() {
	}
	
	public static Modality getModality(String code) {
		switch (code.toUpperCase()) {
		case "PRE":
			return Modality.PREPAID;
		case "HIB":
			return Modality.HYBRID;
		case "POS":
			return Modality.POSPAID;
		case "HOR":
			return Modality.HORARY;
		case "CCF":
			return Modality.CCF;
		case "CCM":
			return Modality.CCM;		
		default:
			break;
		}
		return null;
	}
	
	public List<ResultName> getResultsNames(ServiceType serviceType) {
		List<ResultName> listOfResultName = new LinkedList<Modality.ResultName>();
		for (ResultName resultName : ResultName.class.getEnumConstants()) {
			if(this.equals(Modality.POSPAID) && resultName.isPospaid && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
			if(this.equals(Modality.PREPAID) && resultName.isPrepaid && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
			if(this.equals(Modality.HYBRID) && resultName.isHybrid && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
			if(this.equals(Modality.HORARY) && resultName.isHorary && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
			if(this.equals(Modality.CCF) && resultName.isCCFixed && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
			if(this.equals(Modality.CCM) && resultName.isCCMobile && resultName.applyServiceType(serviceType))
				listOfResultName.add(resultName);
		}
		return listOfResultName;
	}
	
	public Boolean applyResultName(ResultName resultName) {
		if(this.equals(Modality.POSPAID) && resultName.isPospaid)
			return true;
		if(this.equals(Modality.PREPAID) && resultName.isPrepaid)
			return true;
		if(this.equals(Modality.HYBRID) && resultName.isHybrid)
			return true;
		if(this.equals(Modality.HORARY) && resultName.isHorary)
			return true;
		if(this.equals(Modality.CCF) && resultName.isCCFixed)
			return true;
		if(this.equals(Modality.CCM) && resultName.isCCMobile)
			return true;
		return false;
	}

	public enum ResultName {
		//TEL
		PRE_IC_VOZ_ONNET_900XXXX("900", "TelcoGsmTelephony", false, true, true, true, true, false,false,false, ""), 
		PRE_IC_VOZ_FIJO_CC_ESPECIALES_900XXXX("10", "TelcoGsmTelephony", false, true, false, false, true, false,false,false, ""), 
		PRE_IC_VOZ_MOVIL_CC_ESPECIALES_900XXXX("11", "TelcoGsmTelephony", false, true, false, false, true, false,false,false, ""),
		//SMS
		PRE_IC_E_SXXXXMT("05", "TelcoGsmSms", false, true, true, false, false, true,false,false, "MT"), 
		PRE_IC_E_SXXXXMO("04", "TelcoGsmSms", false, true, true, false, false, true,false,false, "MO");
		//MIDA
		PRE_IC_MIDA_XXX_XX("02", "TelcoGsmTelephony", false, true, true, false, true, false,true,true, "");
		
		public String originPrefix, productName, group;
		public Boolean isPospaid, isPrepaid, isHybrid, isTel, isSms, isHorary, isCCFixed, isCCMobile;

		private ResultName(String originPrefix, String productName, Boolean isPospaid, Boolean isPrepaid, Boolean isHybrid, Boolean isHorary, Boolean isTel, Boolean isSms, Boolean isCCFixed, Boolean isCCMobile, String group) {
			this.originPrefix = originPrefix;
			this.productName = productName;
			this.isPospaid = isPospaid;
			this.isPrepaid = isPrepaid;
			this.isHybrid = isHybrid;
			this.isHorary = isHorary;
			this.isTel = isTel;
			this.isSms = isSms;
			this.group = group;
			this.isCCFixed = isCCFixed;
			this.isCCMobile = isCCMobile;
		}
		
		public Boolean applyServiceType(ServiceType serviceType) {
			if(serviceType.equals(ServiceType.TEL) && isTel)
				return true;
			else if(serviceType.equals(ServiceType.SMS) && isSms)
				return true;
			return false;
		}
		
		public String getZoneName(String campaign) {
			return this.name().replace("XXXX", campaign);
		}
		
	}
}
