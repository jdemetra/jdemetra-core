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
package jdplus.arima;

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import demetra.math.Complex;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRootsSolver;
import org.checkerframework.checker.nullness.qual.NonNull;

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
@Immutable(lazy = true)
@Development(status = Development.Status.Alpha)
public final class ArimaModel extends AbstractArimaModel {

    public static final ArimaModel ONE = whiteNoise(), NULL = whiteNoise(0);

    private static final double EPS = 1e-6;
    private final BackFilter ar;// stationary part
    private final BackFilter delta; // non stationary ar
    private final BackFilter ma;
    private final double var;
    private final SymmetricFilter sma; // stationary part

    // Computed elements
    private volatile SymmetricFilter.Factorization smaFactorization;
    private volatile SymmetricFilter sar, derivedsma; // stationary part

    /**
     * Creates a white noise, with variance 1.
     *
     * @return
     */
    public static ArimaModel whiteNoise() {
        return new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, 1);
    }

    /**
     * Creates a white noise, with variance 1.
     *
     * @param var
     * @return 
     */
    public static ArimaModel whiteNoise(double var) {
        return new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, var);
    }

    public static ArimaModel of(IArimaModel arima) {
        return new ArimaModel(arima.getStationaryAr(), arima.getNonStationaryAr(),
                arima.getMa(), arima.getInnovationVariance());
    }

    /**
     * Creates a new Arima model
     *
     * @param ar The stationary auto-regressive polynomial
     * @param delta The non stationary auto-regressive polynomial
     * @param ma The moving average polynomial
     * @param var The innovation variance
     */
    public ArimaModel(@NonNull final BackFilter ar, @NonNull final BackFilter delta, @NonNull final BackFilter ma,
            final double var) {
        this.var = var;
        this.ar = ar;
        this.delta = delta;
        this.ma = ma;
        sma = null;
    }

    /**
     * Creates a new Arima model
     *
     * @param ar The stationary auto-regressive polynomial P(B)
     * @param delta The non stationary auto-regressive polynomial D(B)
     * @param sma A symmetric filter corresponding to var*Q(B)*Q(F)
     */
    public ArimaModel(@NonNull final BackFilter ar, @NonNull final BackFilter delta,
            @NonNull final SymmetricFilter sma) {
        this.ar = ar;
        this.delta = delta;
        this.sma = sma;
        this.ma = null;
        this.var = 0;
    }

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
        return of(l).plus(r);
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
        return of(l).minus(r);
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
        if (!lm.getNonStationaryAr().asPolynomial().equals(rm.getNonStationaryAr().asPolynomial(), eps)) {
            return false;
        }
        if (!lm.getStationaryAr().asPolynomial().equals(rm.getStationaryAr().asPolynomial(), eps)) {
            return false;
        }
        if (!lm.getMa().asPolynomial().equals(rm.getMa().asPolynomial(), eps)) {
            return false;
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

        ArimaModel m;
        if (sma == null) {
            m = new ArimaModel(ar, delta, ma, var * factor);
        } else {
            m = new ArimaModel(ar, delta, sma.times(factor));
        }
        m.sar = sar;
        SymmetricFilter dma = derivedsma;
        if (dma != null) {
            m.derivedsma = derivedsma.times(factor);
        }
        SymmetricFilter.Factorization fac = smaFactorization;
        if (fac != null) {
            m.smaFactorization = new SymmetricFilter.Factorization(fac.factor, fac.scaling * factor);
        }
        return m;
    }

    /**
     * Normalizes the variance of the innovation
     *
     * @return A new arima model is returned. Its innovation variance is 1.
     */
    public ArimaModel normalize() {
        return scaleVariance(1 / getInnovationVariance());
    }

    private static ArimaModel pm(final ArimaModel l, final ArimaModel r,
            final boolean plus, final boolean simplify) {
        if (r.isWhiteNoise()) {
            if (plus) {
                return l.plus(r.getInnovationVariance());
            } else {
                return l.minus(r.getInnovationVariance());
            }
        } else if (l.isWhiteNoise() && plus) {
            return ArimaModel.add(l.getInnovationVariance(), r);
        }

        // compute the denominator
        BackFilter lar = l.ar, rar = r.ar, ar;
        // BFilter lar = l.bar(), rar = r.bar(), ar = null;
        BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
        if (smp.simplify(lar, rar)) {
            ar = lar.times(smp.getRight());
            lar = smp.getLeft();
            rar = smp.getRight();
        } else {
            ar = lar.times(rar);
        }

        BackFilter lur = l.delta, rur = r.delta, ur;
        BackFilter.SimplifyingTool smpur = new BackFilter.SimplifyingTool();
        if (smpur.simplify(lur, rur)) {
            ur = lur.times(smpur.getRight());
            lar = lar.times(smpur.getLeft());
            rar = rar.times(smpur.getRight());
        } else {
            ur = lur.times(rur);
            lar = lar.times(lur);
            rar = rar.times(rur);
        }

        SymmetricFilter sl = SymmetricFilter.convolutionOf(lar), sr = SymmetricFilter.convolutionOf(rar);

        // use SymmetricFilter for the numerator.
        SymmetricFilter lma = l.symmetricMa(), rma = r.symmetricMa(); // contains the innovation
        // variances...
        SymmetricFilter snum;
        if (plus) {
            snum = lma.times(sr).plus(rma.times(sl));
        } else {
            snum = lma.times(sr).minus(rma.times(sl));
        }
        if (snum.isNull()) {
            return NULL;
        }

        ArimaModel rslt = new ArimaModel(ar, ur, snum);
        if (simplify) {
            return rslt.simplify();
        } else {
            return rslt.simplifyAr();
        }
    }

    @Override
    public BackFilter getAr() {
        // get { return bar()*delta; }
        return ar.times(delta);
    }

    /**
     *
     * @return
     */
    @Override
    public int getArOrder() {
        return getStationaryArOrder() + getNonStationaryArOrder();
    }

    /**
     *
     * @return @throws ArimaException
     */
    @Override
    public double getInnovationVariance() throws ArimaException {
        if (ma != null) {
            return var;
        }
        SymmetricFilter.Factorization fac = smaFactorization;
        if (fac == null) {
            synchronized (this) {
                fac = smaFactorization;
                if (fac == null) {
                    fac = sma.factorize();
                    smaFactorization = fac;
                }
            }
        }
        return fac.scaling;
    }

    /**
     *
     * @return @throws ArimaException
     */
    @Override
    public BackFilter getMa() {
        if (ma != null) {
            return ma;
        }
        SymmetricFilter.Factorization fac = smaFactorization;
        if (fac == null) {
            synchronized (this) {
                fac = smaFactorization;
                if (fac == null) {
                    fac = sma.factorize();
                    smaFactorization = fac;
                }
            }
        }
        if (fac == null)
            throw new ArimaException(ArimaException.INVALID);
        return fac.factor;
    }

    @Override
    public int getMaOrder() {
        if (ma != null) {
            return ma.length()-1;
        } else {
            return sma.getUpperBound();
        }
    }

    @Override
    public BackFilter getNonStationaryAr() {
        return delta;
    }

    @Override
    public int getNonStationaryArOrder() {
        return delta.length()-1;
    }

    @Override
    public BackFilter getStationaryAr() {
        return ar;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStationaryArOrder() {
        return ar.length()-1;
    }

    @Override
    public boolean isInvertible() {
        try {
            Complex[] rma = getMa().roots();
            if (rma != null) {
                for (int i = 0; i < rma.length; ++i) {
                    double nrm = rma[i].absSquare();
                    if (nrm <= 1 + EPS) {
                        return false;
                    }
                }
            }
            return true;
        } catch (ArimaException ex) {
            return false;
        }
    }

    @Override
    public boolean isNull() {
        if (this == NULL) {
            return true;
        }
        return delta.length() == 1
                && (sma != null ? sma.isNull() : Math.abs(var)<EPS);
    }

    /**
     *
     * @return
     */
    public boolean isWhiteNoise() {
        if (ar.length() > 1) {
            return false;
        }
        if (delta.length() > 1) {
            return false;
        }
        if (ma != null && ma.length() > 1) {
            return false;
        }
        if (sma != null && sma.length() > 1) {
            return false;
        }
        return getInnovationVariance() > 0;
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
            if (Math.abs(var-getInnovationVariance())<EPS)
            return NULL;
        }
        // use SymmetricFilter for the numerator.
        SymmetricFilter sma = symmetricMa(), sar = symmetricAr();
        SymmetricFilter snum = sma.minus(SymmetricFilter.multiply(v, sar));
        ArimaModel rslt = new ArimaModel(ar, delta, snum);
        rslt.sar = sar;
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
            return ArimaModel.whiteNoise(v + getInnovationVariance());
        }
        // use SymmetricFilter for the numerator.
        SymmetricFilter sma = symmetricMa(), sar = symmetricAr();
        SymmetricFilter snum = sma.plus(SymmetricFilter.multiply(v, sar));
        ArimaModel rslt = new ArimaModel(ar, delta, snum);
        rslt.sar = sar;
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
        return plus(of(r));
    }
    
    @Override
    public SymmetricFilter symmetricAr() {
        SymmetricFilter s = sar;
        if (s == null) {
            synchronized (this) {
                s = sar;
                if (s == null) {
                    s = SymmetricFilter.convolutionOf(ar.times(delta));
                    sar = s;
                }
            }
        }
        return s;
    }

    /**
     *
     * @return
     */
    public ArimaModel simplify() {
        ArimaModel m = simplifyAr();
        return m.simplifyUr();
    }

    /**
     * Simplifies possible common roots between the stationary auto-regressive
     * polynomial and the moving average polynomial.
     *
     * @return True if the model has been simplified
     */
    public ArimaModel simplifyAr() {
        try {
            BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
            if (smp.simplify(ar, getMa())) {
                BackFilter nar = smp.getLeft();
                BackFilter nma = smp.getRight();
                return new ArimaModel(nar, delta, nma, getInnovationVariance());
            } else {
                return this;
            }
        } catch (ArimaException e) {
            return this;
        }
    }

    /**
     * Simplifies possible common roots between the non-stationary
     * auto-regressive polynomial and the moving average polynomial.
     *
     * @return The simplified model (=this f no simplification)
     */
    public ArimaModel simplifyUr() {
        try {
            UnitRootsSolver urs = new UnitRootsSolver();
            if (!urs.factorize(getMa().asPolynomial())) {
                return this;
            }
            Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
            if (smp.simplify(urs.getUnitRoots().asPolynomial(), delta.asPolynomial())) {
                BackFilter ndelta = new BackFilter(smp.getRight());
                BackFilter nma = new BackFilter(smp.getLeft().times(urs.remainder()));
                return new ArimaModel(getStationaryAr(), ndelta, nma, getInnovationVariance());
            } else {
                return this;
            }

        } catch (ArimaException e) {
            return this;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public SymmetricFilter symmetricMa() {
        if (sma != null) {
            return sma;
        }
        SymmetricFilter s = derivedsma;
        if (s == null) {
            synchronized (this) {
                s = derivedsma;
                if (s == null) {
                    s = SymmetricFilter.convolutionOf(ma, var);
                    derivedsma = s;
                }
            }
        }
        return derivedsma;
    }

    @Override
    public StationaryTransformation<ArimaModel> stationaryTransformation() {
        ArimaModel st;
        if (sma == null) {
            st = new ArimaModel(ar, BackFilter.ONE, ma, var);
            st.derivedsma = derivedsma;
        } else {
            st = new ArimaModel(ar, BackFilter.ONE, sma);
            st.smaFactorization = smaFactorization;
        }
        return new StationaryTransformation(st, delta);
    }

}
