/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sigex;

import demetra.arima.AutoCovarianceFunction;
import demetra.data.DoubleSequence;
import demetra.maths.polynomials.Polynomial;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class ARMAauto {
    DoubleSequence compute(@Nonnull DoubleSequence ar, @Nonnull DoubleSequence ma, int maxlag){
        double[] dar=new double[1+ar.length()];
        dar[0]=1;
        ar.fastFn(x->-x).copyTo(dar, 1);
        
        double[] dma=new double[1+ma.length()];
        dma[0]=1;
        ma.copyTo(dma, 1);
        Polynomial MA=Polynomial.of(dma), AR=Polynomial.of(dar);
        
        AutoCovarianceFunction acf=new AutoCovarianceFunction(MA, AR, 1);
        return DoubleSequence.of(acf.values(maxlag+1));
    }
}
