package cust;

import config.Parameters;

public enum ServiceType {
	TEL("NONE", "900"), SMS("FROM_BAL_IMPACT", "");

	public String distributionMethod, prefix;
	private String pdcConfigZonning, pdcConfigCharges, pdcConfigItem;

	private ServiceType(String distributionMethod, String prefix) {
		this.distributionMethod = distributionMethod;
		this.prefix = prefix;
	}
	
	public static ServiceType getServiceType(String code) {
		switch (code.toUpperCase()) {
		case "900":
			return ServiceType.TEL;
		case "SMS":
			return ServiceType.SMS;
		}
		return null;
	}
	
	public void setPDCConfigItem(Modality modality) {
		new Throwable("No implementado");
	}
	
	public String getPDCConfigItem() {
		new Throwable("No implementado");
		return this.pdcConfigItem;
	}
	//CAMBIO
	public String getPDCConfigCharges(Modality modality) {
		return getPDCConfigCharges(modality,false);
	}
	
	public String getPDCConfigCharges(Modality modality, Boolean mida) {
		if(this.pdcConfigCharges != null && (!mida))
			return this.pdcConfigCharges;
		if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID) && (!mida))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_PRE;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.PREPAID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_SMS_PRE;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID) && (!mida))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_HYB;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.HYBRID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_SMS_HYB;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HORARY))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_HYB_HORARIO;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID) && mida)
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_PRE_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID) && mida)
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_HIB_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.CCM) && mida)
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_CCM_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.CCF) && mida)
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_CCF_MIDA;

		return this.pdcConfigCharges;
	}
	//CAMBIO
	public String getPDCConfigZonning(Modality modality) {
		return getPDCConfigZonning(modality,false);
	}

	public String getPDCConfigZonning(Modality modality,Boolean mida) {
		if(this.pdcConfigZonning != null && (!mida))
			return this.pdcConfigZonning;
		if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID) && (!mida))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_PRE;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.PREPAID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_SMS_PRE;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID) && (!mida))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_HYB;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.HYBRID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_SMS_HYB;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HORARY))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_HYB_HORARIO;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID) && mida)
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_PRE_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID) && mida)
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_HIB_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.CCM) && mida)
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_CCM_MIDA;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.CCF) && mida)
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_CCF_MIDA;
		return this.pdcConfigZonning;
	}

	public Boolean equals(ServiceType serviceType) {
		if (serviceType == null)
			return false;
		else if (!this.name().equalsIgnoreCase(serviceType.name()))
			return false;
		return true;
	}
}
