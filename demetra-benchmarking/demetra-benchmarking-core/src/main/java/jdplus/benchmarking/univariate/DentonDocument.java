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

import demetra.benchmarking.univariate.DentonSpec;
import demetra.timeseries.AbstractMultiTsDocument;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class DentonDocument extends AbstractMultiTsDocument<DentonSpec, BenchmarkingResults> {

    public DentonDocument() {
        super(DentonSpec.DEFAULT);
    }

    @Override
    protected BenchmarkingResults internalProcess(DentonSpec spec, List<TsData> data) {
        if (data.size() > 2 || data.isEmpty()) {
            return null;
        }
        TsData l, h, b;
        if (data.size() == 1) {
            l = data.get(0);
            h = null;
            b = DentonProcessor.PROCESSOR.benchmark(TsUnit.ofAnnualFrequency(spec.getDefaultPeriod()), l, spec);
        } else {
            h = data.get(0);
            l = data.get(1);
            b = DentonProcessor.PROCESSOR.benchmark(h, l, spec);
        }

        if (b == null) {
            return null;
        }
        return BenchmarkingResults.builder()
                .original(h)
                .target(l)
                .benchmarked(b)
                .build();
    }

}
