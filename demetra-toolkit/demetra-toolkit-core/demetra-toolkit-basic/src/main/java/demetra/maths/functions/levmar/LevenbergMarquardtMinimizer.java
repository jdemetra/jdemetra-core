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
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionDerivatives;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.CanonicalMatrix;

/**
 *
 * @author Jean Palate
 */
public class LevenbergMarquardtMinimizer implements ISsqFunctionMinimizer {

    static final int DEF_MAX_ITER = 200;
    static final double EPSILON = 1e-17;
    static final double DEF_INIT_MU = 1e-03;
    static final double DEF_STOP_THRESH = 1e-15, DEF_STOP_THRESH_3 = 1e-12;
    private int itmax = DEF_MAX_ITER, iter = 0;
    private double tau = DEF_INIT_MU,
            eps1 = DEF_STOP_THRESH,
            eps2 = DEF_STOP_THRESH,
            eps2_sq = DEF_STOP_THRESH * DEF_STOP_THRESH,
            eps3 = DEF_STOP_THRESH_3;
    private DataBlock Jte;
    ///////////////////////////////////////////
    private ISsqFunction fn_;
    private ISsqFunctionPoint fcur_, ftry_;
    private DataBlock ecur;
    private double Fcur_, Ftry_;
    private FastMatrix J, V;
    private DoubleSeq G;
    //private SubMatrix J, K;
    private double scale_, scale2_;
    ///////////////////////////////////////////
    private double mu;
    private long nu;
    private int stop;
    private static final double ONE_THIRD = 1.0 / 3;

    public boolean hasConverged() {
        return stop == 2;
    }

    @Override
    public ISsqFunctionMinimizer exemplar() {
        LevenbergMarquardtMinimizer ex = new LevenbergMarquardtMinimizer();
        ex.eps1 = eps1;
        ex.eps2 = eps2;
        ex.eps3 = eps3;
        ex.eps2_sq = eps2_sq;
        ex.itmax = itmax;
        return ex;
    }

    @Override
    public double getFunctionPrecision() {
        return eps3;
    }

    @Override
    public FastMatrix curvatureAtMinimum() {
        if (V == null) {
            ISsqFunctionDerivatives derivatives = fcur_.ssqDerivatives();
            V = derivatives.hessian();
            G = derivatives.gradient();
        }
        return V;
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
        if (G == null) {
            ISsqFunctionDerivatives derivatives = fcur_.ssqDerivatives();
            V = derivatives.hessian();
            G = derivatives.gradient();
        }
        return G;
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
        eps3 = value;
    }

    @Override
    public void setMaxIter(int n) {
        itmax = n;
    }

