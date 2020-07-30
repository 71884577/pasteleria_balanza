/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maypi.balance;

import com.fazecast.jSerialComm.SerialPort;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.SAVE_DIALOG;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.apache.poi.util.ArrayUtil;
import org.json.JSONArray;
import org.json.JSONObject;



public class JFrameBalance extends javax.swing.JFrame {

    String service_recetas = "/receta";
    String service_produccion = "/produccion";
    
    HashMap<String, ArrayList<Insumo>> hashMap_recetas = new HashMap<String, ArrayList<Insumo>>();
    HashMap <String, Double> produccion = new HashMap <String, Double>();
    private String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public String NAME_TEMP_CONFIG = "rc_regs_conf.bin";
    public Config config;
    public Boolean flag_recetas = false;
    private String token, user;

    boolean reading_bal = false;
    Reading reading;
     
    /**
     * Creates new form balance
     */
    public JFrameBalance() {
        initComponents();
        
        this.loadImages();
        this.loadBalance();
        
        //this load port
        this.jButton_load.setMnemonic(KeyEvent.VK_F9);
        this.jButton_download.setMnemonic(KeyEvent.VK_F12);
        
        jTable_pesaje.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {             
            }
        });
    }
    
    public void loadUser(){
        jLabel_user.setText(this.user);
    }
    
    public void loadOptions(){
        
        String url = ("http://"+Constan.ruta+this.service_recetas);
        Service service1 = new Service(this, url, null, token, Constan.method_get);
        service1.run();
            
    }
    
    public void loadImages(){
        Image img_logo, img_act, img_act_r, img_conf, img_conf_r;
        ImageIcon img_act_i, img_conf_i;
        
        try {
            img_logo = ImageIO.read(getClass().getResource("/logo.png"));
            img_act = ImageIO.read(getClass().getResource("/refresh.png"));
            img_conf = ImageIO.read(getClass().getResource("/config.png"));
            
            img_act_r = img_act.getScaledInstance(jButton_act.getWidth() - 6, jButton_act.getHeight() - 10, Image.SCALE_SMOOTH);
            img_conf_r = img_conf.getScaledInstance(jButton_config.getWidth() - 8, jButton_config.getHeight() - 10, Image.SCALE_SMOOTH);
            
            img_act_i = new ImageIcon(img_act_r);
            img_conf_i = new ImageIcon(img_conf_r);
            
            jButton_act.setIcon(img_act_i);
            jButton_config.setIcon(img_conf_i);
            
            this.setIconImage(img_logo);
            
        } catch (IOException ex) {
            Logger.getLogger(JFrameBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setToken(String token){
        this.token = token;
        this.loadOptions();
        
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        this.loadUser();
    }
    

    public void changeConfig(Config config){
        this.loadBalance(config);
    }
    
     public void response(String data){
         
         jTextField_balance.setText(data);
     }
     
     public void responseService(String data,String url,String method){
         
        JSONObject jSONObject = new JSONObject(data);
        JSONArray result = jSONObject.getJSONArray("result");
        
        if(url.equals(("http://"+Constan.ruta+this.service_recetas)) && method.equals(Constan.method_get) ){
            
            jComboBox_Receta.removeAllItems();
            
            for(int i = 0; i < result.length(); i++){
                
                String receta = result.getJSONObject(i).getString("nombreReceta");
                JSONArray insumos = result.getJSONObject(i).getJSONArray("insumos");
                
                jComboBox_Receta.addItem(receta);
                
                ArrayList<Insumo> array_insumos = new ArrayList<Insumo>();
                
                for(int j = 0; j < insumos.length(); j++){
                    String nombreInsumo = insumos.getJSONObject(j).getString("nombreInsumo");
                    String umInsumo = insumos.getJSONObject(j).getString("umInsumo");
                    double cantInsumo = insumos.getJSONObject(j).getDouble("cantInsumo");
                    
                    Insumo insumo = new Insumo(nombreInsumo,cantInsumo,umInsumo);
                    array_insumos.add(insumo);
                    
                    if(i==0){
                        if(j==0){
                            jLabel_cantidad.setText(cantInsumo+"");
                            jLabel_unidad.setText(umInsumo);
                        }
                        
                        jComboBox_Insumo.addItem(nombreInsumo);
                    }
                    
                }
                
                hashMap_recetas.put(receta, array_insumos);
                flag_recetas = true;
                
            }
        
        }

        
     }
     
     
     private void loadBalance(){
         
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(this.TEMP_DIR+this.NAME_TEMP_CONFIG));
            config = (Config) objectInputStream.readObject();
            objectInputStream.close();
            
            if( config != null){
                this.loadBalance(config);
            }
            else{
                JOptionPane.showMessageDialog(this, "Configuracion no existe","INFO",JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
        }
        
     }
     
     private void loadBalance(Config config){
         
        if(config != null && config.isValid()){
            
            reading = new Reading(config.getPort(),this);
            
            try { 
                reading.start();
                jButton_desconectar.setText("Desconectar");
                reading_bal = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            config = new Config();
            JOptionPane.showMessageDialog(this, "Puerto desconocido","ERROR",JOptionPane.ERROR_MESSAGE);
        }
     }
       
     private void loadreg(){
        
        String[] lect_balance_array = jTextField_balance.getText().toString().split("\\s+");
        double lect_balance = 0;
        
        if(lect_balance_array.length == 3){    
            lect_balance = Double.parseDouble(lect_balance_array[1]);
        }
        else if(lect_balance_array.length == 1){
            lect_balance = Double.parseDouble(lect_balance_array[0]);
        }
        try {
            String insumo = jComboBox_Insumo.getSelectedItem().toString();
        
            DefaultTableModel model_pesaje = (DefaultTableModel) jTable_pesaje.getModel();

            model_pesaje.addRow(new Object[]{insumo, lect_balance,false});
            
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Error en Cargar Peso de Insumo");
        }
        
     }

     private void sendProduccion(){
         
        String url = ("http://"+Constan.ruta+this.service_produccion);
        
        for (String name: this.produccion.keySet()){
            
            String key = name.toString();
            double value = this.produccion.get(name);

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("cantidad", jSpinner_cantidad.getValue().toString());  
            params.put("receta", jComboBox_Receta.getSelectedItem().toString());
            params.put("insumo", key);
            params.put("peso", value);
            
            Service service = new Service(this, url, params, this.token, Constan.method_post);
            service.run();
        }
         
     }
     
     private void sendBalance(){

         try {
             
            DefaultTableModel model = (DefaultTableModel) jTable_pesaje.getModel();

            int nRow = model.getRowCount();
          
            this.produccion.clear();
            
            for (int i=0;i<nRow;i++){
                String insumo = model.getValueAt(i,0).toString();
                double peso = Double.parseDouble(model.getValueAt(i,1).toString());
                
                if(this.produccion.get(insumo) != null){
                    double temp = this.produccion.get(insumo);
                    this.produccion.put(insumo, peso+temp);
                }else{
                    this.produccion.put(insumo, peso);
                }
                
            }
            jLabel_resultado.setText("");
            for (String name: this.produccion.keySet()){
                String key = name.toString();
                double value = this.produccion.get(name);
                
                String temp_result = jLabel_resultado.getText().toString();
                jLabel_resultado.setText(temp_result + "  " + key + ":" + value);
            }
 
         } catch (Exception e) {
             
         }
         
         
     }
     
     private void cleanreg(){
         
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult = JOptionPane.showConfirmDialog (null, "Desea limpiar registro?","Warning",dialogButton);
        if(dialogResult == JOptionPane.YES_OPTION){
            DefaultTableModel model = (DefaultTableModel) jTable_pesaje.getModel();
            int rowCount = model.getRowCount();
            //Remove rows one by one from the end of the table
            for (int i = rowCount - 1; i >= 0; i--) model.removeRow(i);
        }
     }
     
     private void download(){
         
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Specificar archivos a guardar");
        int userSelection = chooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            TableModel model = jTable_pesaje.getModel();
            int nRow = model.getRowCount();
            int nCol = model.getColumnCount();

            String filename = fileToSave.getAbsoluteFile().toString();
            System.out.println(filename.indexOf(".csv"));
            if(filename.indexOf(".csv")<0){
                filename = filename + ".csv";
            }
            
            File file = new File(filename);
            Boolean flag = true;
            
            if(file.exists()){
                int result = JOptionPane.showConfirmDialog(this,"El archivo existe,  ¿Desea sobrescribir?","Archivo existente",JOptionPane.YES_NO_CANCEL_OPTION);
                if(result != JOptionPane.YES_OPTION) flag = false;
            }
            
            if(flag){
                FileWriter out;

                try {
                    out = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(out);
                    for (int i=0;i<nCol;i++){
                      bw.write(model.getColumnName(i)+",");
                    }
                    bw.write("\n");
                    for (int i=0;i<nRow;i++){
                      for (int j=0;j<nCol;j++){
                        bw.write(model.getValueAt(i,j).toString()+",");
                      }
                      bw.write("\n");
                    }
                    bw.close();
                    
                    JOptionPane.showMessageDialog(this, "Se descargo exitosamente en:\n"+file.getAbsolutePath(),"DESCARGA",JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    Logger.getLogger(JFrameBalance.class.getName()).log(Level.SEVERE, null, ex);
                }    
            }
            else{
                this.download();
            }
            
        }
     }     
     
     public void updateInsumos(){
         
         if(flag_recetas && jComboBox_Receta != null && jComboBox_Receta.getSelectedItem() != null){
             
            jComboBox_Insumo.removeAllItems();
             
            String receta = jComboBox_Receta.getSelectedItem().toString();
            ArrayList<Insumo> array_insumos = hashMap_recetas.get(receta);
            double cantidad = Double.parseDouble(jSpinner_cantidad.getValue().toString());

            if(array_insumos!=null){
                for(int i=0;i<array_insumos.size();i++){

                   jLabel_cantidad.setText((cantidad * array_insumos.get(i).peso)+"");
                   jLabel_unidad.setText(array_insumos.get(i).unidad);
                   jComboBox_Insumo.addItem(array_insumos.get(i).insumo);  

               }
            }
         }
         
         
         
     }
     
     public void updatePesoInsumo(){
         
          if(flag_recetas && jComboBox_Receta != null && jComboBox_Receta.getSelectedItem() != null && jComboBox_Insumo != null && jComboBox_Insumo.getSelectedItem() != null){
                String receta = jComboBox_Receta.getSelectedItem().toString();
                String insumo = jComboBox_Insumo.getSelectedItem().toString();
            
                double cantidad = Double.parseDouble(jSpinner_cantidad.getValue().toString());
                ArrayList<Insumo> array_insumos = hashMap_recetas.get(receta);

                if(array_insumos!=null){
                    for(int i=0;i<array_insumos.size();i++){

                        if(insumo.equals(array_insumos.get(i).insumo)){
                           jLabel_cantidad.setText((cantidad * array_insumos.get(i).peso)+"");
                           jLabel_unidad.setText(array_insumos.get(i).unidad);
                        }         
                   }
                }
             }

     }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTextField_balance = new javax.swing.JTextField();
        jButton_load = new javax.swing.JButton();
        jButton_download = new javax.swing.JButton();
        jButton_config = new javax.swing.JButton();
        jComboBox_Receta = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox_Insumo = new javax.swing.JComboBox<>();
        jButton_act = new javax.swing.JButton();
        jLabel_user = new javax.swing.JLabel();
        jButton_desconectar = new javax.swing.JButton();
        jLabel_unidad = new javax.swing.JLabel();
        jLabel_cantidad = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSpinner_cantidad = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_pesaje = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jButton_eliminar = new javax.swing.JButton();
        jLabel_resultado = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CONTINENTAL - PESAJE");
        setExtendedState(6);
        setForeground(java.awt.Color.red);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTextField_balance.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jTextField_balance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_balanceActionPerformed(evt);
            }
        });

        jButton_load.setBackground(new java.awt.Color(255, 255, 255));
        jButton_load.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton_load.setText("Cargar (Alt + F9)");
        jButton_load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_loadActionPerformed(evt);
            }
        });
        jButton_load.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton_loadKeyPressed(evt);
            }
        });

        jButton_download.setBackground(new java.awt.Color(255, 255, 255));
        jButton_download.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton_download.setText("Descargar y Enviar (Alt + F12)");
        jButton_download.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_downloadActionPerformed(evt);
            }
        });
        jButton_download.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton_downloadKeyPressed(evt);
            }
        });

        jButton_config.setBackground(new java.awt.Color(255, 255, 255));
        jButton_config.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_configActionPerformed(evt);
            }
        });

        jComboBox_Receta.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_RecetaItemStateChanged(evt);
            }
        });
        jComboBox_Receta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jComboBox_RecetaMouseEntered(evt);
            }
        });

        jLabel2.setText("Seleccionar Receta");

        jLabel3.setText("Seleccionar Insumo");

        jComboBox_Insumo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_InsumoItemStateChanged(evt);
            }
        });
        jComboBox_Insumo.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBox_InsumoPropertyChange(evt);
            }
        });

        jButton_act.setBackground(new java.awt.Color(255, 255, 255));
        jButton_act.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton_act.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_actActionPerformed(evt);
            }
        });

        jLabel_user.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel_user.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_user.setText("user");

        jButton_desconectar.setText("Conectar");
        jButton_desconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_desconectarActionPerformed(evt);
            }
        });

        jLabel_unidad.setText("Unidad");

        jLabel_cantidad.setText("Cantidad");

        jLabel5.setText("Cantidad");

        jSpinner_cantidad.setValue(1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_balance)
                    .addComponent(jComboBox_Receta, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_download, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton_config, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_user, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton_act, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton_desconectar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox_Insumo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel_cantidad)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel_unidad))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinner_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_config, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton_act, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel_user)
                        .addGap(14, 14, 14)))
                .addComponent(jTextField_balance, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_desconectar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jComboBox_Receta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(41, 41, 41)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox_Insumo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_cantidad)
                    .addComponent(jLabel_unidad))
                .addGap(33, 33, 33)
                .addComponent(jButton_load, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_download, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTable_pesaje.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Insumo", "Peso", "Seleccionar"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_pesaje.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTable_pesajeInputMethodTextChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTable_pesaje);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 51, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("PESOS DE INSUMOS");

        jButton_eliminar.setBackground(new java.awt.Color(255, 255, 255));
        jButton_eliminar.setText("Eliminar");
        jButton_eliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_eliminarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton_eliminar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_resultado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton_eliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_resultado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_downloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_downloadActionPerformed
       this.sendProduccion();
        this.download();
    }//GEN-LAST:event_jButton_downloadActionPerformed

    private void jButton_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_loadActionPerformed
        // TODO add your handling code here:      
        this.loadreg();
        this.sendBalance();
        
        jTable_pesaje.scrollRectToVisible(jTable_pesaje.getCellRect(jTable_pesaje.getRowCount()-1, 0, true));

    }//GEN-LAST:event_jButton_loadActionPerformed

    private void jButton_loadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton_loadKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            this.loadreg();
        }
    }//GEN-LAST:event_jButton_loadKeyPressed

    private void jButton_downloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton_downloadKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            this.download();
        }
    }//GEN-LAST:event_jButton_downloadKeyPressed

    private void jButton_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_configActionPerformed
        // TODO add your handling code here:
        JFrameConfiguration jframeConfiguration = new JFrameConfiguration();
        jframeConfiguration.setJFrameBalance(this);
        jframeConfiguration.setVisible(true);
    }//GEN-LAST:event_jButton_configActionPerformed

    private void jComboBox_InsumoPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBox_InsumoPropertyChange
        // TODO add your handling code here:
       
    }//GEN-LAST:event_jComboBox_InsumoPropertyChange

    private void jComboBox_InsumoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_InsumoItemStateChanged
        
        this.updatePesoInsumo();
        
    }//GEN-LAST:event_jComboBox_InsumoItemStateChanged

    private void jButton_actActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_actActionPerformed
        // TODO add your handling code here:
        this.loadOptions();
    }//GEN-LAST:event_jButton_actActionPerformed

    private void jTextField_balanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_balanceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_balanceActionPerformed

    private void jTable_pesajeInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTable_pesajeInputMethodTextChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTable_pesajeInputMethodTextChanged

    private void jButton_eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_eliminarActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model_pesaje = (DefaultTableModel) jTable_pesaje.getModel();
        
        for (int i = 0; i < jTable_pesaje.getRowCount(); i++) {
            Boolean isChecked = Boolean.valueOf(jTable_pesaje.getValueAt(i, 2).toString());

            if(isChecked) {
                
                model_pesaje.removeRow(i);
                i--;
            }
        }
        
        this.sendBalance();
        
    }//GEN-LAST:event_jButton_eliminarActionPerformed

    private void jComboBox_RecetaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_RecetaItemStateChanged
        // TODO add your handling code here:
        this.updateInsumos();
        
    }//GEN-LAST:event_jComboBox_RecetaItemStateChanged

    private void jButton_desconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_desconectarActionPerformed
        // TODO add your handling code here:
        if(reading_bal){
            reading.closePort();
            jButton_desconectar.setText("Conectar");
            reading_bal = false;
            
        }
        else{
            this.loadBalance();
        }
       
        
        
    }//GEN-LAST:event_jButton_desconectarActionPerformed

    private void jComboBox_RecetaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox_RecetaMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_RecetaMouseEntered

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
            java.util.logging.Logger.getLogger(JFrameBalance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrameBalance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrameBalance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrameBalance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameBalance().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_act;
    private javax.swing.JButton jButton_config;
    private javax.swing.JButton jButton_desconectar;
    private javax.swing.JButton jButton_download;
    private javax.swing.JButton jButton_eliminar;
    private javax.swing.JButton jButton_load;
    private javax.swing.JComboBox<String> jComboBox_Insumo;
    private javax.swing.JComboBox<String> jComboBox_Receta;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel_cantidad;
    private javax.swing.JLabel jLabel_resultado;
    private javax.swing.JLabel jLabel_unidad;
    private javax.swing.JLabel jLabel_user;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner_cantidad;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable_pesaje;
    private javax.swing.JTextField jTextField_balance;
    // End of variables declaration//GEN-END:variables
}
