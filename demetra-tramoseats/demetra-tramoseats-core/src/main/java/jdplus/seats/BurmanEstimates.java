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
import demetra.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.MatrixException;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.WienerKolmogorovEstimators;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import jdplus.arima.ssf.ExactArimaForecasts;
import jdplus.math.matrices.Matrix;
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
    }

    private final int nfcasts, nbcasts;
    private final DoubleSeq data;
    private final UcarimaModel ucm;
    private final boolean bmean;
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
        if (cmp == 0 && isTrendConstant()) {
            double m = correctedMean();
            estimates[cmp] = DoubleSeq.onMapping(n, i -> m);
            forecasts[cmp] = DoubleSeq.onMapping(nfcasts, i -> m);
            backcasts[cmp] = DoubleSeq.onMapping(nbcasts, i -> m);
            return;
        } else if (g[cmp] == null) {
            return;
        }

        int nf = xfcasts.length();
        int nb = xbcasts.length();
        double[] ma = this.ma.toArray();
        double[] ar = this.ar.toArray();

        // qstar is the order of the ma polynomial
        // pstar is the order of the ar polynomial
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;
        if (useD1()) {
            ++qstar;
        }

        // //////////////////////////////////
        // Complete z, the original series
        // z is the extended series with forecasts and backcasts
        double[] z = new double[n + nf + nb];
        data.copyTo(z, nb);
             
        int n0=0, n1 = nb, n2= n1+n, n3=n2+nf;
        xfcasts.copyTo(z, n1);
        xbcasts.copyTo(z, n0);
        if (useMean()) {
            double m = correctedMean();
            for (int i = 0; i < z.length; ++i) {
                z[i] -= m;
            }
        }
        // //////////////////////////////////
        // Compute w1(t) = g(F) z(t)
        double[] g = this.g[cmp].toArray();
        int gstar = g.length;
        double[] w1 = new double[n2 + qstar];
        for (int i = n1; i < n2 + qstar; ++i, ++i) {
            double s = g[0] * z[i];
            for (int j = 1; j < gstar; ++j) {
                s += g[j] * z[i + j];
            }
            w1[i] = s;
        }

        // calculation of w2=g*data, for -q<=t<n+nf , elements -q to n+nf of
        // data w1: elt t=0 at place q
        double[] w2 = new double[n + nf + qstar];
        for (int i = 0; i < n + nf + qstar; ++i) {
            double s = g[0] * z[nf - qstar + i];
            for (int j = 1; j < gstar; ++j) {
                s += g[j] * z[nf - qstar + i - j];
            }
            w2[i] = s;
        }

        double[] ww = new double[pstar + qstar];
        int ntmp = n + qstar - pstar;
        for (int i = 0; i < pstar; ++i) {
            ww[i] = w1[ntmp + i];
        }
        if (ww.length > 0) {
            lu.solve(DataBlock.of(ww));
        }
        double[] mx = ww.length == 0 ? new double[0] : ww;
        int nx1 = n + Math.max(2 * qstar, nf);
        double[] x1 = new double[nx1];
        for (int i = 0; i < pstar + qstar; ++i) {
            x1[ntmp + i] = mx[i];
            // backward iteration
        }
        for (int i = ntmp - 1; i >= 0; --i) {
            double s = w1[i];
            for (int j = 1; j < ma.length; ++j) {
                s -= x1[i + j] * ma[j];
            }
            x1[i] = s;
        }

        // forward iteration
        for (int i = ntmp + pstar + qstar; i < nx1; ++i) {
            double s = 0;
            for (int j = 1; j <= pstar; ++j) {
                s -= ar[j] * x1[i - j];
            }
            x1[i] = s;
        }

        ww = new double[pstar + qstar];
        for (int i = 0; i < pstar; ++i) {
            ww[i] = w2[pstar - i - 1];
        }
        if (ww.length > 0) {
            lu.solve(DataBlock.of(ww));
        }
        mx = ww.length == 0 ? new double[0] : ww;
        int nx2 = n + 2 * qstar + Math.max(nf, 2 * qstar);
        double[] x2 = new double[nx2];
        for (int i = 0; i < pstar + qstar; ++i) {
            x2[pstar + qstar - 1 - i] = mx[i];

            // iteration w2 start in -q, x2 in -2*q delta q
        }
        for (int i = pstar + qstar; i < nx2; ++i) {
            double s = w2[i - qstar];
            for (int j = 1; j < ma.length; ++j) {
                s -= x2[i - j] * ma[j];
            }
            x2[i] = s;
        }

        double[] rslt = new double[n];
        for (int i = 0; i < n; ++i) {
            rslt[i] = (x1[i] + x2[i + 2 * qstar]);
        }
        if (cmp == 0 && useMean()) {
            double m = correctedMean();
            for (int i = 0; i < rslt.length; ++i) {
                rslt[i] += m;
            }
        }
        estimates[cmp] = DoubleSeq.of(rslt);

        if (this.nfcasts > 0) {
            double[] fcast = new double[this.nfcasts];

            for (int i = 0; i < this.nfcasts; ++i) {
                fcast[i] = (x1[n + i] + x2[n + i + 2 * qstar]);
            }
            if (cmp == 0 && useMean()) {
                double m = correctedMean();
                for (int i = 0; i < fcast.length; ++i) {
                    fcast[i] += m;
                }
            }
            forecasts[cmp] = DoubleSeq.of(fcast);
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
        if (xbcasts != null) {
            return;
        }
        int nf = 0;
        for (int i = 0; i < g.length; ++i) {
            int nr = 0;
            if (g[i] != null) {
                nr = g[i].degree();
            }
            if (ma != null) {
                nr += ma.degree();
            }
            if (bmean) {
                nr += 2;
            }
            if (nr > nf) {
                nf = nr;
            }
        }
        if (bmean && nf <= ar.degree()) {
            nf = ar.degree() + 1;
        }
        int nb = nf;
        if (nfcasts > nf) {
            nf = nfcasts;
        }
        if (nbcasts > nb) {
            nb = nbcasts;
        }

        ExactArimaForecasts fcasts = new ExactArimaForecasts();
        fcasts.prepare(wk.getUcarimaModel().getModel(), bmean);
        xfcasts = fcasts.forecasts(data, nf);
        xbcasts = fcasts.backcasts(data, nb);
        if (bmean) {
            mean = fcasts.getMean();
        } else {
            mean = 0;
        }
    }

    private IArimaModel model() {
        return wk.getUcarimaModel().getModel();
    }

    // compute mean/P(1), where P is the stationary AR 
    private double correctedMean() {
        IArimaModel arima = model();
        return mean / arima.getStationaryAr().asPolynomial().evaluateAt(1);
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
        extendSeries();
        return xbcasts.drop(xbcasts.length() - nbcasts, 0);
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesForecasts() {
        extendSeries();
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
        if (useD1()) {
            ar = ar.times(UnitRoots.D1);
        }

        initSolver();
    }

    private boolean useD1() {
        // we use D1 correction when there is a mean and UR in the AR part of the model
        return bmean && model().getNonStationaryArOrder() > 0;
    }

    private boolean useMean() {
        // we use the mean if there is a mean and if we don't use D1 correction
        // it happens when the model doesn't contain any non stationary root
        return bmean && model().getNonStationaryArOrder() == 0;
    }

    private boolean isTrendConstant() {
        return wk.getUcarimaModel().getComponent(0).isNull();
    }

    private void initSolver() {
        int qstar = ma.degree();
        int pstar = ar.degree();
        if (useD1()) {
            qstar += 1;
        }

        //////////////////////////////////
//         Complete z, the original series
//         z is the extended series with forecasts and backcasts
        Matrix m = Matrix.square(pstar + qstar);
        for (int i = 0; i < pstar; ++i) {
            for (int j = 0; j <= ma.degree(); ++j) {
                m.set(i, i + j, ma.get(j));
            }
        }
        for (int i = 0; i < qstar; ++i) {
            for (int j = 0; j <= pstar; ++j) {
                m.set(i + pstar, i + j, ar.get(pstar - j));
            }
        }

        lu = Gauss.decompose(m);
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
     * @return
     */
    public DoubleSeq stdevForecasts(final int cmp, final boolean signal) {
        try {
            if (wk.getUcarimaModel().getComponent(cmp).isNull() || nfcasts == 0) {
                return Doubles.EMPTY;
            }

            double[] e = wk.totalErrorVariance(cmp, signal, -nfcasts, nfcasts);
            double[] err = new double[nfcasts];
            for (int i = 0; i < nfcasts; ++i) {
                err[i] = ser * Math.sqrt(e[nfcasts - 1 - i]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return Doubles.EMPTY;
        }
    }

    public DoubleSeq stdevBackcasts(final int cmp, final boolean signal) {
        try {
            if (wk.getUcarimaModel().getComponent(cmp).isNull() || nbcasts == 0) {
                return Doubles.EMPTY;
            }

            double[] e = wk.totalErrorVariance(cmp, signal, -nbcasts, nbcasts);
            double[] err = new double[nbcasts];
            for (int i = 0; i < nbcasts; ++i) {
                err[i] = ser * Math.sqrt(e[nbcasts - 1 - i]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return Doubles.EMPTY;
        }

    }
}
