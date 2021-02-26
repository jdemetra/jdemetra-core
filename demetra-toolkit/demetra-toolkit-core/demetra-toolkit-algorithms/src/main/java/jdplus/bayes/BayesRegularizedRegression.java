/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoublesMath;
import demetra.math.matrices.MatrixType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.Exponential;
import jdplus.dstats.Gamma;
import jdplus.dstats.InverseGaussian;
import jdplus.dstats.Normal;
import jdplus.dstats.internal.SpecialFunctions;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.random.MersenneTwister;
import jdplus.random.RandomNumberGenerator;
import jdplus.stats.samples.Moments;

/**
 * TODO See R package bayesreg
 * For the time being, I only implemented the linear regression (logistic will
 * be implemented later)
 *
 * @author PALATEJ
 */
public class BayesRegularizedRegression {

    public static enum Model {
        GAUSSIAN, LAPLACE, T;
    }

    public static enum Prior {
        RIDGE,
        LASSO,
        HORSESHOE,
        HORSESHOEPLUS
    }

    private final DataBlock y;
    private final Matrix X;
    private final Model model;
    private final Prior prior;
    private final int burnin, nsamples;
    private final int tdf;

    // auxiliary information
    private double[] xm, xstd;
    private int n, p;
    private double ydiff;
    private double b0;
    private DataBlock b, omega2;
    private double sigma2, muSigma2;
    private double tau2; // global shrinkage parameter
    private double xi;
    private DataBlock lambda2, nu, eta2, phi, kappa, z;
    private DataBlock e;

    // Quantities for computing WAIC 
    private DataBlock waicProb, waicLProb, waicLProb2;

    // Use Rue's MVN sampling algorithm
    private boolean mvnrue, precomputedXtX;
    private Matrix XtX;
    private DataBlock Xty;
    
    @lombok.Value
    public static class Result{
        DoubleSeq b;
        double b0;
        double tau2;
    }
    
    public static final List<Result> results=new ArrayList<>();

    private RandomNumberGenerator rng = MersenneTwister.fromSystemNanoTime();

    public BayesRegularizedRegression(final DoubleSeq y, final MatrixType X, final Model model, final int tdf, final Prior prior,
            final int burnin, final int nsamples) {
        this.y = DataBlock.of(y);
        this.X = Matrix.of(X);
        this.model = model;
        this.tdf = tdf;
        this.prior = prior;
        this.burnin = burnin;
        this.nsamples = nsamples;
        initialize();
        standardize();
        int k = 0;
        while (k < nsamples) {
            samplingIteration();
            if (k >= burnin) {
                // Store results
                results.add(new Result(b.fn(DoubleSeq.of(xstd), (x,q)->x*q),b0,tau2));
            }
            ++k;
        }
    }
    
    public List<Result> results(){
        return Collections.unmodifiableList(results);
    }

