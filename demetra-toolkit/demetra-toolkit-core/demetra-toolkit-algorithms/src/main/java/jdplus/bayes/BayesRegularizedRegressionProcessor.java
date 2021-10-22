/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import jdplus.bayes.BayesRegularizedRegressionModel.ModelType;
import jdplus.bayes.BayesRegularizedRegressionModel.Prior;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.Normal;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.random.MersenneTwister;
import demetra.dstats.RandomNumberGenerator;
import jdplus.stats.samples.Moments;

/**
 *
 * @author PALATEJ
 */
public class BayesRegularizedRegressionProcessor {

    @lombok.Value
    @lombok.Builder
    public static class Spec {

        public static Builder builder() {
            return new Builder()
                    .nsamples(1000)
                    .burnin(1000)
                    .thin(5)
                    .normalize(true);
        }

        int nsamples;
        int burnin;
        int thin;
        int blocksize;
        boolean waic;
        boolean normalize;
    }

    private final RandomNumberGenerator rng = MersenneTwister.fromSystemNanoTime();

    private final Spec spec;

    private BayesRegularizedRegressionModel model;

    private int n, p;
    private DataBlock z;
    private double muZ, ztz;
    private FastMatrix X;
    private double[] muX;
    private double[] stdX;

    private double b0;
    private DataBlock b, omega2;
    private double sigma2, muSigma2;
    private double tau2; // global shrinkage parameter
    private double xi, psi2, muPsi2;
    private DataBlock lambda2, nu, phi2, zeta, w2;
    private DataBlock e;

    // Quantities for computing WAIC 
    private DataBlock waicProb, waicLProb, waicLProb2;

    // Use Rue's MVN sampling algorithm
    private boolean mvnrue;
    private FastMatrix XtX;
    private DataBlock Xtz;

    public BayesRegularizedRegressionProcessor(Spec spec) {
        this.spec = spec;
    }

    public BayesRegularizedRegressionResults process(BayesRegularizedRegressionModel model) {
        clear();
        initialize(model);
        if (spec.isNormalize()) {
            standardize(false);
        }
        precompute();
        return null;
    }

    private void clear() {
        model = null;
        n = 0;
        p = 0;
        z = null;
        muZ = 0;
        X = null;
        muX = null;
        stdX = null;
    }

    private void initialize(BayesRegularizedRegressionModel model) {
        this.model = model;
        z = DataBlock.of(model.getY().toArray());
        X = FastMatrix.of(model.getX());
        n = z.length();
        p = X.getColumnsCount();

        b0 = 0;
        b = DataBlock.of(p, () -> Normal.random(rng, 0, 1));
        sigma2 = 1;
        e = null;
        tau2 = 1;
        xi = .001;
        lambda2 = DataBlock.make(p, 1);
        omega2 = DataBlock.make(n, 1);
        nu = DataBlock.make(p, 1);
        phi2 = DataBlock.make(p, 1);
        zeta = DataBlock.make(p, 1);
        psi2 = 1;
        muPsi2 = 0;
        w2 = DataBlock.make(p, 1);
        // Quantities for computing WAIC 
        waicProb = DataBlock.make(n);
        waicLProb = DataBlock.make(n);
        waicLProb2 = DataBlock.make(n);

        // Use Rue's MVN sampling algorithm or Bhatta's MVN sampling algorithm
        mvnrue = p < 2 * n;

        // Precompute XtX?
        XtX = null;
        Xtz = null;
    }

    private void standardize(boolean bz) {
        muX = new double[p];
        stdX = new double[p];
        int pos = 0;
        DataBlockIterator cols = X.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            double mean = Moments.mean(col);
            double std = Math.sqrt(Moments.variance(col, mean, false) * n);
            col.apply(a -> (a - mean) / std);
            muX[pos] = mean;
            stdX[pos++] = std;
        }
        if (bz) {
            muZ = z.average();
            z.sub(muZ);
        }
    }

    private boolean isGaussian() {
        return model.getModel() == ModelType.GAUSSIAN;
    }

    private void precompute() {
        if ((isGaussian() && mvnrue)
                || model.getPrior() == Prior.G) {
            if (isGaussian()) {
                Xtz = DataBlock.make(p);
                Xtz.product(z, X.columnsIterator());
                ztz = z.ssq();
            }
            XtX = SymmetricMatrix.XtX(X);
        }
    }

    private double tauA() {
        switch (model.getTau2Prior()) {
            case HC:
                return .5;
            case SB:
                return .5;
            default:
                return 1;
        }
    }

    private double tauB() {
        switch (model.getTau2Prior()) {
            case HC:
                return .5;
            case SB:
                return 1;
            default:
                return 1;
        }
    }

}
