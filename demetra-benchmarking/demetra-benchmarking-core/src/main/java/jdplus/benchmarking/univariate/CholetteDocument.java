/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.CholetteSpec;
import demetra.timeseries.AbstractMultiTsDocument;
import demetra.timeseries.TsData;
import java.util.List;


/**
 *
 * @author Jean Palate
 */
public class CholetteDocument extends AbstractMultiTsDocument<CholetteSpec, BenchmarkingResults> {

    public CholetteDocument() {
        super(CholetteSpec.DEFAULT);
    }

    @Override
    protected BenchmarkingResults internalProcess(CholetteSpec spec, List<TsData> data) {
        if (data.isEmpty())
            return null;
        TsData low=data.get(0);
        TsData high= data.size() == 1 ? null : data.get(1);
        // TODO
        if (high == null)
            return null;
        TsData benchmark = CholetteProcessor.PROCESSOR.benchmark(high, low, spec);
        if (benchmark == null)
            return null;
        return BenchmarkingResults.builder()
                .original(high)
                .target(low)
                .benchmarked(benchmark)
                .build();
    }

}