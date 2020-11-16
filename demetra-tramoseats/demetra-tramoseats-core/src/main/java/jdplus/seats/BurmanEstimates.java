/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or � as soon they will be approved 
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
package jdplus.seats;

import jdplus.arima.ArimaException;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import nbbrd.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.MatrixException;
import jdplus.math.polynomials.Polynomial;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.WienerKolmogorovEstimators;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import demetra.data.DoublesMath;
import jdplus.arima.ssf.ExactArimaForecasts;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixWindow;
import jdplus.math.matrices.decomposition.Gauss;
import jdplus.math.matrices.decomposition.LUDecomposition;

/**
 * Estimation of the components of an UCARIMA model using a variant of the
 * Burman's algorithm.</br>This class is based on the program SEATS+ developed
 * by Gianluca Caporello and Agustin Maravall -with programming support from
 * Domingo Perez and Roberto Lopez- at the Bank of Spain, and on the program
 * SEATS, previously developed by Victor Gomez and Agustin Maravall.<br>It
 * corresponds more especially to a modified version of the routine
 * <i>ESTBUR</i>
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class BurmanEstimates {

    public static class Builder {

        private int nf, nb;
        private DoubleSeq data;
        private UcarimaModel ucm;
        private boolean bmean;
        private int mcmp;
        private double ser = 1;

        public Builder forecastsCount(int nf) {
            this.nf = nf;
            return this;
        }

        public Builder backcastsCount(int nb) {
            this.nb = nb;
            return this;
        }

        public Builder data(DoubleSeq y) {
            this.data = y;
            return this;
        }

        public Builder mean(boolean mean) {
            this.bmean = mean;
            return this;
        }

        /**
         * Index of the component associated to the mean correction
         *
         * @param cmp
         * @return
         */
        public Builder meanComponent(int cmp) {
            this.mcmp = cmp;
            return this;
        }

        public Builder innovationStdev(double ser) {
            this.ser = ser;
            return this;
        }

        public Builder ucarimaModel(UcarimaModel ucm) {
            this.ucm = ucm;
            return this;
        }

        public BurmanEstimates build() {
            return new BurmanEstimates(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private BurmanEstimates(Builder builder) {
        this.data = builder.data;
        this.bmean = builder.bmean;
        this.mcmp = builder.mcmp;
        this.ucm = builder.ucm;
        this.ser = builder.ser;
        this.nfcasts = builder.nf;
        this.nbcasts = builder.nb;

        wk = new WienerKolmogorovEstimators(ucm);
        initModel();
        extendSeries();
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            calc(i);
        }
        if (bmean) {
            completeCasts();
        }
    }

    private final int nfcasts, nbcasts;
    private final DoubleSeq data;
    private final UcarimaModel ucm;
    private final boolean bmean;
    private final int mcmp;
    private final double ser;
    private final WienerKolmogorovEstimators wk;

    private Polynomial ar, ma;
    private Polynomial[] g;
    private double mean;
    private DoubleSeq[] estimates, forecasts, backcasts;
    private DoubleSeq xbcasts, xfcasts;
    private LUDecomposition lu;

    private void calc(final int cmp) {
        int n = data.length();
        if (cmp == mcmp && isTrendConstant()) {
            estimates[cmp] = DoubleSeq.onMapping(n, i -> mean);
            forecasts[cmp] = DoubleSeq.onMapping(nfcasts, i -> mean);
            backcasts[cmp] = DoubleSeq.onMapping(nbcasts, i -> mean);
            return;
        } else if (g[cmp] == null) {
            return;
        }

        double[] ma = this.ma.toArray();
        double[] ar = this.ar.toArray();

        // qstar is the order of the ma polynomial
        // pstar is the order of the ar polynomial
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;
        int nf = pstar + qstar;

        // //////////////////////////////////
        // Complete z, the original series
        // z is the extended series with forecasts and backcasts
        double[] z = new double[n + 2 * nf];
        data.copyTo(z, nf);

        xfcasts.range(0, nf).copyTo(z, nf + n);
        xbcasts.drop(xbcasts.length() - nf, 0).copyTo(z, 0);
        // //////////////////////////////////
        // Compute w1(t) = g(F) z(t)
        double[] g = this.g[cmp].toArray();
        int gstar = g.length - 1; //gstar <=pstar
        // w1 is computed in [-nf, n+qstar[ 
        double[] w1 = new double[n + nf + qstar];
        for (int i = 0; i < w1.length; ++i) {
            double s = g[0] * z[i];
            for (int j = 1; j <= gstar; ++j) {
                s += g[j] * z[i + j];
            }
            w1[i] = s;
        }

        // symmetric computation for w2 =g(B) z(t)
        double[] w2 = new double[n + nf + qstar];
        // w2 is computed in [-qstar, n+nf[ 
        for (int i = 0, k = pstar; i < w2.length; ++i, ++k) {
            double s = g[0] * z[k];
            for (int j = 1; j <= gstar; ++j) {
                s += g[j] * z[k - j];
            }
            w2[i] = s;
        }

        double[] ww = new double[pstar + qstar];
        int ntmp = n + 2 * qstar;
        for (int i = 0; i < pstar; ++i) {
            ww[i] = w1[ntmp + i];
        }
        if (cmp == mcmp && mean != 0) {
            for (int i = pstar; i < ww.length; ++i) {
                ww[i] = mean / 2;
            }
        }

        lu.solve(DataBlock.of(ww));
        // ww contains estimates of the signal for t=N+q*-p* to N+2q*
        int nx1 = n + 3 * qstar;
        // MA(F) w = x1 
        double[] x1 = new double[nx1];
        int nlast = nx1 - ww.length;
        for (int i = 0, j = nlast; i < ww.length; ++i, ++j) {
            x1[j] = ww[i];
        }
        // backward iteration
        // x1 defined for [-q*, n+2*q*[
        // w1 is defined for [-nf,n+q*[
        // t=n+q*-p* in x1 corresponds to index n+2q in w1
        for (int i = nlast - 1, j = n + 2 * qstar - 1; i >= 0; --i, --j) {
            double s = w1[j];
            for (int k = 1; k < ma.length; ++k) {
                s -= x1[i + k] * ma[k];
            }
            x1[i] = s / ma[0];
        }

        ww = new double[pstar + qstar];
        for (int i = 0; i < pstar; ++i) {
            ww[i] = w2[pstar - i - 1];
        }
        if (cmp == mcmp && mean != 0) {
            for (int i = pstar; i < ww.length; ++i) {
                ww[i] = mean / 2;
            }
        }
        lu.solve(DataBlock.of(ww));
        // ww contains estimates of the signal for t= -2q* to p*-q* (in reverse order)
        // MA(B) w = x2 
        // x2 defined for [-2q*, n+q*[
        double[] x2 = new double[nx1];
        for (int i = 0, j = ww.length - 1; i < ww.length; ++i, --j) {
            x2[j] = ww[i];
        }
        // forward recursion    
        // x2 defined for [-2q*, n+q*[
        // w2 is defined for [-q*,n+nf[
        // t=p*-q* in x2 corresponds to index p* in w2
        for (int i = ww.length, j = pstar; i < x2.length; ++i, ++j) {
            double s = w2[j];
            for (int k = 1; k < ma.length; ++k) {
                s -= x2[i - k] * ma[k];
            }
            x2[i] = s / ma[0];
        }

        int nfc = Math.max(qstar, nfcasts), nbc = Math.max(qstar, nbcasts);

        double[] rslt = new double[n + nfc + nbc];

        int nmax = n + nbc + qstar;
        for (int i = nbc - qstar, j = 0, k = qstar; i < nmax; ++i, ++j, ++k) {
            rslt[i] = x1[j] + x2[k];
        }
        estimates[cmp] = DoubleSeq.of(rslt, nbc, n);
        // complete backcasts
        if (!bmean || cmp != mcmp) {
            double[] car = ucm.getComponent(cmp).getAr().asPolynomial().toArray();
            for (int j = nbc - qstar - 1; j >= 0; --j) {
                double s = 0;
                for (int k = 1; k < car.length; ++k) {
                    s -= car[k] * rslt[j + k];
                }
                rslt[j] = s;
            }
            // complete forecasts
            for (int j = nbc + n + qstar; j < rslt.length; ++j) {
                double s = 0;
                for (int k = 1; k < car.length; ++k) {
                    s -= car[k] * rslt[j - k];
                }
                rslt[j] = s;
            }
            if (nfcasts > 0) {
                forecasts[cmp] = DoubleSeq.of(rslt, n + nbc, nfcasts);
            }
            if (nbcasts > 0) {
                backcasts[cmp] = DoubleSeq.of(rslt, nbc - nbcasts, nbcasts);
            }
        }
    }

    private void completeCasts() {
        if (nbcasts > 0) {
            DoubleSeq tmp = this.getSeriesBackcasts();
            for (int i = 0; i < backcasts.length; ++i) {
                if (backcasts[i] != null) {
                    tmp = DoublesMath.subtract(tmp, backcasts[i]);
                }
            }
            backcasts[mcmp] = tmp;
        }
        if (nfcasts > 0) {
            DoubleSeq tmp = this.getSeriesForecasts();
            for (int i = 0; i < forecasts.length; ++i) {
                if (forecasts[i] != null) {
                    tmp = DoublesMath.subtract(tmp, forecasts[i]);
                }
            }
            forecasts[mcmp] = tmp;
        }

    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq estimates(final int cmp, final boolean signal) {

        if (signal) {
            return estimates[cmp];
        } else {
            return data.fastOp(estimates[cmp], (a, b) -> a - b);
        }
    }

    /**
     *
     */
    private void extendSeries() {

        // we must extend the series with at least pstar+qstar forecasts/backcasts
        int nf = ar.degree() + ma.degree();

        ExactArimaForecasts fcasts = new ExactArimaForecasts();
        fcasts.prepare(wk.getUcarimaModel().getModel(), bmean);
        xfcasts = fcasts.forecasts(data, Math.max(nf, nfcasts));
        xbcasts = fcasts.backcasts(data, Math.max(nf, nbcasts));
        if (bmean) {
            mean = fcasts.getMean();
        } else {
            mean = 0;
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq forecasts(final int cmp, final boolean signal) {
        if (signal) {
            return forecasts[cmp];
        } else {
            DoubleSeq xf = xfcasts.range(0, nfcasts);
            return forecasts[cmp].fastOp(xf, (a, b) -> a - b);
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq backcasts(final int cmp, final boolean signal) {
        if (signal) {
            return backcasts[cmp];
        } else {
            int nb = xbcasts.length();
            DoubleSeq xb = xbcasts.range(nb - nbcasts, nb);
            return backcasts[cmp].fastOp(xb, (a, b) -> a - b);
        }
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesBackcasts() {
        return xbcasts.drop(xbcasts.length() - nbcasts, 0);
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesForecasts() {
        return xfcasts.range(0, nfcasts);
    }

    /**
     *
     */
    private void initModel() {
        // cfr burman-wilson algorithm
        IArimaModel model = ucm.getModel();
        int ncmps = ucm.getComponentsCount();
        estimates = new DoubleSeq[ncmps];
        forecasts = new DoubleSeq[ncmps];
        backcasts = new DoubleSeq[ncmps];
        g = new Polynomial[ncmps];

        ma = model.getMa().asPolynomial();
        double v = model.getInnovationVariance();
        if (v != 1) {
            ma = ma.times(Math.sqrt(v));
        }
        ar = model.getAr().asPolynomial();

        for (int i = 0; i < ncmps; ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            if (!cmp.isNull()) {
                SymmetricFilter sma = cmp.symmetricMa();
                if (!sma.isNull()) {
                    BackFilter umar = model.getNonStationaryAr(), ucar = cmp.getNonStationaryAr();
                    BackFilter nar = umar.divide(ucar);
                    BackFilter smar = model.getStationaryAr(), scar = cmp.getStationaryAr();
                    BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
                    if (smp.simplify(smar, scar)) {
                        smar = smp.getLeft();
                        scar = smp.getRight();
                    }

                    BackFilter dar = scar;
                    nar = nar.times(smar);

                    BackFilter denom = new BackFilter(ma).times(dar);
                    SymmetricFilter c = sma.times(SymmetricFilter.convolutionOf(nar));
                    double mvar = model.getInnovationVariance();
                    if (mvar != 1) {
                        c = c.times(1 / mvar);
                    }
                    BackFilter gf = c.decompose(denom);
                    g[i] = gf.asPolynomial();
                } else {
                    g[i] = Polynomial.ZERO;
                }
            }
        }
        initSolver();
    }

    private boolean isTrendConstant() {
        return wk.getUcarimaModel().getComponent(mcmp).isNull();
    }

    private void initSolver() {
        int qstar = ma.degree();
        int pstar = ar.degree();

        Matrix M = Matrix.square(pstar + qstar);
        MatrixWindow top = M.top(0);
        Matrix M1 = top.vnext(pstar);
        DoubleSeqCursor cursor = ma.coefficients().cursor();
        for (int j = 0; j <= qstar; ++j) {
            M1.subDiagonal(j).set(cursor.getAndNext());
        }
        cursor = ar.coefficients().reverse().cursor();
        Matrix M2 = top.vnext(qstar);
        for (int j = 0; j <= pstar; ++j) {
            M2.subDiagonal(j).set(cursor.getAndNext());
        }
        lu = Gauss.decompose(M);
    }

    /**
     *
     * @return
     */
    public boolean isMeanCorrection() {
        return bmean;
    }

    /**
     *
     * @param cmp
     * @return
     */
    public DoubleSeq stdevEstimates(final int cmp) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull()) {
            return Doubles.EMPTY;
        } else {
            try {
                int n = (data.length() + 1) / 2;
                double[] err = wk.totalErrorVariance(cmp, true, 0, n);
                double[] e = new double[data.length()];
                for (int i = 0; i < err.length; ++i) {
                    double x = ser * Math.sqrt(err[i]);
                    e[i] = x;
                    e[e.length - i - 1] = x;
                }
                return DoubleSeq.of(e);
            } catch (ArimaException | MatrixException err) {
                return Doubles.EMPTY;
            }
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return null if no forecasts are requested
     */
    public DoubleSeq stdevForecasts(final int cmp, final boolean signal) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull() || nfcasts == 0) {
            return null;
        }
        try {

            double[] e = wk.totalErrorVariance(cmp, signal, -nfcasts, nfcasts);
            double[] err = new double[nfcasts];
            for (int i = 0; i < nfcasts; ++i) {
                err[i] = ser * Math.sqrt(e[nfcasts - 1 - i]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }
    }

    public DoubleSeq stdevBackcasts(final int cmp, final boolean signal) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull() || nbcasts == 0) {
            return null;
        }
        try {
            double[] e = wk.totalErrorVariance(cmp, signal, -nbcasts, nbcasts);
            double[] err = new double[nbcasts];
            for (int j=nbcasts-1; j >= 0; --j) {
                err[j] = ser * Math.sqrt(e[j]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }

    }
}
