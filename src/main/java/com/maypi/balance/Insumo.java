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
public class Insumo implements Serializable {
    
    String insumo;
    Double peso;
    String unidad;

    public Insumo() {
    }

    public Insumo(String insumo, Double peso, String unidad) {
        this.insumo = insumo;
        this.peso = peso;
        this.unidad = unidad;
    }
    
    public String getInsumo() {
        return insumo;
    }

    public void setInsumo(String insumo) {
        this.insumo = insumo;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Insumo clone(){  
        return new Insumo(this.insumo, this.peso, this.unidad);  
    }  
}
