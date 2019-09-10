package cust;

public class DetailPrint implements Comparable<DetailPrint>{

	String priceModel,  validFrom, runName, glid, descGlid, price, tax;
	
	public DetailPrint(String priceModel,  String validFrom, String runName, String price, String tax, String glid, String descGlid) {
		this.priceModel = priceModel;
		this.validFrom = validFrom;
		this.runName = runName;
		this.glid = glid;
		this.descGlid = descGlid;
		this.price = price;
		this.tax = tax;
	}

	/**
	 * @return the priceModel
	 */
	public String getPriceModel() {
		return priceModel;
	}

	/**
	 * @return the validFrom
	 */
	public String getValidFrom() {
		return validFrom;
	}

	/**
	 * @return the runName
	 */
	public String getRunName() {
		return runName;
	}

	/**
	 * @return the glid
	 */
	public String getGlid() {
		return glid;
	}

	/**
	 * @return the descGlid
	 */
	public String getDescGlid() {
		return descGlid;
	}

	/**
	 * @return the price
	 */
	public String getPrice() {
		return price;
	}

	/**
	 * @return the tax
	 */
	public String getTax() {
		return tax;
	}

	@Override
	public int compareTo(DetailPrint detailPrint) {
		int v = validFrom.compareTo(detailPrint.getValidFrom());
		if (v == 0)
			v = priceModel.compareTo(detailPrint.getPriceModel());
		return v;
	}
	
}
