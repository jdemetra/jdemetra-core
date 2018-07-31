/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.univariate.ISsf;
import demetra.sts.SeasonalComponent;
import demetra.sts.SeasonalModel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StsBuilder {
    
    private ISsf trend, seasonal, cyclical;
    private double noiseVar;
    
    public StsBuilder localLevel(double lvar){
        trend=LocalLevel.of(lvar);
        return this;
    }

    public StsBuilder trend(ISsf tssf){
        trend=tssf;
        return this;
    }
    
    public StsBuilder localLinearTrend(double lvar, double svar){
        trend= LocalLinearTrend.of(lvar, svar);
        return this;
    }

    public StsBuilder seasonal(SeasonalModel model, int period, double seasvar){
        seasonal= SeasonalComponent.of(model, period, seasvar);
        return this;
    }
    
        public StsBuilder noise(double nvar){
        noiseVar=nvar;
        return this;
    }

}
