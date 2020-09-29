/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.sts.BsmSpec;
import demetra.sts.ComponentUse;
import demetra.sts.SeasonalModel;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import java.util.Arrays;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.Normal;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixFactory;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.random.JdkRNG;
import jdplus.random.RandomNumberGenerator;
import jdplus.ssf.akf.AugmentedSmoother;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.RandomSsfGenerator;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.internal.BsmMapping;
import jdplus.sts.internal.BsmMonitor;
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
        BsmSpec spec = new BsmSpec();
        spec.setSeasUse(ComponentUse.Unused);
        spec.setSlopeUse(ComponentUse.Free);
        spec.setLevelUse(ComponentUse.Free);

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
        BsmSpec spec = new BsmSpec();
        spec.setSeasUse(ComponentUse.Free);
        spec.setSeasonalModel(SeasonalModel.HarrisonStevens);
        spec.setSlopeUse(ComponentUse.Free);
        spec.setLevelUse(ComponentUse.Free);

        long t0 = System.currentTimeMillis();
        OutliersDetection od = OutliersDetection.builder()
                .bsm(spec)
                .forwardEstimation(OutliersDetection.Estimation.Full)
                .build();
        double[] A = Data.PROD.clone();
        A[74] *= 1.3;
        A[69] *= .1;
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
        int K = 100000;
        int[] NN = new int[]{/*20, 40, 60, 80, 100, 120, 180, 240, 300, */360, 420, 480, 540, 600};
        for (int h = 0; h < NN.length; ++h) {
            int N = NN[h];
            System.out.println(N);
            System.out.println("");
            for (int q = 0; q < 3; ++q) {

                BsmSpec spec = new BsmSpec();
                spec.setSeasUse(ComponentUse.Unused);
                spec.setSeasUse(ComponentUse.Free);
                spec.setSeasonalModel(SeasonalModel.HarrisonStevens);
                spec.setSlopeUse(ComponentUse.Free);
                spec.setLevelUse(ComponentUse.Free);
                int period = 4;

                Matrix M = Matrix.make(N, K);
                double[] SAO = new double[K];
                double[] SLS = new double[K];
                double[] SSO = new double[K];
                double[] SALL = new double[K];
                BasicStructuralModel[] models = randomBsm(spec, period, M);
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
                    double sig2 = DkToolkit.likelihood(ssf, data, true, false).sigma();
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

    public static BasicStructuralModel[] randomBsm(BsmSpec spec, int period, Matrix simul) {
        double[] p = new double[spec.getParametersCount()];
        Random rnd = new Random();
        BsmMapping mapping = new BsmMapping(spec, period, BsmMapping.Transformation.None);
        BasicStructuralModel[] models = new BasicStructuralModel[simul.getColumnsCount()];

        DataBlockIterator cols = simul.columnsIterator();
        int i = 0;
        while (cols.hasNext()) {
            for (int j = 0; j < p.length; ++j) {
                p[j] = Math.exp(rnd.nextGaussian());
            }
            BasicStructuralModel bsm = mapping.map(DoubleSeq.of(p));
            SsfBsm ssf = SsfBsm.of(bsm);
            RandomSsfGenerator generator = new RandomSsfGenerator(ssf, 1, simul.getRowsCount());
            generator.newSimulation(cols.next(), RNG);
            models[i++] = bsm;
        }
        return models;
    }

    private static final RandomNumberGenerator RNG = JdkRNG.newRandom();

}
