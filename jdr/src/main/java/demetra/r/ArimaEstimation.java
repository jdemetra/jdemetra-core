/*
 * Copyright 2019 National Bank of Belgium
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

import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.information.InformationMapping;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.arima.SarimaProcess;
import demetra.sarima.RegSarimaProcessor;
import demetra.descriptors.arima.SarimaDescriptor;
import demetra.util.IntList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.Setter
public class ArimaEstimation {

    private double[] phi, theta, bphi, btheta;
    private int[] order, seasonalOrder;
    private int period;
    private double[] y;
    private final List<double[]> xreg = new ArrayList<>();
    private boolean mean;

    public void addX(double[] x) {
        xreg.add(x);
    }

    public Results process() {
        SarimaSpecification spec = new SarimaSpecification(period);
        if (order != null) {
            spec.setP(order[0]);
            spec.setD(order[1]);
            spec.setQ(order[2]);
        }
        if (seasonalOrder != null) {
            spec.setBp(seasonalOrder[0]);
            spec.setBd(seasonalOrder[1]);
            spec.setBq(seasonalOrder[2]);
        }
        SarimaModel.Builder builder = SarimaModel.builder(spec);
        //
        SarimaModel arima = builder.setDefault().build();
        RegSarimaProcessor monitor = RegSarimaProcessor.builder()
                .useParallelProcessing(true)
                .useMaximumLikelihood(true)
                .useCorrectedDegreesOfFreedom(false) // compatibility with R
                .precision(1e-12)
                .startingPoint(RegSarimaProcessor.StartingPoint.Multiple)
                .computeExactFinalDerivatives(true)
                .build();

        IntList missings = new IntList();
        demetra.data.interpolation.AverageInterpolator.cleanMissings(y, missings);
        RegArimaModel.Builder<SarimaModel> rbuilder = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSeq.copyOf(y))
                .arima(arima)
                .meanCorrection(mean)
                .missing(missings.toArray());

        for (double[] x : xreg) {
            rbuilder.addX(DoubleSeq.of(x));
        }

        RegArimaEstimation<SarimaModel> rslt = monitor.process(rbuilder.build());
        return new Results(rslt.getModel(), rslt.getConcentratedLikelihood(), rslt.statistics(0),
                SymmetricMatrix.inverse(rslt.getMax().getHessian()), rslt.getMax().getGradient());
    }

    @lombok.Value
    public static class Results implements ProcResults {

        RegArimaModel<SarimaModel> regarima;
        ConcentratedLikelihoodWithMissing concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        FastMatrix parametersCovariance;
        double[] score;

        public SarimaProcess getArima() {
            return regarima.arima().toType();
        }

        private static final String ARIMA = "arima", LL = "likelihood", PCOV = "pcov", SCORE = "score",
                B = "b", UNSCALEDBVAR = "unscaledbvar", MEAN = "mean";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(ARIMA, SarimaDescriptor.getMapping(), r -> r.getArima());
            MAPPING.delegate(LL, LikelihoodStatisticsDescriptor.getMapping(), r -> r.statistics);
            MAPPING.set(PCOV, FastMatrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
            MAPPING.set(B, double[].class, source
                    -> {
                DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                return b.toArray();
            });
            MAPPING.set(MEAN, Double.class, source
                    -> {
                if (source.getRegarima().isMean()) {
                    DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                    int mpos = source.getRegarima().getMissingValuesCount();
                    return b.get(mpos);
                } else {
                    return 0.0;
                }
            });
            MAPPING.set(UNSCALEDBVAR, FastMatrix.class, source -> source.getConcentratedLogLikelihood().unscaledCovariance());
        }

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

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

    }
}
