/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.eco;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SparseSystemSolver;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametersDomain;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunction;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;
import ec.tstoolkit.maths.realfunctions.NumericalDerivatives;
import ec.tstoolkit.maths.realfunctions.SsqNumericalDerivatives;
import ec.tstoolkit.utilities.IntList;

/**
 *
 * @author Jean Palate
 */
public class DifferenceStationaryModelHelper {

    public static Matrix missings(final IReadDataBlock data) {
        return missings(data, null);
    }

    public static Matrix missings(final IReadDataBlock data, final int[] inits) {
        int n = data.getLength();
        IntList m = new IntList();
        for (int i = 0; i < n; ++i) {
            if (!Double.isFinite(data.get(i))) {
                m.add(i);
            }
        }
        if (m.isEmpty()) {
            return Matrix.identity(n);
        } else {
            Matrix J = new Matrix(n - m.size(), n);
            int r = 0;
            if (inits != null) {
                for (; r < inits.length; ++r) {
                    J.set(r, inits[r], 1);
                }
            }
            int mcur = m.get(0);
            for (int c = 0, k = 1; c < n; ++c) {
                if (contains(inits, c)) {
                    continue;
                }
                if (mcur != c) {
                    J.set(r++, c, 1);
                } else if (k < m.size()) {
                    mcur = m.get(k++);
                } else {
                    mcur = -1;
                }
            }
            return J;
        }
    }

    private static boolean contains(int[] inits, int c) {
        if (inits == null) {
            return false;
        }
        for (int i = 0; i < inits.length; ++i) {
            if (inits[i] == c) {
                return true;
            }
        }
        return false;
    }

    public static Matrix last(final int len, final int step) {
        int nc = len, nr = nc / step;
        Matrix J = new Matrix(nr, nc);
        for (int r = 0, c = step - 1; r < nr; ++r, c += step) {
            J.set(r, c, 1);
        }
        return J;
    }

    public static Matrix first(final int len, final int step) {
        int nc = len, nr = 1 + (nc - 1) / step;
        Matrix J = new Matrix(nr, nc);
        for (int r = 0, c = 0; r < nr; ++r, c += step) {
            J.set(r, c, 1);
        }
        return J;
    }

    public static Matrix aggregation(final int len, final int nagg) {
        int nc = len, nr = nc / nagg;
        Matrix J = new Matrix(nr, nc);
        for (int r = 0, c = 0; r < nr; ++r) {
            for (int k = 0; k < nagg; ++k, ++c) {
                J.set(r, c, 1);
            }
        }
        return J;
    }

    /**
     * Search for d successive non missing values
     *
     * @param y Partially observed) data
     * @param d Differencing order
     * @return true if such a sequence has been found, false otherwise
     */
    public static int[] searchDefaultInitialValues(IReadDataBlock y, int d) {
        int n = y.getLength();
        for (int i = 0; i < n - d; ++i) {
            int j = 0;
            for (; j < d; ++j) {
                if (!Double.isFinite(y.get(i + j))) {
                    break;
                }
            }
            if (j == d) {
                int[] ivals = new int[d];
                for (int k = 0; k < d; ++k) {
                    ivals[k] = i + k;
                }
                return ivals;
            } else {
                i += j;
            }
        }
        return null;
    }

    private final int n_;
    private final Matrix J_;
    private final BackFilter Delta_;
    private Matrix B_, D_;
    private final DataBlock y_;
    private DataBlock dy_;
    private final Matrix X_, DX_;

    private boolean valid_ = true;

