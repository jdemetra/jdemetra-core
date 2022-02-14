/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.advanced.arima;

import nbbrd.design.Development;

/**
 * Unobserved components Arima model
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class UcarimaModel {

    /**
     * Reduced form of the model
     */
    private ArimaModel sum;
    /**
     * Unobserved components
     */
    @lombok.NonNull 
    private ArimaModel[] components;
    
    /**
     * Number of components
     * @return 
     */
    public int size(){
        return components.length;
    }
    
    /**
     * Gets the ith component
     * @param i 0-based position of the component
     * @return 
     */
    public ArimaModel getComponent(int i){
        return components[i];
    }

}
