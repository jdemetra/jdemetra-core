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
package jdplus.highfreq.extendedairline.decomposiiton;

import jdplus.highfreq.extendedairline.ExtendedAirlineResults;
import jdplus.highfreq.regarima.HighFreqRegArimaModel;
import jdplus.highfreq.extendedairline.ExtendedAirlineKernel;
import demetra.data.DoubleSeq;
import demetra.highfreq.ExtendedAirlineDecompositionSpec;
import demetra.highfreq.ExtendedAirlineDictionaries;
import demetra.modelling.ComponentInformation;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.ModellingContext;
import jdplus.sa.modelling.TwoStepsDecomposition;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionKernel {

    public static final String EA = "extended airline";

    private final ExtendedAirlineKernel preprocessor;
    private final DecompositionKernel decomposer;

    public ExtendedAirlineDecompositionKernel(ExtendedAirlineDecompositionSpec spec, ModellingContext context) {
        this.preprocessor = new ExtendedAirlineKernel(spec.getPreprocessing(), context);
        this.decomposer = new DecompositionKernel(spec.getDecomposition());
    }

    public static final SeriesDecomposition finalComponents(ExtendedAirlineDecomposition decomp, TsDomain edom) {

        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(decomp.isMultiplicative() ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        int nb = decomp.getBackcastsCount(), nf = decomp.getForecastsCount(), n = edom.length(), ntot = n + nb + nf;

        TsPeriod start = edom.getStartPeriod(), fstart = edom.getEndPeriod(), bstart = start.plus(-nb);
        DoubleSeq z = decomp.getFinalComponent(ExtendedAirlineDictionaries.Y_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Series, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Series, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Series, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.SA_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.T_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Trend, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Trend, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Trend, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.S_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Seasonal, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Seasonal, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.I_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Irregular, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Irregular, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Irregular, ComponentInformation.Forecast);
            }
        }
        return builder.build();
    }

    public ExtendedAirlineResults process(TsData y, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        HighFreqRegArimaModel preprocessing = preprocessor.process(y, log);
        TsData lin = preprocessing.linearizedSeries();
        boolean mul = preprocessing.getDescription().isLogTransformation();
        ExtendedAirlineDecomposition decomp = decomposer.process(lin.getValues(), mul, log);

        // compute the final decomposition
        SeriesDecomposition components = finalComponents(decomp, lin.getDomain());
        SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, components);

        return ExtendedAirlineResults.builder()
                .preprocessing(preprocessing)
                .decomposition(decomp)
                .components(components)
                .finals(finals)
                .log(log)
                .build();
    }

}
