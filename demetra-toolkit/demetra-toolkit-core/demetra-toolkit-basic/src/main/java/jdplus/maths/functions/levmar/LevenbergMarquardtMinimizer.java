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
package jdplus.maths.functions.levmar;

import jdplus.data.DataBlock;
import jdplus.maths.functions.ParamValidation;
import jdplus.maths.functions.ssq.ISsqFunction;
import jdplus.maths.functions.ssq.ISsqFunctionDerivatives;
import jdplus.maths.functions.ssq.ISsqFunctionPoint;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.functions.ssq.SsqFunctionMinimizer;

/**
 *
 * @author Jean Palate
 */
public class LevenbergMarquardtMinimizer implements SsqFunctionMinimizer {

    private static final int DEF_MAX_ITER = 200;
    private static final double EPSILON = 1e-17;
    private static final double DEF_INIT_MU = 1e-03;
    private static final double DEF_STOP_THRESH = 1e-15, DEF_STOP_THRESH_3 = 1e-12;

    public static class LmBuilder implements Builder{

        private double fnPrecision = DEF_STOP_THRESH_3;
        private double paramPrecision = DEF_STOP_THRESH;
        private double gPrecision = DEF_STOP_THRESH;
        private double tau = DEF_INIT_MU;
        private int maxIter = DEF_MAX_ITER;

        private LmBuilder() {
        }

        @Override
        public LmBuilder functionPrecision(double eps) {
            fnPrecision = eps;
            return this;
        }

        public LmBuilder parametersPrecision(double eps) {
            paramPrecision = eps;
            return this;
        }

        public LmBuilder gradientPrecision(double eps) {
            gPrecision = eps;
            return this;
        }

        public LmBuilder initialMu(double tau) {
            this.tau = tau;
            return this;
        }

        @Override
        public LmBuilder maxIter(int niter) {
            maxIter = niter;
            return this;
        }

        @Override
        public LevenbergMarquardtMinimizer build() {
            return new LevenbergMarquardtMinimizer(this);
        }

    }

    public static LmBuilder builder() {
        return new LmBuilder();
    }

    private final int maxIter;

    private final double tau, eps1, eps2, eps3;
    private int iter = 0;
    private DataBlock Jte;
    ///////////////////////////////////////////
    private ISsqFunction fn_;
    private ISsqFunctionPoint fcur_, ftry_;
    private DataBlock ecur;
    private double Fcur_, Ftry_;
    private CanonicalMatrix J, V;
    private DoubleSeq G;
    //private SubMatrix J, K;
    private double scale_, scale2_;
    ///////////////////////////////////////////
    private double mu;
    private long nu;
    private int stop;
    private static final double ONE_THIRD = 1.0 / 3;

    public LevenbergMarquardtMinimizer(LmBuilder builder) {
        this.maxIter = builder.maxIter;
        this.eps1 = builder.gPrecision;
        this.eps2 = builder.paramPrecision;
        this.eps3 = builder.fnPrecision;
        this.tau = builder.tau;
    }

    public boolean hasConverged() {
        return stop == 2;
    }

    @Override
    public CanonicalMatrix curvatureAtMinimum() {
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
            CanonicalMatrix K = V.deepClone();
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
        while (iter++ < maxIter) {
            if (!iterate()) {
                break;
            }
        }
        //validate();
        return (stop != 7 && stop != 4 && iter < maxIter);
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

}
