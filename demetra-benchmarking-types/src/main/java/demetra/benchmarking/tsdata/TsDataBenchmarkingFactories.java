/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.benchmarking.tsdata;

import demetra.benchmarking.univariate.CholetteSpecification;
import demetra.benchmarking.univariate.DentonSpecification;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class TsDataBenchmarkingFactories {

    public static void setDentonFactory(Function<DentonSpecification, TsDataBenchmarking> fac) {
        DENTON.set(fac);
    }

    public static void setCholetteFactory(Function<CholetteSpecification, TsDataBenchmarking> fac) {
        CHOLETTE.set(fac);
    }

    public TsDataBenchmarking denton(DentonSpecification spec) {
        return DENTON.get().apply(spec);
    }

    public TsDataBenchmarking denton(CholetteSpecification spec) {
        return CHOLETTE.get().apply(spec);
    }

    private static AtomicReference<Function<DentonSpecification, TsDataBenchmarking>> DENTON;
    private static AtomicReference<Function<CholetteSpecification, TsDataBenchmarking>> CHOLETTE;
}
