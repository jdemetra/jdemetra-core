/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ucarima;

import jdplus.arima.ArimaException;
import jdplus.arima.ArimaModel;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.arima.IArimaModel;
import jdplus.arima.LinearProcess;
import jdplus.arima.StationaryTransformation;
import nbbrd.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.ForeFilter;
import jdplus.math.linearfilters.RationalBackFilter;
import jdplus.math.linearfilters.RationalFilter;
import jdplus.math.linearfilters.RationalForeFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.MatrixException;
import java.util.function.IntToDoubleFunction;
import jdplus.arima.ILinearProcess;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WienerKolmogorovEstimators {

    private final UcarimaModel ucm;
    private WienerKolmogorovEstimator[][] finals;

    /**
     *
     * @param ucm
     */
    public WienerKolmogorovEstimators(final UcarimaModel ucm) {
        this.ucm = ucm;
    }

    /**
     * model: P(B)D(B)y(t) = Q(B)e(t), e=N(0, v^2I) 
     * signal: Ps(B)Ds(B)s(t) = Qs(B)es(t), es=N(0, vs^2I)
     *
     * rem: special treatment when specific stationary roots are added
     * artificially in Ps(B): we consider P'(x), Ps'(x) so that
     * P(x)/Ps(x)=P'(x)/Ps'(x) and P'(x), Ps'(x) don't contain any common factor
     * we write Dn(x)=D(x)/Ds(x)
     *
     * The WK filter is 
     * WK=(vs/v)^2 Qs(B)P'(B)Dn(B)Qs(F)P'(F)Dn(F)]/[Q(B)Ps'(B)Q(F)Ps'(F)]
     *
     * In most cases, we will have WK=(vs/v)^2
     * Qs(B)Pn(B)Dn(B)Qs(F)P'(F)D(F)]/[Q(B)Q(F)]
     *
     * The model of the estimator is (usually: see rem above)
     * Ps(B)Ds(B)s^(t)=vs/v Qs(B)Qs(F)Pn(F)Dn(F)/Q(F)e(t)
     *
     * Derivation:
     *
     * s^(t)=vs/v
     * [Qs(B)P(B)D(B)Qs(F)P(F)D(F)]/[Q(B)Ps(B)Ds(B)Q(F)Ps(F)Ds(F)]y(t)
     *
     * Taking into account that D(x)=Ds(x)Dn(x) and that y(t)= Q(B)/P(B)D(B)
     * e(t), we get
     *
     * WK=(vs/v)^2 Qs(B)P(B)Dn(B)Qs(F)P(F)Dn(F)]/[Q(B)Ps(B)Q(F)Ps(F)
     *
     * s^(t)=vs/v [Qs(B)P(B)Qs(F)P(F)Dn(F)]/[Ps(B)Q(F)Ps(F)P(B)Ds(B)] e(t)
     *
     * s^(t)=vs/v [Qs(B)/(Ps(B)Ds(B))]* [Qs(F)P(F)Dn(F)]/[Q(F)Ps(F)] e(t)
     *
     * Computation:
     *
     * 1 Compute Dn(x) 2 Simplify P(x), Ps(x), we get P'(x), Ps'(x)
     *
     * 3 Compute [Qs(B)/(Ps(B)Ds(B))] 4 Compute [Qs(F)P'(F)Dn(F)]/[Q(F)Ps'(F)]
     *
     * @param cmp Considered component
     * @param signal True for signal, false for noise (signal + noise = model)
     * @throws ArimaException
     * @throws MatrixException
     */
    private void calcestimator(final int cmp, final boolean signal)
            throws ArimaException, MatrixException {
        if (ucm == null) {
            return;
        }
        int k = signal ? 1 : 0;
        if (finals == null) {
            finals = new WienerKolmogorovEstimator[ucm.getComponentsCount()][2];
        } else if (finals[cmp][k] != null) {
            return;
        }

        IArimaModel a = ucm.getComponent(cmp);
        IArimaModel s = signal ? a : ucm.getComplement(cmp);

        BackFilter nar = ucm.getModel().getStationaryAr(), dar = s.getStationaryAr();
        BackFilter nur = ucm.getModel().getNonStationaryAr(), dur = s.getNonStationaryAr();

        BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
        // we computes P'(B), Ps'(B) (respectively in nar, dar)
        if (smp.simplify(nar, dar)) {
            nar = smp.getLeft();
            dar = smp.getRight();
        }
        nur = nur.divide(dur);
        // nur is Dn(B)
        nar = nar.times(nur);
        // nar is P'(B)Dn(B)

        BackFilter denom = ucm.getModel().getMa().times(dar);
        // denom is Q(B)Ps'(B)
        BackFilter num = s.getMa().times(nar);
        // num is Qs(B)P'(B)Dn(B)
        SymmetricFilter c = SymmetricFilter.convolutionOf(num);
        double svar = s.getInnovationVariance(), mvar = ucm.getModel().getInnovationVariance();
        c = c.times(svar / mvar);
        BackFilter gf = c.decompose(denom);

        RationalBackFilter rb = new RationalBackFilter(gf, denom, 0);

        RationalFilter f = new RationalFilter(rb, rb.mirror(), c, SymmetricFilter.convolutionOf(denom));
        RationalFilter mf = new RationalFilter(s.getMa().times(svar / mvar), s.getAr(), num.mirror(), denom.mirror());
        LinearProcess m = new LinearProcess(mf, mvar);

        finals[cmp][k] = new WienerKolmogorovEstimator(f, m);
    }

    /**
     * Compute the model of the final error (= difference between a component
     * and its estimator)
     *
     * Q(B) e(t) = Qs(B)*Qn(B) u(t)
     *
     * @param cmp The component
     * @return The corresponding Arima model
     * @throws ArimaException
     */
    public ArimaModel finalErrorModel(final int cmp) throws ArimaException {
        if (ucm == null) {
            return null;
        }
        // error model...
        ArimaModel s = ucm.getComponent(cmp);
        if (s.isNull()) {
            return null;
        }
        ArimaModel n = ucm.getComplement(cmp);
        IArimaModel model = ucm.getModel();
        BackFilter ar = model.getMa();
        SymmetricFilter ss = s.symmetricMa(), sn = n.symmetricMa();
        SymmetricFilter num = ss.times(sn);
        return new ArimaModel(ar, BackFilter.ONE, num);
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     * @throws ArimaException
     * @throws MatrixException
     */
    public WienerKolmogorovEstimator finalEstimator(final int cmp,
            final boolean signal) throws ArimaException, MatrixException {
        if (ucm == null) {
            return null;
        }
        IArimaModel a = ucm.getComponent(cmp);
        if (a.isNull()) {
            return null;
        }
        calcestimator(cmp, signal);
        return finals[cmp][signal ? 1 : 0];
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     * @throws ArimaException
     */
    public StationaryTransformation<ILinearProcess> finalStationaryEstimator(final int cmp,
            final boolean signal) throws ArimaException {
        BackFilter ur;
        if (ucm == null) {
            return null;
        }
        IArimaModel model = ucm.getModel();
        IArimaModel a = signal ? ucm.getComponent(cmp) : ucm.getComplement(cmp);
        if (a.isNull()) {
            return null;
        }

        // stationary model: Ka * MAa(B)/Stationary(ARa(B))
        // *MAa(F)/MA(F)*[AR(F)/ARa(F)], ur=ARa(B)
        BackFilter nar = ucm.getModel().getStationaryAr(), dar = a.getStationaryAr();
        BackFilter nur = ucm.getModel().getNonStationaryAr(), dur = a.getNonStationaryAr();

        BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
        if (smp.simplify(nar, dar)) {
            nar = smp.getLeft();
            dar = smp.getRight();
        }
        nur = nur.divide(dur);
        nar = nar.times(nur);
        BackFilter denom = model.getMa().times(dar);
        BackFilter num = a.getMa().times(nar);
        double avar = a.getInnovationVariance(), mvar = model.getInnovationVariance();
        BackFilter star = a.getStationaryAr();
        ur = a.getNonStationaryAr();
        RationalFilter mf = new RationalFilter(a.getMa().times(avar / mvar), star, num.mirror(), denom.mirror());

        return new StationaryTransformation<>(new LinearProcess(mf, mvar), ur);
    }

    /**
     *
     * @return
     */
    public UcarimaModel getUcarimaModel() {
        return ucm;
    }

    /**
     * Computes the model of the revision in t+n for the component cmp. The
     * model is computed as follows: 1. Compute the model of the final estimator
     * s(t) We have: Ps(B) s(t) = ks * Qs(B) * [Qs(F)*Pn(F)/Q(F)]a(t) 2. Compute
     * the "F" part of the model s(t) = ks *[Qs(B)/Ps(B)] *
     * [Qs(F)*Pn(F)/Q(F)]a(t)=K(B)a(t) + L(F)a(t) To be noted that the model of
     * the concurrent revision model needs the elimination of the first term of
     * that part 3. Drop the (n+1) first elements,of L(F) and compute the new ma
     * part of the model
     *
     * @param cmp Component of the Ucarima model
     * @param n Time delay
     * @return The arima model corresponding to the revisions
     * @throws ArimaException
     * @throws MatrixException
     */
    public LinearProcess revisionModel(final int cmp, final int n)
            throws ArimaException, MatrixException// , ref double
    // scale)
    {
        if (n < 0) {
            return null;
        }
        LinearProcess ln = finalEstimator(cmp, true).getEstimatorModel();
        RationalFilter rf = ln.getFilter();
        RationalForeFilter rff = rf.getRationalForeFilter().drop(n + 1);
        ForeFilter num = rff.getNumerator(), denom = rff.getDenominator();
        RationalFilter crf = new RationalFilter(RationalBackFilter.ZERO, new RationalForeFilter(num, denom, 0)); // 0 should be modified
        return new LinearProcess(crf, ln.getInnovationVariance());
    }

    public AutoCovarianceFunction totalErrorAcf(final int cmp, final int n) {
        LinearProcess rev = revisionModel(cmp, n);
        ArimaModel fm = finalErrorModel(cmp);
        RationalForeFilter rf = rev.getFilter().getRationalForeFilter();
        BackFilter rnum = rf.getNumerator().mirror();
        double n0 = rnum.get(0);

        ArimaModel rm = new ArimaModel(rf.getDenominator().mirror(), BackFilter.ONE, rnum.normalize(), rev.getInnovationVariance() * n0 * n0);
        return fm.plus(rm).getAutoCovarianceFunction();
    }

    public AutoCovarianceFunction revisionAcf(final int cmp, final int n) {
        LinearProcess rev = revisionModel(cmp, n);
        return rev.getAutoCovarianceFunction();
    }

    /**
     * Computes the variance of the revision errors, The revision k is defined
     * as S(t|T-start+k) - S(t)
     *
     * @param cmp The considered component
     * @param signal True if signal, false if noise
     * @param start The position of the reference estimator (0 for the
     * concurrent estimator, <0 for forecast, >0 for other preliminary
     * estimator)
     * @param n The number of revisions
     * @return
     * @throws ArimaException
     * @throws MatrixException
     */
    public double[] revisionVariance(final int cmp, final boolean signal,
            int start, final int n) throws ArimaException, MatrixException {
        if (ucm.getComponent(cmp).isNull()) {
            return null;
        }
        double[] rvar = new double[n];
        // variance of concurrent estimator...
        // double scale=0;
        double var0 = revisionModel(cmp, 0).getAutoCovarianceFunction().get(0);// , ref
        // scale).ACGF[0];
        LinearProcess lm = finalEstimator(cmp, signal).getEstimatorModel();
        RationalFilter rf = lm.getFilter();
        double mvar = lm.getInnovationVariance();

        if (start <= 0 && -start < n) {
            rvar[-start] = var0;
        }

        // 
        double var = var0;
        IntToDoubleFunction weights = rf.weights();
        for (int i = -1; i >= start; --i) {
            double w = weights.applyAsDouble(i + 1);
            var += w * w * mvar;
            if (i - start < n) {
                rvar[i - start] = var;
            }
        }
        // iterate to start, if necessary
        var = var0;
        for (int i = 1; i < start; ++i) {
            double w = weights.applyAsDouble(i);
            var -= w * w * mvar;
            if (var <= 0) {
                return rvar;
            }
        }

        for (int i = Math.max(start, 1); i < n + start; ++i) {
            double w = weights.applyAsDouble(i);
            var -= w * w * mvar;
            if (var <= 0) {
                break;
            }
            rvar[i - start] = var;
        }
        //
        return rvar;
    }

    /**
     * Computes the variance of the revision errors, in comparison with the
     * estimator E(t|T-start). The revision k is defined as S(t|T-start+k) -
     * S(t|T-start)
     *
     * @param cmp The considered component
     * @param signal True if signal, false if noise
     * @param start The position of the reference estimator (0 for the
     * concurrent estimator, <0 for forecast, >0 for other preliminary
     * estimator)
     * @param n The number of revisions
     * @return
     * @throws ArimaException
     * @throws MatrixException
     */
    public double[] relativeRevisionVariance(final int cmp, final boolean signal,
            int start, final int n) throws ArimaException, MatrixException {
        if (ucm.getComponent(cmp).isNull()) {
            return null;
        }
        double[] rvar = new double[n];
        // variance of concurrent estimator...
        // double scale=0;
        // scale).ACGF[0];
        LinearProcess lm = finalEstimator(cmp, signal).getEstimatorModel();
        RationalFilter rf = lm.getFilter();
        double mvar = lm.getInnovationVariance();
        IntToDoubleFunction weights = rf.weights();
        // 
        double var = 0;
        for (int i = 1; i < n; ++i) {
            double w = weights.applyAsDouble(i + start);
            var += w * w * mvar;
            rvar[i] = var;
        }
        return rvar;
    }

    /**
     *
     * @param cmp
     * @param signal
     * @param start
     * @param n
     * @return
     * @throws ArimaException
     * @throws MatrixException
     */
    public double[] totalErrorVariance(final int cmp, final boolean signal,
            final int start, final int n) throws ArimaException,
            MatrixException {
        if (ucm.getComponent(cmp).isNull()) {
            return null;
        }
        double[] tvar = revisionVariance(cmp, signal, start, n);
        double fvar = finalErrorModel(cmp).getAutoCovarianceFunction().get(0);
        for (int i = 0; i < tvar.length; ++i) {
            tvar[i] += fvar;
        }
        return tvar;
    }

    /**
     *
     * @param cmp Component of the model
     * @param start Time delay. 0 = concurrent estimator. n > 0 for other
     * preliminary estimator for instance n=3 corresponds to the estimator
     * s(t|t+3)
     * @param del Time delay of the variation. We consider s(t)-s(t-del)
     * @param all If true, the variance includes the total error (final +
     * revisions), otherwise only the revisions are considered
     * @return The variance of the considered "growth rate"
     */
    public double variationPrecision(final int cmp,
            final int start, final int del, boolean all) {
        if (ucm.getComponent(cmp).isNull()) {
            return Double.NaN;
        }
        LinearProcess me = finalEstimator(cmp, true).getEstimatorModel();
        RationalFilter fe = me.getFilter();
        IntToDoubleFunction weights = fe.weights();
        double mv = me.getInnovationVariance();
        AutoCovarianceFunction acf;
        if (all) {
            acf = totalErrorAcf(cmp, start);
        } else {
            acf = revisionAcf(cmp, start);
        }
        double v = 2 * (acf.get(0) - acf.get(del));
        for (int i = 0; i < del; ++i) {
            double x = weights.applyAsDouble(i + start + 1);
            v -= x * x * mv;
        }
        return v;
    }

    /**
     * Computes the variance of [s(t|t-start)-s(t-del|t-start)] -
     * [s(t|t-start+nrevs)-s(t-del|t-start+nrevs)]
     *
     * [s(t|t-l)-s(t-d|t-l)] - [s(t|t-l + k)-s(t-d|t-l+k)] = [s(t|t-l) - s(t|t-l
     * + k)] - [s(t-d|t-l) - s(t-d|t-l+k)] = sum(p(i)e(i), -l<i<=-l+k) -
     * sum(p(j)e(j), -l-d<j<=-l-d+k)
     *
     * @param cmp Component of the model
     * @param start Time delay. 0 = concurrent estimator. n > 0 for other
     * preliminary estimator for instance n=3 corresponds to the estimator
     * s(t|t+3)
     * @param del Time delay of the variation. We consider s(t)-s(t-del)
     * @param nrevs The number of revisions
     * @return The variance of the considered "growth rate"
     */
    public double variationRevisionVariance(final int cmp,
            final int start, final int del, final int nrevs) {
        if (ucm.getComponent(cmp).isNull()) {
            return Double.NaN;
        }
        LinearProcess me = finalEstimator(cmp, true).getEstimatorModel();
        RationalFilter fe = me.getFilter();
        IntToDoubleFunction weights = fe.weights();
        double mv = me.getInnovationVariance();
        double v = 0;
        for (int i = 1; i <= nrevs; ++i) {
            double x = weights.applyAsDouble(i + start);
            double y = weights.applyAsDouble(i + start + del);
            v += x * x + y * y - 2 * x * y;
        }
        return v * mv;
    }
}
