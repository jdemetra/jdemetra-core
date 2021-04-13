/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.r;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import java.util.function.IntToDoubleFunction;
import jdplus.stats.AutoCovariances;
import jdplus.stats.InverseAutoCorrelations;
import jdplus.stats.tests.BowmanShenton;
import jdplus.stats.tests.DoornikHansen;
import jdplus.stats.tests.JarqueBera;
import jdplus.stats.tests.LjungBox;
import jdplus.stats.tests.TestOfRuns;
import jdplus.stats.tests.TestOfUpDownRuns;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Tests {
    
    public double[] autocorrelations(double[] data, boolean mean, int n){
        double[] iac=new double[n];
        DoubleSeq x=DoubleSeq.of(data);
        IntToDoubleFunction fn = AutoCovariances.autoCorrelationFunction(x, mean ? x.average() : 0);
        for (int i=1; i<=n; ++i){
            iac[i-1]=fn.applyAsDouble(i);
        }
        return iac;
    }

    public double[] partialAutocorrelations(double[] data, boolean mean, int n){
        DoubleSeq x=DoubleSeq.of(data);
        IntToDoubleFunction fn = AutoCovariances.autoCorrelationFunction(x, mean ? x.average() : 0);
        return AutoCovariances.partialAutoCorrelations(fn, n);
    }
    
    
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
        return new DoornikHansen(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest jarqueBera(double[] data, int k, boolean sample){
        return new JarqueBera(DoubleSeq.of(data))
                .degreeOfFreedomCorrection(k)
                .correctionForSample(sample)
                .build();
    }
    
    public StatisticalTest testOfRuns(double[] data, boolean mean, boolean number){
        TestOfRuns test = new TestOfRuns(DoubleSeq.of(data))
                .useMean(mean);
        return number ? test.testNumber() : test.testLength();
    }

    public StatisticalTest testOfUpDownRuns(double[] data, boolean number){
        TestOfUpDownRuns test = new TestOfUpDownRuns(DoubleSeq.of(data));
        return number ? test.testNumber() : test.testLength();
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
