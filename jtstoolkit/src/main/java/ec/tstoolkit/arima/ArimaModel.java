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
package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFrequencyResponseDecomposer3;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFrequencyResponseDecomposer;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRootsSolver;

/**
 * Box-Jenkins ARIMA model P(B)D(B) y(t) = Q(B) e(t). P(B) is the stationary
 * auto-regressive polynomial. D(B) is the non stationary auto-regressive
 * polynomial. Q(B) is the moving average polynomial. e(t) ~ N(0, var*I) models
 * the innovations. An Arima model can be identified either by P, D, Q, var or
 * by P(B)*P(F), D, var*Q(B)*Q(F). The last representation, which corresponds to
 * the auto-covariance function, is more appropriate for most operations on
 * Arima models. The current implementation is able to store both
 * representations. It will generate the missing one, if any, when it is needed.
 * Such an operation is transparent for the user of the class. It should also be
 * noted that the class is able to handle "invalid" models (models with a
 * negative pseudo-spectrum). Using such models may be necessary for instance in
 * some decomposition schemes
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Alpha)
public class ArimaModel extends AbstractArimaModel implements IArimaModel {

    private static final double EPS = 1e-4;
    private BackFilter bar_, bma_; // stationary part
    private BackFilter delta_; // non stationary ar
    private double var_;
    private SymmetricFilter sar_, sma_; // stationary part
    private boolean st_, inv_, initprop_;

    /**
     * Adds an Arima model to a white noise process with variance var. The
     * innovations of the Arima model and the white noise process are considered
     * as independent.
     *
     * @param var The variance of the white noise/
     * @param arima The arima model
     * @return
     */
    public static ArimaModel add(final double var, final ArimaModel arima) {
        return arima.plus(var);
    }

    /**
     * Adds two Arima models, considering that their innovations are
     * independent. The sum of two Arima models is computed as follows: The
     * auto-regressive parts (stationary and non stationary of the aggregated
     * model are the smaller common multiple of the corresponding polynomials of
     * the components. The sum of the acf of the modified moving average
     * polynomials is then computed and factorized, to get the moving average
     * polynomial and innovation variance of the sum. See the class
     * ec.tstoolkit.maths.linearfilters.SymmetricFrequencyResponseDecomposer for
     * the factorization of the aggregated acf.
     *
     * @param l The left operand. May be any generic IArimaModel.
     * @param r The right operand. Must be a standard ArimaModel object
     * @return A new Arima model
     * @throws ArimaException An exception can be thrown if the generic Arima
     * model cannot be converted into a standard Arima model (an intermediary
     * step of the procedure)
     */
    public static ArimaModel add(final IArimaModel l, final ArimaModel r) {
        ArimaModel m = ArimaModel.create(l);
        if (m == null) {
            throw new ArimaException("+ operation failed");
        }
        return m.plus(r);
    }

    /**
     * Subtracts two Arima models. More formally the model m = l - r is such
     * that m + r = l, considering that the innovations of m and of r are
     * independent.
     *
     * @param l The left operand. May be any generic IArimaModel.
     * @param r The right operand. Must be a standard ArimaModel object
     * @return A new Arima model
     * @throws ArimaException An exception can be thrown if the generic Arima
     * model cannot be converted into a standard Arima model (an intermediary
     * step of the procedure)
     */
    public static ArimaModel subtract(final IArimaModel l, final ArimaModel r) {
        ArimaModel m = ArimaModel.create(l);
        if (m == null) {
            throw new ArimaException("+ operation failed");
        }
        return m.minus(r);
    }

    private static BackFilter check(final BackFilter bf) {
        if (bf == null) {
            return null;
        }
        Polynomial c = bf.getPolynomial();
        int n = 0;
        while (n <= c.getDegree() && c.get(n) == 0) {
            ++n;
        }
        if (n == c.getDegree() + 1) {
            return BackFilter.ZERO;
        } else if (n > 0) {
            double[] nc = new double[c.getDegree() + 1 - n];
            for (int i = 0; i < nc.length; ++i) {
                nc[i] = c.get(i + n);
            }
            return BackFilter.of(nc);
        } else {
            return null;
        }
    }

    /**
     * Compares two Arima models. Their are considered as identical if all their
     * parameters are equals, up to a given small eps. The parameters are the AR
     * polynomials, the MA polynomials and the innovation variances.
     *
     * @param lm The first element of the comparison
     * @param rm The right element of the comparison
     * @param eps The small tolerance value used in the comparison.
     * @return True if the models can be considered as identical, false
     * otherwise.
     */
    public static boolean same(IArimaModel lm, IArimaModel rm, double eps) {
        if (Math.abs(lm.getInnovationVariance() - rm.getInnovationVariance()) > eps) {
            return false;
        }
        if (!same(lm.getNonStationaryAR().getCoefficients(), rm.getNonStationaryAR().getCoefficients(), eps)) {
            return false;
        }
        if (!same(lm.getStationaryAR().getCoefficients(), rm.getStationaryAR().getCoefficients(), eps)) {
            return false;
        }
        if (!same(lm.getMA().getCoefficients(), rm.getMA().getCoefficients(), eps)) {
            return false;
        }
        return true;
    }

    private static boolean same(double[] l, double[] r, double eps) {
        if (l == null && r == null) {
            return true;
        }
        if (l == null || r == null) {
            return false;
        }
        int ll = l.length - 1, lr = r.length - 1;
        while (ll >= 0 && Math.abs(l[ll]) < eps) {
            --ll;
        }
        while (lr >= 0 && Math.abs(r[lr]) < eps) {
            --lr;
        }
        if (ll != lr) {
            return false;
        }
        for (int i = 0; i <= lr; ++i) {
            if (Math.abs(l[i] - r[i]) > eps) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scales the variance of the innovations by a given factor
     *
     * @param factor The scaling factor
     * @return A new arima model is returned
     */
    public ArimaModel scaleVariance(double factor) {
        ArimaModel m = new ArimaModel();
        double var = this.getInnovationVariance();
        if (var == 0) {
            return this;
        }

        m.delta_ = delta_;
        m.bar_ = bar_;
        m.bma_ = bma_;
        m.var_ = var * factor;
        m.sar_ = sar_;
        if (this.sma_ != null) {
            m.sma_ = sma_.times(factor);
        }
        return m;
    }

    @Override
    protected AutoCovarianceFunction initAcgf() throws ArimaException {
        if (sma_ != null) {
            return new AutoCovarianceFunction(sma_, getAR());
        } else {
            return super.initAcgf();
        }
    }

    /**
     * Normalizes the variance of the innovation
     *
     * @return A new arima model is returned. Its innovation variance is 1.
     */
    public ArimaModel normalize() {
        return scaleVariance(1 / getInnovationVariance());
    }

    /**
     * Creates a new Arima model from a generic arima model
     *
     * @param arima The generic arima model
     * @return A new arima model is returned.
     */
    public static ArimaModel create(final IArimaModel arima) {
        try {
            ArimaModel m = new ArimaModel();

            m.delta_ = arima.getNonStationaryAR();
            m.bar_ = arima.getStationaryAR();
            m.bma_ = arima.getMA();
            m.var_ = arima.getInnovationVariance();
            return m;
        } catch (ArimaException ex) {
            return null;
        }
    }

    private static ArimaModel pm(final ArimaModel l, final ArimaModel r,
            final boolean plus, final boolean simplify) {
        if (r.isWhiteNoise()) {
            if (plus) {
                return l.plus(r.var_);
            } else {
                return l.minus(r.var_);
            }
        } else if (l.isWhiteNoise() && plus) {
            return ArimaModel.add(l.var_, r);
        }

        // compute the denominator
        BackFilter lar = l.bar_, rar = r.bar_, ar;
        // BFilter lar = l.bar(), rar = r.bar(), ar = null;
        BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(false);
        if (smp.simplify(lar, rar)) {
            ar = lar.times(smp.getRight());
            lar = smp.getLeft();
            rar = smp.getRight();
        } else {
            ar = lar.times(rar);
        }

        BackFilter lur = l.delta_, rur = r.delta_, ur;
        BackFilter.SimplifyingTool smpur = new BackFilter.SimplifyingTool(true);
        if (smpur.simplify(lur, rur)) {
            ur = lur.times(smpur.getRight());
            lar = lar.times(smpur.getLeft());
            rar = rar.times(smpur.getRight());
        } else {
            ur = lur.times(rur);
            lar = lar.times(lur);
            rar = rar.times(rur);
        }

        SymmetricFilter sl = SymmetricFilter.createFromFilter(lar), sr = SymmetricFilter.createFromFilter(rar);

        // use SymmetricFilter for the numerator.
        SymmetricFilter lma = l.sma(), rma = r.sma(); // contains the innovation
        // variances...
        SymmetricFilter snum;
        if (plus) {
            snum = lma.times(sr).plus(rma.times(sl));
        } else {
            snum = lma.times(sr).minus(rma.times(sl));
        }
        if (snum.isNull()) {
            return new ArimaModel(null, null, null, 0);
        }

        ArimaModel rslt = new ArimaModel(ar, ur, null, null, 0, snum);
        if (simplify) {
            rslt.simplify();
        } else {
            rslt.simplifyAr();
        }
        return rslt;
    }

    /**
     * Creates a white noise, with variance 1.
     */
    public ArimaModel() {
        bar_ = BackFilter.ONE;
        bma_ = BackFilter.ONE;
        delta_ = BackFilter.ONE;
        var_ = 1;
    }

    /**
     * Creates a white noise, with variance var.
     *
     * @param var The variance of the innovations
     */
    public ArimaModel(double var) {
        bar_ = BackFilter.ONE;
        bma_ = BackFilter.ONE;
        delta_ = BackFilter.ONE;
        var_ = var;
    }

    /**
     * Creates a new Arima model
     *
     * @param ar The stationary auto-regressive polynomial
     * @param delta The non stationary auto-regressive polynomial
     * @param ma The moving average polynomial
     * @param var The innovation variance
     */
    public ArimaModel(final BackFilter ar, final BackFilter delta, final BackFilter ma,
            final double var) {
        var_ = var;
        if (ar == null) {
            bar_ = BackFilter.ONE;
        } else {
            bar_ = ar;
        }
        if (ma == null) {
            bma_ = BackFilter.ONE;
        } else {
            bma_ = ma;
        }
        if (delta == null) {
            delta_ = BackFilter.ONE;
        } else {
            delta_ = delta;
        }
        check();
    }

    /**
     * Creates a new Arima model
     *
     * @param ar The stationary auto-regressive polynomial P(B)
     * @param delta The non stationary auto-regressive polynomial D(B)
     * @param sma A symmetric filter corresponding to var*Q(B)*Q(F)
     */
    public ArimaModel(final BackFilter ar, final BackFilter delta,
            final SymmetricFilter sma) {
        if (ar == null) {
            bar_ = BackFilter.ONE;
        } else {
            bar_ = ar;
        }
        if (sma == null) {
            bma_ = BackFilter.ONE;
        } else {
            sma_ = sma;
        }
        if (delta == null) {
            delta_ = BackFilter.ONE;
        } else {
            delta_ = delta;
        }
        check();
    }

    /**
     * Creates a new Arima model, using its polynomial representation and its
     * symmetric filter representation. Attention, the constructor will not
     * verify the coherence of the information.
     *
     * @param bar Stationary auto-regressive polynomial. May be null.
     * @param ur Non-stationary auto-regressive polynomial. May be null.
     * @param sar Symmetric auto-regressive filter. Should be equal to
     * bar(B)*bar(F) if bar is defined.
     * @param bma Moving average polynomial. May be null
     * @param var Innovation variance.
     * @param sma Symmetric moving average filter. Should be equal to
     * var*bma(B)*bma(F) if bma is defined.
     */
    public ArimaModel(final BackFilter bar, final BackFilter ur,
            final SymmetricFilter sar, final BackFilter bma, final double var,
            final SymmetricFilter sma) {
        bar_ = bar;
        delta_ = ur;
        sar_ = sar;
        bma_ = bma;
        sma_ = sma;
        var_ = var;
        check();
    }

    /**
     * Create a new Arima model, starting from a stationary model and from a
     * differencing filter. if starima ~ p(B)y(t) = q(B) e(t) e~N(0, vI), the
     * result is p(B) ur(B) y(t) = q(B) e(t) e~N(0, vI)
     *
     * @param starima The stationary model
     * @param ur The differencing filter
     */
    public ArimaModel(final IArimaModel starima, final BackFilter ur) {
        if (!starima.isStationary()) {
            throw new ArimaException(ArimaException.NonStationary);
        }
        bar_ = starima.getAR();
        bma_ = starima.getMA();
        if (ur == null) {
            delta_ = BackFilter.ONE;
        } else {
            delta_ = ur;
        }
        var_ = starima.getInnovationVariance();
    }

    private void ar_btos() {
        sar_ = SymmetricFilter.createFromFilter(bar_.times(delta_));
    }

    private BackFilter bma() throws ArimaException {
        if (bma_ == null) {
            ma_stob();
        }
        return bma_;
    }

    private void check() {
        if (delta_ == null) {
            delta_ = BackFilter.ONE;
        }
        if (bar_ == null) {
            bar_ = BackFilter.ONE;
        } else {
            BackFilter ar = check(bar_);
            if (ar != null) {
                bar_ = ar;
                sar_ = null;
            }
        }
        BackFilter ma = check(bma_);
        if (ma != null) {
            bma_ = ma;
            sma_ = null;
        }
    }

    @Override
    public BackFilter getAR() {
        // get { return bar()*delta_; }
        return bar_.times(delta_);
    }

    /**
     *
     * @return
     */
    @Override
    public int getARCount() {
        return getStationaryARCount() + getNonStationaryARCount();
    }

    /**
     *
     * @return @throws ArimaException
     */
    @Override
    public double getInnovationVariance() throws ArimaException {
        return var();
    }

    /**
     *
     * @return @throws ArimaException
     */
    @Override
    public BackFilter getMA() {
        return bma();
    }

    @Override
    public int getMACount() {
        if (bma_ != null) {
            return bma_.getDegree();
        } else {
            return sma_.getDegree();
        }
    }

    @Override
    public BackFilter getNonStationaryAR() {
        return delta_;
    }

    @Override
    public int getNonStationaryARCount() {
        return delta_.getDegree();
    }

    @Override
    public BackFilter getStationaryAR() {
        return bar_;
        // get { return bar(); }
    }

    /**
     *
     * @return
     */
    @Override
    public int getStationaryARCount() {
        return bar_.getDegree();
    }

    private void initproperties() {
        st_ = true;
        if (delta_.getLength() > 1) {
            st_ = false;
        }
        try {
            Complex[] rma = bma().roots();
            inv_ = true;
            if (rma != null) {
                for (int i = 0; i < rma.length; ++i) {
                    double nrm = rma[i].absSquare();
                    if (nrm <= 1 + EPS) {
                        inv_ = false;
                        break;
                    }
                }
            }
        } catch (ArimaException ex) {
            inv_ = false;
        }

        initprop_ = true;
    }

    @Override
    protected Spectrum initSpectrum() {
        return new Spectrum(sma(), sar());
    }

    @Override
    public boolean isInvertible() {
        if (!initprop_) {
            initproperties();
        }
        return inv_;
    }

    @Override
    public boolean isNull() {
        return delta_.getDegree() == 0
                && (sma_ != null ? sma_.isNull() : var_ == 0);
    }

    @Override
    public boolean isStationary() {
        if (!initprop_) {
            initproperties();
        }
        return st_;
    }

    /**
     *
     * @return
     */
    public boolean isWhiteNoise() {
        if (bar_.getDegree() != 0) {
            return false;
        }
        if (delta_.getDegree() != 0) {
            return false;
        }
        if (bma_ != null && bma_.getDegree() != 0) {
            return false;
        }
        if (sma_ != null && sma_.getDegree() != 0) {
            return false;
        }
        // computes the variance, if necessary
        if (bma_ == null) {
            if (sma_ == null) {
                var_ = 0;
            } else {
                var_ = sma_.getWeight(0);
                bma_ = BackFilter.ONE;
            }
        }
        return true;
    }

    private void ma_btos() {
        sma_ = SymmetricFilter.createFromFilter(bma_);
        sma_ = sma_.times(var_);
    }

    // conversion between BFilter and SymmetricFilter
    private void ma_stob() {
        if (sma_.getLength() == 1) {
            bma_ = BackFilter.ONE;
            var_ = sma_.getWeight(0);
        } else {
//             bma_ = new SymmetricFilter.Decomposer().factorize(sma_);
//            if (bma_ == null)
            ma_stob2();
//            else{
//                double v=bma_.get(0);
//                var_=v*v;
//                bma_=bma_.normalize();
//            }
        }
    }

    // conversion between BFilter and SymmetricFilter
    private void ma_stob2() {
        //SymmetricFrequencyResponseDecomposer2 sfr2 = new SymmetricFrequencyResponseDecomposer2();
        SymmetricFrequencyResponseDecomposer sfr = new SymmetricFrequencyResponseDecomposer();
        if (sfr.decompose(sma_)) {
            bma_ = sfr.getBFilter();
            var_ = sfr.getFactor();
//        } else if (sfr2.decompose(sma_)) {
//            bma_ = sfr2.getBFilter();
//            var_ = sfr2.getFactor();
        } else {
            SymmetricFrequencyResponseDecomposer3 sfr3 = new SymmetricFrequencyResponseDecomposer3();
            if (sfr3.decompose(sma_)) {
                bma_ = sfr3.getBFilter();
                var_ = sfr3.getFactor();
            } else {
                throw new ArimaException(ArimaException.InvalidDecomposition);
            }
        }
    }

    /**
     *
     * @param r
     * @return
     */
    public ArimaModel minus(final ArimaModel r) {
        return pm(this, r, false, false);
    }

    /**
     *
     * @param r
     * @return
     */
    public ArimaModel minus(final ArimaModel r, boolean simplify) {
        return pm(this, r, false, simplify);
    }

    /**
     *
     * @param v
     * @return
     */
    public ArimaModel minus(final double v) {
        if (isWhiteNoise()) {
            return new ArimaModel(null, null, null, var_ - v);
        }
        // use SymmetricFilter for the numerator.
        SymmetricFilter sma = sma(), sar = sar();
        SymmetricFilter snum = sma.minus(SymmetricFilter.multiply(v, sar));
        ArimaModel rslt = new ArimaModel(bar_, delta_, sar_, null, 0, snum);
        return rslt;
    }

    /**
     *
     * @param r
     * @return
     */
    public ArimaModel plus(final ArimaModel r) {
        return pm(this, r, true, true);
    }

    public ArimaModel plus(final ArimaModel r, boolean simplify) {
        return pm(this, r, true, simplify);
    }

    /**
     *
     * @param v
     * @return
     */
    public ArimaModel plus(final double v) {
        if (isWhiteNoise()) {
            return new ArimaModel(null, null, null, v + var_);
        }
        // use SymmetricFilter for the numerator.
        SymmetricFilter sma = sma(), sar = sar();
        SymmetricFilter snum = sma.plus(SymmetricFilter.multiply(v, sar));
        ArimaModel rslt = new ArimaModel(bar_, delta_, sar_, null, 0, snum);
        return rslt;
    }

    /**
     * Computes the sum of two arima models (considering that their innovations
     * are independent.
     *
     * @param r
     * @return
     * @throws ArimaException
     */
    public ArimaModel plus(final IArimaModel r) throws ArimaException {
        ArimaModel m = ArimaModel.create(r);
        if (m == null) {
            throw new ArimaException("+ operation failed");
        }
        return plus(m);
    }

    SymmetricFilter sar() {
        if (sar_ == null) {
            ar_btos();
        }
        return sar_;
    }

    /**
     *
     * @return
     */
    public boolean simplify() {
        boolean sar = simplifyAr();
        boolean sur = simplifyUr();
        if (sar || sur) {
            clearCachedObjects();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Simplifies possible common roots between the stationary auto-regressive
     * polynomial and the moving average polynomial.
     *
     * @return True if the model has been simplified
     */
    public boolean simplifyAr() {
        try {
            BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(false);
            if (smp.simplify(bar_, bma())) {
                bar_ = smp.getLeft();
                bma_ = smp.getRight();
                sar_ = null;
                sma_ = null;
                return true;
            } else {
                return false;
            }
        } catch (ArimaException e) {
            return false;
        }
    }

    /**
     * Simplifies possible common roots between the non-stationary
     * auto-regressive polynomial and the moving average polynomial.
     *
     * @return True if the model has been simplified
     */
    public boolean simplifyUr() {
        try {
            UnitRootsSolver urs = new UnitRootsSolver();
            if (!urs.factorize(bma().getPolynomial())) {
                return false;
            }
            Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
            if (smp.simplify(urs.getUnitRoots().toPolynomial(), delta_.getPolynomial())) {
                delta_ = new BackFilter(smp.getRight());
                bma_ = new BackFilter(smp.getLeft().times(urs.remainder()));
                sma_ = null;
                return true;
            } else {
                return false;
            }

//            BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(true);
//            if (smp.simplify(delta_, bma())) {
//                delta_ = smp.getLeft();
//                bma_ = smp.getRight();
//                sma_ = null;
//                return true;
//            } else {
//                return false;
//            }
        } catch (ArimaException e) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public SymmetricFilter sma() {
        if (sma_ == null) {
            ma_btos();
        }
        return sma_;
    }

    @Override
    public StationaryTransformation stationaryTransformation() {
        return new StationaryTransformation(new ArimaModel(bar_, BackFilter.ONE,
                null, bma_, var_, sma_), delta_);
    }

    double var() throws ArimaException {
        if (bma_ == null) {
            ma_stob();
        }
        return var_;
    }
}
