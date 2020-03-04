/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.businesscycle.r;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import jdplus.businesscycle.HodrickPrescottFilter;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class HodrickPrescott {
    
    public MatrixType filter(double[] data, double lambda, double cycleLength){
       HodrickPrescottFilter filter=new HodrickPrescottFilter(
               cycleLength <=0 ?lambda : HodrickPrescottFilter.lambda(cycleLength));
        DoubleSeq[] rslt = filter.process(DoubleSeq.of(data));
        int n=data.length;
        double[] all=new double[n*2];
        rslt[0].copyTo(all, 0);
        rslt[1].copyTo(all, n);
        return MatrixType.of(all, n, 2);
    }
    
}
