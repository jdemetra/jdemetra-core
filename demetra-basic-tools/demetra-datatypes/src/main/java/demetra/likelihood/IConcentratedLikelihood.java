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

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.eco.EcoException;
import demetra.maths.MatrixType;

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
    DoubleSequence coefficients();

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
            return DoubleSequence.EMPTYARRAY;
        }
        double[] e = unscaledCovariance().diagonal().toArray();
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        double ssq = ssq();
        DoubleSequence b = coefficients();
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i] * ssq / ndf);
        }
        return e;
    }

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
        DoubleReader reader = coefficients().reader();
        for (int i = 0; i < t.length; ++i) {
            t[i] = reader.next() / t[i];
        }
        return t;
    }

}
