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

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
public interface IConcentratedLikelihood extends ILikelihood {

    /**
     *
     * @param idx
     * @return
     */
    default double getCoefficient(int idx){
        return getCoefficients().get(idx);
    }

    /**
     *
     * @return
     */
    Doubles getCoefficients();

    /**
     *
     * @param idx
     * @param unbiased
     * @param hpcount
     * @return
     */
    default double getCoefficientStdev(int idx, boolean unbiased, int hpcount){
        Matrix var = getCoefficientsCovariance();
        return Math.sqrt(var.get(idx,idx));
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    default Doubles getCoefficientsStdev(boolean unbiased, int hpcount) {
        Matrix var = getCoefficientsCovariance();
        if (var == null) {
            return DataBlock.EMPTY;
        }
        DataBlock diag = DataBlock.copyOf(var.diagonal());
        if (unbiased) {
            double n = getN();
            double c = n / (n - diag.length() - hpcount);
            diag.apply(x -> Math.sqrt(x * c));
        } else {
            diag.apply(x -> Math.sqrt(x));
        }
        return diag;
    }

    /**
     *
     * @return
     */
    Matrix getCoefficientsCovariance();

    /**
     * Returns the variance/covariance matrix V of the coefficients of the
     * regression variables. V = sig2 * (X'X)^-1 sig2 may be computed as 1.
     * ssqErr/n (ml estimate) or as 2. ssqErr/(n-nx-nhp) (unbiased estimate)
     *
     * n is the number of obs. nx is the number of regression variables nhp is
     * the number of hyper-parameters
     *
     * ML estimate will always lead to smaller (co)variances.
     *
     * @param unbiased False if the ML estimate is used. True otherwise. See the
     * description for more information.
     * @param hpcount The number of hyper-parameters. Can be 0.
     * @return The covariance matrix. The matrix should not be modified.
     */
    default Matrix getCoefficientsCovariance(boolean unbiased, int hpcount) {
        Matrix var = getCoefficientsCovariance();
        if (var == null) {
            return null;
        }
        if (unbiased) {
            double n = getN();
            return var.times(n / getDegreesOfFreedom(unbiased, hpcount));
        } else {
            return var;
        }

    }

    /**
     * Returns the number of degrees of freedom used in the computation of the
     * different variance/standard deviations
     *
     * @param unbiased True if ML estimates are used, false otherwise.
     * @param hpcount Number of hyper-paraneters that should be taken into
     * account. hpcount is not considered if unbiased is set to false.
     * @return
     */
    default int getDegreesOfFreedom(boolean unbiased, int hpcount) {
        int n = getN();
        if (unbiased) {
            n -= getNx() + hpcount;
        }
        return n;
    }

    /**
     * Number of regression variables
     *
     * @return
     */
    default int getNx(){
        return getCoefficients().length();
    }

    /**
     *
     * @return
     */
    default Doubles getTStats() {
        return getTStats(false, 0);
    }

    /**
     *
     * @param unbiased
     * @param hpcount
     * @return
     */
    default Doubles getTStats(boolean unbiased, int hpcount) {
        Matrix var = getCoefficientsCovariance();
        if (var == null) {
            return null;
        }
        final double c;
        if (unbiased) {
            double n = getN();
            c = n / getDegreesOfFreedom(unbiased, hpcount);
        } else {
            c = 1;
        }

        DataBlock t = DataBlock.copyOf(getCoefficients());
        t.apply(var.diagonal(), (x, y) -> x == 0 ? 0 : x / Math.sqrt(c * y));
        return t;
    }

    default double getTStat(int idx, boolean unbiased, int hpcount) {
        double e = getCoefficientStdev(idx, unbiased, hpcount);
        if (e == 0) {
            return Double.NaN;
        } else {
            return getCoefficient(idx) / e;
        }
    }
    // / <summary>
    // / Adjust the likelihood if the data (y and/or Xs) have been
    // pre-multiplied by a given scaling factor
    // / </summary>
    // / <param name="yfactor"></param>
    // / <param name="xfactor"></param>

}
