
package com.mycompany.ad05.notificaciones;

import com.mycompany.ad05.sincronizar.Sincronizar;
import java.io.File;
import java.sql.Connection;

/**
 * Clase Notifier
 * 
 * @author Marcelino Álvarez García
 */
public class Notifier extends Thread{
    private Connection conn;
    private Sincronizar sinc;

    /**
     * Constructor 
     * 
     * @param conn              Sesión con la base de datos
     * @param sinc              Instancia de la clase Sincronizar
     */
    public Notifier(Connection conn, Sincronizar sinc)    {
        this.conn = conn;
        this.sinc = sinc;
    }

    @Override
    public void run(){
        while (true){
            try{        
                //Cada 20 segundos
                Thread.sleep(20000);
                
                //comprobar si hay archivos nuevos
                System.out.println("Subiendo ...");   
                sinc.uploadFiles(new File(sinc.getDirectorioRaiz()).listFiles(), conn);
            } catch (Exception e) {
                System.err.println("Error: " + e.toString());
            } 
        }
    }

}
