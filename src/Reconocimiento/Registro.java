/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Reconocimiento;

import BD.conectar;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 *
 * @author joshua
 */
public class Registro extends javax.swing.JFrame {
    
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet featuresverificacion;
    Connection conectarbd;
    
    Connection desconectarbd;
    CallableStatement cst;
    ResultSet resultado;

    conectar con = new conectar();
    
    public void EstadoHuellas(){
        EnviarTexto("Muestra de huellas Necesarias para Guardar Template " + Reclutador.getFeaturesNeeded());
    }
    public void EnviarTexto(String string){
        EnviarTexto.append(string + "\n");
    }
    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
    DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try{
            return extractor.createFeatureSet(sample,purpose);
        }catch (DPFPImageQualityException e){
            return null;
        }
    }
    public Image CrearImagenHuella(DPFPSample sample){
    return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    
    //otroImage
    public void DibujarHuella(Image image){
        lblImagenHuella.setIcon(new ImageIcon(image.getScaledInstance(lblImagenHuella.getWidth(), lblImagenHuella.getHeight(),Image.SCALE_DEFAULT)));
        repaint();
    }
    public void setTemplate(DPFPTemplate template){
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }
    public void stop(){
        Lector.stopCapture();
        EnviarTexto("No se esta usabdo el lector de huella dactilar");
    }
    public void start(){
        Lector.startCapture();
        EnviarTexto("Utilizando el lector de huella dactilar");
    }
    protected void Iniciar(){
        Lector.addDataListener(new  DPFPDataAdapter(){
        @Override
        public void dataAcquired(final DPFPDataEvent e){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    EnviarTexto("Huella Digital ha sido capturada");
                    ProcesarCaptura(e.getSample());
                }
            });
          }     
       });
       Lector.addReaderStatusListener(new DPFPReaderStatusAdapter(){
       @Override
       public void readerConnected (final DPFPReaderStatusEvent e){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    EnviarTexto("El sensor de Huella Digital esta activado o Conectado");
                }
            });
        }       
       @Override
        public void readerDisconnected (final DPFPReaderStatusEvent e){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    EnviarTexto("El sensor de Huella Digital esta activado o Conectado");
                }
            });
          }
       });
       Lector.addSensorListener(new DPFPSensorAdapter(){
       @Override
       public void fingerTouched(final DPFPSensorEvent e){
           SwingUtilities.invokeLater(new Runnable(){
           public void run(){
               EnviarTexto("El dedo ha sido colocado sobre el lector de Huella");
             }
           });
       }
       @Override
       public void fingerGone(final DPFPSensorEvent e){
           SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                   EnviarTexto("El dedo ha sido quitado del Lector de Huella");
                }
           });
          }
        });
       Lector.addErrorListener(new DPFPErrorAdapter(){
        public void errorReader(final DPFPErrorEvent e){
               SwingUtilities.invokeLater(new Runnable(){
                   public void run(){
                       EnviarTexto("Error: " + e.getError());
                   }
               });
           }
       });
    }
    public void ProcesarCaptura(DPFPSample sample){
    
    featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
    
    featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
    
    if(featuresinscripcion !=null)
   try{
        System.out.println("Las caracteristicas de la huella han sido creadas");
        Reclutador.addFeatures(featuresinscripcion);//Agregar las caracteristicas de la huella digital
        
        //dibuja la huella dactilar capturada.
        Image image = CrearImagenHuella(sample);
        DibujarHuella(image);
      
   }catch (DPFPImageQualityException ex){
       System.out.println("Error: " + ex.getMessage());
   }finally{
       EstadoHuellas();
       //Comprueba si la plantilla se ha creado.
       switch (Reclutador.getTemplateStatus()){
           case TEMPLATE_STATUS_READY: //informe de exito y detine la captura de huellas
               stop();
               setTemplate(Reclutador.getTemplate());
               EnviarTexto("La plantilla de la huella ha sido creada, ya puedde verificar o identificarla");
                       
               guardarbt.setEnabled(true);
               guardarbt.grabFocus();
               break;
           case TEMPLATE_STATUS_FAILED: //informe de fallas y reiniciar la captura de huellas
               Reclutador.clear();
               stop();
               EstadoHuellas();
               setTemplate(null);
               JOptionPane.showMessageDialog(Registro.this,"La plantilla de la huella no pudo ser creada, repita el Proceso", "Incripcion de huella dactilar", JOptionPane.ERROR_MESSAGE);
               start();
               break;
               
       }
   }
    }
    public boolean guardarHuella(){
    conectarbd = con.conexion();
        ByteArrayInputStream datoHuella = new ByteArrayInputStream(template.serialize());
        Integer tamañoHuella = template.serialize().length;
        
        String nombre = JOptionPane.showInputDialog("Nombre: ");
        try{
        //Establece los valores para las setencias SQL
        Connection c = con.conexion();
        PreparedStatement guardarStmt = c.prepareStatement("INSERT INTO admin(NombrePersona, huella) values(?,?)");
        guardarStmt.setString(1, nombre);
        guardarStmt.setBinaryStream(2, datoHuella,tamañoHuella);
        
        JOptionPane.showMessageDialog(null, "Huella guardada Correctamente");
        
            conectarbd.setAutoCommit(false);
            guardarStmt.executeUpdate();
            conectarbd.commit();
            
            return true;
            
        }catch(Exception ex){
        JOptionPane.showMessageDialog(null, ex.toString());
        }
        return false;
    }

    
        
        
        
     
   
    /**
     * Creates new form Registro
     */
    public Registro() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        guardarbt = new javax.swing.JButton();
        lblImagenHuella = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        EnviarTexto = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setText("Huella");

        guardarbt.setText("Guardar");
        guardarbt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                guardarbtMouseClicked(evt);
            }
        });

        EnviarTexto.setColumns(20);
        EnviarTexto.setRows(5);
        jScrollPane1.setViewportView(EnviarTexto);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 328, Short.MAX_VALUE)
                        .addComponent(guardarbt)
                        .addGap(109, 109, 109))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(guardarbt)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(12, 12, 12)
                .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        Iniciar();
        start();
        EstadoHuellas();
        //guardarbt.setEnabled(false);
    }//GEN-LAST:event_formWindowOpened

    private void guardarbtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_guardarbtMouseClicked
        // TODO add your handling code here:
        guardarHuella();
    }//GEN-LAST:event_guardarbtMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Registro().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea EnviarTexto;
    private javax.swing.JButton guardarbt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblImagenHuella;
    // End of variables declaration//GEN-END:variables
}
