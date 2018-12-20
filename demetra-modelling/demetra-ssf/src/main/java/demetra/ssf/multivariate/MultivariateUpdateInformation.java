/*
 * Copyright 2013-2014 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.multivariate;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
public class MultivariateUpdateInformation {

    /**
     * U is the transformed prediction error (=L^-1)*(y(t)-Z(t)A(t))) U is 1 x
     * nvars
     */
    private final DataBlock U;

    /**
     * =(ZPZ'+H)^1/2 Cholesky factor of the variance/covariance matrix of the
     * prediction errors (lower triangular). nvars x nvars
     */
    private final Matrix R;

    /**
     * K = P Z' L'^-1 dim x nvars
     */
    private final Matrix K;

    /**
     *
     * @param dim
     * @param nvars
     */
    public MultivariateUpdateInformation(final int dim, final int nvars) {
        U = DataBlock.make(nvars);
        R = Matrix.square(nvars);
        K = Matrix.make(dim, nvars);
    }

    public DataBlock getTransformedPredictionErrors() {
        return U;
    }

    public Matrix getPredictionErrorCovariance() {
        if (R.getRowsCount() == 1) {
            double l = R.get(0, 0);
            return Matrix.builder(new double[]{l * l}).nrows(1).ncolumns(1).build();
        } else {
            return SymmetricMatrix.LLt(R);
        }
    }

    public Matrix getCholeskyFactor() {
        return R;
    }

    /**
     * @return the K
     */
    public Matrix getK() {
        return K;
    }

    public void compute(IMultivariateSsf ssf, int t, State state, DoubleSequence x, int[] equations) {
        ISsfMeasurements measurements = ssf.measurements();
        ISsfErrors errors = ssf.errors();

        ZM(t, measurements, equations, state.P(), K.transpose());
        // computes ZPZ'; results in pe_.L
        ZM(t, measurements, equations, K, R);
        addH(t, errors, equations, R);
        SymmetricMatrix.reenforceSymmetry(R);

        // pe_L contains the Cholesky factor !!!
        SymmetricMatrix.lcholesky(R, State.ZERO);

        // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
        // K L' = PZ' or L K' = ZP
        LowerTriangularMatrix.rsolve(R, K.transpose(), State.ZERO);
        if (equations == null) {
            for (int i = 0; i < x.length(); ++i) {
                double y = x.get(i);
                U.set(i, y - measurements.loading(i).ZX(t, state.a()));
            }
        } else {
            for (int i = 0; i < equations.length; ++i) {
                double y = x.get(equations[i]);
                U.set(i, y - measurements.loading(equations[i]).ZX(t, state.a()));
            }
        }

    }

    /**
     * Computes zm = Z * M
     *
     * @param t
     * @param measurements
     * @param equations
     * @param M
     * @param zm
     */
    protected void ZM(int t, ISsfMeasurements measurements, int[] equations, Matrix M, Matrix zm) {
        DataBlockIterator zrows = zm.rowsIterator();
        if (equations == null) {
            int eq = 0;
            while (zrows.hasNext()) {
                measurements.loading(eq++).ZM(t, M, zrows.next());
            }
        } else {
            int eq = 0;
            while (zrows.hasNext()) {
                measurements.loading(equations[eq++]).ZM(t, M, zrows.next());
            }
        }
    }

    /**
     * *
     *
     * @param t The current position
     * @param errors The errors
     * @param equations The position of the measurement corresponding to
     * available observations. When we have observations for all equations, set
     * to null.
     * @param P The covariance matrix of the prediction errors
     */
    private void addH(int t, ISsfErrors errors, int[] equations, Matrix P) {
        if (errors == null) {
            return;
        }
        if (equations == null) {
            errors.addH(t, P);
        } else {
            Matrix H = Matrix.square(P.getColumnsCount());
            errors.H(t, H);
            for (int i = 0; i < equations.length; ++i) {
                for (int j = 0; j < i; ++j) {
                    double h = H.get(equations[i], equations[j]);
                    P.add(i, j, h);
                    P.add(j, i, h);
                }
                P.add(i, i, H.get(equations[i], equations[i]));
            }
        }
    }
}
