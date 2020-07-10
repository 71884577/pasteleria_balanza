package com.maypi.balance;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Roycer
 */
public class Reading extends Thread{
    
    String port;
    SerialPort serialport;
    JFrame frame;
    Boolean closereading;
    
    public Reading(String port, JFrame frame) {
        
        this.closereading = false;
        this.port = port;
        this.frame = frame;
        this.serialport = SerialPort.getCommPort(port);  
    }
    
    @Override
    public void run(){    
        try {
            
            this.serialport.setBaudRate(9600);
            this.serialport.setNumDataBits(7);
            this.serialport.setNumStopBits(1);
            this.serialport.setParity​(SerialPort.ODD_PARITY);
            this.serialport.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            this.serialport.openPort();

            try {
                InputStream inputstream = serialport.getInputStream();
                Scanner scanner = new Scanner(inputstream); 
                scanner.nextLine();
                
                while(scanner.hasNextLine()) {          
                    if(closereading) break;
                    
                    String data = scanner.nextLine();
                    this.response(data);
                }
                
                scanner.close();
                inputstream.close();
            }
            catch (Exception e) {
                Logger.getLogger(Reading.class.getName()).log(Level.SEVERE, null, e);
            }  
            this.closePort();
        } catch (Exception e) {
            Logger.getLogger(Reading.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }

    private void response(String data){
        
        switch(frame.getClass().getName()){
            case "com.maypi.balance.JFrameBalance": 
                ((JFrameBalance)frame).response(data);
                break;          
        }
    }
    
    public void closePort(){
        this.closereading = true;
        this.serialport.closePort();
    }
    
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
  
}
