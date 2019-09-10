package cust;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import config.Parameters;
import log.Logger;

/**
 *
 * @author Adrian Rocha
 */
public class DateFormater {
    
    private static SimpleDateFormat formatoDeFechaHora = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static SimpleDateFormat formatoDeFechaArchivo = new SimpleDateFormat("yyyyMMddHHmmss");
    private static SimpleDateFormat formatoDeFechaCorta = new SimpleDateFormat("yyyyMMdd");
    //private static SimpleDateFormat formatoDeFechaReporte = new SimpleDateFormat("yyyy-MM-dd");
    
    public static Date stringToDate(String sDate, SimpleDateFormat format) {
        Date myDate = new Date();
        try {
        	format.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
            myDate = format.parse(sDate);//(sDate.substring(0, 9)+sDate.substring(11, 18));
            Logger.log(Logger.Debug, "Fecha parseada: "+myDate.toString());
        } catch (Exception e) {
            Logger.screen(Logger.Error, "Error al parsear fecha: "+sDate);
            Logger.screen(Logger.Error, "Saliendo...");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
        return myDate;
    }
    
    public static Date stringToDate(String sDate) {
    	if (sDate.length() != 8) {
    		Logger.screen(Logger.Error, "Fecha invalida: "+sDate);
    		System.exit(Parameters.ERR_INVALID_VALUE);
    	}
        return stringToDate(sDate, formatoDeFechaCorta);
    }
    
    public static String dateToString(Date date) {
        String mySDate = "";
        try {
        	formatoDeFechaHora.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
            //mySDate = formatoDeFecha.format(date)+"T"+formatoDeHora.format(date);
        	mySDate = formatoDeFechaHora.format(date);
        } catch (Exception e) {
            Logger.screen(Logger.Error, "Error al convertir fecha interna: "+date.toString());
            Logger.screen(Logger.Error, "Saliendo...");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
        return mySDate;
    }
    
    public static String dateToShortString(Date date) {
        String mySDate = "";
    	formatoDeFechaArchivo.setTimeZone(TimeZone.getTimeZone("GMT-06:00"));
        try {
            mySDate = formatoDeFechaArchivo.format(date);
        } catch (Exception e) {
            Logger.screen(Logger.Error, "Error al convertir fecha interna: "+date.toString());
            Logger.screen(Logger.Error, "Saliendo...");
            System.exit(Parameters.ERR_INVALID_VALUE);
        }
        return mySDate;
    }
    
    public static String sumDays(String date, int days) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(stringToDate(date, formatoDeFechaCorta));
    	c.add(Calendar.DATE, days);
    	return formatoDeFechaCorta.format(c.getTime());
    }
    
    public static String dateToShortString() {
        return dateToShortString(new Date());
    }
    
    public static String shortDateFormat(String d) {
    	String s = d;
    	try {
    		s = d.substring(6, 8)+"/"+d.substring(4, 6)+"/"+d.substring(0, 4);
    	}
    	catch (Exception e) {
    		Logger.screen(Logger.Error, "Error al convertir fecha: "+d);
    	}
    	return s;
    }
}
