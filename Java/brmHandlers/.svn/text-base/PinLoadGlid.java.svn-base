package brmHandlers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import log.Logger;
import config.Parameters;
import cust.DateFormater;
import cust.Glid;
import database.DBManager;

public class PinLoadGlid {
    
    private static final String glidFile = "ice_load_gild";
    private static final String glidBaseFile = "load_gild.tpl";
    private HashMap<String, String> glidHash;
    
    public PinLoadGlid(boolean loadIfwGlids) {
    	this.fillHashMap(DBManager.getConnectionPin(), loadIfwGlids);
    }
    
    public void addGlid(Glid glid) {
    	Logger.log(Logger.Debug, "Insertando GLID: "+glid.getCode()+" con descripcion: "+glid.getDescription());
    	glidHash.put(glid.getCode(), glid.getDescription());
    }
    
    public boolean modGlid(Glid glid) {
    	Logger.log(Logger.Debug, "Modificando GLID: "+glid.getCode()+" con descripcion: "+glid.getDescription());
    	return (glidHash.replace(glid.getCode(), glid.getDescription()) != null);
    }
    
    public boolean modGlidIfw(Glid glid) {
    	Logger.log(Logger.Debug, "Modificando GLID en IFW: "+glid.getCode()+" con descripcion: "+glid.getDescription());
    	Connection con = DBManager.getConnectionIfw();
    	try {
			Statement stmt = con.createStatement();
			stmt.execute("update ifw_glaccount set name = '"+glid.getDescription()+"' where glaccount = '"+glid.getCode()+"'");
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(Logger.Error, "Error al modificar GLID en IFW: "+glid.getCode()+" con descripcion: "+glid.getDescription());
			System.exit(Parameters.ERR_DATABASE);
		}
    	return true;
    }
    
    public void loadToDB() {
        Logger.log(Logger.Debug, "loadToDB - inicio");
        this.writeGlidFile();
        this.runGlidLoader();
        Logger.log(Logger.Debug, "loadToDB - fin");    	
    }
    
    private void fillHashMap(Connection pinConnection, boolean loadIfwGlids) {
        Logger.log(Logger.Debug, "Leyendo informacion de los GLIDs en pin DB");
        String id = "";
        try {
        	glidHash = new HashMap<String, String>();
            Statement stmt = pinConnection.createStatement();
            String query = "select to_char(rec_id) glid, descr from config_glid_t where rec_id > 10000000";
            if (loadIfwGlids) {
            	query += " union select glaccount, name from ifw_glaccount order by glid";
            }
            ResultSet resultado = stmt.executeQuery(query);
            while (resultado.next()) {
            	id = resultado.getString(1);
            	String s = resultado.getString(2);
                Logger.log(Logger.Debug, "GLID leido: "+id+", "+s);
            	glidHash.put(id, s);
            }
        } catch(Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al leer informacion de GLIDs en pin DB, ultimo leido: "+id+", Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
        }
        Logger.log(Logger.Debug, "Total de GLIDs leidos: "+glidHash.size());
    }
    
