
package com.mycompany.ad05.notificaciones;

import com.mycompany.ad05.sincronizar.Sincronizar;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase Listener
 * 
 * @author Marcelino Álvarez García
 */
public class Listener extends Thread{
    
    private Connection conn;
    private org.postgresql.PGConnection pgconn;
    private Sincronizar sinc;

    /**
     * Constructor 
     * 
     * @param conn              Sesión con la base de datos
     * @param sinc              Instancia de la clase Sincronizar
     * @throws SQLException
     */
    public Listener(Connection conn, Sincronizar sinc) throws SQLException{
        this.conn = conn;
        this.pgconn = conn.unwrap(org.postgresql.PGConnection.class);
        this.sinc = sinc;
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN novamensaxe");
        stmt.close();
    }

    @Override
    public void run(){
        try{
            while (true){
                org.postgresql.PGNotification notifications[] = pgconn.getNotifications();

                //Si hay notificaciones
                if (notifications != null){
                    System.out.println("---------------------------------------");
                    for (int i=0; i < notifications.length; i++){                        
                        System.out.print("NOTF: "); 
                        
                        //Descargamos el archivo 
                        sinc.downloadFileById(conn, Long.parseLong(notifications[i].getParameter()));                    
                    }
                    System.out.println("---------------------------------------");                    
                }

                // Espera por nuevas notificaciones
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
        } 
    }
}
