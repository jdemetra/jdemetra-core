/*
 * Copyright 2016-2017 National Bank copyOf Belgium
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
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class MultivariateSsfHelper {

    /**
     *
     * @param ssf The considered SSF
     * @param t The current position
     * @param state The current state
     * @param x The observation at t. Could contain missing values. the length
     * of obs should be identical to the number of measurement equations at
     * t=pos.
     *
     * @return
     */
    public MultivariateUpdateInformation of(IMultivariateSsf ssf, int t, State state, DoubleSequence x) {
        // use ordinary filter
        ISsfDynamics dynamics = ssf.dynamics();
        ISsfMeasurements measurements = ssf.measurements();
        ISsfErrors errors = ssf.errors();

        int dim = ssf.getStateDim();
        int nmissing = x.count(y -> Double.isInfinite(y));
        int nobs = x.length() - nmissing;
        if (nobs == 0)
            return null;
        int[] obs;
        if (nmissing != 0) {
            obs = new int[nobs];
            Doubles.search(x, y -> Double.isFinite(y), obs);
        } else {
            obs = null;
        }
        MultivariateUpdateInformation updinfo;
        if (nobs == 0) {
            updinfo = new MultivariateUpdateInformation(dim, 0);
        } else {
            updinfo = new MultivariateUpdateInformation(dim, nobs);
            Matrix L = updinfo.getCholeskyFactor();
            // K = PZ'(ZPZ'+H)^-1/2
            // computes (ZP)' in K'. Missing values are set to 0 
            // Z~v x r, P~r x r, K~r x v
            Matrix K = updinfo.getK();
            ZM(t, measurements, obs, state.P(), K.transpose());
            // computes ZPZ'; results in pe_.L
            ZM(t, measurements, obs, K, L);
            addH(t, errors, obs, L);
            SymmetricMatrix.reenforceSymmetry(L);

            // pe_L contains the Cholesky factor !!!
            SymmetricMatrix.lcholesky(L, State.ZERO);

            // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP
            LowerTriangularMatrix.rsolve(L, K.transpose(), State.ZERO);
            DataBlock U = updinfo.getTransformedPredictionErrors();
            if (obs == null) {
                for (int i = 0; i < x.length(); ++i) {
                    double y = x.get(i);
                    U.set(i, y - measurements.loading(i).ZX(t, state.a()));
                }
            } else {
                for (int i = 0; i < obs.length; ++i) {
                    double y = x.get(obs[i]);
                    U.set(i, y - measurements.loading(obs[i]).ZX(t, state.a()));
                }
            }
        }

        return updinfo;
    }

    /**
     * Computes zm = Z * M
     *
     * @param t
     * @param measurements
     * @param idx
     * @param M
     * @param zm
     */
    private void ZM(int t, ISsfMeasurements measurements, int[] idx, Matrix M, Matrix zm) {
        DataBlockIterator zrows = zm.rowsIterator();
        if (idx == null) {
            int eq = 0;
            while (zrows.hasNext()) {
                measurements.loading(eq++).ZM(t, M, zrows.next());
            }
        } else {
            int eq = 0;
            while (zrows.hasNext()) {
                measurements.loading(idx[eq++]).ZM(t, M, zrows.next());
            }
        }
    }

    /**
     * *
     *
     * @param t The current position
     * @param errors The errors
     * @param idx The position of the measurement corresponding to available
     * observations. When we have observations for all equations, set to null.
     * @param P The covariance matrix of the prediction errors
     */
    private void addH(int t, @Nullable ISsfErrors errors, @Nullable int[] idx, Matrix P) {
        if (errors == null) {
            return;
        }
        if (idx == null) {
            errors.addH(t, P);
        } else {
            Matrix H = Matrix.square(P.getColumnsCount());
            errors.H(t, H);
            for (int i = 0; i < idx.length; ++i) {
                for (int j = 0; j < i; ++j) {
                    double h = H.get(idx[i], idx[j]);
                    P.add(i, j, h);
                    P.add(j, i, h);
                }
                P.add(i, i, H.get(idx[i], idx[i]));
            }
        }
    }

}
