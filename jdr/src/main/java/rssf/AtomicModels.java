/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import demetra.maths.linearfilters.BackFilter;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AtomicModels {
    
    public ISsf arma(double[] ar, double[] ma, double var){
        ArimaModel arima=new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ONE, BackFilter.ofInternal(ma), var);
        return SsfArima.of(arima);
    }
    
    public ISsf arima(double[] ar, double[] diff, double[] ma, double var){
        ArimaModel arima=new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ofInternal(diff), BackFilter.ofInternal(ma), var);
        return SsfArima.of(arima);
    }
}
