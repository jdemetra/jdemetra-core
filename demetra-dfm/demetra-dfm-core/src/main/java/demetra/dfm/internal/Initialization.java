/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.dfm.internal;

import jdplus.data.DataBlock;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfInitialization;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
class Initialization implements ISsfInitialization {

    static CanonicalMatrix unconditional(Dynamics dynamics) {
         int nl = dynamics.nl(), nf=dynamics.nf();
        // We have to solve the steady state equation:
        // V = T V T' + Q
        // We consider first the [nl*nf, nl*nf] sub-system
        CanonicalMatrix v = dynamics.getV();
        CanonicalMatrix t = dynamics.getT();

        int n = nf * nl;
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
        int nlx = dynamics.nlx;
        if (dynamics == null || nl == nlx) {
            return cov;
        }
        int dim = nlx*nf;
        CanonicalMatrix fullCov = CanonicalMatrix.square(dim);

        for (int r = 0; r < nf; ++r) {
            for (int c = 0; c < nf; ++c) {
                fullCov.extract(r * nlx, r * nlx + nl, c * nlx, c * nlx + nl).copy(cov.extract(r * nl, (r + 1) * nl, c * nl, (c + 1) * nl));
            }
        }
        for (int i = nl; i < nlx; ++i) {
            dynamics.TVT(0, fullCov);
            dynamics.addV(0, fullCov);
        }
        return fullCov;
    }

    int dim;
    CanonicalMatrix V0;

    @Override
    public int getStateDim() {
        return dim;
    }

    @Override
    public boolean isDiffuse() {
        return false;
    }

    @Override
    public int getDiffuseDim() {
        return 0;
    }

    @Override
    public void diffuseConstraints(FastMatrix b) {
    }

    @Override
    public void a0(DataBlock a0) {
    }

    @Override
    public void Pf0(FastMatrix pf0) {
        if (V0 != null) {
            pf0.copy(V0);
        }
    }

    private static int pos(int r, int c, int n) {
        return r + c * (2 * n - c - 1) / 2;
    }
}
