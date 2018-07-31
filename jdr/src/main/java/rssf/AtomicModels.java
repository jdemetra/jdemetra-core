/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.BackFilter;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.univariate.ISsf;
import demetra.sts.CyclicalComponent;
import demetra.sts.Noise;
import demetra.sts.SeasonalComponent;
import demetra.sts.SeasonalModel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AtomicModels {

    public ISsf arma(double[] ar, double[] ma, double var) {
        ArimaModel arima = new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ONE, BackFilter.ofInternal(ma), var);
        return SsfArima.of(arima);
    }

    public ISsf arima(double[] ar, double[] diff, double[] ma, double var) {
        ArimaModel arima = new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ofInternal(diff), BackFilter.ofInternal(ma), var);
        return SsfArima.of(arima);
    }

    public ISsf sarima(int period, int[] orders, int[] seasonal, double[] parameters) {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setP(orders[0]);
        spec.setD(orders[1]);
        spec.setQ(orders[2]);
        if (seasonal != null) {
            spec.setBp(seasonal[0]);
            spec.setBd(seasonal[1]);
            spec.setBq(seasonal[2]);
        }
        SarimaModel sarima = SarimaModel.builder(spec)
                .parameters(DoubleSequence.ofInternal(parameters))
                .build();
        return SsfArima.of(sarima);
    }
    
    public ISsf localLevel(double lvar){
        return LocalLevel.of(lvar);
    }

    public ISsf localLinearTrend(double lvar, double svar, double nvar){
        return LocalLinearTrend.of(lvar, svar);
    }

    public ISsf seasonalComponent(String model, int period, double seasvar){
        return SeasonalComponent.of(SeasonalModel.valueOf(model), period, seasvar);
    }
    
    public ISsf cycle(double dumpingFactor, double cyclicalPeriod, double cvar){
        return CyclicalComponent.of(dumpingFactor, cyclicalPeriod, cvar);
    }
    
    public ISsf noise(double var){
        return new Noise(var);
    }
}
