/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maypi.balance;

import java.io.Serializable;

/**
 *
 * @author rcordova
 */
public class Config implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String port;
    private int baudRate;
    private int numStopBits;
    private int parity;
    private int comPortTimeouts;

     public Config() {
         this.port = "";
     }

     public String getPort() {
         return port;
     }

     public void setPort(String port) {
         this.port = port;
     }

     public int getBaudRate() {
         return baudRate;
     }

     public void setBaudRate(int baudRate) {
         this.baudRate = baudRate;
     }

     public int getNumStopBits() {
         return numStopBits;
     }

     public void setNumStopBits(int numStopBits) {
         this.numStopBits = numStopBits;
     }

     public int getParity() {
         return parity;
     }

     public void setParity(int parity) {
         this.parity = parity;
     }

     public int getComPortTimeouts() {
         return comPortTimeouts;
     }

     public void setComPortTimeouts(int comPortTimeouts) {
         this.comPortTimeouts = comPortTimeouts;
     }

     public boolean isValid(){
         return (this.port.length() > 0);
     }

}
