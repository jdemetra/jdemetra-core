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

/**
 *
 * @author Jean Palate
 */
public class MultivariateSsfHelper {

    public static MultivariateUpdateInformation next(IMultivariateSsf ssf, int t, State state, DoubleSequence x) {
        // use ordinary filter
        ISsfDynamics dynamics = ssf.getDynamics();
        ISsfMeasurements measurements = ssf.getMeasurements();
         // error
        if (measurements.getCount(t) != x.length()) {
            return null;
        }
        int dim = ssf.getStateDim();
        int[] obs = Doubles.search(x, y -> Double.isFinite(y));
        int nobs = obs.length;
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
            if (measurements.hasError(t)) {
                addH(t, measurements, obs, L);
            }
            SymmetricMatrix.reenforceSymmetry(L);

            // pe_L contains the Cholesky factor !!!
            SymmetricMatrix.lcholesky(L, State.ZERO);

            // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP
            LowerTriangularMatrix.rsolve(L, K.transpose(), State.ZERO);
            DataBlock U = updinfo.getTransformedPredictionErrors();
            for (int i = 0, j = 0; i < x.length(); ++i) {
                double y = x.get(i);
                if (Double.isFinite(y)) {
                    U.set(j, y - measurements.ZX(t, i, state.a()));
                    ++j;
                }
            }
            // E = e*L'^-1 or E L' = e or L*E' = e'
            LowerTriangularMatrix.rsolve(L, U, State.ZERO);
            // update
            int n = updinfo.getK().getColumnsCount();
            // P = P - (M)* F^-1 *(M)' --> Symmetric
            // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
            // A = a + (M)* F^-1 * v
//        for (int i = 0; i < n; ++i) {
//            state.P().addXaXt(-1, updinfo.getK().column(i));//, state_.K.column(i));
//            state.a().addAY(updinfo.getTransformedPredictionErrors().get(i), updinfo.getK().column(i));
//        }
            for (int i = 0; i < n; ++i) {
                state.P().addXaXt(-1, K.column(i));//, state_.K.column(i));
                state.a().addAY(U.get(i), K.column(i));
            }
        }
        // prediction
        dynamics.TX(t, state.a());
        dynamics.TVT(t, state.P());
        dynamics.addV(t, state.P());

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
    public static void ZM(int t, ISsfMeasurements measurements, int[] idx, Matrix M, Matrix zm) {
        DataBlockIterator zrows = zm.rowsIterator();
        for (int i = 0; i < idx.length; ++i) {
            measurements.ZM(t, idx[i], M, zrows.next());
            if (!zrows.hasNext()) {
                return;
            }

        }
    }

    public static void addH(int t, ISsfMeasurements measurements, int[] idx, Matrix P) {
        Matrix H = Matrix.square(P.getColumnsCount());
        measurements.H(t, H);
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
