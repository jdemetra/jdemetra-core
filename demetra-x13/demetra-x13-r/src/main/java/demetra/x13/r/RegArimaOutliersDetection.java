/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.x13.r;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Iterables;
import demetra.information.GenericExplorable;
import demetra.information.InformationMapping;
import demetra.math.matrices.Matrix;
import demetra.modelling.OutlierDescriptor;
import demetra.outliers.io.protobuf.OutliersProtos;
import demetra.timeseries.TsData;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.PeriodicOutlierFactory;
import jdplus.modelling.regression.TransitoryChangeFactory;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaToolkit;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.regarima.outlier.SingleOutlierDetector;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.regsarima.ami.ExactOutliersDetector;
import jdplus.sarima.SarimaModel;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.stats.RobustStandardDeviationComputer;
import jdplus.stats.likelihood.LikelihoodStatistics;
import jdplus.x13.regarima.X13Utility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaOutliersDetection {

    @lombok.Value
    @lombok.Builder
    public static class Results implements GenericExplorable {

        public byte[] buffer() {
            int nx = x == null ? 0 : x.getColumnsCount();
            OutliersProtos.RegArimaSolution.Builder builder = OutliersProtos.RegArimaSolution.newBuilder()
                    .addAllArimaInitial(Iterables.of(initialArima.parameters()))
                    .addAllArimaFinal(Iterables.of(finalArima.parameters()))
                    .addAllCoefficients(Iterables.of(coefficients))
                    .setCovariance(ToolkitProtosUtility.convert(coefficientsCovariance))
                    .setRegressors(ToolkitProtosUtility.convert(regressors))
                    .setLikelihoodInitial(ToolkitProtosUtility.convert(initialLikelihood))
                    .setLikelihoodFinal(ToolkitProtosUtility.convert(finalLikelihood))
                    .addAllResiduals(Iterables.of(residuals));
            
            DoubleSeq diag = coefficientsCovariance.diagonal();
            for (int i = 0, j = nx; i < outliers.length; ++i, ++j) {
                builder.addOutliers(
                        OutliersProtos.Outlier.newBuilder()
                                .setCode(outliers[i].getCode())
                                .setPosition(outliers[i].getPosition())
                                .setCoefficient(coefficients[j])
                                .setStde(Math.sqrt(diag.get(j)))
                                .build());
            }
            return builder.build().toByteArray();
        }
        SarimaModel initialArima, finalArima;

        DoubleSeq y;
        Matrix x;
        OutlierDescriptor[] outliers;

        double[] coefficients;
        Matrix coefficientsCovariance;
        Matrix regressors;
        DoubleSeq linearized;
        DoubleSeq residuals;

        LikelihoodStatistics initialLikelihood, finalLikelihood;

        public double[] tstats() {
            double[] t = coefficients.clone();
            if (t == null) {
                return null;
            }
            DoubleSeqCursor v = coefficientsCovariance.diagonal().cursor();
            for (int i = 0; i < t.length; ++i) {
                t[i] /= Math.sqrt(v.getAndNext());
            }
            return t;
        }

        public int getNx() {
            return coefficients == null ? 0 : coefficients.length;
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

        static final String Y = "y", ARIMA0 = "initialarima", ARIMA1 = "finalarima",
                LL0 = "initiallikelihood", LL1 = "finallikelihood", B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", REGRESSORS = "regressors", BNAMES = "variables",
                CMPS = "cmps", LIN = "linearized";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class<Results> getSourceClass() {
                return Results.class;
            }
        };

        static {
            MAPPING.delegate(ARIMA0, SarimaModel.class, r -> r.getInitialArima());
            MAPPING.delegate(ARIMA1, SarimaModel.class, r -> r.getFinalArima());
            MAPPING.delegate(LL0, LikelihoodStatistics.class, r -> r.getInitialLikelihood());
            MAPPING.delegate(LL1, LikelihoodStatistics.class, r -> r.getFinalLikelihood());
            MAPPING.set(B, double[].class, source -> source.getCoefficients());
            MAPPING.set(T, double[].class, source -> source.tstats());
            MAPPING.set(BVAR, Matrix.class, source -> source.getCoefficientsCovariance());
            MAPPING.set(BNAMES, String[].class, source -> {
                int nx = source.getNx();
                if (nx == 0) {
                    return null;
                }
                String[] names = new String[nx];
                OutlierDescriptor[] outliers = source.getOutliers();
                int no = outliers == null ? 0 : outliers.length;
                for (int i = 0; i < nx - no; ++i) {
                    names[i] = "x-" + (i + 1);
                }
                for (int i = nx - no, j = 0; i < nx; ++i, ++j) {
                    names[i] = outliers[j].toString();
                }

                return names;
            });
            MAPPING.set(OUTLIERS, String[].class, source -> {
                OutlierDescriptor[] outliers = source.getOutliers();
                int no = outliers == null ? 0 : outliers.length;
                String[] names = new String[no];
                for (int i = 0; i < no; ++i) {
                    names[i] = outliers[i].toString();
                }
                return names;
            });
            MAPPING.set(REGRESSORS, Matrix.class, source -> source.getRegressors());
            MAPPING.set(LIN, double[].class, source -> source.getLinearized().toArray());
        }
    }

    public Results process(TsData ts, int[] order, int[] seasonal, boolean mean, Matrix x,
            boolean bao, boolean bls, boolean btc, boolean bso, double cv) {
        TsData y = ts.cleanExtremities();
        if (x != null && ts.length() != y.length()){
            int start=ts.getStart().until(y.getStart());
            x=x.extract(start, y.length(), 0, x.getColumnsCount());
        }
        SarimaOrders spec = new SarimaOrders(y.getAnnualFrequency());
        spec.setP(order[0]);
        spec.setD(order[1]);
        spec.setQ(order[2]);
        if (seasonal != null) {
            spec.setBp(seasonal[0]);
            spec.setBd(seasonal[1]);
            spec.setBq(seasonal[2]);
        }

        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();

        RegArimaModel regarima = RegArimaModel.builder()
                .arima(arima)
                .meanCorrection(mean)
                .y(y.getValues())
                .addX(FastMatrix.of(x))
                .build();
        RegArimaEstimation<SarimaModel> estimation0 = RegSarimaComputer.builder()
                .build()
                .process(regarima, null);

        SingleOutlierDetector sod = new ExactSingleOutlierDetector(RobustStandardDeviationComputer.mad(false),
                null, X13Utility.mlComputer());
        
        List<IOutlierFactory> factory=new ArrayList<>();
        if (bao) {
            factory.add(AdditiveOutlierFactory.FACTORY);
        }
        if (bls) {
            factory.add(LevelShiftFactory.FACTORY_ZEROENDED);
        }
        int freq=y.getAnnualFrequency();
        if (btc) {
            double c = 0.7;
            int r = 12 / freq;
            if (r > 1) {
                c = Math.pow(c, r);
            }
            factory.add(new TransitoryChangeFactory(c));
        }
        if (freq > 1 && bso) {
            factory.add(new PeriodicOutlierFactory(freq, true));
        }
        sod.setOutlierFactories(factory.toArray(new IOutlierFactory[factory.size()]));

        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(sod)
                .criticalValue(cv <= 0 ? X13Utility.calcCv(y.length()) : cv)
                .processor(RegArimaUtility.processor(true, 1e-7))
                .build();

        od.prepare(y.length());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] o = od.getOutliers();
        String[] ids = od.outlierTypes();

        OutlierDescriptor[] outliers = new OutlierDescriptor[o.length];
        for (int i = 0; i < o.length; ++i) {
            outliers[i] = new OutlierDescriptor(ids[o[i][1]], o[i][0]);
        }

        int np = spec.getParametersCount();
        RegArimaModel<SarimaModel> regarima1 = od.getRegArima();
        RegArimaEstimation<SarimaModel> estimation1 = RegArimaToolkit.concentratedLikelihood(regarima1);
        return Results.builder()
                .initialArima(estimation0.getModel().arima())
                .finalArima(regarima1.arima())
                .initialLikelihood(estimation0.statistics())
                .finalLikelihood(estimation1.statistics())
                .regressors(estimation1.getModel().variables())
                .outliers(outliers)
                .coefficients(estimation1.getConcentratedLikelihood().coefficients().toArray())
                .coefficientsCovariance(estimation1.getConcentratedLikelihood().covariance(np, true))
                .x(x)
                .y(y.getValues())
                .linearized(RegArimaUtility.linearizedData(estimation1.getModel(), estimation1.getConcentratedLikelihood()))
                .residuals(estimation1.getConcentratedLikelihood().e())
                .build();
    }

}
