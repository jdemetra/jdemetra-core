/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.examples;

import demetra.benchmarking.univariate.Cholette;
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.Denton;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Benchmarking {

    public void main(String[] args) {
//        denton();
        cholette();
    }

    public void denton() {
        DentonSpec spec = DentonSpec.builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Average)
                .build();

        TsData highFreqSeries = Data.TS_PROD;
        TsData aggregationConstraint = highFreqSeries.aggregate(TsUnit.YEAR, AggregationType.Average, true);
        // add some noise
        Random rnd = new Random(0);
        aggregationConstraint = aggregationConstraint.fn(z -> z * rnd.nextDouble(.9, 1.1));

        TsData benchmark = Denton.benchmark(highFreqSeries, aggregationConstraint, spec);
        TsDataTable table = TsDataTable.of(Arrays.asList(highFreqSeries, benchmark, aggregationConstraint));
        System.out.println(table);
    }

    /**
     * Impact of Lambda (additive -> multiplicative)
     */
    public void cholette() {
        TsData highFreqSeries = Data.TS_PROD;
        TsData aggregationConstraint = highFreqSeries.aggregate(TsUnit.YEAR, AggregationType.Average, true);
        // add some noise
        Random rnd = new Random();
        aggregationConstraint = aggregationConstraint.fn(z -> z * rnd.nextDouble(.7, 1.3));
        List<TsData> all = new ArrayList<>();
        for (int k = 0; k <= 10; ++k) {
            CholetteSpec spec = CholetteSpec.builder()
                    .lambda(k * .1)
                    .rho(1)
                    .aggregationType(AggregationType.Average)
                    .build();

            all.add(Cholette.benchmark(highFreqSeries, aggregationConstraint, spec));
        }
        TsDataTable table = TsDataTable.of(all);
        System.out.println(table);
    }

}
