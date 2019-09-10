/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cust;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Adrian Rocha
 */
public class PriceTier {

    protected String name;
    protected String rum;
    protected HashMap<String, List<PriceTierRange>> priceTierRanges;

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

    public boolean existePriceModelStepAnterior(String date) {
        boolean existe = false;
        for (String d : priceTierRanges.keySet()) {
			if (d.compareTo(date) < 0) {
				existe = true;
				break;
			}
		}
        return existe;
    }

    public boolean existePriceModelStepPosterior(String date) {
        boolean existe = false;
        for (String d : priceTierRanges.keySet()) {
			if (d.compareTo(date) > 0) {
				existe = true;
				break;
			}
		}
        return existe;
    }
    
	public PriceTier(String name, HashMap<String, List<PriceTierRange>> priceTierRanges, String rum) {
		super();
		this.name = name;
		this.priceTierRanges = priceTierRanges;
		this.rum = rum;
	}

	public HashMap<String, List<PriceTierRange>> getPriceTierRanges() {
		return priceTierRanges;
	}

	public void setPriceTierRanges(HashMap<String, List<PriceTierRange>> priceTierRanges) {
		this.priceTierRanges = priceTierRanges;
	}

	public String getRum() {
		return rum;
	}

	public void setRum(String rum) {
		this.rum = rum;
	}
}
