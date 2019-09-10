/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import config.Parameters;
/**
 *
 * @author Adrian Rocha
 */
public class DBManager {
	
    private static Connection connectionPin=null;
    private static Connection connectionIfw=null;

    public static void createConnection() {
        try{
        	Class.forName(Parameters.DB_PIN_CLASSFORNAME);
            //connectionPin = DatabaseConnection.createOracleConnection(Parameters.DB_PIN_USERNAME,Parameters.DB_PIN_USERPASSWD,Parameters.DB_PIN_URL.split(":")[5]);
            //connectionPdc = DatabaseConnection.createOracleConnection(Parameters.DB_PDC_USERNAME,Parameters.DB_PDC_USERPASSWD,Parameters.DB_PIN_URL.split(":")[5]);
            connectionPin = DriverManager.getConnection(Parameters.DB_PIN_URL, Parameters.DB_PIN_USERNAME, Parameters.DB_PIN_USERPASSWD);
            connectionIfw = DriverManager.getConnection(Parameters.DB_PIN_URL, Parameters.DB_IFW_USERNAME, Parameters.DB_IFW_USERPASSWD);
        }catch(ClassNotFoundException cnfe){
            System.out.println("Database Driver Not found");
            System.exit(Parameters.ERR_DATABASE);
        }catch(SQLException sqle){
            System.out.println("SQL Exception al crear conectar");
            System.out.println(sqle.getMessage());
            System.exit(Parameters.ERR_DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Parameters.ERR_DATABASE);
        }
    }

    public static Connection getConnectionPin() {
        try {
            if (connectionPin==null||connectionPin.isClosed()){
                createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(Parameters.ERR_DATABASE);
        }
        
        return connectionPin;
    }

    public static Connection getConnectionIfw() {
        try {
            if (connectionPin==null||connectionPin.isClosed()){
                createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(Parameters.ERR_DATABASE);
        }
        
        return connectionIfw;
    }
    
    public static void closeConnections() {
        try {
            if (connectionPin!=null&&!connectionPin.isClosed()){
                connectionPin.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try {
            if (connectionIfw!=null&&!connectionIfw.isClosed()){
                connectionIfw.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }	

}