    private void writeGlidFile() {
        Logger.log(Logger.Debug, "Iniciando escritura de archivo de carga de GLIDs");
        String fileName = Parameters.GLID_LOADER_PATH+glidFile;
        String fileNameBase = Parameters.GLID_LOADER_PATH+glidBaseFile;
        FileWriter fichero = null;
        PrintWriter pw = null;
        FileReader base = null;
        BufferedReader reader = null;
        try
        {
            fichero = new FileWriter(fileName);
            pw = new PrintWriter(fichero);
            base = new FileReader(fileNameBase);
            reader = new BufferedReader(base);
            Logger.log(Logger.Debug, "Escribiendo archivo temporal...");
            String line;
            while ((line = reader.readLine()) != null) {
            	pw.println(line);
            }
            Logger.log(Logger.Debug, "Escribiendo custom glids...");
            for (Map.Entry<String, String> e: glidHash.entrySet()) {
            	pw.println();
				pw.println("#======================================================================");
            	pw.println("glid (");
				pw.println("\tid              "+e.getKey());
				pw.println("\tDESCR   "+e.getValue());
				pw.println(")");
				pw.println("#======================================================================");

			}
        } catch (Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al escribir archivo: "+fileName);
            System.exit(Parameters.ERR_FILESYSTEM);
        } finally {
           Logger.log(Logger.Debug, "Cerrando archivos...");
           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	                fichero.close();
	           } catch (Exception e) {
	                Logger.screen(Logger.Error, e.toString());
	                Logger.screen(Logger.Error, "Error al escribir archivo: "+fileName);
	                System.exit(Parameters.ERR_FILESYSTEM);
	        }
           try {
        	   if(null != base)
        		   base.close();
           } catch (Exception e) {
               Logger.screen(Logger.Warning, "Error al cerrar archivo template: "+fileNameBase);
           }
        }
    }
    
    private void runGlidLoader() {
        Logger.log(Logger.Debug, "Ejecutando Glid Loader...");
        Process process;
        try {
            ProcessBuilder pb = new ProcessBuilder(Parameters.GLID_LOADER_COMMAND, "-v", glidFile);
            pb.directory(new File(Parameters.GLID_LOADER_PATH));
            Logger.log(Logger.Debug, "Iniciando "+Parameters.GLID_LOADER_COMMAND);
            process = pb.start();
            InputStream iop = process.getInputStream();
            BufferedInputStream bif = new BufferedInputStream(iop);
            InputStream ier = process.getErrorStream();
            BufferedInputStream bie = new BufferedInputStream(ier);
            process.waitFor();
            Logger.log(Logger.Debug, "Loader ejecutado!");
            String salida = IOUtils.toString(bif, "UTF-8");
            Logger.log(Logger.Debug, salida);
            if (process.exitValue() != 0) {
            	Logger.screen(Logger.Error, IOUtils.toString(bie, "UTF-8"));
            }
        } catch (IOException ioe) {
                Logger.screen(Logger.Error, ioe.toString());
                Logger.screen(Logger.Error, "Error al ejecutar GLID Loader!");
                Logger.screen(Logger.Error, "Saliendo");
                System.exit(Parameters.ERR_IOERROR);
        } catch (InterruptedException e) {
            Logger.screen(Logger.Error, e.toString());
			Logger.screen(Logger.Error, "Ocurrio un error en la ejecucion del GLID loader "+Parameters.GLID_LOADER_COMMAND);
			System.exit(Parameters.ERR_IOERROR);
		}
        finally {
            try {
                Logger.log(Logger.Debug, "Moviendo archivo temporal al directorio de procesados...");
                ProcessBuilder pb = new ProcessBuilder("mv", glidFile, Parameters.GLID_LOADER_DONE_DIR+glidFile+DateFormater.dateToShortString());
                pb.directory(new File(Parameters.GLID_LOADER_PATH));
                process = pb.start();
                InputStream iop = process.getInputStream();
                BufferedInputStream bif = new BufferedInputStream(iop);
                process.waitFor();
                if (process.exitValue() != 0) {
	                Logger.log(Logger.Warning, "No se pudo mover el archivo a: "+Parameters.GLID_LOADER_DONE_DIR+glidFile+DateFormater.dateToShortString());
	                Logger.log(Logger.Warning, IOUtils.toString(bif, "UTF-8"));
	                iop = process.getErrorStream();
	                Logger.log(Logger.Warning, IOUtils.toString(new BufferedInputStream(iop), "UTF-8"));
                }
                else {
	                Logger.log(Logger.Debug, "Archivo movido a: "+Parameters.GLID_LOADER_DONE_DIR+glidFile+DateFormater.dateToShortString());
	                Logger.log(Logger.Debug, IOUtils.toString(bif, "UTF-8"));
                }
            } catch (IOException ioe) {
                Logger.screen(Logger.Error, ioe.toString());
                Logger.screen(Logger.Error, "Error al mover el archivo temporal al directorio: "+Parameters.GLID_LOADER_DONE_DIR);
            } catch (Exception e) {
                Logger.screen(Logger.Error, e.toString());
                Logger.screen(Logger.Error, "Error al mover el archivo temporal al directorio: "+Parameters.GLID_LOADER_DONE_DIR);
            }
        }
    }
    
    public static boolean existsGlid(String glid) {
        Logger.log(Logger.Debug, "Verificando GLID en PDC DB");
        boolean exists = false;
        try {
            Statement stmt = DBManager.getConnectionPin().createStatement();
            ResultSet resultado = stmt.executeQuery("select * from (select to_char(rec_id) glid, descr from config_glid_t where rec_id > 10000000 union select glaccount, name from ifw_glaccount) where glid = '"+glid+"'");
            while (resultado.next()) {
            	exists = resultado.getInt(1) > 0;
            }
        } catch(Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al leer informacion de GLIDs en DB: "+glid+", Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
        }
        return exists;
    }
    
    public static String getGlidDescription(String glid) {
        Logger.log(Logger.Debug, "getGlidDescription - Inicio");
        String description = "N/A";
        try {
            Statement stmt = DBManager.getConnectionPin().createStatement();
            ResultSet resultado = stmt.executeQuery("select descr from (select to_char(rec_id) glid, descr from config_glid_t where rec_id > 10000000 union select glaccount, name from ifw_glaccount) where glid = '"+glid+"'");
            while (resultado.next()) {
            	description = resultado.getString(1);
            }
        } catch(Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al leer informacion de GLIDs en BRM DB: "+glid+", Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
        }
        return description;
    }
    
    public static HashMap<String, String> getGlidDescriptions() {
        Logger.log(Logger.Debug, "getGlidDescriptions - Inicio");
        HashMap<String, String> descriptions = new HashMap<String, String>();
        try {
            Statement stmt = DBManager.getConnectionPin().createStatement();
            ResultSet resultado = stmt.executeQuery("select to_char(rec_id) glid, descr from config_glid_t where rec_id > 10000000 union select glaccount, name from ifw_glaccount order by glid");
            while (resultado.next()) {
            	descriptions.put(resultado.getString(1), resultado.getString(2));
            }
        } catch(Exception e) {
            Logger.screen(Logger.Error, e.toString());
            Logger.screen(Logger.Error, "Error al leer informacion de GLIDs en BRM DB: , Saliendo!");
            System.exit(Parameters.ERR_DATABASE);
        }
        return descriptions;
    }

}
