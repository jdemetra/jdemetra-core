/*
 * Copyright 2016-2017 National Bank of Belgium
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
package demetra.var;

import jd.data.DataBlock;
import jd.maths.matrices.CanonicalMatrix;
import jd.maths.matrices.SymmetricMatrix;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;
import demetra.linearsystem.LinearSystemSolver;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Var {

    public CanonicalMatrix unconditionalInitialization(VarDescriptor desc) {
        int nl = desc.getLagsCount();
        int nvars = desc.getVariablesCount();
        // We have to solve the steady state equation:
        // V = T V T' + Q
        // We consider the nlag*nb, nlag*nb sub-system
        CanonicalMatrix v = desc.getInnovationsVariance();
        CanonicalMatrix t = desc.getVarMatrix();
        int n = nvars * nl;
        CanonicalMatrix cov = CanonicalMatrix.square(n);
        int np = (n * (n + 1)) / 2;
        CanonicalMatrix M = CanonicalMatrix.square(np);
        double[] b = new double[np];
        // fill the matrix
        for (int c = 0, i = 0; c < n; ++c) {
            for (int r = c; r < n; ++r, ++i) {
                M.set(i, i, 1);
                if (r % nl == 0 && c % nl == 0) {
                    b[i] = v.get(r / nl, c / nl);
                }
                for (int k = 0; k < n; ++k) {
                    for (int l = 0; l < n; ++l) {
                        double zr = 0, zc = 0;
                        if (r % nl == 0) {
                            zr = t.get(r / nl, l);
                        } else if (r == l + 1) {
                            zr = 1;
                        }
                        if (c % nl == 0) {
                            zc = t.get(c / nl, k);
                        } else if (c == k + 1) {
                            zc = 1;
                        }
                        double z = zr * zc;
                        if (z != 0) {
                            int p = l <= k ? pos(k, l, n) : pos(l, k, n);
                            M.add(i, p, -z);
                        }
                    }
                }
            }
        }

        LinearSystemSolver.robustSolver().solve(M, DataBlock.of(b));

        for (int i = 0, j = 0; i < n; i++) {
            cov.column(i).drop(i, 0).copyFrom(b, j);
            j += n - i;
        }
        SymmetricMatrix.fromLower(cov);
        return cov;
//            Matrix fullCov = new Matrix(getStateDim(), getStateDim());
//            for (int r = 0; r < nf_; ++r) {
//                for (int c = 0; c < nf_; ++c) {
//                    fullCov.subMatrix(r * c_, r * c_ + nl, c * c_, c * c_ + nl).copy(cov.subMatrix(r * nl, (r + 1) * nl, c * nl, (c + 1) * nl));
//                }
//            }
//            for (int i = nl; i < c_; ++i) {
//                TVT(0, fullCov.subMatrix());
//                addV(0, fullCov.subMatrix());
//            }
//            return fullCov;
    }

    private static int pos(int r, int c, int n) {
        return r + c * (2 * n - c - 1) / 2;
    }
    
    public MultivariateSsf of(VarDescriptor desc) {
        VarDynamics dynamics = VarDynamics.of(desc);
        VarInitialization initialization = new VarInitialization(desc.getVariablesCount() * desc.getLagsCount(), null);
        ISsfMeasurements measurements = new VarMeasurements(desc.getVariablesCount(), desc.getLagsCount());
        return new MultivariateSsf(initialization, dynamics, measurements);
    }

    public static MultivariateSsf of(VarDescriptor desc, int nlags) {
        int nl = Math.max(nlags, desc.getLagsCount());
        VarDynamics dynamics = VarDynamics.of(desc);
        VarInitialization initialization = new VarInitialization(desc.getVariablesCount() * nl, null);
        ISsfMeasurements measurements = new VarMeasurements(desc.getVariablesCount(), desc.getLagsCount());
        return new MultivariateSsf(initialization, dynamics, measurements);
    }

}
