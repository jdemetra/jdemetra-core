/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.math.linearfilters;

import demetra.data.DoubleSeq;
import java.util.function.IntFunction;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class CrossValidation {
    public double[] doCrossValidation(DoubleSeq data, int low, int high, IntFunction<SymmetricFilter> factory){
        int start=high, end=data.length()-high;
        double[] rslt=new double[high-low];
        double[] pdata = data.toArray();
        for (int i=0; i<rslt.length; ++i){
            int h=low+i;
            SymmetricFilter sf = factory.apply(i+low);
            double w0=sf.weights().applyAsDouble(0);
            double d=0;
            for (int j=start; j<end; ++j){
                double del=(pdata[j]-sf.apply(pdata, j, 1));
                d+=del*del;
            }
            rslt[i]=Math.sqrt(d)/(1-w0)/(end-start);
        }        
        return rslt;
    }
}
