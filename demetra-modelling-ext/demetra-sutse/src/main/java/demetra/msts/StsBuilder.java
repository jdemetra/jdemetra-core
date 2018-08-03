/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

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
public class StsBuilder {

    private ISsf trend, seasonal, cycle, noise;
    private double mvar;

    public StsBuilder localLevel(double lvar, double loading) {
        trend = LocalLevel.of(lvar, loading);
        return this;
    }

    public StsBuilder trend(ISsf tssf) {
        trend = tssf;
        return this;
    }

    public StsBuilder localLinearTrend(double lvar, double svar, double loading) {
        trend = LocalLinearTrend.of(lvar, svar);
        return this;
    }

    public StsBuilder cycle(final double dumpingFactor, final double period, double cvar, double loading) {
        cycle = CyclicalComponent.of(dumpingFactor, period, cvar, loading);
        return this;
    }

    public StsBuilder seasonal(SeasonalModel model, int period, double seasvar, double loading) {
        seasonal = SeasonalComponent.of(model, period, seasvar, loading);
        return this;
    }
    
    public StsBuilder noise(double nvar, double loading) {
        noise=Noise.of(nvar, loading);
        return this;
    }
    
    public StsBuilder measurementVariance(double mvar) {
        this.mvar=mvar;
        return this;
    }
    

}
