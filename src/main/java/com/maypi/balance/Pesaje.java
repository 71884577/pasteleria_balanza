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
public class Pesaje implements Serializable {
    
    String insumo;
    Double pesoReal;

    public Pesaje() {
    }
    
    public Pesaje(String insumo, Double pesoReal) {
        this.insumo = insumo;
        this.pesoReal = pesoReal;
    }
    
    public String getInsumo() {
        return insumo;
    }

    public void setInsumo(String insumo) {
        this.insumo = insumo;
    }

    public Double getPesoReal() {
        return pesoReal;
    }

    public void setPesoReal(Double pesoReal) {
        this.pesoReal = pesoReal;
    }

    public Pesaje clone(){  
        return new Pesaje(this.insumo, this.pesoReal);  
    }  
}
