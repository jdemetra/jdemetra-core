/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.stlplus;

import demetra.modelling.ComponentInformation;
import demetra.modelling.regular.SeriesSpec;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
import demetra.sa.SeriesDecomposition;
import demetra.stl.StlPlusSpec;
import demetra.stl.StlSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.ModellingUtility;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sa.CholetteProcessor;
import jdplus.sa.PreliminaryChecks;
import jdplus.sa.SaBenchmarkingResults;
import jdplus.sa.modelling.RegArimaDecomposer;
import jdplus.sa.modelling.TwoStepsDecomposition;
import jdplus.sa.regarima.FastKernel;
import jdplus.stl.StlKernel;
import jdplus.stl.StlResults;

@lombok.Value
public class StlPlusKernel {

    private static PreliminaryChecks.Tool of(StlPlusSpec spec) {

        SeriesSpec series = spec.getPreprocessing().getSeries();
        return (s, logs) -> {
            TsData sc = s.select(series.getSpan());
            if (series.isPreliminaryCheck()) {
                jdplus.sa.PreliminaryChecks.testSeries(sc);
            }
            if (!spec.getPreprocessing().isEnabled()) {
                return s.select(series.getSpan());
            } else {
                return s;
            }
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private FastKernel preprocessor;
    private StlSpec spec;
    private CholetteProcessor cholette;

    public static StlPlusKernel of(StlPlusSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        FastKernel preprocessor = FastKernel.of(spec.getPreprocessing(), context);
        return new StlPlusKernel(check, preprocessor, spec.getStl(), CholetteProcessor.of(spec.getBenchmarking()));
    }

    public StlPlusResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            if (preprocessor == null) {
                // Step 0. Preliminary checks
                TsData sc = preliminary.check(s, log);
                StlKernel stl = StlKernel.of(spec);
                StlResults rslt = stl.process(sc);
                // Step 5. Benchmarking
                SaBenchmarkingResults bench = null;
                // Step 6. Diagnostics
                StlPlusDiagnostics diagnostics = StlPlusDiagnostics.of(null, rslt, rslt.asDecomposition());

                return StlPlusResults.builder()
                        .preprocessing(null)
                        .decomposition(rslt)
                        .finals(rslt.asDecomposition())
                        .benchmarking(bench)
                        .diagnostics(diagnostics)
                        .log(log)
                        .build();

            } else {
                // Step 0. Preliminary checks
                TsData sc = preliminary.check(s, log);
                // Step 1. RegArima
                RegSarimaModel preprocessing = preprocessor.process(sc, log);
                // Step 2. Link between regarima and stl
                StlSpec cspec = spec;
                boolean mul = preprocessing.getDescription().isLogTransformation();
                if (cspec == null) {
                    cspec = StlSpec.createDefault(s.getAnnualFrequency(), mul, true);
                } else if (cspec.isMultiplicative() != mul) {
                    cspec = spec.toBuilder().multiplicative(mul).build();
                }
                StlKernel stl = StlKernel.of(cspec);

                TsData det = preprocessing.deterministicEffect(s.getDomain(), v -> !SaVariable.isRegressionEffect(v, ComponentType.Undefined));
                TsData user = RegArimaDecomposer.deterministicEffect(preprocessing, s.getDomain(), ComponentType.Series, true, v -> ModellingUtility.isUser(v));
                det = TsData.subtract(det, user);
                TsData cseries;
                if (mul) {
                    det = preprocessing.backTransform(det, true);
                    cseries = TsData.divide(s, det);
                } else {
                    cseries = TsData.subtract(s, det);
                }

                StlResults rslt = stl.process(cseries);
                // Step 4. Final decomposition
                SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, rslt.asDecomposition());
                // Step 5. Benchmarking
                SaBenchmarkingResults bench = null;
                if (cholette != null) {
                    bench = cholette.process(s, TsData.concatenate(finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
                            finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)), preprocessing);
                }
                // Step 6. Diagnostics
                StlPlusDiagnostics diagnostics = StlPlusDiagnostics.of(preprocessing, rslt, finals);

                return StlPlusResults.builder()
                        .preprocessing(preprocessing)
                        .decomposition(rslt)
                        .finals(finals)
                        .benchmarking(bench)
                        .diagnostics(diagnostics)
                        .log(log)
                        .build();
            }
        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }
}
