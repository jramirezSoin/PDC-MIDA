package cust;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import log.Logger;

import org.apache.commons.io.IOUtils;


public abstract class Utils {
	
	public static boolean isNumeric(String cadena){
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	public static String fixedLenthString(String string, int length) {
	    //return String.format("%1$"+length+ "s", string);
		if (string.length() < length) {
			return String.format("%1$1s%2$"+(length-string.length())+"s", string, "");
		}
		else
			return string.substring(0, length);
	}
	
	public static String getServiceCode(String evtType, String rateType) {
		if (evtType.equals("SMS")) {
			return "SMS";
		}
		else if (rateType.equals("EVT")) {
			return "RESP";
		}
		else {
			return "TEL";
		}
	}
	
	public static void removeSpecialChar(String[] input) {
		for (int i = 0; i < input.length; i++) {
			removeSpecialChar(input[i]);
		}
	}
	
	public static void removeSpecialChar(String input) {
	    // Cadena de caracteres original a sustituir.
	    String original = "Ã¡Ã Ã¤Ã©Ã¨Ã«Ã­Ã¬Ã¯Ã³Ã²Ã¶ÃºÃ¹uÃ±Ã�Ã€Ã„Ã‰ÃˆÃ‹Ã�ÃŒÃ�Ã“Ã’Ã–ÃšÃ™ÃœÃ‘Ã±Ã§Ã‡";
	    // Cadena de caracteres ASCII que reemplazarÃ¡n los originales.
	    String ascii =    "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNncC";
	    //StringBuilder myStrBuilder = new StringBuilder(input);
	    for (int i=0; i < 35; i++) {
	        // Reemplazamos los caracteres especiales.
	        input = input.replace(original.charAt(i), ascii.charAt(i));
	    }
	}
	
    public static void notifyNewCampaign(String type, String campaign, String range, String charge, String glid, String name, Boolean applyNote) {
        Logger.log(Logger.Debug, "ItemTypes.notifyNewCampaign ("+type+", "+campaign+", "+range+", "+charge+", "+glid+", "+name+")");
        //NotificaciÃ³n de nueva campaÃ±a al ICE
        try {
        	String shell = "notificacion_nueva_campana.sh";
        	if(applyNote)
        		shell = "notificacion_nueva_campana_PRE.sh";
            ProcessBuilder pb = new ProcessBuilder("ksh", shell, type, campaign, range, charge, glid, name.replace("_", " "));
            pb.directory(new File(System.getProperty("user.dir")));
            Logger.log(Logger.Debug, "Notificando al ICE");
            Process process = pb.start();
            InputStream iop = process.getInputStream();
            InputStream ioe = process.getErrorStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            BufferedInputStream bie = new BufferedInputStream(ioe);
            process.waitFor();
            Logger.log(Logger.Debug, shell + " ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            String error = IOUtils.toString(bie, "UTF-8");
            Logger.log(Logger.Debug, "Output: "+salida);
            Logger.log(Logger.Debug, "Error: "+error);
            if (process.exitValue() != 0) {
                Logger.screen(Logger.Error, salida);
                Logger.screen(Logger.Error, "Ocurri\u00F3 un error al notificar a ICE. La campa\u00F1a se ha creado correctamente pero no se pudo enviar la notificaci\u00F3n por correo!");
            }
        } catch (Exception ioe) {
            Logger.screen(Logger.Error, ioe.toString());
            Logger.screen(Logger.Error, "Ocurri\u00F3 un error con el proceso que debe notificar a ICE. La campa\u00F1a se ha creado correctamente pero no se pudo enviar la notificaci\u00F3n por correo!");
        }
    }
}
