/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.r;

import demetra.arima.ArimaModel;
import demetra.arima.ArimaType;
import demetra.arima.IArimaModel;
import demetra.arima.UcarimaType;
import demetra.arima.mapping.ArimaInfo;
import demetra.arima.mapping.UcarimaInfo;
import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.DataBlockStorage;
import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.mapping.LikelihoodInfo;
import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.processing.IProcResults;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarimaType;
import demetra.sarima.estimation.RegArimaEstimator;
import demetra.sarima.mapping.SarimaInfo;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.simplets.TsData;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;
import demetra.ucarima.AllSelector;
import demetra.ucarima.ModelDecomposer;
import demetra.ucarima.SeasonalSelector;
import demetra.ucarima.TrendCycleSelector;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;
import java.util.LinkedHashMap;
import java.util.Map;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AirlineDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        TsData y, t, s, i;
        UcarimaType ucarima;
        RegArimaModel<SarimaModel> regarima;
        SarimaType sarima;
        ConcentratedLikelihood concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        Matrix parametersCovariance;
        double[] score;

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa",
                UCM = "ucm", UCARIMA = "ucarima", ARIMA = "arima",
                LL = "likelihood", PCOV = "pcov", SCORE = "score";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.set(Y, TsData.class, source -> source.getY());
            MAPPING.set(T, TsData.class, source -> source.getT());
            MAPPING.set(S, TsData.class, source -> source.getS());
            MAPPING.set(I, TsData.class, source -> source.getI());
            MAPPING.set(SA, TsData.class, source -> subtract(source.getY(), source.getS()));
            MAPPING.delegate(UCARIMA, UcarimaInfo.getMapping(), source -> source.getUcarima());
            MAPPING.set(UCM, UcarimaType.class, source -> source.getUcarima());
            MAPPING.delegate(ARIMA, SarimaInfo.getMapping(), r -> r.getSarima());
            MAPPING.delegate(LL, LikelihoodInfo.getMapping(), r -> r.statistics);
            MAPPING.set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }
    }

    public Results process(TsData s) {
        int period = s.getTsUnit().ratioOf(TsUnit.YEAR);
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(period);
        SarimaModel arima = SarimaModel
                .builder(spec)
                .setDefault()
                .build();
        //
        RegArimaEstimator monitor = RegArimaEstimator.builder()
                .useParallelProcessing(true)
                .useMaximumLikelihood(true)
                .useCorrectedDegreesOfFreedom(false) // compatibility with R
                .precision(1e-12)
                .startingPoint(RegArimaEstimator.StartingPoint.Multiple)
                .build();

        RegArimaModel<SarimaModel> regarima
                = RegArimaModel.builder(DoubleSequence.of(s.values()), arima)
                .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regarima);
        UcarimaModel ucm = ucm(rslt.getModel().arima());

        ucm = ucm.simplify();
        SsfUcarima ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s.values());
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        TsPeriod start = s.getStart();

        ArimaType sum = ArimaModel.copyOf(ucm.getModel()).toType(null);
        ArimaType mt = ArimaModel.copyOf(ucm.getComponent(0)).toType("trend");
        ArimaType ms = ArimaModel.copyOf(ucm.getComponent(1)).toType("seasonal");
        ArimaType mi = ArimaModel.copyOf(ucm.getComponent(2)).toType("irregular");

        return Results.builder()
                .y(s)
                .t(TsData.of(start, ds.item(ssf.getComponentPosition(0))))
                .s(TsData.of(start, ds.item(ssf.getComponentPosition(1))))
                .i(TsData.of(start, ds.item(ssf.getComponentPosition(2))))
                .ucarima(new UcarimaType(sum, new ArimaType[]{mt, ms, mi}))
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood().getLikelihood())
                .regarima(rslt.getModel())
                .sarima(rslt.getModel().arima().toType())
                .statistics(rslt.statistics(spec.getParametersCount(), 0))
                .score(monitor.getScore())
                .parametersCovariance(monitor.getParametersCovariance())
                .build();

    }

    public static UcarimaModel ucm(IArimaModel arima) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

}
