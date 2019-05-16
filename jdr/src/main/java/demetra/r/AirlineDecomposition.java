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
import demetra.arima.ArimaProcess;
import demetra.arima.IArimaModel;
import demetra.arima.UcarimaProcess;
import demetra.descriptors.arima.UcarimaDescriptor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import jdplus.data.DataBlockStorage;
import demetra.information.InformationMapping;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import demetra.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.arima.SarimaProcess;
import demetra.sarima.RegSarimaProcessor;
import demetra.descriptors.arima.SarimaDescriptor;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.ucarima.AllSelector;
import demetra.ucarima.ModelDecomposer;
import demetra.ucarima.TrendCycleSelector;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.data.Doubles;
import jdplus.maths.matrices.FastMatrix;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class AirlineDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        TsData y, t, s, i;
        UcarimaProcess ucarima;
        RegArimaModel<SarimaModel> regarima;
        SarimaProcess sarima;
        ConcentratedLikelihoodWithMissing concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        FastMatrix parametersCovariance;
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
            MAPPING.delegate(UCARIMA, UcarimaDescriptor.getMapping(), source -> source.getUcarima());
            MAPPING.set(UCM, UcarimaProcess.class, source -> source.getUcarima());
            MAPPING.delegate(ARIMA, SarimaDescriptor.getMapping(), r -> r.getSarima());
            MAPPING.delegate(LL, LikelihoodStatisticsDescriptor.getMapping(), r -> r.statistics);
            MAPPING.set(PCOV, FastMatrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }
    }

    public Results process(TsData s, boolean sn) {
        int period = s.getTsUnit().ratioOf(TsUnit.YEAR);
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.airline(true);
        SarimaModel arima = SarimaModel
                .builder(spec)
                .setDefault()
                .build();
        //
        RegSarimaProcessor monitor = RegSarimaProcessor.builder()
                .useParallelProcessing(true)
                .useMaximumLikelihood(true)
                .useCorrectedDegreesOfFreedom(false) // compatibility with R
                .precision(1e-12)
//                .startingPoint(RegSarimaProcessor.StartingPoint.Multiple)
                .build();

        RegArimaModel<SarimaModel> regarima
                = RegArimaModel.builder(SarimaModel.class)
                        .y(s.getValues())
                        .arima(arima)
                        .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regarima);
        UcarimaModel ucm = ucm(rslt.getModel().arima(), sn);

        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s.getValues());
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        TsPeriod start = s.getStart();

        ArimaProcess sum = ArimaModel.of(ucm.getModel()).toType(null);
        ArimaProcess mt = ArimaModel.of(ucm.getComponent(0)).toType("trend");
        ArimaProcess ms = ArimaModel.of(ucm.getComponent(1)).toType("seasonal");
        ArimaProcess mi = ArimaModel.of(ucm.getComponent(2)).toType("irregular");
        int[] pos = ssf.componentsPosition();
        return Results.builder()
                .y(s)
                .t(TsData.of(start, Doubles.of(ds.item(pos[0]))))
                .s(TsData.of(start, Doubles.of(ds.item(pos[1]))))
                .i(TsData.of(start, Doubles.of(ds.item(pos[2]))))
                .ucarima(new UcarimaProcess(sum, new ArimaProcess[]{mt, ms, mi}))
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood())
                .regarima(rslt.getModel())
                .sarima(rslt.getModel().arima().toType())
                .statistics(rslt.statistics(0))
                .score(rslt.getMax().getGradient())
                .parametersCovariance(rslt.getMax().getHessian())
                .build();

    }

    public static UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, false);
        } else {
            ucm = ucm.setVarianceMax(-1, false);
        }
        return ucm;
    }

}
