/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ar;

import demetra.arima.ArimaModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.data.DoubleSeq;

/**
 *
 * @author PALATEJ
 */
public class ArBuilder {
    private double[] ar=DoubleSeq.EMPTYARRAY;
    private double var=1;
    
    /**
     * Sign of the AR term in y(t) =ar(1)y(t-1) + ar(n)y(t-n)
     * @param c
     * @return 
     */
    public ArBuilder ar(double... c){
        this.ar=ar;
        return this;
    }
    
    public ArBuilder innovationVariance(double var){
        this.var=var;
        return this;
    }
    
    public ArimaModel build(){
        double[] c=new double[ar.length+1];
        c[1]=1;
        for (int i=0; i<ar.length; ++i){
            c[i+1]=-ar[i];
        }
        return new ArimaModel(BackFilter.ofInternal(c), BackFilter.ONE, BackFilter.ONE, var);
    }
}
