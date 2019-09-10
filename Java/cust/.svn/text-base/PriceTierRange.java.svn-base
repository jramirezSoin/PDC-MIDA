package cust;

/**
 *
 * @author Adrian Rocha
 */
public class PriceTierRange {

    protected String balanceElementNumCode, taxCode, glid, unitOfMeasure, upperBound, group;
    protected Double price, incrementStep;
    protected Boolean isScaledCharge = true;

    public PriceTierRange(String upperBound, String balanceElementNumCode, String taxCode, Double price, String unitOfMeasure, Double incrementStep,  String glid) {
    	this.upperBound = upperBound;
        this.price = price;
        this.incrementStep = incrementStep;
        this.unitOfMeasure = unitOfMeasure;
        this.glid = glid;
        this.balanceElementNumCode = balanceElementNumCode;
        this.taxCode = taxCode;
    }
    
    public PriceTierRange(String upperBound, String balanceElementNumCode, String taxCode, Double price, String unitOfMeasure, Double incrementStep, String glid, Boolean isScaledCharge) {
    	this.upperBound = upperBound;
        this.price = price;
        this.incrementStep = incrementStep;
        this.unitOfMeasure = unitOfMeasure;
        this.glid = glid;
        this.balanceElementNumCode = balanceElementNumCode;
        this.taxCode = taxCode;
        this.isScaledCharge = isScaledCharge;
    }
    
    public PriceTierRange(String upperBound, String balanceElementNumCode, String taxCode, Double price, String unitOfMeasure, Double incrementStep, String glid, Boolean isScaledCharge, String group) {
    	this.upperBound = upperBound;
        this.price = price;
        this.incrementStep = incrementStep;
        this.unitOfMeasure = unitOfMeasure;
        this.glid = glid;
        this.balanceElementNumCode = balanceElementNumCode;
        this.taxCode = taxCode;
        this.isScaledCharge = isScaledCharge;
        this.group= group;
    }
    
    public Boolean isScaledCharge() {
    	return isScaledCharge;
    }

	public String getBalanceElementNumCode() {
		return balanceElementNumCode;
	}

	public void setBalanceElementNumCode(String balanceElementNumCode) {
		this.balanceElementNumCode = balanceElementNumCode;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getGlid() {
		return glid;
	}

	public void setGlid(String glid) {
		this.glid = glid;
	}

	public Double getIncrementStep() {
		return incrementStep;
	}

	public void setIncrementStep(Double incrementStep) {
		this.incrementStep = incrementStep;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public String getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(String upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

}