    /**
     *
     * @param y JY = observed data (including permutation)
     * @param X regression variables. The regression variables correspond to Y
     * before any transformation. JX will be computed internally. X_ may be null
     * @param transformation
     * @param difference
     */
    public DifferenceStationaryModelHelper(final DataBlock y, final Matrix X, final Matrix transformation, final BackFilter difference) {
        n_ = transformation.getColumnsCount();
        J_ = transformation;
        Delta_ = difference;
        y_ = y;
        X_ = X;
        if (X_ != null) {
            int d = Delta_.getDegree();
            DX_ = new Matrix(X_.getRowsCount() - d, X_.getColumnsCount());
            for (int i = 0; i < X_.getColumnsCount(); ++i) {
                Delta_.filter(X_.column(i), DX_.column(i));
            }
        } else {
            DX_ = null;
        }
    }

    public DifferenceStationaryModelHelper(final IModelProvider provider) {
        this(provider.getTransformedData(), provider.getDesignMatrix(), provider.getTransformation(), provider.getDifferencing());
    }

    public Matrix getB() {
        if (B_ == null) {
            calc();
        }
        return B_;
    }

    public Matrix getD() {
        if (D_ == null) {
            calc();
        }
        return D_;
    }

    /**
     *
     * @param Vw Matrix of the unobserved stationary process
     * @return
     */
    public ConcentratedLikelihood compute(Matrix Vw) {
        if (D_ == null) {
            calc();
        }
        if (!valid_) {
            return null;
        }
        int d = Delta_.getDegree();
        if (Vw.getRowsCount() != n_ - d) {
            return null;
        }
        int m = y_.getLength();
        // compute DY
        Matrix Q = SymmetricMatrix.quadraticFormT(Vw, B_);
        // compute Delta*X
        // Delta*X

        try {
            SymmetricMatrix.lcholesky(Q);
            ConcentratedLikelihood ll = new ConcentratedLikelihood();
            double ldet = 2 * Q.diagonal().sumLog().value;
            DataBlock dy = dy_.deepClone();
            LowerTriangularMatrix.rsolve(Q, dy);
            if (X_ != null) {
                // compute B*Delta*X
                Matrix BDX = B_.times(DX_);
                for (int i = 0; i < X_.getColumnsCount(); ++i) {
                    LowerTriangularMatrix.rsolve(Q, BDX.column(i));
                }
                Householder qr = new Householder(true);
                qr.setEpsilon(1e-12);
                qr.decompose(BDX);
                DataBlock b = new DataBlock(qr.getRank());
                DataBlock res = new DataBlock(BDX.getRowsCount() - qr.getRank());
                qr.leastSquares(dy, b, res);
                Matrix R = qr.getR();
                double ssqerr = res.ssq();
                Matrix bvar = SymmetricMatrix.XXt(UpperTriangularMatrix.inverse(R));
                bvar.mul(ssqerr / dy.getLength());
                ll.set(ssqerr, ldet, dy.getLength());
                ll.setRes(res.getData());
                ll.setB(b.getData(), bvar, qr.getRank());
            } else {
                ll.set(dy.ssq(), ldet, m - d);
                ll.setRes(dy.getData());
            }
            return ll;
        } catch (MatrixException err) {
            return null;
        }
    }

