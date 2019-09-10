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
	
	public String getPDCConfigCharges(Modality modality) {
		if(this.pdcConfigCharges != null)
			return this.pdcConfigCharges;
		if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_PRE;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.PREPAID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_SMS_PRE;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_HYB;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.HYBRID))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_SMS_HYB;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HORARY))
			this.pdcConfigCharges = Parameters.PDC_CONFIG_CHARGES_TEL_HYB_HORARIO;
		return this.pdcConfigCharges;
	}
	
	public String getPDCConfigZonning(Modality modality) {
		if(this.pdcConfigZonning != null)
			return this.pdcConfigZonning;
		if(this.equals(ServiceType.TEL) && modality.equals(Modality.PREPAID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_PRE;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.PREPAID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_SMS_PRE;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HYBRID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_HYB;
		else if(this.equals(ServiceType.SMS) && modality.equals(Modality.HYBRID))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_SMS_HYB;
		else if(this.equals(ServiceType.TEL) && modality.equals(Modality.HORARY))
			this.pdcConfigZonning = Parameters.PDC_CONFIG_ZONNING_TEL_HYB_HORARIO;
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