    protected boolean iterate() {
        // Step 1: Initialize e, J)
        if (!Double.isFinite(Fcur_)) {
            stop = 7;
            return false;
        }

        if (Fcur_ <= eps3 * scale2_) {
            stop = 6;
            return false;
        }

        try {
            fcur_.ssqDerivatives().jacobian(J);
        } catch (Exception ex) {
            return false;
        }

        // Computes J'J, J'e
        Jte.product(J.columnsIterator(), ecur);
        int m = J.getColumnsCount();
        V = SymmetricMatrix.XtX(J);

        double nJte = Jte.normInf();
        if (nJte <= eps1 * scale_) {
            stop = 1;
            return false;
        }

//        if (mu == 0) {
//             mu = tau * V.diagonal().max();
//        }
        int kiter = 0;
        while (kiter++ < 100) {
            DataBlock dp = null;
            FastMatrix K = V.deepClone();
            if (mu > 0) {
                K.diagonal().add(mu);
            }
            boolean solved = false;
            try {
                SymmetricMatrix.lcholesky(K);
                dp = DataBlock.of(Jte);
                dp.chs();
                LowerTriangularMatrix.rsolve(K, dp);
                LowerTriangularMatrix.lsolve(K, dp);
                solved = true;
//                Householder qr = new Householder(true);
//                qr.decompose(M);
//                if (qr.isFullRank()) {
//                    DataBlock b = new DataBlock(M.getRowsCount());
//                    ecur.copyTo(b.getData(), 0);
//                    qr.solve(b, dp);
//                    dp.chs();
//                    solved = true;
//                }
            } catch (Exception ex) {
            }
            if (solved) {
                if (!Double.isFinite(dp.ssq())) {
                    stop = 7;
                    return false;
                }
                DataBlock np = DataBlock.of(fcur_.getParameters());
                double dpl2 = dp.ssq(), pl2 = np.ssq();
                if (dpl2 <= eps2 * (pl2 + eps2)) {
                    /*
                     * relative change in p is small, stop
                     */

                    stop = 2;
                    return false;
                }
                if (dpl2 >= (pl2 + eps2) / (EPSILON * EPSILON)) {
                    /*
                     * almost singular
                     */

                    stop = 4;
                    return false;
                }
                //if (fn_.getDomain().checkBoundaries(np)) {
                np.add(dp);
                ParamValidation status = fn_.getDomain().validate(np);
                if (status != ParamValidation.Invalid) {
                    try {
                        ftry_ = fn_.ssqEvaluate(np);
                        Ftry_ = ftry_.getSsqE();
                        double dF = Fcur_ - Ftry_;
                        if (dF > 0.0) {
                            if (status == ParamValidation.Changed) {
                                // we have a new starting point (better than the current point).
                                // restart the processing. 
                                fcur_ = ftry_;
                                Fcur_ = Ftry_;
                                ecur = DataBlock.of(fcur_.getE());
//                        mu = 0;
//                        nu = 4;
                                return true;
                            }
                            DataBlock dl = DataBlock.of(Jte);
                            dl.addAY(-mu, dp);
                            double dL = -dl.dot(dp);
                            double ratio = dF / dL;
                            if (ratio > 0.0001) {
                                /*
                                 * reduction in error, increment is accepted
                                 */

                                double tmp = 2.0 * ratio - 1.0;
                                tmp = 1.0 - tmp * tmp * tmp;
                                if (mu != 0) {
                                    mu *= tmp >= ONE_THIRD ? tmp : ONE_THIRD;
                                }
                                nu = 4;
                                // accept the solution
                                boolean end = dF <= eps3 * scale2_;
                                fcur_ = ftry_;
                                Fcur_ = Ftry_;
                                ecur = DataBlock.of(fcur_.getE());
                                if (end) {
                                    // clear the variance
                                    V = null;
                                    stop = 2;
                                    return false;
                                } else {
                                    return true;
                                }

                            }
                        }
                    } catch (Exception err) {
                    }
                } else {
                }
            }

            /*
             * if this point is reached, either the linear system could not be
             * solved or the error did not reduce; in any case, the increment
             * must be rejected
             */
            if (mu == 0) {
                mu = tau * V.diagonal().max();
            } else {
                mu *= nu;
            }
            long nu2 = nu << 2; // 4*nu;
            if (nu2 <= nu) {
                stop = 5;
                return false;
            }
            nu = nu2;
        }

        return false;
    }

    private boolean calc() {
        G = null;
        V = null;
        iter = 0;
        nu = 4;
        mu = 0;
        ecur = DataBlock.of(fcur_.getE());
        Fcur_ = fcur_.getSsqE();
        scale2_ = Fcur_;
        scale_ = Math.sqrt(Fcur_);
        int n = ecur.length(), m = fn_.getDomain().getDim();
        J = CanonicalMatrix.make(n, m);
        Jte = DataBlock.make(m);
        while (iter++ < itmax) {
            if (!iterate()) {
                break;
            }
        }
        //validate();
        return (stop != 7 && stop != 4 && iter < itmax);
    }
//    private void validate() {
//        DataBlock np = new DataBlock(fcur_.getParameters());
//
//        ParamValidation status = fn_.getDomain().validate(np);
//        if (status == ParamValidation.Changed) {
//            fcur_ = fn_.ssqEvaluate(np);
//            Fcur_ = ftry_.getSsqE();
//            ecur = new DataBlock(fcur_.getE());
//        }
//    }

    @Override
    public double getParametersPrecision() {
        return eps2;
    }

    @Override
    public void setParametersPrecision(double value) {
        eps2 = value;
    }
}