    /**
     * The first column contains the projection and the remaining columns their
     * covariance
     *
     * @param Vw
     * @param Z
     * @return
     */
    public Matrix computeProjections(Matrix Z, Matrix Vw) {
        int nz = Z == null ? n_ : Z.getRowsCount();
        Matrix z = new Matrix(nz, nz + 1);
        // z = Z D^-1(P)* |X*                     |=Z D^-1(P)* K
        //                |Vw B'(BVw B')^-1 DX|
        // Raw implementation
        // Compute BV=B*Vw
        int n = B_.getColumnsCount(), m = B_.getRowsCount(), d = Delta_.getDegree();
        Matrix BV = B_.times(Vw);
        Matrix Q = new Matrix(m, m);
        Q.subMatrix().product(BV.subMatrix(), B_.subMatrix().transpose());
        SymmetricMatrix.reinforceSymmetry(Q);
        try {
            SymmetricMatrix.lcholesky(Q);
            // (BVw B')^-1 DX = (QQ')^-1 DX =  Q'^-1 Q^-1 DX
            // Q^-1 DX = Z <=> DX = Q*Z
            DataBlock dy = dy_.deepClone();
            LowerTriangularMatrix.rsolve(Q, dy);
            DataBlock b = null;
            Matrix bvar = null;
            Matrix zvar = null;
            double sigma;
            if (X_ != null) {
                Matrix BDX = B_.times(DX_);
                LowerTriangularMatrix.rsolve(Q, BDX.subMatrix());
                Householder qr = new Householder(true);
                qr.setEpsilon(1e-12);
                qr.decompose(BDX);
                b = new DataBlock(qr.getRank());
                DataBlock res = new DataBlock(BDX.getRowsCount() - qr.getRank());
                qr.leastSquares(dy, b, res);
                Matrix R = qr.getR();
                double ssqerr = res.ssq();
                sigma = ssqerr / dy.getLength();
                bvar = SymmetricMatrix.XXt(UpperTriangularMatrix.inverse(R));
                bvar.mul(sigma);
                // we correct dy with the given coefficients
                for (int i = 0; i < b.getLength(); ++i) {
                    dy.addAY(-b.get(i), BDX.column(i));
                }
            } else {
                sigma = dy.ssq() / dy.getLength();
            }
            try {
                LowerTriangularMatrix.rsolve(Q, BV.subMatrix());
                Matrix T = SymmetricMatrix.XtX(BV);
                T.chs();
                T.add(Vw);
                Matrix U = new Matrix(n_, 2 * n_);
                delta(U.subMatrix(0, n_, 0, n_));
                U.subDiagonal(n_).set(1);
                if (SparseSystemSolver.solve(U)) {
                    zvar = new Matrix(n_, n_);
                    SymmetricMatrix.quadraticFormT(T.subMatrix(), U.subMatrix(0, d, n_ + d, -1), zvar.subMatrix(0, d, 0, d));
                    SymmetricMatrix.quadraticFormT(T.subMatrix(), U.subMatrix(d, -1, n_ + d, -1), zvar.subMatrix(d, -1, d, -1));
                    zvar.clean(1e-6);
                    zvar.mul(sigma);
                }
            } catch (MatrixException err) {
            }

            //Q'^-1 Z = U <=> Z' Q^-1 = U' <=> Z' = U' Q 
            LowerTriangularMatrix.lsolve(Q, dy);
            // compute B'* U
            DataBlock bu = new DataBlock(n);
            bu.product(B_.columns(), dy);
            Matrix M = new Matrix(n_, n_ + 1);
            delta(M.subMatrix(0, n_, 0, n_));
            DataBlock lc = M.column(n_);
            lc.range(d, n_).product(Vw.rows(), bu);
            lc.range(0, d).copy(y_.range(0, d));
            // correct the inital values for X
            if (b != null) {
                Matrix Xc = new Matrix(d, b.getLength());
                Xc.subMatrix().product(J_.subMatrix(0, d, 0, -1), X_.subMatrix());
                // we correct dy with the given coefficients
                for (int i = 0; i < b.getLength(); ++i) {
                    lc.range(0, d).addAY(-b.get(i), Xc.column(i));
                }
            }
            // D^-1(P)* K = S
            if (SparseSystemSolver.solve(M)) {
                if (b != null) {
                    // we correct dy with the given coefficients
                    for (int i = 0; i < b.getLength(); ++i) {
                        lc.addAY(b.get(i), X_.column(i));
                    }
                }
                if (Z == null) {
                    z.column(0).copy(lc);
                    if (zvar != null) {
                        z.subMatrix(0, n_, 1, n_ + 1).copy(zvar.subMatrix());
                    }
                } else {
                    for (int i = 0; i < Z.getRowsCount(); ++i) {
                        z.set(i, 0, Z.row(i).dot(lc));
                    }
                }
            }
            return z;
        } catch (MatrixException err) {
            return null;
        }
    }

