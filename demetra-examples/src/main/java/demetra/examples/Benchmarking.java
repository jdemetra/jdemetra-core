/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
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
