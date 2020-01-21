/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
class BenchmarkingUtility {
    TsData constraintsByPosition(TsData highFreqSeries, TsData aggregationConstraint, int pos){
        TsDomain adom = highFreqSeries.getDomain().aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        adom=adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }

    TsData constraints(TsData highFreqSeries, TsData aggregationConstraint){
        TsDomain adom = highFreqSeries.getDomain().aggregate(aggregationConstraint.getTsUnit(), true);
        adom=adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }
    
}
