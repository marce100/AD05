
package com.mycompany.ad05;

import com.mycompany.ad05.notificaciones.Notifier;
import com.mycompany.ad05.notificaciones.Listener;
import com.mycompany.ad05.bd.Config;
import com.google.gson.Gson;
import com.mycompany.ad05.bd.BD;
import com.mycompany.ad05.disco.Archivo;
import com.mycompany.ad05.sincronizar.Sincronizar;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Marce
 */
public class AD05 {

    private static final String CONFIG = "config.json";

    /**
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
                        
        //Leer archivo de configuración
        Config cfg = new Gson().fromJson(new Archivo().leerArchivo(CONFIG), Config.class);

        //Instancia de la clase que se utiliza para subir y descargar archivos
        Sincronizar sinc = new Sincronizar(cfg.getDirectory());
        
        //Conectarse e inicializar la base de datos
        BD.createDatabase(cfg);
        Connection conn = BD.connectDatabase(cfg);
        BD.createTables(conn);
       
        //Subir archivos a la base de datos
        System.out.println("Subiendo ...");   
        sinc.uploadFiles(new File(sinc.getDirectorioRaiz()).listFiles(), conn);

        //Descargar archivos al disco
        System.out.println("Descargando ...");   
        sinc.downloadFiles(conn);
                                 
        //Funcion y trigger que crear una notificación cada vez que se añade un archivo
        BD.crearFuncion(conn);
        BD.crearTrigger(conn);
                                                        
        //Desconectar la base de datos
        BD.disconnectDatabase(conn);       
                       
        //Creamos dos conexiones distintas, una para el Notifier y otra pra el Listener
        Connection lConn = BD.connectDatabase(cfg);
        Connection nConn = BD.connectDatabase(cfg);

        //Creamos dos threads, uno para enviar notificaciones y otro para recibirlas
        try {
            Listener listener = new Listener(lConn,sinc);
            Notifier notifier = new Notifier(nConn,sinc);
            listener.start();
            notifier.start();     
        } catch (SQLException e) {
            System.err.println("Error: " + e.toString()); 
        }   

    }

}
