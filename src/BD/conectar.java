/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package BD;

import javax.swing.JOptionPane;
import java.sql.*;
/**
 *
 * @author Elr0d
 */
public class conectar {
    Connection con=null;
    public Connection conexion (){
        try{
        //cargar nuestro driver
        String url = "jdbc:mysql://localhost:3306/unab";
        String user = "root";
        String pass = "root";
                
        con = DriverManager.getConnection(url, user, pass);
    }
        catch (Exception e) {
            System.out.println("Error de conexion");
            JOptionPane.showMessageDialog(null, "Error de Conexion: " + e);
        }
        return con;
    }
    
    public Connection cerrarConexion(){
        try{
            con.close();
            System.out.println("Cerrando Conexion");
        }
        catch (SQLException ex) {
            System.out.println(ex);
        }
        con = null;
        return con;
    }
}
            

