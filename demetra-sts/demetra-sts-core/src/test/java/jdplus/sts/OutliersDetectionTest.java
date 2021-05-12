/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.ComponentUse;
import demetra.sts.SeasonalModel;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import java.util.Arrays;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixFactory;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.random.JdkRNG;
import demetra.dstats.RandomNumberGenerator;
import jdplus.ssf.akf.AugmentedSmoother;
import jdplus.ssf.akf.SmoothationsComputer;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.RandomSsfGenerator;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.internal.BsmMapping;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class OutliersDetectionTest {

    public OutliersDetectionTest() {
    }

    @Test
    public void testNile() {
        BsmSpec spec = BsmSpec.builder()
                .seasonal(null)
                .build();

        OutliersDetection od = OutliersDetection.builder()
                .bsm(spec)
                .build();
        DoubleSeq Y = DoubleSeq.of(Data.NILE);

        od.process(Y, null, 1);
        int[] ao = od.getAoPositions();
        System.out.print("AO:");
        for (int i = 0; i < ao.length; ++i) {
            System.out.print('\t');
            System.out.print(ao[i] + 1);
        }
        System.out.println();
        int[] ls = od.getLsPositions();
        System.out.print("LS:");
        for (int i = 0; i < ls.length; ++i) {
            System.out.print('\t');
            System.out.print(ls[i] + 1);
        }
        System.out.println();
    }

    @Test
    public void testProd() {
        BsmSpec spec = BsmSpec.builder()
                .seasonal(SeasonalModel.HarrisonStevens)
                .build();

        long t0 = System.currentTimeMillis();
        OutliersDetection od = OutliersDetection.builder()
                .bsm(spec)
                .forwardEstimation(OutliersDetection.Estimation.Full)
                .build();
        double[] A = Data.PROD.clone();
        A[14] *= 1.3;
        A[55] *= .7;
        DoubleSeq Y = DoubleSeq.of(A);

        Matrix days = Matrix.make(A.length, 7);
        GenericTradingDaysFactory.fillTdMatrix(TsPeriod.monthly(1967, 1), days);
        Matrix td = GenericTradingDaysFactory.generateContrasts(DayClustering.TD3, days);
        od.process(Y.log(), td, 12);
        long t1 = System.currentTimeMillis();
        int[] ao = od.getAoPositions();
        System.out.print("AO:");
        for (int i = 0; i < ao.length; ++i) {
            System.out.print('\t');
            System.out.print(ao[i] + 1);
        }
        System.out.println();
        int[] ls = od.getLsPositions();
        System.out.print("LS:");
        for (int i = 0; i < ls.length; ++i) {
            System.out.print('\t');
            System.out.print(ls[i] + 1);
        }
        System.out.println();
        System.out.println(t1 - t0);
//        System.out.println(DoubleSeq.of(A));
    }

    public static void main(String[] args) {
        stressTest();
    }

    public static void stressTest() {
        int K = 1000;
        BsmSpec spec = BsmSpec.builder()
                .seasonal(SeasonalModel.HarrisonStevens)
                .build();
        double[] A = Data.PROD.clone();
        A[14] *= 1.3;
        A[55] *= .7;
        DoubleSeq Y = DoubleSeq.of(A);
        Matrix days = Matrix.make(A.length, 7);
        GenericTradingDaysFactory.fillTdMatrix(TsPeriod.monthly(1967, 1), days);
        Matrix td = GenericTradingDaysFactory.generateContrasts(DayClustering.TD3, days);

        int[] length = new int[]{60, 120, 180, 240, 300, 336};
        for (int l = 0; l < length.length; ++l) {
            long t0 = System.currentTimeMillis();
            for (int k = 0; k < 10; ++k) {
                OutliersDetection od = OutliersDetection.builder()
                        .bsm(spec)
                        .build();
                od.process(Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()), 12);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
        for (int l = 0; l < length.length; ++l) {
            long t0 = System.currentTimeMillis();
            for (int k = 0; k < K; ++k) {
                BsmData model = new BsmData(spec, 12);
                forwardstep(model, Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()));
//                OutliersDetection od = OutliersDetection.builder()
//                        .bsm(spec)
//                        .maxIter(1)
//                        .build();
//                od.process(Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()), 12);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
    }

    public static void simulation() {
        int K = 100000;
        int[] NN = new int[]{/*20, 40, 60, 80, 100, 120, 180, 240, 300, */360, 420, 480, 540, 600};
        for (int h = 0; h < NN.length; ++h) {
            int N = NN[h];
            System.out.println(N);
            System.out.println("");
            for (int q = 0; q < 3; ++q) {

                BsmSpec spec = BsmSpec.builder()
                        .seasonal(SeasonalModel.HarrisonStevens)
                        .build();
                int period = 4;

                Matrix M = Matrix.make(N, K);
                double[] SAO = new double[K];
                double[] SLS = new double[K];
                double[] SSO = new double[K];
                double[] SALL = new double[K];
                BsmData[] models = randomBsm(spec, period, M);
                DataBlockIterator cols = M.columnsIterator();
                int k = 0;
                while (cols.hasNext()) {
                    DataBlock y = cols.next();
                    Ssf ssf = SsfBsm.of(models[k]);
                    SsfData data = new SsfData(y);
                    AugmentedSmoother smoother = new AugmentedSmoother();
                    smoother.setCalcVariances(true);
                    DefaultSmoothingResults sd = DefaultSmoothingResults.full();
                    sd.prepare(ssf.getStateDim(), 0, data.length());
                    smoother.process(ssf, data, sd);
                    double sig2 = DkToolkit.likelihood(ssf, data, true, false).sigma2();
                    double saomax = 0, slsmax = 0, ssomax = 0, smax = 0;
                    for (int i = 4; i < N - 4; ++i) {
                        DataBlock R = DataBlock.of(sd.R(i));
                        Matrix Rvar = sd.RVariance(i);
                        double sao = R.get(0) * R.get(0) / Rvar.get(0, 0) / sig2;
                        double sls = R.get(1) * R.get(1) / Rvar.get(1, 1) / sig2;
                        double sso = R.get(3) * R.get(3) / Rvar.get(3, 3) / sig2;
                        int[] sel = new int[]{0, 1, 3};
                        Matrix S = MatrixFactory.select(Rvar, sel, sel);
                        DataBlock ur = DataBlock.select(R, sel);
                        double sall = 0;
                        try {
                            SymmetricMatrix.lcholesky(S, 1e-9);
                            LowerTriangularMatrix.solveLx(S, ur, 1e-9);
                            sall = ur.ssq() / sig2;
                        } catch (Exception err) {
                        }
                        if (!Double.isFinite(sao)) {
                            sao = 0;
                        }
                        if (!Double.isFinite(sls)) {
                            sls = 0;
                        }
                        if (!Double.isFinite(sall)) {
                            sall = 0;
                        }
                        if (sao > saomax) {
                            saomax = sao;
                        }
                        if (sls > slsmax) {
                            slsmax = sls;
                        }
                        if (sso > ssomax) {
                            ssomax = sso;
                        }
                        if (sall > smax) {
                            smax = sall;
                        }
                    }
                    SAO[k] = saomax;
                    SLS[k] = slsmax;
                    SSO[k] = ssomax;
                    SALL[k++] = smax;
                }
                Arrays.sort(SAO);
                Arrays.sort(SLS);
                Arrays.sort(SSO);
                Arrays.sort(SALL);
                System.out.println(SAO[(int) (K * .95)]);
                System.out.println(SLS[(int) (K * .95)]);
                System.out.println(SSO[(int) (K * .95)]);
                System.out.println(SALL[(int) (K * .95)]);
            }
            System.out.println("");
        }
    }

    public static BsmData[] randomBsm(BsmSpec spec, int period, Matrix simul) {
        double[] p = new double[spec.getFreeParametersCount()];
        Random rnd = new Random();
        BsmMapping mapping = new BsmMapping(spec, period, null);
        BsmData[] models = new BsmData[simul.getColumnsCount()];

        DataBlockIterator cols = simul.columnsIterator();
        int i = 0;
        while (cols.hasNext()) {
            for (int j = 0; j < p.length; ++j) {
                p[j] = Math.exp(rnd.nextGaussian());
            }
            BsmData bsm = mapping.map(DoubleSeq.of(p));
            SsfBsm ssf = SsfBsm.of(bsm);
            RandomSsfGenerator generator = new RandomSsfGenerator(ssf, 1, simul.getRowsCount());
            generator.newSimulation(cols.next(), RNG);
            models[i++] = bsm;
        }
        return models;
    }

    private static final RandomNumberGenerator RNG = JdkRNG.newRandom();

    private static boolean forwardstep(BsmData model, DoubleSeq y, Matrix W) {
        SsfBsm ssf = SsfBsm.of(model);
        Ssf wssf = W == null ? ssf : RegSsf.ssf(ssf, W);
        SsfData data = new SsfData(y);
        int n = data.length();
        SmoothationsComputer computer = new SmoothationsComputer();
        computer.process(wssf, data);
        double sig2 = computer.getFilteringResults().var();
        boolean isnoise = model.getNoiseVar() > 0;
        for (int i = 0; i < n; ++i) {
            try {
                DataBlock R = computer.R(i);
                Matrix Rvar = computer.Rvar(i);
                double sao = 0, sls = 0, sall = 0;
                sao = R.get(0) * R.get(0) / Rvar.get(0, 0) / sig2;
                if (i > 0) {
                    int c = isnoise ? 1 : 0;
                    sls = R.get(c) * R.get(c) / Rvar.get(c, c) / sig2;
                }
                double sso = R.get(3) * R.get(3) / Rvar.get(3, 3) / sig2;
                int[] sel = new int[]{0, 1, 3};
                Matrix S = MatrixFactory.select(Rvar, sel, sel);
                DataBlock ur = DataBlock.select(R, sel);
                try {
                    SymmetricMatrix.lcholesky(S, 1e-9);
                    LowerTriangularMatrix.solveLx(S, ur, 1e-9);
                    sall = ur.ssq() / sig2;
                } catch (Exception err) {
                }
            } catch (Exception err) {
            }
        }
        return true;
    }
}
