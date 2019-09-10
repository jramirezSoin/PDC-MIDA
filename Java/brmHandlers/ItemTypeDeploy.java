package brmHandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import log.Logger;

import org.apache.commons.io.IOUtils;

import config.Parameters;

/**
 *
 * @author Adrian Rocha
 */
public class ItemTypeDeploy {
	
	private static final String filePrefix = "class_item_ice_";
	private static final String fileSufix = ".podl";
	
	/**
	 * Crea el archivo podl para la nueva clase item e invoca al pin_deploy
	 * @param item Nuevo item type a crear
	 */
    public static void AddItemType(String item) {
        Logger.log(Logger.Debug, "Agregando nueva clase item type en BRM...");        
        String fileName = Parameters.PIN_DEPLOY_LOADER_PATH+filePrefix+item+fileSufix;
        Logger.log(Logger.Debug, "Creando archivo podl para pin_deploy");
        Logger.log(Logger.Debug, "Archivo: "+fileName);
        try
        {
        	FileWriter file = new FileWriter(fileName);
        	PrintWriter pw = new PrintWriter(file);
            pw.println("#=======================================");
            pw.println("# Storable Class /item/ice/"+item);
            pw.println("#=======================================");
            pw.println();
            pw.println("STORABLE CLASS /item/ice/"+item+" {");
            pw.println();
            pw.println("READ_ACCESS = \"Self\";");
            pw.println("WRITE_ACCESS = \"Self\";");
            pw.println("IS_PARTITIONED = \"1\";");
            pw.println("}");
            pw.println("#=======================================");
            pw.println("#  Storable Class /item/ice/"+item);
            pw.println("#=======================================");
            pw.println();
            pw.println("STORABLE CLASS /item/ice/"+item+" IMPLEMENTATION ORACLE7 {");
            pw.println();
            pw.println("}");
            pw.println();
            Logger.log(Logger.Debug, "Cerrando archivo temporal...");
            file.close();
            pinDeploy(new File(fileName));
            notifyNewItem(true);
        } catch (Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al escribir archivo: "+fileName);
            System.exit(Parameters.ERR_FILESYSTEM);
        }
    }
    
    private static void pinDeploy(File podlFile) {
    	Logger.log(Logger.Debug, "pinDeploy - Inicio");
        Process process;
        try {
            ProcessBuilder pb = new ProcessBuilder(Parameters.PIN_DEPLOY_LOADER_COMMAND, "replace", podlFile.getName());
            pb.directory(new File(Parameters.PIN_DEPLOY_LOADER_PATH));
            Logger.log(Logger.Debug, "Iniciando pin_deploy");
            process = pb.start();
            InputStream iop = process.getInputStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            InputStream ier = process.getErrorStream();
            BufferedInputStream bie = new BufferedInputStream(ier);
            process.waitFor();
            Logger.log(Logger.Debug, "pin_deploy ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            Logger.log(Logger.Debug, salida);
			if (!podlFile.renameTo(new File(Parameters.PIN_DEPLOY_LOADER_DONE_DIR+podlFile.getName()))) {
				Logger.log(Logger.Warning, "No se pudo mover el archivo generado al archivo de procesados para pin_deploy!");
			}
            if (process.exitValue() != 0) {
            	Logger.screen(Logger.Error, IOUtils.toString(bie, "UTF-8"));
            	Logger.screen(Logger.Error, "Error al ejecutar pin_deploy. Verifique el problema con soporte operativo de BRM.");
            	System.exit(Parameters.ERR_INTEGRATE);
            }
            else {
            	Logger.log(Logger.Debug, "Se verific\u00F3 conexi\u00F3n al CM!");
            }
        } catch (IOException|InterruptedException ioe) {
                Logger.screen(Logger.Error, ioe.toString());
                Logger.screen(Logger.Error, "Error al ejecutar naptest.sh!");
                Logger.screen(Logger.Error, "Saliendo");
                System.exit(Parameters.ERR_IOERROR);
        }
    }
    
    public static void validateCM() {
    	Logger.log(Logger.Debug, "validateCM - Inicio");
        Process process;
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "naptest.sh");
            pb.directory(new File(Parameters.PIN_DEPLOY_LOADER_PATH));
            Logger.log(Logger.Debug, "Iniciando naptest.sh");
            process = pb.start();
            InputStream iop = process.getInputStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            InputStream ier = process.getErrorStream();
            BufferedInputStream bie = new BufferedInputStream(ier);
            process.waitFor();
            Logger.log(Logger.Debug, "naptest.sh ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            Logger.log(Logger.Debug, salida);
            if (process.exitValue() != 0) {
            	Logger.screen(Logger.Error, IOUtils.toString(bie, "UTF-8"));
            	Logger.screen(Logger.Error, "No se pudo establecer la conexi\u00F3n al CM. Verifique el problema con soporte operativo de BRM.");
            	System.exit(Parameters.ERR_INTEGRATE);
            }
            else {
            	Logger.log(Logger.Debug, "Se verific\u00F3 conexi\u00F3n al CM!");
            }
        } catch (IOException|InterruptedException ioe) {
                Logger.screen(Logger.Error, ioe.toString());
                Logger.screen(Logger.Error, "Error al ejecutar naptest.sh!");
                Logger.screen(Logger.Error, "Saliendo");
                System.exit(Parameters.ERR_IOERROR);
        }
    }
 
    
    public static void notifyNewItem(boolean isNew) {
        //Notificación de reinicio de CMs
    	Process process;
    	ProcessBuilder pb;
        try {
        	if (isNew) {
            	pb = new ProcessBuilder("ksh", "notificacion_nuevo_item.sh");
        	}
        	else {
        		pb = new ProcessBuilder("ksh", "notificacion_cambio_item.sh");
        	}
            pb.directory(new File(System.getProperty("user.dir")));
            Logger.log(Logger.Debug, "Notificando el reinicio de los CMs");
            process = pb.start();
            InputStream iop = process.getInputStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            process.waitFor();
            Logger.log(Logger.Debug, "notificacion_nuevo_item.sh ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            Logger.log(Logger.Debug, salida);
            if (process.exitValue() != 0) {
                Logger.screen(Logger.Error, salida);
                Logger.screen(Logger.Error, "Ocurri\u00F3 un error al notificar a soporte operativo. El nuevo item type se ha creado pero es necesario que avise a Soporte Operativo que se requiere el reinicio de los CMs para su correcto funcionamiento.");
            }
        } catch (Exception ioe) {
            Logger.screen(Logger.Error, ioe.toString());
            Logger.screen(Logger.Error, "Ocurri\u00F3 un error con el proceso que debe notificar a soporte operativo. El nuevo item type se ha creado pero es necesario que avise a Soporte Operativo que se requiere el reinicio de los CMs para su correcto funcionamiento.!");
        }
    }

}