    private void standardize() {
        
        xm = new double[p];
        xstd = new double[p];
        int pos = 0;
        DataBlockIterator cols = X.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            double mean = Moments.mean(col);
            double std = Math.sqrt(Moments.variance(col, mean, true));
            col.apply(a -> (a - mean) / std);
            xm[pos] = mean;
            xstd[pos++] = std;
        }
    }

    private void initialize() {
        // Initial values
        n = y.length();
        p = X.getColumnsCount();
        ydiff = 0;
        b0 = 0;
        b = DataBlock.make(p);
        omega2 = DataBlock.make(n);
        omega2.set(1);
        sigma2 = 1;
        tau2 = 1;
        xi = 1;
        lambda2 = DataBlock.make(p);
        lambda2.set(1);
        nu = DataBlock.make(p);
        nu.set(1);
        eta2 = DataBlock.make(p);
        eta2.set(1);
        phi = DataBlock.make(p);
        phi.set(1);

        kappa = y.deepClone();
        kappa.sub(.5);
        z = y.deepClone();

        // Quantities for computing WAIC 
        waicProb = DataBlock.make(n);
        waicLProb = DataBlock.make(n);
        waicLProb2 = DataBlock.make(n);

        // Use Rue's MVN sampling algorithm
        mvnrue = true;
        precomputedXtX = false;
        if (p >= 2 * n) {
            // Switch to Bhatta's MVN sampling algorithm
            mvnrue = false;
        }

        // Precompute XtX?
        XtX = null;
        Xty = null;

        if (model == Model.GAUSSIAN && mvnrue) {
            XtX = SymmetricMatrix.XtX(X);
            Xty = DataBlock.make(p);
            Xty.product(X.columnsIterator(), y);
            precomputedXtX = true;
        }
    }

    private void samplingIteration() {
        // sample beta
        sampleBeta();
        // sample beta0
        sampleBeta0();
        // sample sigma2
        sampleSigma2();
        // sample omega2
        sampleOmega2();
        // sample tau2
        sampleTau2();
        // sample lambda2
        sampleLambda2();
        // sample delta2
        sampleDelta2();
    }

    // sample the coefficients of the regression
    private void sampleBeta() {
        DoubleSeq alpha = z.fastOp(q -> q - b0);
        double d = sigma2 * tau2;
        DoubleSeq Lambda = lambda2.fastOp(q -> q * d);
        double sigma = Math.sqrt(sigma2);
        if (mvnrue) {
            if (!precomputedXtX) {
                DoubleSeq omega = omega2.fn(q -> Math.sqrt(q) * sigma);
                Matrix X0 = X.deepClone();
                DoubleSeqCursor ocur = omega.cursor();
                DataBlockIterator cols = X0.columnsIterator();
                while (cols.hasNext()) {
                    cols.next().div(ocur.getAndNext());
                }
                rue_nongaussian(X0, alpha, Lambda, sigma2, omega);
            } else {
                rue_gaussian(X, alpha, Lambda, XtX, Xty, sigma2);
            }
        }

    }

    // sample the coefficients of the regression
    private void sampleBeta0() {
        // e = y - X*beta
        // It is usually more efficient to work by column
        DataBlockIterator cols = X.columnsIterator();
        DoubleSeqCursor.OnMutable bcur = b.cursor();
        e = y.deepClone();
        while (cols.hasNext()) {
            DataBlock coli = cols.next();
            double bi = bcur.getAndNext();
            e.addAY(-bi, coli);
        }

        // w = sum(1/omega2)
        double W = omega2.fastOp(q -> 1 / q).sum();
        // muB0    = sum(e / omega2) / W
        double muB0 = e.fastOp(omega2, (q, w) -> q / w).sum() / W;

        // v = sigma2 / W
        double v = sigma2 / W;

        // Sample b0 and update residuals
        Normal N = new Normal(muB0, Math.sqrt(v));
        b0 = N.random(rng);
        e.sub(b0);
    }

    private void sampleSigma2() {
        double shape = (n + p) / 2.0;
        double scale = e.fastOp(omega2, (q, w) -> q * q / w).sum() / 2 + b.fastOp(lambda2, (q, l) -> q * q / l).sum() / (tau2 * 2);
        sigma2 = 1 / Gamma.random(rng, shape, 1/scale);
        muSigma2 = scale / (shape - 1);
    }

    private void sampleOmega2() {
        switch (model) {
            case LAPLACE: {
                // mu = sqrt(2 * sigma2 / e^2)
                // omega2 = 1 / bayesreg.rinvg(mu,1/2)
                // we use lambda, which is the shape. In R, 1/shape is used!
                omega2 = DataBlock.of(e.fastOp(q -> Math.sqrt(2 * sigma2 / (q * q))));
                omega2.apply(q -> 1 / InverseGaussian.random(rng, q, .2));
            }
            break;
            case T: {
                double shape = (tdf + 1) * .5;
                omega2 = DataBlock.of(e.fastOp(q -> (q * q / sigma2 + tdf) / 2));
                omega2.apply(q -> Gamma.random(rng, shape, 1 / q));
            }
        }
    }

    private void sampleTau2() {
        switch (prior) {
            case HORSESHOE:
            case HORSESHOEPLUS:
            case RIDGE: {
                double shape = (p + 1) * .5;
                double scale = 1 / xi + b.fastOp(lambda2, (q, l) -> q * q / l).sum() / (2 * sigma2);
                tau2 = 1 / Gamma.random(rng, shape, 1 / scale);
                scale = 1 + 1 / tau2;
                xi = scale / Exponential.random(rng, 1);
            }
            break;
            case LASSO: {
                double shape = p * .5 + 1;
                double scale = 1 + b.fastOp(lambda2, (q, l) -> q * q / l).sum() / (2 * sigma2);
                tau2 = 1 / Gamma.random(rng, shape, 1 / scale);
            }
        }
    }

    private void sampleLambda2() {
        switch (prior) {
            case HORSESHOE: {
                double d = 2 * tau2 * sigma2;
                DoubleSeq scale = b.fastOp(nu, (q, c) -> 1 / c + q * q / d);
                lambda2.set(scale, s -> s / Exponential.random(rng, 1));

                scale = lambda2.fastOp(q -> 1 + 1 / q);
                nu.set(scale, s -> s / Exponential.random(rng, 1));
            }
            break;
            case HORSESHOEPLUS: {
                double d = 2 * tau2 * sigma2;
                DoubleSeq be = b.fastOp(eta2, (q, t) -> q * q / (d * t));

                DoubleSeq scale = nu.fastOp(be, (q, c) -> 1 / q + c);
                lambda2.set(scale, s -> s / Exponential.random(rng, 1));

                scale = lambda2.fastOp(q -> 1 + 1 / q);
                nu.set(scale, s -> s / Exponential.random(rng, 1));

                DoubleSeq bl = b.fastOp(lambda2, (q, l) -> q * q / (d * l));
                scale = phi.fastOp(bl, (q, l) -> 1 / q + l);
                eta2.set(scale, s -> s / Exponential.random(rng, 1));

                scale = eta2.fastOp(q -> 1 + 1 / q);
                phi.set(scale, s -> s / Exponential.random(rng, 1));

                lambda2.op(eta2, (l, t) -> l * t);
            }
            break;
            case LASSO: {
                double d = 2 * sigma2 * tau2;
                DoubleSeq mu = b.fastOp(q -> Math.sqrt(d / (q * q)));
                lambda2.set(mu, q -> 1 / InverseGaussian.random(rng, q, 2));
            }
        }
    }

    private void sampleDelta2() {
        // NO GROUP FOR THE MOMENT
    }

    /**
     * compute the errors of the linear regression
     *
     * @return
     */
    private void errLinearRegression() {
        // It is usually more efficient to work by column
        DataBlockIterator cols = X.columnsIterator();
        DoubleSeqCursor.OnMutable bcur = b.cursor();
        e = y.deepClone();
        e.sub(b0);
        while (cols.hasNext()) {
            DataBlock coli = cols.next();
            double bi = bcur.getAndNext();
            e.addAY(-bi, coli);
        }
    }

    private double nllLinearRegression() {
        errLinearRegression();
        switch (model) {
            case GAUSSIAN:
                return glr();
            case LAPLACE:
                return llr();
            case T:
                return tlr();
            default:
                return Double.NaN;
        }
    }

    /**
     * Gaussian likelihood
     *
     * @return
     */
    private double glr() {
        return n / 2 * Math.log(2 * Math.PI * sigma2) + 1 / (2 * sigma2) * e.ssq();
    }

    /**
     * Laplace likelihood
     *
     * @return
     */
    private double llr() {
        double scale = Math.sqrt(sigma2 / 2);
        return n * Math.log(2 * scale) + e.norm1() / scale;
    }

    private double tlr() {
        DoubleSeq te = e.fastOp(z -> Math.log(1 + z * z / (tdf * sigma2)));
        double c = te.sum();
        return n * (-SpecialFunctions.logGamma((tdf + 1.0) / 2) + SpecialFunctions.logGamma(tdf / 2.0) + Math.log(Math.PI * tdf * sigma2) / 2) + (tdf + 1) / 2 * c;
    }

    private void rue_nongaussian(Matrix X0, DoubleSeq alpha, DoubleSeq Lambda, double sigma2, DoubleSeq omega) {
        Matrix S=SymmetricMatrix.XtX(X0);
        S.diagonal().add(Lambda.fastOp(q->1/q));
        DataBlock y=DataBlock.of(alpha.fastOp(omega, (q,r)->q/r));
        SymmetricMatrix.solve(S, y);
        // b.mu=y
        DataBlock w=DataBlock.of(p, i->Normal.random(rng, 0, 1));
        LowerTriangularMatrix.solvexL(S, w);
        b.copy(y);
        b.add(w);
    }

    private void rue_gaussian(Matrix X, DoubleSeq alpha, DoubleSeq Lambda, Matrix XtX, DataBlock Xty, double sigma2) {
        Matrix S=XtX;
        if (XtX == null){
         S=SymmetricMatrix.XtX(X);
        }
        S=S.dividedBy(sigma2);
        S.diagonal().add(Lambda.fastOp(q->1/q));
        
        DataBlock y=Xty.deepClone();
        y.div(sigma2);
        SymmetricMatrix.solve(S, y);
        // b.mu=y
        DataBlock w=DataBlock.of(p, i->Normal.random(rng, 0, 1));
        LowerTriangularMatrix.solvexL(S, w);
        b.copy(y);
        b.add(w);
    }

}
