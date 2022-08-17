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
package jdplus.benchmarking.extractors;

import demetra.benchmarking.BenchmarkingDictionaries;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.timeseries.TsData;
import jdplus.benchmarking.univariate.BenchmarkingResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class BenchmarkingResultsExtractor extends InformationMapping<BenchmarkingResults> {

    public BenchmarkingResultsExtractor() {
        set(BenchmarkingDictionaries.ORIGINAL, TsData.class, source -> source.getOriginal());
        set(BenchmarkingDictionaries.TARGET, TsData.class, source -> source.getTarget());
        set(BenchmarkingDictionaries.BENCHMARKED, TsData.class, source -> source.getBenchmarked());
        set(BenchmarkingDictionaries.BIRATIO, TsData.class, source -> source.getBiRatio());
    }

    @Override
    public Class<BenchmarkingResults> getSourceClass() {
        return BenchmarkingResults.class;
    }

}
