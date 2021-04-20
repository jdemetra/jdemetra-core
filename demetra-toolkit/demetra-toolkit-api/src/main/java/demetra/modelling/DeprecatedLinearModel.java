/*
 * Copyright 2021 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
package demetra.modelling;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import nbbrd.design.Development;
import demetra.math.matrices.MatrixType;

/**
 * Describes the linear model: y = a + b * X
 * The constant should be defined explicitly (not included in X)
 *
 * The coefficients are provided with their covariance matrix when it makes
 * sense
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder
public class DeprecatedLinearModel {

    @lombok.NonNull
    /**
     * Exogenous variable. Might contain missing values (whose estimations are
     * provided in missing)
     */
    private DoubleSeq y;

    /**
     * Mean correction
     */
    private boolean meanCorrection;

    /**
     * Regression variables (without the mean)
     */
    private MatrixType X;

    /**
     * All the parameters of the model (including the mean)
     */
    private ParametersEstimation coefficients;

    /**
     * Estimation of the missing values, if any
     */
    private MissingValueEstimation[] missing;

    /**
     * All the regression variables, including the mean correction
     *
     * @return
     */
    public MatrixType variables() {
        if (!meanCorrection) {
            return X;
        }
        int n = y.length();
        if (X == null) {
            double[] m = new double[n];
            for (int i = 0; i < n; ++i) {
                m[i] = 1;
            }
            return MatrixType.of(m, n, 1);
        }
        int m = X.getColumnsCount();
        double[] v = new double[n * (m + 1)];
        for (int i = 0; i < n; ++i) {
            v[i] = 1;
        }
        X.copyTo(v, m);
        return MatrixType.of(v, n, m + 1);
    }

    public double[] residuals() {
        double[] r = y.toArray();
        if (coefficients == null) {
            return r;
        }
        DoubleSeqCursor b = coefficients.getValues().cursor();
        if (meanCorrection) {
            double m = b.getAndNext();
            for (int i = 0; i < r.length; ++i) {
                r[i] -= m;
            }
        }
        if (X != null) {
            int m = X.getColumnsCount();
            for (int j = 0; j < m; ++j) {
                double curb = b.getAndNext();
                DoubleSeqCursor c = X.column(j).cursor();
                for (int i = 0; i < r.length; ++i) {
                    r[i] -= curb * c.getAndNext();
                }
            }
        }
        return r;
    }
    
    /**
     * Y corrected for missing values
     * @return 
     */
    public double[] interpolatedY(){
        double[] z=y.toArray();
        if (missing != null){
            for (int i=0; i<missing.length; ++i){
                z[missing[i].getPosition()]=missing[i].getValue();
            }
        }
        return z;
    }

    /**
     * All regression effects, without the mean
     *
     * @return
     */
    public double[] regressionEffect() {
        int n = y.length();
        double[] r = new double[n];
        if (X == null) {
            return r;
        }
        DoubleSeqCursor b = coefficients.getValues().cursor();
        if (meanCorrection) {
            b.skip(1);
        }
        int m = X.getColumnsCount();
        for (int j = 0; j < m; ++j) {
            double curb = b.getAndNext();
            DoubleSeqCursor c = X.column(j).cursor();
            for (int i = 0; i < r.length; ++i) {
                r[i] += curb * c.getAndNext();
            }
        }
        return r;
    }

    
    public double mean(){
        if (meanCorrection)
            return coefficients.getValues().get(0);
        else
            return 0;
    }
    
    /**
     * Positions of the missing values
     *
     * @return null if there is no missing value
     */
    public int[] missingPositions() {
        if (missing == null) {
            return null;
        }
        int[] m = new int[missing.length];
        for (int i = 0; i < m.length; ++i) {
            m[i] = missing[i].getPosition();
        }
        return m;
    }

}
