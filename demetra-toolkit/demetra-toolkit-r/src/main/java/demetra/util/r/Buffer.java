/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.util.r;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <T>
 */
public abstract class Buffer<T> {
    protected final double[] buffer;
    
     protected Buffer(double[] buffer){
        this.buffer=buffer; 
    }
    
    public double[] data(){
        return buffer;
    }
    
    public abstract T build();
}
