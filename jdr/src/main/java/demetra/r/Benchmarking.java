/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.benchmarking.univariate.CholetteSpecification;
import demetra.benchmarking.univariate.DentonSpecification;
import demetra.benchmarking.univariate.TsCholette;
import demetra.benchmarking.univariate.TsDenton;
import demetra.data.AggregationType;
import demetra.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Benchmarking {

    public TsData denton(TsData source, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion) {
        DentonSpecification spec = new DentonSpecification();
        spec.setDifferencing(differencing);
        spec.setMultiplicative(multiplicative);
        spec.setModified(modified);
        spec.setAggregationType(AggregationType.valueOf(conversion));
        return TsDenton.benchmark(source, bench, spec);
    }

    public TsData cholette(TsData source, TsData bench, double rho, double lambda, String bias, String conversion) {
        CholetteSpecification spec = new CholetteSpecification();
        spec.setRho(rho);
        spec.setLambda(lambda);
        AggregationType agg = AggregationType.valueOf(conversion);
        if (agg != null) {
            spec.setAggregationType(agg);
        }
        CholetteSpecification.BiasCorrection corr = CholetteSpecification.BiasCorrection.valueOf(bias);
        if (corr != null) {
            spec.setBias(corr);
        }
        return TsCholette.benchmark(source, bench, spec);
    }
}
