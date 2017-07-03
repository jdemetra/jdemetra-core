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
package demetra.sarima;

import ec.tstoolkit.arima.*;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * Box-Jenkins seasonal arima model AR(B)* SAR(B)*D(B)*SD(B) y(t) =
 * MA(B)*SMA(B)e(t), e~N(0, var) AR(B) = 1+a(1)B+...+a(p)B^p, regular
 * auto-regressive polynomial SAR(B) = 1+b(1)B^s+...+b(bp)B^s*bp, seasonal
 * auto-regressive polynomial D(B) = 1+e(1)B+...+e(d)B^d, regula differencing
 * polynomial SD(B) = 1+f(1)B^s+...+f(bd)B^s*bd, seasonal differencing
 * polynomial MA(B) = 1+c(1)B+...+c(q)B^q, regular moving average polynomial
 * SMA(B) = 1+d(1)B^s+...+d(bq)B^s*bq, seasonal moving average polynomial
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaModel extends AbstractArimaModel implements IArimaModel,
        Cloneable {

    private static final double EPS = 1e-6;

    private double[] params_;
//    private double var_ = 1;
    private SarimaSpecification spec_;
//    public static final int PMax = 3, QMax = 3, DMax = 2, BPMax = 1, BQMax = 1,
//            BDMax = 1;

    /**
     *
     */
    private SarimaModel() {
    }

    /**
     *
     * @param spec
     */
    public SarimaModel(final SarimaSpecification spec) {
        spec_ = spec.clone();
        params_ = new double[spec.getParametersCount()];
        setDefault();
    }

//    /**
//     *
//     * @param spec
//     * @param var
//     */
//    public SarimaModel(final SarimaSpecification spec, final double var) {
//        spec_ = spec.clone();
//        params_ = new double[spec.getParametersCount()];
//        var_ = var;
//        setDefault();
//    }
    /**
     *
     * @param spec
     * @param parameters
     * @throws ArimaException
     */
    public SarimaModel(final SarimaSpecification spec, final double[] parameters)
            throws ArimaException {
        if (parameters.length != spec.getParametersCount()) {
            throw new ArimaException(ArimaException.InvalidModel);
        }
        spec_ = spec.clone();
        params_ = parameters.clone();
    }

//    /**
//     *
//     * @param spec
//     * @param parameters
//     * @param var
//     * @throws ArimaException
//     */
//    public SarimaModel(final SarimaSpecification spec,
//            final double[] parameters, final double var) throws ArimaException {
//        spec_ = spec.clone();
//        params_ = parameters.clone();
//        if (parameters.length != spec.getParametersCount()) {
//            throw new ArimaException(ArimaException.InvalidModel);
//        }
//        var_ = var;
//    }
    /**
     *
     * @param spec
     */
    public SarimaModel(final SarmaSpecification spec) {
        spec_ = new SarimaSpecification(spec);
        params_ = new double[spec.getParametersCount()];
        setDefault();
    }

    /**
     *
     * @return @throws ArimaException
     */
    public boolean adjustSpecification() throws ArimaException {
        boolean rslt = false;
        int p = spec_.getP();
        for (int i = p; i > 0; --i) {
            if (Math.abs(phi(i)) < EPS) {
                --p;
                rslt = true;
            } else {
                break;
            }
        }
        int bp = spec_.getBP();
        for (int i = bp; i > 0; --i) {
            if (Math.abs(bphi(i)) < EPS) {
                --bp;
                rslt = true;
            } else {
                break;
            }
        }
        int q = spec_.getQ();
        for (int i = q; i > 0; --i) {
            if (Math.abs(theta(i)) < EPS) {
                --q;
                rslt = true;
            } else {
                break;
            }
        }
        int bq = spec_.getBQ();
        for (int i = bq; i > 0; --i) {
            if (Math.abs(btheta(i)) < EPS) {
                --bq;
                rslt = true;
            } else {
                break;
            }
        }
        if (rslt) {
            SarimaSpecification spec = spec_.clone();
            spec.setP(p);
            spec.setBP(bp);
            spec.setQ(q);
            spec.setBQ(bq);
            double[] nparams = new double[spec.getParametersCount()];
            // P
            int i = 0, j = 0;
            for (int k = 0; k < p; ++k) {
                nparams[j++] = params_[i++];
            }
            i += spec_.P - p;
            for (int k = 0; k < bp; ++k) {
                nparams[j++] = params_[i++];
            }
            i += spec_.BP - bp;
            for (int k = 0; k < q; ++k) {
                nparams[j++] = params_[i++];
            }
            i += spec_.Q - q;
            for (int k = 0; k < bq; ++k) {
                nparams[j++] = params_[i++];
            }
            spec_ = spec;
            params_ = nparams;
            clearCachedObjects();
        }
        return rslt;
    }

    /**
     *
     * @param lag
     * @return
     * @throws ArimaException
     */
    public double bphi(final int lag) throws ArimaException {
        if (lag <= 0 || lag > spec_.getBP()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        return params_[spec_.getP() + lag - 1];
    }

    /**
     *
     * @param lag
     * @return
     * @throws ArimaException
     */
    public double btheta(final int lag) throws ArimaException {
        if (lag <= 0 || lag > spec_.getBQ()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        return params_[spec_.getP() + spec_.getBP() + spec_.getQ() + lag
                - 1];
    }

    @Override
    public SarimaModel clone() {
        SarimaModel model = (SarimaModel) super.clone();
        model.spec_ = spec_.clone();
        model.params_ = params_.clone();
        return model;
    }

    @Override
    public BackFilter getAR() {
//        // compute the length...
//        int f = spec_.getFrequency();
//        int n = spec_.getP() + spec_.getD() + f
//                * (spec_.getBP() + spec_.getBD()) + 1;
//        double[] c = new double[n];
//        int d = 0;
//        c[0] = 1;
//        // regular part
//        if (spec_.getP() > 0) {
//            System.arraycopy(params_, 0, c, 1, spec_.getP());
//            d += spec_.getP();
//        }
//
//        // seasonal part
//        if (f > 1 && spec_.getBP() > 0) {
//            int p0 = spec_.getP();
//            if (spec_.getP() < f) {
//                for (int i = 0; i < spec_.getBP(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j <= d; ++j) {
//                        c[(i + 1) * f + j] = c[j] * x;
//                    }
//                }
//            } else {
//                // we must use a buffer
//                double[] tmp = new double[spec_.getP()];
//                System.arraycopy(c, 0, tmp, 0, tmp.length);
//                for (int i = 0; i < spec_.getBP(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j < tmp.length; ++j) {
//                        c[(i + 1) * f + j] += tmp[j] * x;
//                    }
//                }
//            }
//            d += f * spec_.getBP();
//        }
//
//        // times (1-B)^D
//        for (int i = 0; i < spec_.getD(); ++i, ++d) {
//            for (int j = d; j >= 0; --j) {
//                c[j + 1] -= c[j];
//            }
//        }
//        // times (1-B^freq)^D
//        for (int i = 0; i < spec_.getBD(); ++i, d += f) {
//            for (int j = d; j >= 0; --j) {
//                c[j + f] -= c[j];
//            }
//        }
//
//        return BackFilter.of(c);
//
        BackFilter df=spec_.getDifferencingFilter();
        BackFilter st=this.getStationaryAR();
        return df.times(st);
    }

    /**
     *
     * @return
     */
    @Override
    public int getARCount() {
        int n = spec_.getP() + spec_.getD();
        if (spec_.getFrequency() != 1) {
            n += spec_.getFrequency() * (spec_.getBP() + spec_.getBD());
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getDifferenceOrder() {
        int n = spec_.getD();
        if (spec_.getFrequency() > 1) {
            n += spec_.getFrequency() * spec_.getBD();
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return spec_.getFrequency();
    }

    /**
     *
     * @return
     */
    @Override
    public double getInnovationVariance() {
//        return var_;
        return 1;
    }

    @Override
    public BackFilter getMA() {
//        // compute the length...
//        int f = spec_.getFrequency();
//        int n = spec_.getQ() + f * (spec_.getBQ()) + 1;
//        double[] c = new double[n];
//        int d = 0;
//        c[0] = 1;
//        // regular part
//        int p0 = spec_.getP() + spec_.getBP();
//        for (int i = 0; i < spec_.getQ(); ++i) {
//            c[i + 1] = params_[p0 + i];
//        }
//        d += spec_.getQ();
//        // seasonal part
//        if (f > 1 && spec_.getBQ() > 0) {
//            p0 += spec_.getQ();
//            if (spec_.getQ() < f) {
//                for (int i = 0; i < spec_.getBQ(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j <= d; ++j) {
//                        c[(i + 1) * f + j] = c[j] * x;
//                    }
//                }
//            } else {
//                // we must use a buffer
//                double[] tmp = new double[spec_.getQ() + 1];
//                System.arraycopy(c, 0, tmp, 0, tmp.length);
//                for (int i = 0; i < spec_.getBQ(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j < tmp.length; ++j) {
//                        c[(i + 1) * f + j] += tmp[j] * x;
//                    }
//                }
//            }
//        }
//
//        return BackFilter.of(c);
        Polynomial pr = getRegularMA();
        Polynomial ps = seasonalMA();
        return new BackFilter(pr.times(ps, false));
    }

    /**
     *
     * @return
     */
    @Override
    public int getMACount() {
        int n = spec_.getQ();
        if (spec_.getFrequency() != 1) {
            n += spec_.getFrequency() * spec_.getBQ();
        }
        return n;
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter getNonStationaryAR() {
        return spec_.getDifferencingFilter();
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryARCount() {
        int n = spec_.getD();
        if (spec_.getFrequency() != 1) {
            n += spec_.getFrequency() * spec_.getBD();
        }
        return n;
    }

    /**
     *
     * @param idx
     * @return
     */
    public double getParameter(int idx) {
        return params_[idx];
    }

    /**
     *
     * @return
     */
    public IReadDataBlock getParameters() {
        return new ReadDataBlock(params_);
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return params_ == null ? 0 : params_.length;
    }

    /**
     *
     * @return
     */
    public Polynomial getRegularAR() {
        double[] p = Polynomial.Doubles.fromDegree(spec_.getP());
        p[0] = 1;
        for (int i = 0; i < spec_.getP(); ++i) {
            p[i + 1] = params_[i];
        }
        return Polynomial.of(p);
    }

    /**
     *
     * @return
     */
    public int getRegularDifferenceOrder() {
        return spec_.getD();
    }

    public int getRegularAROrder() {
        return spec_.P;
    }

    public int getRegularMAOrder() {
        return spec_.Q;
    }

    /**
     *
     * @return
     */
    public Polynomial getRegularMA() {
        double[] p = Polynomial.Doubles.fromDegree(spec_.getQ());
        p[0] = 1;
        int i0 = spec_.getP() + spec_.getBP();
        for (int i = 0; i < spec_.getQ(); ++i) {
            p[i + 1] = params_[i0 + i];
        }
        return Polynomial.of(p);
    }

    /**
     *
     * @return
     */
    public Polynomial getSeasonalAR() {
        double[] p = Polynomial.Doubles.fromDegree(spec_.getBP());
        p[0] = 1;
        int i0 = spec_.getP();
        for (int i = 0; i < spec_.getBP(); ++i) {
            p[i + 1] = params_[i0 + i];
        }
        return Polynomial.of(p);
    }

    private Polynomial seasonalAR() {
        if (spec_.BP == 0) {
            return Polynomial.ONE;
        }
        int i0 = spec_.P;
        int freq = spec_.frequency;
        if (spec_.BP == 1) {
            return Polynomial.factor(-params_[i0], freq);
        } else {
            double[] p = Polynomial.Doubles.fromDegree(spec_.BP * freq);
            p[0] = 1;
            for (int i = freq, j = i0; i < spec_.BP; i += freq, ++j) {
                p[i] = params_[j];
            }
            return Polynomial.of(p);
        }
    }

    private Polynomial seasonalMA() {
        if (spec_.BQ == 0) {
            return Polynomial.ONE;
        }
        int i0 = spec_.P + spec_.BP + spec_.Q;
        int freq = spec_.frequency;
        if (spec_.BQ == 1) {
            return Polynomial.factor(-params_[i0], freq);
        } else {
            double[] p = Polynomial.Doubles.fromDegree(spec_.BQ * freq);
            p[0] = 1;
            for (int i = freq, j = i0; i < spec_.BQ; i += freq, ++j) {
                p[i] = params_[j];
            }
            return Polynomial.of(p);
        }
    }

    /**
     *
     * @return
     */
    public int getSeasonalDifferenceOrder() {
        return spec_.getBD();
    }

    public int getSeasonalAROrder() {
        return spec_.BP;
    }

    public int getSeasonalMAOrder() {
        return spec_.BQ;
    }

    /**
     *
     * @return
     */
    public Polynomial getSeasonalMA() {
        double[] p = Polynomial.Doubles.fromDegree(spec_.getBQ());
        p[0] = 1;
        int i0 = spec_.getP() + spec_.getBP() + spec_.getQ();
        for (int i = 0; i < spec_.getBQ(); ++i) {
            p[i + 1] = params_[i0 + i];
        }
        return Polynomial.of(p);
    }

    /**
     *
     * @return
     */
    @NewObject
    public SarimaSpecification getSpecification() {
        return spec_.clone();
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter getStationaryAR() {
//        // compute the length...
//        int f = spec_.getFrequency();
//        int n = spec_.getP() + f * (spec_.getBP()) + 1;
//        double[] c = new double[n];
//        int d = 0;
//        c[0] = 1;
//        // regular part
//        int p0 = 0;
//        for (int i = 0; i < spec_.getP(); ++i) {
//            c[i + 1] = params_[p0 + i];
//        }
//        d += spec_.getP();
//        // seasonal part
//        if (f > 1 && spec_.getBP() > 0) {
//            p0 += spec_.getP();
//            if (spec_.getP() < f) {
//                for (int i = 0; i < spec_.getBP(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j <= d; ++j) {
//                        c[(i + 1) * f + j] = c[j] * x;
//                    }
//                }
//            } else {
//                // we must use a buffer
//                double[] tmp = new double[spec_.getP() + 1];
//                System.arraycopy(c, 0, tmp, 0, tmp.length);
//                for (int i = 0; i < spec_.getBP(); ++i) {
//                    double x = params_[p0 + i];
//                    for (int j = 0; j < tmp.length; ++j) {
//                        c[(i + 1) * f + j] += tmp[j] * x;
//                    }
//                }
//            }
//        }
//
//        return BackFilter.of(c);
        Polynomial pr = getRegularAR();
        Polynomial ps = seasonalAR();
        return new BackFilter(pr.times(ps, true));
    }

    /**
     *
     * @return
     */
    @Override
    public int getStationaryARCount() {
        int n = spec_.getP();
        if (spec_.getFrequency() != 1) {
            n += spec_.getFrequency() * spec_.getBP();
        }
        return n;
    }

    @Override
    public boolean isInvertible() {
        return ec.tstoolkit.maths.linearfilters.Utilities.checkStability(getRegularMA())
                && ec.tstoolkit.maths.linearfilters.Utilities.checkStability(getSeasonalMA());
    }

    @Override
    public boolean isNull() {
        return false;
//        return this.var_ == 0;
    }

    /**
     *
     * @param checkMA
     * @return
     */
    public boolean isStable(boolean checkMA) {
        int pos = 0;
        if (spec_.P > 0) {
            if (!ec.tstoolkit.maths.linearfilters.Utilities.checkStability(new DataBlock(params_, pos, pos
                    + spec_.P, 1))) {
                return false;
            }
            pos += spec_.P;
        }
        if (spec_.BP > 0) {
            if (!ec.tstoolkit.maths.linearfilters.Utilities.checkStability(new DataBlock(params_, pos, pos
                    + spec_.BP, 1))) {
                return false;
            }
            pos += spec_.BP;
        }
        if (checkMA && spec_.Q > 0) {
            if (!ec.tstoolkit.maths.linearfilters.Utilities.checkStability(new DataBlock(params_, pos, pos
                    + spec_.Q, 1))) {
                return false;
            }
            pos += spec_.Q;
        }
        if (checkMA && spec_.BQ > 0) {
            if (!ec.tstoolkit.maths.linearfilters.Utilities.checkStability(new DataBlock(params_, pos, pos
                    + spec_.BQ, 1))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isStationary() {
        return spec_.getD() == 0 && spec_.getBD() == 0;
    }

    /**
     *
     * @param checkMA
     * @return
     */
    public boolean isValid(boolean checkMA) {
        // TODO: some invalid models could be considered as valid by this routine 
        // seasonal MA/AR roots that are equal to regular AR/MA roots
        SarimaModel tmp = this.clone();
        tmp.adjustSpecification();
        if (!tmp.isStable(checkMA)) {
            return false;
        }
        Polynomial p = tmp.getRegularAR(), q = tmp.getRegularMA();
        Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
        if (smp.simplify(p, q)) {
            return false;
        }
        Polynomial bp = tmp.getSeasonalAR(), bq = tmp.getSeasonalMA();
        return !smp.simplify(bp, bq);

    }

    /**
     *
     * @return
     */
    public boolean isWhiteNoise() {
        return spec_.getParametersCount() == 0 && spec_.getD() == 0
                && spec_.getBD() == 0;
    }

//    /**
//     *
//     */
//    public void normalize() {
//        if (var_ != 1) {
//            var_ = 1;
//            clearCachedObjects();
//        }
//    }
    /**
     *
     * @param lag
     * @return
     * @throws ArimaException
     */
    public double phi(final int lag) throws ArimaException {
        if (lag <= 0 || lag > spec_.getP()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        return params_[lag - 1];
    }

    /**
     *
     * @param lag
     * @param val
     * @throws ArimaException
     */
    public void setBPhi(final int lag, final double val) throws ArimaException {
        if (lag <= 0 || lag > spec_.getBP()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        params_[spec_.getP() + lag - 1] = val;
        clearCachedObjects();
    }

    /**
     *
     * @param lag
     * @param val
     * @throws ArimaException
     */
    public void setBTheta(final int lag, final double val)
            throws ArimaException {
        if (lag <= 0 || lag > spec_.getBQ()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        params_[spec_.getP() + spec_.getBP() + spec_.getQ() + lag - 1] = val;
        clearCachedObjects();
    }

    /**
     *
     */
    public final void setDefault() {
        setDefault(-0.1, -0.2);
    }

    /**
     *
     * @param ar
     * @param ma
     */
    public final void setDefault(final double ar, final double ma) {
        int i0 = 0, i1 = spec_.getP() + spec_.getBP();
        for (int i = i0; i < i1; ++i) {
            params_[i] = ar;
        }
        i0 = i1;
        i1 = params_.length;
        for (int i = i0; i < i1; ++i) {
            params_[i] = ma;
        }
        clearCachedObjects();
    }

    /**
     *
     * @param val
     */
    public void setParameters(IReadDataBlock val) {
        if (params_ == null || val.getLength() != params_.length) {
            return;
        }
        val.copyTo(params_, 0);
        this.clearCachedObjects();
    }

    /**
     *
     * @param lag
     * @param val
     * @throws ArimaException
     */
    public void setPhi(final int lag, final double val) throws ArimaException {
        if (lag <= 0 || lag > spec_.getP()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        params_[lag - 1] = val;
        clearCachedObjects();
    }

    /**
     *
     * @param lag
     * @param val
     * @throws ArimaException
     */
    public void setTheta(final int lag, final double val) throws ArimaException {
        if (lag <= 0 || lag > spec_.getQ()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        params_[spec_.getP() + spec_.getBP() + lag - 1] = val;
        clearCachedObjects();
    }

    /**
     *
     * @return
     */
    @Override
    public StationaryTransformation stationaryTransformation() {
        if (isStationary()) {
            return new StationaryTransformation(this, BackFilter.ONE);
        } else {
            BackFilter ur = spec_.getDifferencingFilter();
            SarimaModel model = new SarimaModel();
            model.spec_ = new SarimaSpecification(spec_.doStationary());
            model.params_ = params_;
//            model.var_ = var_;
            return new StationaryTransformation(model, ur);
        }
    }

    /**
     *
     * @param lag
     * @return
     * @throws ArimaException
     */
    public double theta(final int lag) throws ArimaException {
        if (lag <= 0 || lag > spec_.getQ()) {
            throw new ArimaException(ArimaException.SArimaOutofRange);
        }
        return params_[spec_.getP() + spec_.getBP() + lag - 1];
    }

    public boolean isAirline(boolean seas) {
        return spec_.isAirline(seas);
    }

    /**
     * Copy a model which contains more parameters as this one.
     *
     * @param model
     * @return true is the copy succeeded, false otherwise
     */
    public boolean copy(SarimaModel model) {
        if (spec_.P > model.spec_.P) {
            return false;
        }
        if (spec_.BP > model.spec_.BP) {
            return false;
        }
        if (spec_.Q > model.spec_.Q) {
            return false;
        }
        if (spec_.BQ > model.spec_.BQ) {
            return false;
        }
        for (int i = 0; i < spec_.P; ++i) {
            params_[i] = model.params_[i];
        }
        int j = spec_.P, k = model.spec_.P;
        for (int i = 0; i < spec_.BP; ++i) {
            params_[i + j] = model.params_[i + k];
        }
        j += spec_.BP;
        k += model.spec_.BP;
        for (int i = 0; i < spec_.Q; ++i) {
            params_[i + j] = model.params_[i + k];
        }
        j += spec_.Q;
        k += model.spec_.Q;
        for (int i = 0; i < spec_.BQ; ++i) {
            params_[i + j] = model.params_[i + k];
        }
        clearCachedObjects();
        return true;
    }

    public int getPhiPosition(int lag) {
        if (spec_.P < lag) {
            return -1;
        }
        return lag - 1;
    }

    public int getBPhiPosition(int lag) {
        if (spec_.BP < lag) {
            return -1;
        }
        return spec_.P + lag - 1;
    }

    public int getThetaPosition(int lag) {
        if (spec_.Q < lag) {
            return -1;
        }
        return spec_.P + spec_.BP + lag - 1;
    }

    public int getBThetaPosition(int lag) {
        if (spec_.BQ < lag) {
            return -1;
        }
        return spec_.P + spec_.BP + spec_.Q + lag - 1;
    }
}
