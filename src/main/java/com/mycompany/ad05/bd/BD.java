
package com.mycompany.ad05.bd;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Clase BD
 * 
 * En esta clase se utilizan 3 tipos de consultas; Statement, PreparedStatement o CallableStatement.
 * Para más información consultar: 
 * https://javaconceptoftheday.com/statement-vs-preparedstatement-vs-callablestatement-in-java 
 * 
 * @author Marcelino Álvarez García
 */
public class BD {

    /**
     * Conecta con la base de datos
     * 
     * @param cfg           Parámetros de configuración
     * @return              Sesión con la base de datos
     */
    public static Connection connectDatabase(Config cfg){
        Connection conn = null;
        
        Properties p = new Properties();
        p.setProperty("user", cfg.getUser());
        p.setProperty("password", cfg.getPassword());                              
        String url = "jdbc:postgresql://"+cfg.getAddress()+"/"+cfg.getName();
       
        try {
            conn = DriverManager.getConnection(url,p);
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());                                                                                  
        }    
        
        return conn;
    } 
    
    /**
     * Crea una nueva base de datos
     * Para conectarse se sive de la base de datos por defecto (postgres)
     * 
     * @param cfg           Parámetros de configuración
     */    
    public static void createDatabase(Config cfg){
        Properties p = new Properties();
        p.setProperty("user", cfg.getUser());
        p.setProperty("password", cfg.getPassword());                              
        String url = "jdbc:postgresql://"+cfg.getAddress()+"/postgres";
                        
        try {     
            Connection conn = DriverManager.getConnection(url,p);
            Statement s = conn.createStatement();
            s.execute("CREATE DATABASE "+cfg.getName());
        } catch (SQLException e) {
            //Si la base de datos ya existe no se crea
        }                  
    }
           
    /**
     * Desconecta la base de datos
     *
     * @param conn          Sesión con la base de datos
     */
    public static void disconnectDatabase(Connection conn){
        try{            
            conn.close();            
        }catch(SQLException e){
            System.err.println("Error: " + e.toString());
        }  
    }     

    /**
     * Crea las tablas que necesita la aplicación y añade el directorio raíz.
     * 
     * @param conn          Sesión con la base de datos
     */
    public static void createTables(Connection conn){
        //Si las tablas ya existen no se crean
        String sql = 
            "CREATE TABLE IF NOT EXISTS directorio ("
                + "idDirectorio serial PRIMARY KEY, "
                + "nombreDirectorio text NOT NULL);"+
            "CREATE TABLE IF NOT EXISTS archivo ("
                + "idArchivo serial PRIMARY KEY, "
                + "nombreArchivo text NOT NULL, "
                + "archivoBinario bytea NOT NULL, "
                + "idDirectorio serial NOT NULL);"+
            "INSERT INTO directorio(nombreDirectorio) SELECT '.' "+
            "WHERE NOT EXISTS (SELECT 1 FROM directorio WHERE nombreDirectorio='.');";
        try{
            conn.createStatement().execute(sql);            
        }catch(SQLException e){
            System.err.println("Error: " + e.toString());
        } 
    }
    
    /**
     * Añade un directorio a la base de datos
     * 
     * @param conn          Sesión con la base de datos
     * @param file          Directorio a añadir, debe ser de la case File
     * @param dirRaiz       Directorio raíz
     */
    public static void insertarDirectorio(Connection conn, File file, String dirRaiz){   
        //El directorio raiz se almacena en la base de datos como "."
        String nombreDirectorio=file.getAbsolutePath().replace(dirRaiz, ".");
        
        //Si el directorio existe no se vuelve a guardar
        String sql =
            "INSERT INTO directorio (nombreDirectorio) SELECT ?"+
            "WHERE NOT EXISTS (SELECT 1 FROM directorio WHERE nombreDirectorio=?);";        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombreDirectorio);   
            ps.setString(2, nombreDirectorio);   
            ps.executeUpdate();               
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }      
    }
    
    /**
     *
     * @param conn          Sesión con la base de datos
     * @param file          Archivo a añadir, debe ser de la clase File
     * @param dirRaiz       Directorio raíz
     */
    public static void insertarArchivo(Connection conn, File file, String dirRaiz){    
        //El directorio raiz se almacena en la base de datos como "."
        String nombreDirectorio = file.getAbsolutePath().replace(dirRaiz, ".").replace("\\"+file.getName(), "");
                
        //Si el archivo existe no se vuelve a guardar
        String sql =
            "INSERT INTO archivo (nombreArchivo,archivoBinario,idDirectorio) "
                + "SELECT ?,?,(SELECT idDirectorio FROM directorio WHERE nombreDirectorio  = ?) "
                + "WHERE NOT EXISTS (SELECT 1 FROM archivo WHERE nombreArchivo=? AND idDirectorio="
                + "(SELECT idDirectorio FROM directorio WHERE nombreDirectorio  = ?));";        
        try {
            FileInputStream fis = new FileInputStream(file);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, file.getName());
            ps.setBinaryStream(2, fis, (int)file.length());
            ps.setString(3, nombreDirectorio);
            ps.setString(4, file.getName());
            ps.setString(5, nombreDirectorio);
            ps.executeUpdate();
            ps.close();
            fis.close();                          
        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
        }      
    }
         
    /**
     * Descarga un archivo de tipo binario
     * 
     * @param conn          Sesión con la base de datos
     * @param nombreArchivo Nombre del archivo
     * @return              Array de bytes
     */
    public static byte[] selectArchivo(Connection conn, String nombreArchivo){    
        byte[] fileBytes = null;                
        String auxArchivo=nombreArchivo.substring(nombreArchivo.lastIndexOf("\\")+1);
        String auxDirectorio=nombreArchivo.substring(0,nombreArchivo.lastIndexOf("\\"));
        
        String sql = new String(
            "SELECT archivoBinario FROM archivo WHERE nombreArchivo = ? AND "
                + "idDirectorio = (SELECT idDirectorio FROM directorio WHERE nombreDirectorio=?);");                
        try {
            PreparedStatement ps2 = conn.prepareStatement(sql); 
            ps2.setString(1, auxArchivo);
            ps2.setString(2, auxDirectorio);            
            ResultSet rs = ps2.executeQuery();                       
            while (rs.next()) { 
               fileBytes = rs.getBytes(1); 
            }            
            rs.close(); 
            ps2.close();           
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }
        return fileBytes;
    }

    /**
     * Devuelve una lista con todos los archivos
     * 
     * @param conn          Sesión con la base de datos
     * @return              Array con la lista de archivos
     */
    public static List<String> getArchivos(Connection conn){
        List<String> archivos=new ArrayList();        
        String sql = 
            "SELECT CONCAT(nombreDirectorio,'\\',nombreArchivo) "
                + "FROM directorio, archivo "
                + "WHERE archivo.idDirectorio = directorio.idDirectorio ; ";                     
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){                
                archivos.add(rs.getString(1));
            }
            rs.close();
            st.close();             
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }               
        return archivos;
    }
    
    /**
     * Devuelve una lista con todos los directorios
     * 
     * @param conn          Sesión con la base de datos
     * @return              Array con la lista de directorios
     */
    public static List<String> getDirectorios(Connection conn){
        List<String> directorios=new ArrayList();        
        String sql = 
            "SELECT nombreDirectorio "
                + "FROM directorio ; ";                     
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){                
                directorios.add(rs.getString(1));
            }
            rs.close();
            st.close();             
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }               
        return directorios;
    }
           
    /**
     * Crea una función que notificará cuando se añade un nuevo archivo.
     * La función pasará como parámetro el identificador del archivo.
     * 
     * Nota: Al intentar ver las funciones a traves del gestor web de la base de datos
     * muestra el mensaje de error: "column p.proisagg does not exist". Lo solucionamos
     * añadiendo desde consola:
     * 
     * sed -i "s/NOT pp.proisagg/pp.prokind='f'/g" /usr/share/phppgadmin/classes/database/Postgres.php
     * sed -i "s/NOT p.proisagg/p.prokind='f'/g" /usr/share/phppgadmin/classes/database/Postgres.php   
     * 
     * @param conn          Sesión con la base de datos
     */      
    public static void crearFuncion(Connection conn){
        //Creamos a función que notificará que se engadiu unha nova mensaxe
        String sql = new String(
            "CREATE OR REPLACE FUNCTION notificar_mensaxe() "+
            "RETURNS trigger AS $$ "+
            "BEGIN " +
                "PERFORM pg_notify('novamensaxe',NEW.idArchivo::text); "+
            "RETURN NEW; "+
            "END; "+
            "$$ LANGUAGE plpgsql; ");
        try {                   
            CallableStatement createFunction = conn.prepareCall(sql);
            createFunction.execute();
            createFunction.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }               
  
    }
    
    /**
     * Crea el trigger que lanza la función de notificación
     * 
     * @param conn          Sesión con la base de datos
     */
    public static void crearTrigger(Connection conn){
        //Creamos o triguer que se executa tras unha nova mensaxe
        String sql = new String(
            "DROP TRIGGER IF EXISTS not_nova_mensaxe ON archivo; "+
            "CREATE TRIGGER not_nova_mensaxe "+
            "AFTER INSERT "+
            "ON archivo "+
            "FOR EACH ROW "+
            "EXECUTE PROCEDURE notificar_mensaxe(); ");
        try {            
            CallableStatement createTrigger = conn.prepareCall(sql);
            createTrigger.execute();
            createTrigger.close();   
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }               
    }
    
    /**
     * Descarga un archivo de tipo binario
     * 
     * @param conn          Sesión con la base de datos
     * @param idArchivo     Identificador del archivo
     * @return              Array de bytes
     */ 
    public static byte[] selectArchivoById(Connection conn, long idArchivo){    
        byte[] fileBytes = null;                
        
        String sql = new String(
            "SELECT archivoBinario FROM archivo WHERE idArchivo = ? ;");                
        try {
            PreparedStatement ps2 = conn.prepareStatement(sql); 
            ps2.setLong(1, idArchivo);          
            ResultSet rs = ps2.executeQuery();                       
            while (rs.next()) { 
               fileBytes = rs.getBytes(1); 
            }            
            rs.close(); 
            ps2.close();           
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }
        return fileBytes;
    }

    /**
     * Devuelve el nombre de un archivo 
     * 
     * @param conn          Sesión con la base de datos
     * @param idArchivo     Identificador del archivo
     * @return              Nombre del archivo
     */
    public static String getArchivoById(Connection conn, long idArchivo){
        String archivo="";
        String sql = 
            "SELECT CONCAT(nombreDirectorio,'\\',nombreArchivo) "
                + "FROM directorio, archivo "
                + "WHERE archivo.idDirectorio = directorio.idDirectorio AND "
                + "archivo.idArchivo = ? ;";    
        try {
            PreparedStatement ps2 = conn.prepareStatement(sql); 
            ps2.setLong(1, idArchivo);          
            ResultSet rs = ps2.executeQuery();                       
            while (rs.next()) { 
               archivo = rs.getString(1);
            }            
            rs.close(); 
            ps2.close();             
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString());
        }               
        return archivo;
    }    
  
}
