
package com.mycompany.ad05.bd;

/**
 * Clase Config
 * 
 * @author Marcelino Álvarez García
 */
public class Config {
    
    private DbConnection dbConnection;
    private App app;    
        
    /**
     * Clase DbConnection
     */
    public class DbConnection{
        String address;
        String name;
        String user;
        String password;
    }
    
    /**
     * Clase App
     */
    public class App{
        String directory;
    }
    
    /**
     * Getters
     */
       
    public String getAddress(){      return dbConnection.address; }
    public String getName(){         return dbConnection.name; }
    public String getUser(){         return dbConnection.user; }
    public String getPassword(){     return dbConnection.password; }
    public String getDirectory(){    return app.directory; }

}
