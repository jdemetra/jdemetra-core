/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.modelling.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.TsLags;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public class LagsFactory implements ModifierFactory<TsLags> {

    static LagsFactory FACTORY=new LagsFactory();

    private LagsFactory(){}

    @Override
    public TsDomain needFor(TsLags lags, TsDomain domain) {
        TsPeriod start=domain.getStartPeriod().plus(-lags.getLastLag());
        return TsDomain.of(start, domain.getLength()+lags.getLagsCount()-1);
    }

    @Override
    public TimeSeriesDomain needForGeneric(TsLags lags, TimeSeriesDomain domain) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean fill(TsLags modifier, TsPeriod start, FastMatrix input, FastMatrix output) {
        int nlags=modifier.getLagsCount();
        for(int i=0, k=0; i<nlags; ++i){
            for (int j=0; j<input.getColumnsCount(); ++j, ++k ){
                output.column(k).copy(input.column(j).drop(nlags-i-1, i));
            }
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(TsLags var, D domain, FastMatrix input, FastMatrix output) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
