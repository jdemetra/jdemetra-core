/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.data.AggregationType;
import demetra.data.DoubleSeqCursor;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BenchmarkingUtility {
    public TsData constraintsByPosition(TsData highFreqSeries, TsData aggregationConstraint, int pos){
        TsDomain adom = highFreqSeries.getDomain().aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        adom=adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }

    public TsData constraints(TsData highFreqSeries, TsData aggregationConstraint){
        TsDomain adom = highFreqSeries.getDomain().aggregate(aggregationConstraint.getTsUnit(), true);
        adom=adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }
    
    public TsData highFreqConstraints(TsData highFreqSeries, TsData aggregationConstraint){
        TsDomain adom = highFreqSeries.getDomain().aggregate(aggregationConstraint.getTsUnit(), true);
        adom=adom.intersection(aggregationConstraint.getDomain());
        TsData cnt = TsData.fitToDomain(aggregationConstraint, adom);
        double[] x=new double[highFreqSeries.length()];
        for (int i=0; i<x.length; ++i){
            x[i]=Double.NaN;
        }
        int start=highFreqSeries.getDomain().indexOf(adom.start());
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        DoubleSeqCursor cursor = cnt.getValues().cursor();
        for (int i=0, j=start+ratio-1; i<cnt.length(); ++i, j+=ratio){
            x[j]=cursor.getAndNext();
        }
        
        return TsData.ofInternal(highFreqSeries.getStart(), x);
    }

    public TsData highFreqConstraintsByPosition(TsData highFreqSeries, TsData aggregationConstraint, int pos){
        TsDomain adom = highFreqSeries.getDomain().aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        adom=adom.intersection(aggregationConstraint.getDomain());
        TsData cnt = TsData.fitToDomain(aggregationConstraint, adom);
        double[] x=new double[highFreqSeries.length()];
        for (int i=0; i<x.length; ++i){
            x[i]=Double.NaN;
        }
        int start=highFreqSeries.getDomain().indexOf(adom.start());
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        DoubleSeqCursor cursor = cnt.getValues().cursor();
        for (int i=0, j=start+pos; i<cnt.length(); ++i, j+=ratio){
            x[j]=cursor.getAndNext();
        }
        return TsData.ofInternal(highFreqSeries.getStart(), x);
    }
    
    public TsData biRatio(TsData highFreqSeries, TsData aggregationConstraint, AggregationType agg){
        TsData H=highFreqSeries.aggregate(aggregationConstraint.getTsUnit(), agg, true);
        return TsData.divide(aggregationConstraint, H);
    }
    
    public TsData biRatio(TsData highFreqSeries, TsData aggregationConstraint, int pos){
        TsData H=highFreqSeries.aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        return TsData.divide(aggregationConstraint, H);
    }
}
