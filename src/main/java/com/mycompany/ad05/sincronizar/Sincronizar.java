
package com.mycompany.ad05.sincronizar;

import com.mycompany.ad05.bd.BD;
import com.mycompany.ad05.disco.Archivo;
import java.io.File;
import java.sql.Connection;
import java.util.List;

/**
 * Clase Sincronizar
 * 
 * @author Marcelino Álvarez García
 */
public class Sincronizar {
    
    private String directorioRaiz;

    /**
     * Constructor
     * 
     * @param directorioRaiz    Nombre del directorio raíz
     */
    public Sincronizar(String directorioRaiz) {
        this.directorioRaiz = directorioRaiz;
    }
    
    /**
     * Comprueba si tenemos un fichero o un directorio y lo sube a la base
     * de datos.
     * 
     * @param files             Objeto de la clase File
     * @param conn              Sesión con la base de datos
     */
    public void uploadFiles(File[] files, Connection conn) {        
        for (File file : files) {            
            if (file.isDirectory()) {
                System.out.println("DIR:  "+file.getAbsolutePath());            
                BD.insertarDirectorio(conn, file, getDirectorioRaiz());                
                uploadFiles(file.listFiles(), conn); 
            }else{   
                System.out.println("FILE: "+file.getAbsolutePath());
                BD.insertarArchivo(conn, file, getDirectorioRaiz());
            }
        }
    }
    
    /**
     * Comprueba si todos los directorios y todos los archivos que hay en la
     * base de datos existen en el disco. Si no es así los crea.
     * 
     * @param conn              Sesión con la base de datos
     */
    public void downloadFiles(Connection conn){
        
        //Para todos los directorios
        List<String> directorios=BD.getDirectorios(conn);
        for (String directorio : directorios){
            System.out.print("DIR:  "+getDirectorioRaiz()+directorio.substring(1));  

            File d = new File(getDirectorioRaiz()+directorio.substring(1)); 
            if (d.exists()) {
                System.out.println(" --> Existe"); 
            }else{
                System.out.println(" --> No existe, se crea el directorio. ");                    
                d.mkdirs();                
            }            
        }        
         
        //Para todos los archivos
        List<String> archivos=BD.getArchivos(conn);
        for (String archivo : archivos){
            System.out.print("FILE: "+getDirectorioRaiz()+archivo.substring(1));

            File f = new File(getDirectorioRaiz()+archivo.substring(1)); 
            if (f.exists()) {
                System.out.println(" --> Existe"); 
            }else{
                System.out.println(" --> No existe, se descarga el archivo. ");                         
                byte[] fileBytes = BD.selectArchivo(conn, archivo);                
                new Archivo().guardarArchivo(f, fileBytes);   
            }
        }
    }
    
    /**
     * Comprueba si un archivos que hay en la base de datos existe en el disco. 
     * Si no es así lo crea.
     * 
     * @param idArchivo         Identificador del archivo
     * @param conn              Sesión con la base de datos
     */
    public void downloadFileById(Connection conn, long idArchivo){ 
        
            File f = new File(getDirectorioRaiz()+BD.getArchivoById(conn, idArchivo).substring(1));
            System.out.print(f.getAbsolutePath());
            if (f.exists()) {
                System.out.println(" --> Existe"); 
            }else{
                System.out.println(" --> No existe, se descarga el archivo. ");                         
                byte[] fileBytes = BD.selectArchivoById(conn, idArchivo);                
                new Archivo().guardarArchivo(f, fileBytes);   
            }
    }    

    /*
     * Getters
     * 
     */
    
    public String getDirectorioRaiz() {
        return directorioRaiz;
    }    
    
}
