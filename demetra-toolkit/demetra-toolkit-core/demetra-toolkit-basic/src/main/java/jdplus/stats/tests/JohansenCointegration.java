/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests;

import demetra.math.Complex;
import demetra.stats.StatException;
import demetra.util.IntList;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixFactory;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.decomposition.EigenSystem;
import jdplus.math.matrices.decomposition.IEigenSystem;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class JohansenCointegration {

    public static enum Spec {
        longrun, transitory
    }

    public static enum ECDet {
        none, cnt, trend
    }

    public static class Builder {

        private Spec spec = Spec.transitory;
        private ECDet ecdet = ECDet.none;
        private int k = 2;
        private int season = 0;

        public Builder season(int season) {
            this.season = season;
            return this;
        }

        public Builder lag(int k) {
            this.k = k;
            return this;
        }

        public Builder errorCorrectionModel(ECDet det) {
            this.ecdet = det;
            return this;
        }

        public JohansenCointegration build() {
            return new JohansenCointegration(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Spec spec;
    private final ECDet ecdet;
    private final int k;
    private final int season;

    private JohansenCointegration(Builder builder) {
        this.spec = builder.spec;
        this.ecdet = builder.ecdet;
        this.k = builder.k;
        this.season = builder.season;
    }

    private FastMatrix X, Z, Z0, Z2, Z1, D, Ds, T;
    private IntList rows;

    private double[] vp;
    private FastMatrix V;

    public void process(FastMatrix x, FastMatrix dummies) {
        clear();
        int p = x.getColumnsCount(), n = x.getRowsCount(), s = season > 0 ? season - 1 : 0;
        if (n * p < p + s * p + k * p * p + p * (p + 1) / 2) {
            throw new StatException("Insufficient degrees of freedom");
        }
        rows = new IntList();
        X = MatrixFactory.cleanRows(x, z -> Double.isFinite(z), rows);
        // No missing values
        if (rows.size() == n) {
            rows = null;
        }
        D = MatrixFactory.selectRows(dummies, rows);
        buildDet(n);
        buildZ();
        // Model: Z0 = ab Z1 + c Z2 +e

        // actual computation
        int nz = Z0.getRowsCount();
        FastMatrix M22 = SymmetricMatrix.XtX(Z2); // Z2'Z2
        FastMatrix M20 = GeneralMatrix.AtB(Z2, Z0); // Z2'Z0
        FastMatrix M21 = GeneralMatrix.AtB(Z2, Z1); // Z2'Z1
        // (Z2'Z2)^(-1)
        FastMatrix M22inv = SymmetricMatrix.inverse(M22);

        // Z0 on Z2: R0=Z0-Z2*(Z2'Z2)^(-1)*Z2'Z0
        FastMatrix R0 = Z0.minus(GeneralMatrix.AB(Z2, GeneralMatrix.AB(M22inv, M20)));
        // Z1 on Z2: R1=Z1-Z2(Z2'Z2)^(-1)*Z2'Z1
        FastMatrix R1 = Z1.minus(GeneralMatrix.AB(Z2, GeneralMatrix.AB(M22inv, M21)));

        // R0 = ab R1 + e
        FastMatrix S00 = SymmetricMatrix.XtX(R0);
        FastMatrix S11 = SymmetricMatrix.XtX(R1);
        FastMatrix S01 = GeneralMatrix.AtB(R0, R1);
        S00.div(nz);
        S11.div(nz);
        S01.div(nz);
        FastMatrix L0 = S00; // S00 not cloned! be careful
        SymmetricMatrix.lcholesky(L0);
        FastMatrix K1 = S01;// S01 not clone! be careful
        LowerTriangularMatrix.solveLX(L0, K1);
        FastMatrix L1 = S11; // S11 not cloned! be careful
        SymmetricMatrix.lcholesky(L1);
        LowerTriangularMatrix.solveXLt(L1, K1);

        // K = L1^(-1)*S10(S00)^(-1)S01*L1^(-1) = L1^(-1)*S10*(L0L0')^(-1)*S01*L1'^(-1)* =  L1^(-1)*S10*L0'^(-1)*L0(^-1)*S01*L1'^(-1) 
        FastMatrix K = SymmetricMatrix.XtX(K1);
        IEigenSystem eig = EigenSystem.create(K, true);
        eig.setComputingEigenVectors(true);
        eig.compute();
        Complex[] eigenValues = eig.getEigenValues();
        FastMatrix E = eig.getEigenVectors(); // normalized vectors by column
        LowerTriangularMatrix.solveLX(L1, E); // 
        V = FastMatrix.make(E.getRowsCount(), E.getColumnsCount());
        vp = new double[eigenValues.length];

        for (int i = 0; i < vp.length; ++i) {
            int jmax = 0;
            double vmax = 0;
            for (int j = 0; j < vp.length; ++j) {
                if (eigenValues[j] != null) {
                    if (eigenValues[j].getRe() > vmax) {
                        vmax=eigenValues[j].getRe();
                        jmax = j;
                    }
                }
            }
            vp[i] = vmax;
            V.column(i).copy(E.column(jmax));
            eigenValues[jmax] = null;
        }
    }
    
    public double traceCriticalValue(int eps){
        return cv(Z0.getColumnsCount(), eps, 1);
    }

    public double maxCriticalValue(int eps){
        return cv(Z0.getColumnsCount(), eps, 0);
    }

    public double traceTest(int k) {
        if (vp == null) {
            return Double.NaN;
        }
        int nz = Z0.getRowsCount();
        double t = 0;
        for (int j = k; j < vp.length; ++j) {
            t += Math.log(1 - vp[j]);
        }
        return -t * nz;
    }

    public double maxTest(int k) {
        if (vp == null) {
            return Double.NaN;
        }
        int nz = Z0.getRowsCount();
        return -nz * Math.log(1 - vp[k]);
    }

    private int[] order(int[] p) {
        int[] o = new int[p.length];
        for (int i = 0; i < p.length; ++i) {
            o[p[i]] = i;
        }
        return o;
    }

    private void buildDet(int n) {
        // Create seasonal dummies if need be
        if (season > 1) {
            Ds = FastMatrix.make(n, season - 1);
            Ds.set(1.0 / season);
            for (int i = 0; i < Ds.getColumnsCount(); ++i) {
                Ds.column(i).extract(i, -1, season).add(1);
            }
            Ds = MatrixFactory.selectRows(Ds, rows);
        }
        if (ecdet == ECDet.trend) {
            if (rows == null) {
                T = FastMatrix.make(n, 1);
                T.set((r, c) -> r + 1);
            } else {
                T = FastMatrix.make(rows.size(), 1);
                T.set((r, c) -> rows.get(r) + 1);
            }
        }
    }

    private void buildZ() {
        // Z0 = a Z1 + b Z2 + e
        int p = X.getColumnsCount();
        Z = MatrixFactory.embed(MatrixFactory.delta(X, 1, 1), k);
        int nz = Z.getRowsCount(); // n-k
        Z0 = Z.extract(0, nz, 0, p);
        Matrix one = one(nz);
        FastMatrix D1 = D == null ? null : D.extract(k, nz, 0, D.getColumnsCount());
        FastMatrix Ds1 = Ds == null ? null : Ds.extract(k, nz, 0, Ds.getColumnsCount());
        FastMatrix T1 = T == null ? null : T.extract(k, nz, 0, 1);
        switch (ecdet) {
            case cnt:
                if (spec == Spec.longrun) {
                    // Z1 =[X(t-k), 1]
                    Z1 = MatrixFactory.columnBind(X.extract(0, nz, 0, p), one);
                } else {
                    // Z1 =[X(t-1), 1]
                    Z1 = MatrixFactory.columnBind(X.extract(k - 1, nz, 0, p), one);
                }
                // Z2=[Ds, D, dX(t-1)...dX(t-k+1)]
                Z2 = MatrixFactory.columnBind(D1, Ds1, Z.extract(0, nz, p, Z.getColumnsCount() - p));
                break;
            case trend:
                if (spec == Spec.longrun) {
                    // Z1 =[X(t-k), T]
                    Z1 = MatrixFactory.columnBind(X.extract(0, nz, 0, p), T1);
                } else {
                    // Z1 =[X(t-1), T]
                    Z1 = MatrixFactory.columnBind(X.extract(k - 1, nz, 0, p), T1);
                }
                // Z2=[1, D, Ds, dX(t-1)...dX(t-k+1)]
                Z2 = MatrixFactory.columnBind(one, D1, Ds1, Z.extract(0, nz, p, Z.getColumnsCount() - p));
                break;
            case none:
                if (spec == Spec.longrun) {
                    // Z1 =[X(t-k)]
                    Z1 = X.extract(0, nz, 0, p);
                } else {
                    // Z1 =[X(t-1)]
                    Z1 = X.extract(k - 1, nz, 0, p);
                }
                // Z2=[1, D, Ds, dX(t-1)...dX(t-k+1)]
                Z2 = MatrixFactory.columnBind(one, D1, Ds1, Z.extract(0, nz, p, Z.getColumnsCount() - p));
                break;
        }
    }

    private Matrix one(int n) {
        FastMatrix one = FastMatrix.make(n, 1);
        one.set(1);
        return one;
    }

    private void clear() {
        X = Z = Z0 = Z2 = Z1 = D = Ds = T = null;
        rows = null;
    }

    private double cv(int a, int b, int c) {
        switch (ecdet) {
            case cnt:
                return cvconst(a, b, c);
            case trend:
                return cvtrend(a, b, c);
            default:
                return cvnone(a, b, c);
        }
    }

    private static double cvnone(int a, int b, int c) {
        if (a >= 11 || b >= 3 || c >= 2) {
            return Double.NaN;
        }
        int idx = a + 11 * b + 33 * c;
        return CVNONE[idx];
    }

    private static double cvconst(int a, int b, int c) {
        if (a >= 11 || b >= 3 || c >= 2) {
            return Double.NaN;
        }
        int idx = a + 11 * b + 33 * c;
        return CVCONST[idx];
    }

    private static double cvtrend(int a, int b, int c) {
        if (a >= 11 || b >= 3 || c >= 2) {
            return Double.NaN;
        }
        int idx = a + 11 * b + 33 * c;
        return CVTREND[idx];
    }

    private static final double[] CVNONE = new double[]{6.5, 12.91, 18.9, 24.78, 30.84, 36.25, 42.06, 48.43, 54.01, 59., 65.07, 8.18, 14.90, 21.07, 27.14, 33.32, 39.43, 44.91, 51.07, 57.00, 62.42, 68.27, 11.65, 19.19, 25.75, 32.14, 38.78, 44.59, 51.30, 57.07, 63.37, 68.61, 74.36, 6.50, 15.66, 28.71, 45.23, 66.49, 85.18, 118.99, 151.38, 186.54, 226.34, 269.53, 8.18, 17.95, 31.52, 48.28, 70.6, 90.39, 124.25, 157.11, 192.84, 232.49, 277.39, 11.65, 23.52, 37.22, 55.43, 78.87, 104.20, 136.06, 168.92, 204.79, 246.27, 292.65}; // [11x3x2]
    private static final double[] CVCONST = new double[]{7.52, 13.75, 19.77, 25.56, 31.66, 37.45, 43.25, 48.91, 54.35, 60.25, 66.02, 9.24, 15.67, 22.00, 28.14, 34.40, 40.30, 46.45, 52.00, 57.42, 63.57, 69.74, 12.97, 20.20, 26.81, 33.24, 39.79, 46.82, 51.91, 57.95, 63.71, 69.94, 76.63, 7.52, 17.85, 32.00, 49.65, 71.86, 97.18, 126.58, 159.48, 196.37, 236.54, 282.45, 9.24, 19.96, 34.91, 53.12, 76.07, 102.14, 131.70, 165.58, 202.92, 244.15, 291.40, 12.97, 24.60, 41.07, 60.16, 84.45, 111.01, 143.09, 177.20, 215.74, 257.68, 307.64};
    private static final double[] CVTREND = new double[]{10.49, 16.85, 23.11, 29.12, 34.75, 40.91, 46.32, 52.16, 57.87, 63.18, 69.26, 12.25, 18.96, 25.54, 31.46, 37.52, 43.97, 49.42, 55.50, 61.29, 66.23, 72.72, 16.26, 23.65, 30.34, 36.65, 42.36, 49.51, 54.71, 62.46, 67.88, 73.73, 79.23, 10.49, 22.76, 39.06, 59.14, 83.20, 110.42, 141.01, 176.67, 215.17, 256.72, 303.13, 12.25, 25.32, 42.44, 62.99, 87.31, 114.90, 146.76, 182.82, 222.21, 263.42, 310.81, 16.26, 30.45, 48.45, 70.05, 96.58, 124.75, 158.49, 196.08, 234.41, 279.07, 327.45};
}
