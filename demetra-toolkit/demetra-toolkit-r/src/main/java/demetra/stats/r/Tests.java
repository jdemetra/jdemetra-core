/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.r;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import java.util.function.IntToDoubleFunction;
import jdplus.stats.InverseAutoCorrelations;
import jdplus.stats.tests.BowmanShenton;
import jdplus.stats.tests.JarqueBera;
import jdplus.stats.tests.LjungBox;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Tests {
    
    public double[] inverseAutocorrelations(double[] data, int nar, int n){
        double[] iac=new double[n];
        IntToDoubleFunction fn = InverseAutoCorrelations
                .sampleInverseAutoCorrelationsFunction(DoubleSeq.of(data), nar);
        for (int i=1; i<=n; ++i){
            iac[i-1]=fn.applyAsDouble(i);
        }
        return iac;
    }
    
    public StatisticalTest bowmanShenton(double[] data){
        return new BowmanShenton(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest doornikHansen(double[] data){
        return new BowmanShenton(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest jarqueBera(double[] data, int k, boolean sample){
        return new JarqueBera(DoubleSeq.of(data))
                .degreeOfFreedomCorrection(k)
                .correctionForSample(sample)
                .build();
    }
    
    public StatisticalTest ljungBox(double[] data, int k, int lag, int nhp, int sign, boolean mean){
        return new LjungBox(DoubleSeq.of(data), mean)
                .autoCorrelationsCount(k)
                .lag(lag)
                .hyperParametersCount(nhp)
                .sign(sign)
                .build();
    }
}
