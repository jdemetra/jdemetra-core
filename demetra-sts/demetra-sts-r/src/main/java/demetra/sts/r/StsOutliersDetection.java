/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;
import demetra.processing.ProcResults;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.ComponentUse;
import demetra.sts.SeasonalModel;
import demetra.timeseries.TsData;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.data.DataBlock;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.akf.AugmentedSmoother;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BasicStructuralModel;
import jdplus.sts.OutliersDetection;
import jdplus.sts.SsfBsm;
import jdplus.sts.extractors.BasicStructuralModelExtractor;
import jdplus.sts.internal.BsmMonitor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class StsOutliersDetection {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        BasicStructuralModel initialBsm, finalBsm;

        DoubleSeq y;
        MatrixType x;
        OutlierDescriptor[] outliers;

        double[] coefficients;
        MatrixType coefficientsCovariance;
        MatrixType regressors;

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

        static final String Y = "y", BSM0 = "initialbsm", BSM1 = "finalbsm",
                LL0 = "initiallikelihood", LL1 = "finallikelihood", B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", REGRESSORS = "regressors", BNAMES = "variables";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(BSM0, BasicStructuralModelExtractor.getMapping(), r -> r.getInitialBsm());
            MAPPING.delegate(BSM1, BasicStructuralModelExtractor.getMapping(), r -> r.getFinalBsm());
            MAPPING.delegate(LL0, LikelihoodStatisticsExtractor.getMapping(), r -> r.getInitialLikelihood());
            MAPPING.delegate(LL1, LikelihoodStatisticsExtractor.getMapping(), r -> r.getFinalLikelihood());
            MAPPING.set(B, double[].class, source -> source.getCoefficients());
            MAPPING.set(T, double[].class, source -> source.tstats());
            MAPPING.set(BVAR, MatrixType.class, source -> source.getCoefficientsCovariance());
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
        }
    }

    public Results process(TsData y, int level, int slope, int cycle, int noise, String seasmodel, MatrixType x,
            double cv, double tcv, String forwardEstimation, String backwardEstimation) {
        SeasonalModel sm = SeasonalModel.valueOf(seasmodel);
        BsmSpec mspec = new BsmSpec();
        mspec.setLevelUse(of(level));
        mspec.setSlopeUse(of(slope));
        mspec.setCycleUse(of(cycle));
        mspec.setNoiseUse(of(noise));
        mspec.setSeasonalModel(sm);
        OutliersDetection.Estimation fe = OutliersDetection.Estimation.valueOf(forwardEstimation);
        OutliersDetection.Estimation be = OutliersDetection.Estimation.valueOf(backwardEstimation);
        OutliersDetection od = OutliersDetection.builder()
                .bsm(mspec)
                .forwardEstimation(fe)
                .backardEstimation(be)
                .criticalValue(cv)
                .tcriticalValue(tcv)
                .build();
        if (!od.process(y.getValues(), Matrix.of(x), y.getAnnualFrequency())) {
            return null;
        }

        int[] ao = od.getAoPositions();
        int[] ls = od.getLsPositions();

        OutlierDescriptor[] outliers = new OutlierDescriptor[ao.length + ls.length];
        for (int i = 0; i < ao.length; ++i) {
            outliers[i] = new OutlierDescriptor("AO", ao[i]);
        }
        for (int i = 0, j = ao.length; i < ls.length; ++i, ++j) {
            outliers[j] = new OutlierDescriptor("LS", ls[i]);
        }
        int np = mspec.getParametersCount();

        return Results.builder()
                .initialBsm(od.getInitialModel())
                .finalBsm(od.getModel())
                .initialLikelihood(od.getInitialLikelihood().stats(0, np))
                .finalLikelihood(od.getLikelihood().stats(0, np))
                .regressors(od.getRegressors())
                .outliers(outliers)
                .coefficients(od.getLikelihood().coefficients().toArray())
                .coefficientsCovariance(od.getLikelihood().covariance(np, true))
                .x(x)
                .y(y.getValues())
                .build();
    }

    private ComponentUse of(int p) {
        if (p == 0) {
            return ComponentUse.Fixed;
        } else if (p > 0) {
            return ComponentUse.Free;
        } else {
            return ComponentUse.Unused;
        }
    }

    public double[] seasonalBreaks(TsData y, int level, int slope, int noise, String seasmodel, MatrixType x) {
        SeasonalModel sm = SeasonalModel.valueOf(seasmodel);
        BsmSpec mspec = new BsmSpec();
        mspec.setLevelUse(of(level));
        mspec.setSlopeUse(of(slope));
        mspec.setNoiseUse(of(noise));
        mspec.setSeasonalModel(sm);
        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(mspec);
        monitor.useDiffuseRegressors(true);
        int freq = y.getAnnualFrequency();
        Matrix X = Matrix.of(x);
        if (!monitor.process(y.getValues(), X, freq)) {
            return null;
        }
        BasicStructuralModel bsm = monitor.getResult();

        Ssf ssf = SsfBsm.of(bsm);
        int nx = 0;

        if (x != null) {
            ssf = RegSsf.ssf(ssf, X);
            nx = X.getColumnsCount();
        }
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(true);
        SsfData data = new SsfData(y.getValues());
        DefaultSmoothingResults sd = DefaultSmoothingResults.full();
        int n = data.length();
        double sig2 = monitor.getLikelihood().sigma();
        sd.prepare(ssf.getStateDim(), 0, data.length());
        smoother.process(ssf, data, sd);

        int spos = 0;
        if (bsm.getVariance(Component.Noise) != 0) {
            ++spos;
        }
        if (mspec.hasLevel()) {
            ++spos;
        }
        if (mspec.hasSlope()) {
            ++spos;
        }
        double[] s = new double[n];
        for (int i = 0; i < n; ++i) {
            try {
                DataBlock R = DataBlock.of(sd.R(i));
                Matrix Rvar = sd.RVariance(i);
                Matrix S = Rvar.extract(spos, freq - 1, spos, freq - 1).deepClone();
                DataBlock ur = R.extract(spos, freq - 1).deepClone();
                SymmetricMatrix.lcholesky(S, 1e-9);
                LowerTriangularMatrix.solveLx(S, ur, 1e-9);
                s[i] = ur.ssq() / sig2;

            } catch (Exception err) {
            }
        }
        return s;

    }
}
