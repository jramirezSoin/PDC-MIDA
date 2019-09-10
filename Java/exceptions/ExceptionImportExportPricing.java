package exceptions;

public class ExceptionImportExportPricing extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5947526092816256541L;
	private String mensaje;
 
    public ExceptionImportExportPricing(String msg) {
        mensaje = msg;
    }
        
    @Override
    public String toString() {
        return mensaje;
    }
}
