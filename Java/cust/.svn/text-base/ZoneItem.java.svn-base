package cust;

/**
 *
 * @author Adrian Rocha
 */
public class ZoneItem implements Comparable<ZoneItem> {

    protected String destinationPrefix;
    protected String zoneName;
    protected String originPrefix = "00";
    protected String productName;
    protected String validFrom;
    protected String validTo;

    /**
     * Get the value of validTo
     *
     * @return the value of validTo
     */
    public String getValidTo() {
        return validTo;
    }

    /**
     * Set the value of validTo
     *
     * @param validTo new value of validTo
     */
    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    /**
     * Get the value of validFrom
     *
     * @return the value of validFrom
     */
    public String getValidFrom() {
        return validFrom;
    }

    /**
     * Set the value of validFrom
     *
     * @param validFrom new value of validFrom
     */
    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Get the value of serviceCode
     *
     * @return the value of serviceCode
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Set the value of serviceCode
     *
     * @param productName new value of serviceCode
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Get the value of originAreaCode
     *
     * @return the value of originAreaCode
     */
    public String getOriginPrefix() {
        return originPrefix;
    }

    /**
     * Set the value of originAreaCode
     *
     * @param originPrefix new value of originPrefix
     */
    public void setOriginPrefix(String originPrefix) {
        this.originPrefix = originPrefix;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return zoneName;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.zoneName = name;
    }

    /**
     * Get the value of destinAreaCode
     *
     * @return the value of destinAreaCode
     */
    public String getDestinationPrefix() {
        return destinationPrefix;
    }

    /**
     * Set the value of destinationPrefix
     *
     * @param destinationPrefix new value of destinationPrefix
     */
    public void setDestinationPrefix(String destinationPrefix) {
        this.destinationPrefix = destinationPrefix;
    }

	public ZoneItem(String destinationPrefix, String name, String productName, String validFrom, String validTo) {
		super();
		this.destinationPrefix = destinationPrefix;
		this.zoneName = name;
		this.productName = productName;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public int compareTo(ZoneItem arg0) {
		int v = validFrom.compareTo(arg0.getValidFrom());
		if (v == 0) {
			v = destinationPrefix.compareTo(arg0.getDestinationPrefix());
		}
		return v;
	}
    
}
