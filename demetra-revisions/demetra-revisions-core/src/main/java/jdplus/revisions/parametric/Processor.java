/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.timeseries.TsDataVintages;
import demetra.timeseries.TsData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Processor {

    public <K extends Comparable> RegressionBasedAnalysis regressionBasedAnalysis(TsDataVintages<K> vintages, int nrevs) {
        RegressionBasedAnalysis.Builder builder=RegressionBasedAnalysis.builder();
        bias(vintages, nrevs, builder);
        return builder.build();
    }
    
    private <K extends Comparable> void bias(TsDataVintages<K> vintages, int nrevs,RegressionBasedAnalysis.Builder builder ){
        TsData preliminary=vintages.preliminary(), current=vintages.current();
        builder.currentBias(BiasComputer.of(TsData.subtract(current, preliminary).getValues()));
        TsData prev=preliminary;
        for (int i=0; i<nrevs; ++i){
            TsData cur=vintages.vintage(i+1);
            if (cur == null)
                break;
            DoubleSeq rev=TsData.subtract(cur, prev).getValues();
            Bias bcur=BiasComputer.of(rev);
            if (bcur == null)
                break;
            builder.revisionBias(bcur);
            prev=cur;
        }
    }
}
