package cust;

import exceptions.ExceptionNotPermited;

/**
 *
 * @author Adrian Rocha
 */
public class Glid {

    protected String code;

    /**
     * Get the value of code
     *
     * @return the value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the value of code
     *
     * @param code new value of code
     */
    public void setCode(String code) {
        this.code = code;
    }
    protected String description;

    /**
     * Get the value of description
     *
     * @return the value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description
     *
     * @param description new value of description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public Glid(String newCode, String newDescription) throws ExceptionNotPermited {
    	if (!Utils.isNumeric(newCode)) {
    		throw new ExceptionNotPermited("C\u00F3digo GLID inv\u00E1lido!");
    	}
        this.code = newCode;
        this.description = newDescription;
    }
}
