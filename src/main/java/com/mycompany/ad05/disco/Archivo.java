
package com.mycompany.ad05.disco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Clase Archivo
 * 
 * @author Marcelino Álvarez García
 */
public class Archivo {
      
    /**
    * Lee el archivo
    *
    * @param nombreFichero Nombre del fichero
    * @return              Contenido del archivo
    */   
    public String leerArchivo(String nombreFichero){
        String entrada="";
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // Apertura del fichero y creacion de BufferedReader 
            fr = new FileReader (new File (nombreFichero));
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;
            while((linea=br.readLine())!=null)
              entrada+=linea;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{     
                // Cerrar fichero               
                if( null != fr )
                    fr.close();     
            }catch (Exception e2){ 
                e2.printStackTrace();
            }
        }   
        return entrada;
    }    

    /**
     * Guarda un archivo en el disco
     * 
     * @param f             Archivo a guardar, debe ser de la clase File
     * @param fileBytes     Contenido del archivo
     */
    public void guardarArchivo(File f, byte[] fileBytes){    
        try {
            FileOutputStream flujoDatos = new FileOutputStream(f);                     
            if(fileBytes != null){
                flujoDatos.write(fileBytes);
            }
            flujoDatos.close();                      
        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
        }    
    }    
    
}
