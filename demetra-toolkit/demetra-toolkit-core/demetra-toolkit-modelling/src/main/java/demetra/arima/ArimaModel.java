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
package demetra.arima;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.PolynomialType;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.internal.UnitRootsSolver;
import javax.annotation.Nonnull;

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
     */
    public static ArimaModel whiteNoise(double var) {
        return new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, var);
    }

//    public ArimaModel of(final Polynomial fullAR, final Polynomial MA,
//            final double var) {
//        try {
//            double x = fullAR.get(0), y = MA.get(0);
//
//            BackFilter ar = new BackFilter(fullAR);
//            if (x != 1) {
//                ar = ar.normalize();
//            }
//            BackFilter ur = BackFilter.ONE;
//            BackFilter.StationaryTransformation st = new BackFilter.StationaryTransformation();
//            if (st.transform(ar)) {
//                ar = st.stationaryFilter;
//                ur = st.unitRoots;
//            }
//            BackFilter ma = new BackFilter(MA);
//            if (y != 1) {
//                ma = ma.normalize();
//            }
//            return new ArimaModel(ar, ur, ma, var * y / x * y / x);
//        } catch (RuntimeException ex) {
//            return null;
//        }
//    }

    public static ArimaModel copyOf(IArimaModel arima) {
        return new ArimaModel(arima.getStationaryAR(), arima.getNonStationaryAR(),
                arima.getMA(), arima.getInnovationVariance());
    }

    /**
     * Creates a new Arima model
     *
     * @param ar The stationary auto-regressive polynomial
     * @param delta The non stationary auto-regressive polynomial
     * @param ma The moving average polynomial
     * @param var The innovation variance
     */
    public ArimaModel(@Nonnull final BackFilter ar, @Nonnull final BackFilter delta, @Nonnull final BackFilter ma,
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
    public ArimaModel(@Nonnull final BackFilter ar, @Nonnull final BackFilter delta,
            @Nonnull final SymmetricFilter sma) {
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
        ArimaModel m = ArimaModel.copyOf(l);
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
        ArimaModel m = ArimaModel.copyOf(l);
        if (m == null) {
            throw new ArimaException("+ operation failed");
        }
        return m.minus(r);
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
        if (!lm.getNonStationaryAR().asPolynomial().equals(rm.getNonStationaryAR().asPolynomial(), eps)) {
            return false;
        }
        if (!lm.getStationaryAR().asPolynomial().equals(rm.getStationaryAR().asPolynomial(), eps)) {
            return false;
        }
        if (!lm.getMA().asPolynomial().equals(rm.getMA().asPolynomial(), eps)) {
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
        BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(false);
        if (smp.simplify(lar, rar)) {
            ar = lar.times(smp.getRight());
            lar = smp.getLeft();
            rar = smp.getRight();
        } else {
            ar = lar.times(rar);
        }

        BackFilter lur = l.delta, rur = r.delta, ur;
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

        SymmetricFilter sl = SymmetricFilter.fromFilter(lar), sr = SymmetricFilter.fromFilter(rar);

        // use SymmetricFilter for the numerator.
        SymmetricFilter lma = l.symmetricMA(), rma = r.symmetricMA(); // contains the innovation
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
    public BackFilter getAR() {
        // get { return bar()*delta; }
        return ar.times(delta);
    }

    /**
     *
     * @return
     */
    @Override
    public int getAROrder() {
        return getStationaryAROrder() + getNonStationaryAROrder();
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
    public BackFilter getMA() {
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
        return fac.factor;
    }

    @Override
    public int getMAOrder() {
        if (ma != null) {
            return -ma.getLowerBound();
        } else {
            return sma.getUpperBound();
        }
    }

    @Override
    public BackFilter getNonStationaryAR() {
        return delta;
    }

    @Override
    public int getNonStationaryAROrder() {
        return delta.getDegree();
    }

    @Override
    public BackFilter getStationaryAR() {
        return ar;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStationaryAROrder() {
        return ar.getDegree();
    }

    @Override
    public boolean isInvertible() {
        try {
            Complex[] rma = getMA().roots();
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
        return delta.getDegree() == 0
                && (sma != null ? sma.isNull() : Math.abs(var)<EPS);
    }

    /**
     *
     * @return
     */
    public boolean isWhiteNoise() {
        if (ar.getDegree() > 0) {
            return false;
        }
        if (delta.getDegree() > 0) {
            return false;
        }
        if (ma != null && ma.getDegree() > 0) {
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
        SymmetricFilter sma = symmetricMA(), sar = symmetricAR();
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
        SymmetricFilter sma = symmetricMA(), sar = symmetricAR();
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
        ArimaModel m = ArimaModel.copyOf(r);
        if (m == null) {
            throw new ArimaException("+ operation failed");
        }
        return plus(m);
    }
    
    public ArimaType toType(String desc){
        return ArimaType.builder()
                .ar(PolynomialType.of(getStationaryAR().asPolynomial().toArray()))
                .delta(PolynomialType.of(getNonStationaryAR().asPolynomial().toArray()))
                .ma(PolynomialType.of(getMA().asPolynomial().toArray()))
                .innovationVariance(getInnovationVariance())
                .name(desc)
                .build();
    }

    @Override
    public SymmetricFilter symmetricAR() {
        SymmetricFilter s = sar;
        if (s == null) {
            synchronized (this) {
                s = sar;
                if (s == null) {
                    s = SymmetricFilter.fromFilter(ar.times(delta));
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
            BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(false);
            if (smp.simplify(ar, getMA())) {
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
            if (!urs.factorize(getMA().asPolynomial())) {
                return this;
            }
            Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
            if (smp.simplify(urs.getUnitRoots().toPolynomial(), delta.asPolynomial())) {
                BackFilter ndelta = new BackFilter(smp.getRight());
                BackFilter nma = new BackFilter(smp.getLeft().times(urs.remainder()));
                return new ArimaModel(getStationaryAR(), ndelta, nma, getInnovationVariance());
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
    public SymmetricFilter symmetricMA() {
        if (sma != null) {
            return sma;
        }
        SymmetricFilter s = derivedsma;
        if (s == null) {
            synchronized (this) {
                s = derivedsma;
                if (s == null) {
                    s = SymmetricFilter.fromFilter(ma, var);
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