    ConcentratedLikelihood lcompute(IModelProviderEx xprovider, IReadDataBlock p) {
        if (D_ == null) {
            calc();
        }
        if (!valid_) {
            return null;
        }
        int d = Delta_.getDegree();
        int m = y_.getLength();
        Matrix Q = xprovider.getLCholesky(p, B_.clone());
        if (Q == null) {
            return null;
        }
        if (m != D_.getColumnsCount()) {
            return null;
        }
        // compute DY
        DataBlock dy = dy_.deepClone();
        // compute Delta*X
        // Delta*X

        try {
            ConcentratedLikelihood ll = new ConcentratedLikelihood();
            double ldet = 2 * Q.diagonal().sumLog().value;
            LowerTriangularMatrix.rsolve(Q, dy);
            if (X_ != null) {
                Matrix DX = new Matrix(X_.getRowsCount() - d, X_.getColumnsCount());
                for (int i = 0; i < X_.getColumnsCount(); ++i) {
                    Delta_.filter(X_.column(i), DX.column(i));
                }
                // compute B*Delta*X
                Matrix BDX = B_.times(DX);
                for (int i = 0; i < X_.getColumnsCount(); ++i) {
                    LowerTriangularMatrix.rsolve(Q, BDX.column(i));
                }
                Householder qr = new Householder(true);
                qr.setEpsilon(1e-12);
                qr.decompose(BDX);
                DataBlock b = new DataBlock(qr.getRank());
                DataBlock res = new DataBlock(BDX.getRowsCount() - qr.getRank());
                qr.leastSquares(dy, b, res);
                Matrix R = qr.getR();
                double ssqerr = res.ssq();
                Matrix bvar = SymmetricMatrix.XXt(UpperTriangularMatrix.inverse(R));
                bvar.mul(ssqerr / dy.getLength());
                ll.set(ssqerr, ldet, dy.getLength());
                ll.setRes(res.getData());
                ll.setB(b.getData(), bvar, qr.getRank());
            } else {
                ll.set(dy.ssq(), ldet, m - d);
                ll.setRes(dy.getData());
            }
            return ll;
        } catch (MatrixException err) {
            return null;
        }
    }

    private void delta(SubMatrix m) {
        // Permutations
        int d = Delta_.getDegree();
        m.extract(0, d, 0, n_).copy(J_.subMatrix(0, d, 0, n_));
        // Differences
        Polynomial p = Delta_.getPolynomial();
        SubMatrix Q = m.extract(d, n_, 0, n_);
        for (int i = 0; i <= d; ++i) {
            Q.subDiagonal(i).set(p.get(d - i));
        }
    }

    private void calc() {
        if (!valid_) {
            return;
        }
        // 
        int d = Delta_.getDegree();
        int m = J_.getRowsCount();
        // M contains D(P)', Jl' and (A,B)' after resolution
        Matrix M = new Matrix(n_, n_ + m - d);
        // Set D(P) in (M0)'
        delta(M.subMatrix(0, n_, 0, n_).transpose());

//        // Permutations
//        M.subMatrix(0, n_, 0, d).copy(J_.subMatrix(0, d, 0, n_).transpose());
//        // Differences
//        Polynomial p = Delta_.getPolynomial();
//        SubMatrix Q = M.subMatrix(0, n_, d, n_);
//        for (int i = 0; i <= d; ++i) {
//            Q.subDiagonal(-i).set(p.get(d - i));
//        }
        // Copy Jl'
        M.subMatrix(0, n_, n_, n_ + m - d).copy(J_.subMatrix(d, m, 0, n_).transpose());
        if (SparseSystemSolver.solve(M)) {
            D_ = new Matrix(m - d, m);
            B_ = new Matrix(m - d, n_ - d);
            SubMatrix A = D_.subMatrix(0, m - d, 0, d);
            A.copy(M.subMatrix(0, d, n_, n_ + m - d).transpose());
            A.chs();
            D_.subDiagonal(d).set(1);
            B_.subMatrix().copy(M.subMatrix(d, n_, n_, n_ + m - d).transpose());
            dy_ = new DataBlock(m - d);
            DataBlock xinit = y_.range(0, d);
            for (int i = 0; i < m - d; ++i) {
                dy_.set(i, y_.get(d + i) + A.row(i).dot(xinit));
            }
        } else {
            valid_ = false;
        }
    }

