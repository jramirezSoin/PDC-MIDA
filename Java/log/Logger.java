package log;

import com.portal.pcm.DefaultLog;
import com.portal.pcm.EBufException;
import com.portal.pcm.ErrorLog;
import config.Parameters;

/**
 *
 * @author Adrian Rocha
 */
public class Logger {
    
    	static {
                try {
                    Logger.setPath(Parameters.LOG_PATH);
                    Logger.setFlags(Parameters.LOG_LEVEL);
                } catch (Exception e) {
                    System.out.println("No se pudo leer informaci\u00F3n del logueo en Infranet.properties!!!");
                    System.out.println("Saliendo");
                    System.exit(1);
                }
	}
	
	private static boolean hasError = false;

	public static int Error = ErrorLog.Error;
	
	public static int Warning = ErrorLog.Warning;
	
	public static int Debug = ErrorLog.Debug;

	public static boolean hasError() {
		return hasError;
	}

	public static void log(Object source, int errorType, String msg) {		
		if (errorType == Logger.Error)
			hasError = true;
		DefaultLog.log(source, errorType, msg);
	}

	public static void log(Object source, int errorType, EBufException ebuf) {
		if (errorType == Logger.Error)
			hasError = true;
		DefaultLog.log(source, errorType, ebuf);
	}

	public static void log(int errorType, String msg) {
		if (errorType == Logger.Error)
			hasError = true;
		DefaultLog.log(errorType, msg);
	}

	public static void screen(int errorType, String msg) {
		log(errorType, msg);
        System.out.println(msg);
	}
	

	public static void onlyScreen(String msg) {
        System.out.println(msg);
    }

	public static void screenOnly(int errorType, String msg) {
        System.out.println(msg);
	}

	public static void setName(String name) {
		DefaultLog.setName(name);
	}

	public static void setPath(String path) {
		DefaultLog.setPath(path);
	}

	public static void setFlags(int logLevel) {
		switch (logLevel) {
		case 1:
			DefaultLog.setFlags(ErrorLog.Error);
			break;
		case 2:
			DefaultLog.setFlags(ErrorLog.Warning);
			break;
		case 3:
			DefaultLog.setFlags(ErrorLog.All);
			break;
		default:
			DefaultLog.setFlags(ErrorLog.Error);
		}
	}

}
