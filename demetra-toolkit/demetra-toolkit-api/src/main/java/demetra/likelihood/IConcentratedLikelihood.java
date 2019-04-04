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
package demetra.likelihood;

import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import demetra.eco.EcoException;
import demetra.maths.MatrixType;
import demetra.data.DoubleSeq;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IConcentratedLikelihood extends ILikelihood {

    /**
     * The coefficients of the regression variables
     *
     * @return
     */
    DoubleSeq coefficients();

    /**
     * The coefficient of the i-th regression variables (0-based). The first one
     * is the mean correction, if any.
     *
     * @param idx Position of the variable
     * @return
     */
    double coefficient(int idx);

    /**
     *
     * @return
     */
    MatrixType unscaledCovariance();

    default MatrixType covariance(int nhp, boolean unbiased) {

        if (nx() == 0) {
            return MatrixType.EMPTY;
        }
        
        double[] v = unscaledCovariance().toArray();
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        double sig2=ssq()/ndf;
        for (int i=0; i<v.length; ++i)
            v[i]*=sig2;
        return MatrixType.ofInternal(v, nx(), nx());
    }
    /**
     * Number of regression variables
     *
     * @return
     */
    default int nx() {
        return coefficients().length();
    }

    /**
     * Gets the standard deviation for the given regression variable (including
     * mean, excluding missing identified by additive outliers)
     *
     * @param ix Position of the variable
     * @param nhp Number of hyper-parameters (for correction of the degrees of
     * freedom)
     * @param unbiased True for use of unbiased variance estimate, false for ML
     * variance estimate
     * @return
     */
    default double ser(int ix, int nhp, boolean unbiased) {

        double e = unscaledCovariance().get(ix, ix);
        if (e == 0) {
            return Double.NaN;
        }
        double b = coefficients().get(ix);
        if (b == 0) {
            return 0;
        }
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        return Math.sqrt(e * ssq() / ndf);
    }

    default double[] ser(int nhp, boolean unbiased) {

        if (nx() == 0) {
            return DoubleSeq.EMPTYARRAY;
        }
        double[] e = unscaledCovariance().diagonal().toArray();
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        double ssq = ssq();
        DoubleSeq b = coefficients();
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i] * ssq / ndf);
        }
        return e;
    }

    /**
     * Gets the T-Stat of the given variable. This method is only defined
     * when the likelihood contains a scaling factor. In the other case,
     * the user should use the corresponding ser() method.
     * When it is defined T(i) = coefficient(i)/ser(i)
     * @param ix The 0-based position of the variable. 0 for mean correction, if any
     * @param nhp The number of hyper-parameters; used to correct the degrees 
     * of freedom (unused if we use the ML (biased) estimator.
     * @param unbiased True if the estimator of the scaling factor is unbiased. False
     * if we use the (biased) ML estimator.
     * @return 
     */
    default double tstat(int ix, int nhp, boolean unbiased) {
        if (!isScalingFactor()) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        return coefficient(ix) / ser(ix, nhp, unbiased);
    }

    default double[] tstats(int nhp, boolean unbiased) {
        if (!isScalingFactor()) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        double[] t = ser(nhp, unbiased);
        DoubleSeqCursor reader = coefficients().cursor();
        for (int i = 0; i < t.length; ++i) {
            t[i] = reader.getAndNext() / t[i];
        }
        return t;
    }

}
