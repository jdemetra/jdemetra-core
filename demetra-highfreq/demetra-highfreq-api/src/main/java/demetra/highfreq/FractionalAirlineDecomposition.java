/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class FractionalAirlineDecomposition {
        double[] y, t, s, i, n;
        demetra.arima.ArimaModel arima;
        demetra.arima.UcarimaModel ucarima;
        LikelihoodStatistics statistics;
        MatrixType parametersCovariance;
        double[] parameters, score;
        
        public double[] getSa(){
            double[] sa=y.clone();
            if (s != null){
                for (int i=0; i<sa.length; ++i){
                    sa[i]-=s[i];
                }
            }
            return sa;
        }
}