    private void clear() {
        B_ = null;
        D_ = null;
        valid_ = true;
    }

    public static interface IModelProvider {

        public Matrix getStationnaryCovariance(IReadDataBlock parameters);

        public BackFilter getDifferencing();

        public Matrix getTransformation();

        public DataBlock getTransformedData();

        public Matrix getDesignMatrix();
    }

    public static interface IModelProviderEx extends IModelProvider {

        /**
         * Computes the Cholesky factor of B*V*B', where V is the stationary
         * covariance matrix
         *
         * @param parameters The parameters of the model
         * @param B A mxn matrix, where n is the size of the covariance matrix
         * and m<=n
         * @return The Cholesky factor. The size of the matrix is mxm
         */
        public Matrix getLCholesky(IReadDataBlock parameters, Matrix B);
    }

    public static class LikelihoodFunction<S extends IModelProvider> implements IFunction, ISsqFunction {

        private final S provider_;
        private final IParametersDomain mapping_;
        private final DifferenceStationaryModelHelper helper_;
        private boolean lcompute_ = true;

        public void setLCompute(boolean lc) {
            lcompute_ = lc;
        }

        public boolean isLCompute() {
            return lcompute_;
        }

        public LikelihoodFunction(S provider, IParametersDomain mapping) {
            provider_ = provider;
            mapping_ = mapping;
            helper_ = new DifferenceStationaryModelHelper(provider);
        }

        public S getModelProvider() {
            return provider_;
        }

        public IParametersDomain getMapping() {
            return mapping_;
        }

        public DifferenceStationaryModelHelper getHelper() {
            return helper_;
        }

        @Override
        public IFunctionInstance evaluate(IReadDataBlock parameters) {
            return new LikelihoodFunctionInstance<>(this, parameters); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
            return new NumericalDerivatives(this, point, false, true);
        }

        @Override
        public IParametersDomain getDomain() {
            return mapping_; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ISsqFunctionDerivatives getDerivatives(ISsqFunctionInstance point) {
            return new SsqNumericalDerivatives(this, point, false, true);
        }

        @Override
        public ISsqFunctionInstance ssqEvaluate(IReadDataBlock parameters) {
            return new LikelihoodFunctionInstance<>(this, parameters); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class LikelihoodFunctionInstance<S extends IModelProvider> implements IFunctionInstance, ISsqFunctionInstance {

        private final DefaultLikelihoodEvaluation<ConcentratedLikelihood> ll_;
        private final DataBlock p_;

        public LikelihoodFunctionInstance(LikelihoodFunction<S> fn, IReadDataBlock p) {
            S provider = fn.getModelProvider();
            Matrix X = provider.getDesignMatrix();
            DataBlock y = provider.getTransformedData();
            ConcentratedLikelihood cll;
            if (fn.isLCompute() && provider instanceof IModelProviderEx) {
                IModelProviderEx xprovider = (IModelProviderEx) provider;
                cll = fn.helper_.lcompute(xprovider, p);
            } else {
                cll = fn.helper_.compute(provider.getStationnaryCovariance(p));
            }
            ll_ = new DefaultLikelihoodEvaluation<>(cll);
            p_ = new DataBlock(p);

        }

        public ConcentratedLikelihood getLikelihood() {
            return ll_.getLikelihood();
        }

        @Override
        public IReadDataBlock getParameters() {
            return p_;
        }

        @Override
        public double getValue() {
            return ll_.getValue();
        }

        @Override
        public double[] getE() {
            return ll_.getE(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double getSsqE() {
            return ll_.getSsqValue();
        }
    }
}
