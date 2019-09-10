package exceptions;

public class ExceptionItemType extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5947526092816256542L;
	private String mensaje;
 
    public ExceptionItemType(String msg) {
        mensaje = msg;
    }
        
    @Override
    public String toString() {
        return mensaje;
    }
}
