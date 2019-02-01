/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.benchmarking.univariate.Cholette;
import demetra.benchmarking.univariate.Denton;
import demetra.data.AggregationType;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Benchmarking {

    public TsData denton(TsData source, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion) {
        DentonSpec spec = DentonSpec
                .builder()
                .differencing(differencing)
                .multiplicative(multiplicative)
                .modified(modified)
                .aggregationType(AggregationType.valueOf(conversion))
                .build();
        return Denton.benchmark(source, bench, spec);
    }

    public TsData cholette(TsData source, TsData bench, double rho, double lambda, String bias, String conversion) {
        CholetteSpec spec = CholetteSpec.builder()
                .rho(rho)
                .lambda(lambda)
                .aggregationType(AggregationType.valueOf(conversion))
                .bias(CholetteSpec.BiasCorrection.valueOf(bias))
                .build();
        return Cholette.benchmark(source, bench, spec);
    }
}
