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
package demetra.maths.functions.levmar;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class DogLegMethod implements ISsqFunctionMinimizer {

    static final int DEF_MAX_ITER = 20;
    static final double EPSILON = 1e-17;
    static final double DEF_STOP_THRESH = 1e-12;
    static final double DEF_LTRUSTED = .1;
    private int itmax = DEF_MAX_ITER, iter = 0;
    private double eps1 = DEF_STOP_THRESH,
            eps2 = DEF_STOP_THRESH,
            eps2_sq = DEF_STOP_THRESH * DEF_STOP_THRESH,
            eps3 = DEF_STOP_THRESH,
            ltrusted_ = DEF_LTRUSTED;
    private double eps = 1e-5;
    private DataBlock g_;
    ///////////////////////////////////////////
    private ISsqFunction fn_;
    private ISsqFunctionPoint fcur_, ftry_;
    private DataBlock ecur_;
    private double Fcur_, Ftry_;
    private Matrix J, JtJ;
    private double scale_, scale2_;
    ///////////////////////////////////////////
    private int stop;

    @Override
    public ISsqFunctionMinimizer exemplar() {
        DogLegMethod ex = new DogLegMethod();
        ex.eps = eps;
        ex.eps1 = eps1;
        ex.eps2 = eps2;
        ex.eps3 = eps3;
        ex.eps2_sq = eps2_sq;
        return ex;
    }

    @Override
    public double getFunctionPrecision() {
        return eps;
    }

    @Override
    public Matrix curvatureAtMinimum() {
        if (JtJ == null) {
            fcur_.ssqDerivatives().jacobian(J);
            JtJ = SymmetricMatrix.XtX(J);
        }
        return JtJ.times(2);
    }

    @Override
    public Doubles gradientAtMinimum() {
        return fcur_.ssqDerivatives().gradient();
    }

    @Override
    public int getIterCount() {
        return iter;
    }

    @Override
    public int getMaxIter() {
        return itmax;
    }

    @Override
    public ISsqFunctionPoint getResult() {
        return fcur_;
    }

    @Override
    public double getObjective() {
        return Fcur_;
    }

    @Override
    public boolean minimize(ISsqFunctionPoint start) {
        fn_ = start.getSsqFunction();
        fcur_ = start;
        return calc();
    }

    @Override
    public void setFunctionPrecision(double value) {
        eps = value;
    }

    @Override
    public void setMaxIter(int n) {
        itmax = n;
    }

    private boolean iterate() {
        if (!Double.isFinite(Fcur_)) {
            stop = 7;
            return false;
        }

        if (Fcur_ <= eps3 * scale2_) {
            stop = 6;
            return false;
        }

        // gets the Jacobian
        fcur_.ssqDerivatives().jacobian(J);
        JtJ = SymmetricMatrix.XtX(J);
        double jdiag_ninf = JtJ.diagonal().normInf();

        // Computes g= J'e
        g_.product(J.columnsIterator(), ecur_); // g

        double ngInf = g_.normInf();
        if (ngInf <= eps * scale_) {
            stop = 1;
            return false;
        }

        double ng2 = g_.ssq();
        double ng = Math.sqrt(ng2);

        DataBlock hgn = null, hdl;
        DataBlock hsd = DataBlock.copyOf(g_);
        hsd.chs();
        double dL;

        // hsd (h steepest descent) alpha= |g|/(|Jg|)
        DataBlock Jg = DataBlock.make(J.getRowsCount());
        Jg.product(J.rowsIterator(), g_);
        double alpha = ng2 / Jg.ssq();
        DataBlock a = DataBlock.copyOf(hsd);
        a.mul(alpha);
        double na = a.norm2();

        double k = 0;
        do {
            Matrix A = JtJ.deepClone();
            if (k == 0) {
                k = 1e-6 * jdiag_ninf;
            } else {
                A.diagonal().add(k);
                k *= 10;
            }
            try {
                SymmetricMatrix.lcholesky(A);
                hgn = DataBlock.copyOf(g_);
                LowerTriangularMatrix.rsolve(A, hgn);
                LowerTriangularMatrix.lsolve(A, hgn);
                hgn.chs();
            } catch (MatrixException err) {
                hgn = null;
            }
        } while (hgn == null && k < jdiag_ninf);
        if (hgn == null) {
            return false;
        }

        while (true) {
            double ro = 0;
            // compute hdl h dog leg
            if (hgn.norm2() <= ltrusted_) {
                hdl = hgn;
                dL = -g_.dot(hdl);
            } else if (na >= ltrusted_) {
                hdl = DataBlock.copyOf(hsd);
                hdl.mul(ltrusted_ / ng);
                dL = ltrusted_ * (2 * Math.abs(alpha) * ng - ltrusted_) / alpha;
            } else {
                DataBlock bma = DataBlock.copyOf(hgn);
                bma.sub(a);
                double c = a.dot(bma);
                double nbma = bma.ssq();
                double z0 = ltrusted_ * ltrusted_ - a.ssq();
                double z1 = Math.sqrt(c * c + nbma * z0);

                double beta;
                if (c <= 0) {
                    beta = (-c + z1) / nbma;
                } else {
                    beta = z0 / (c + z1);
                }
                dL = alpha * (1 - beta) * (1 - beta) * ng + beta * (2 - beta) * Fcur_;
                hdl = DataBlock.copyOf(a);
                hdl.addAY(beta, bma);
            }

            if (!Double.isFinite(hdl.ssq())) {
                stop = 7;
                return false;
            }
            DataBlock np = DataBlock.copyOf(fcur_.getParameters());
            if (hdl.ssq() <= eps2 * (np.ssq() + eps2)) {
                /*
                 * relative change in p is small, stop
                 */
                stop = 2;
                return false;
            }
            np.add(hdl);

//                if (ndp2 >= (np2 + eps2) / EPSILON * EPSILON) { /*
//                     * almost singular
//                     */
//                    stop = 4;
//                    break;
//                }
            boolean solved = fn_.getDomain().checkBoundaries(np);

            boolean accepted = false;
            if (solved) {
                ftry_ = fn_.ssqEvaluate(np);
                Ftry_ = ftry_.getSsqE();
                double dF = Fcur_ - Ftry_;
                ro = dF / dL;

                if (dF > 0.0) {
                    // accept the solution
                    accepted = true;
//                    boolean end = (dF / scale2_) <= eps;
//                    if (end) {
//                        stop = 2;
//                    }
                    fcur_ = ftry_;
                    Fcur_ = Ftry_;
                    ecur_ = DataBlock.copyOf(fcur_.getE());
//                    if (end) {
//                        return false;
//                    }
                }
            }

            if (accepted && ro > .75) {
                ltrusted_ = Math.max(ltrusted_, 3 * hdl.norm2());
            } else if (!accepted || ro < .25) {
                ltrusted_ *= .5;
            }

            if (accepted) {
                return true;
            }
            if (ltrusted_ <= eps * (np.norm2() + eps2)) {
                JtJ = null;
                return false;
            }
        }

    }

    private boolean calc() {
        iter = 0;
        ecur_ = DataBlock.copyOf(fcur_.getE());
        Fcur_ = fcur_.getSsqE();
        scale2_ = Fcur_;
        scale_ = Math.sqrt(Fcur_);
        ltrusted_ = DEF_LTRUSTED;

        int n = ecur_.length(), m = fn_.getDomain().getDim();

        // Jacobian
        J = Matrix.make(n, m);
        g_ = DataBlock.make(m);
        while (iterate() && iter < itmax) {
            ++iter;
        }
        return (stop != 7 && stop != 4);
    }

    @Override
    public double getParametersPrecision() {
        return eps2;
    }

    @Override
    public void setParametersPrecision(double value) {
        eps2 = value;
    }
}
