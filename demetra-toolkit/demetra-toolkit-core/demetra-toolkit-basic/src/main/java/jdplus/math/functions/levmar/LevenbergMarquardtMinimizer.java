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
package jdplus.math.functions.levmar;

import jdplus.data.DataBlock;
import jdplus.math.functions.ParamValidation;
import jdplus.math.functions.ssq.ISsqFunction;
import jdplus.math.functions.ssq.ISsqFunctionDerivatives;
import jdplus.math.functions.ssq.ISsqFunctionPoint;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import demetra.data.DoubleSeq;
import jdplus.math.linearsystem.QRLeastSquaresSolution;
import jdplus.math.linearsystem.QRLeastSquaresSolver;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;

/**
 *
 * @author Jean Palate
 */
public class LevenbergMarquardtMinimizer implements SsqFunctionMinimizer {

    private static final int DEF_MAX_ITER = 200;
    private static final double EPSILON = 1e-17;
    private static final double DEF_INIT_MU = 1e-03;
    private static final double DEF_STOP_THRESH = 1e-15, DEF_STOP_THRESH_3 = 1e-12;

    public static class LmBuilder implements Builder {

        private double fnPrecision = DEF_STOP_THRESH_3;
        private double paramPrecision = DEF_STOP_THRESH;
        private double gPrecision = DEF_STOP_THRESH;
        private double tau = DEF_INIT_MU;
        private int maxIter = DEF_MAX_ITER;
        private boolean qr = true;

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

        public LmBuilder useQR(boolean qr) {
            this.qr = qr;
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
    private final boolean qr;
    private int iter = 0;
    ///////////////////////////////////////////
    private ISsqFunction function;
    private ISsqFunctionPoint currentPoint, tentativePoint;
    private DataBlock currentE;
    private double currentObjective, tentativeObjective;
    /**
     * Approximate hessian of the function at the current point V =
     * 2*[de/dxi*de/dxj]
     */
    private FastMatrix H;
    /**
     * Gradient of the function at the current point G = 2*[de/dxi*e]
     */
    private DoubleSeq G;
    private double scale, scale2;
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
        this.qr = builder.qr;
    }

    public boolean hasConverged() {
        return stop == 2;
    }

    @Override
    public FastMatrix curvatureAtMinimum() {
        if (H == null) {
            ISsqFunctionDerivatives derivatives = currentPoint.ssqDerivatives();
            H = derivatives.hessian();
            G = derivatives.gradient();
        }
        return H;
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
        if (G == null) {
            ISsqFunctionDerivatives derivatives = currentPoint.ssqDerivatives();
            H = derivatives.hessian();
            G = derivatives.gradient();
        }
        return G;
    }

    @Override
    public ISsqFunctionPoint getResult() {
        return currentPoint;
    }

    @Override
    public double getObjective() {
        return currentObjective;
    }

    @Override
    public boolean minimize(ISsqFunctionPoint start) {
        function = start.getSsqFunction();
        currentPoint = start;
        return calc();
    }

    private boolean iterate() {
        if (qr) {
            return iterateQR();
        } else {
            return iterateCholesky();
        }
    }

    private boolean iterateQR() {
        // Step 1: Initialize e, J)
        if (!Double.isFinite(currentObjective)) {
            stop = 7;
            return false;
        }

        if (currentObjective <= eps3 * scale2) {
            stop = 6;
            return false;
        }

        int n = currentE.length(), m = function.getDomain().getDim();
        FastMatrix JC = FastMatrix.make(n + m, m);
        FastMatrix J = JC.extract(0, n, 0, m);
        // e, extended with 0
        double[] e = new double[n + m];
        currentE.copyTo(e, 0);
        ISsqFunctionDerivatives derivatives = currentPoint.ssqDerivatives();
        // Gets the jacobian and the gradient
        try {
            derivatives.jacobian(J);
            G = derivatives.gradient();
        } catch (Exception ex) {
            return false;
        }

        // Computes the norm
        double nJte = G.normInf();
        if (nJte <= eps1 * scale) {
            stop = 1;
            return false;
        }

        int kiter = 0;
        while (kiter++ < 100) {
            FastMatrix V;
            if (mu > 0) {
                double smu = Math.sqrt(mu);
                JC.subDiagonal(-n).set(smu);
            }
            try {
                QRLeastSquaresSolution ls = QRLeastSquaresSolver.fastLeastSquares(DoubleSeq.of(e), JC);
                if (ls.rank() == m) {
                    V = ls.RtR();
                    DoubleSeq dp = ls.getB().times(-1);
                    if (!Double.isFinite(dp.ssq())) {
                        stop = 7;
                        return false;
                    }
                    DataBlock np = DataBlock.of(currentPoint.getParameters());
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
                    ParamValidation status = function.getDomain().validate(np);
                    if (status != ParamValidation.Invalid) {
                        try {
                            tentativePoint = function.ssqEvaluate(np);
                            tentativeObjective = tentativePoint.getSsqE();
                            double dF = currentObjective - tentativeObjective;
                            if (dF > 0.0) {
                                if (status == ParamValidation.Changed) {
                                    // we have a new starting point (better than the current point).
                                    // restart the processing. Undefined score and hessian
                                    currentPoint = tentativePoint;
                                    currentObjective = tentativeObjective;
                                    currentE = DataBlock.of(currentPoint.getE());
                                    H = null;
                                    G = null;
//                        mu = 0;
//                        nu = 4;
                                    return true;
                                }
                                DataBlock dl = DataBlock.of(G);
                                dl.addAY(-2 * mu, dp);
                                double dL = -dl.dot(dp) / 2;
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
                                    boolean end = dF <= eps3 * scale2;
                                    currentPoint = tentativePoint;
                                    currentObjective = tentativeObjective;
                                    currentE = DataBlock.of(currentPoint.getE());
                                    // H = 2* J'J =2*(R'Q'QR) 
                                    V.mul(2);
                                    H = V;
                                    if (end) {
                                        stop = 2;
                                        return false;
                                    } else {
                                        return true;
                                    }

                                }
                            }
                        } catch (Exception err) {
                        }
                    }
                }
            } catch (Exception err) {
            }
            /*
             * if this point is reached, either the linear system could not be
             * solved or the error did not reduce; in any case, the increment
             * must be rejected
             */
            if (mu == 0) {
                mu = tau * scale;
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

    private boolean iterateCholesky() {
        // Step 1: Initialize e, J)
        if (!Double.isFinite(currentObjective)) {
            stop = 7;
            return false;
        }

        if (currentObjective <= eps3 * scale2) {
            stop = 6;
            return false;
        }

        int n = currentE.length(), m = function.getDomain().getDim();
        FastMatrix J = FastMatrix.make(n, m);
        DataBlock Jte = DataBlock.make(m);
        try {
            currentPoint.ssqDerivatives().jacobian(J);
        } catch (Exception ex) {
            return false;
        }

        // Gets the jacobian
        // Computes J'J, J'e
        Jte.product(J.columnsIterator(), currentE);
        H = SymmetricMatrix.XtX(J);

        double nJte = Jte.normInf();
        if (nJte <= eps1 * scale) {
            stop = 1;
            return false;
        }

//        if (mu == 0) {
//             mu = tau * V.diagonal().max();
//        }
        int kiter = 0;
        while (kiter++ < 100) {
            DataBlock dp = null;
            FastMatrix K = H.deepClone();
            if (mu > 0) {
                K.diagonal().add(mu);
            }
            boolean solved = false;
            try {
                SymmetricMatrix.lcholesky(K);
                dp = DataBlock.of(Jte);
                dp.chs();
                LowerTriangularMatrix.solveLx(K, dp);
                LowerTriangularMatrix.solvexL(K, dp);
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
                DataBlock np = DataBlock.of(currentPoint.getParameters());
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
                ParamValidation status = function.getDomain().validate(np);
                if (status != ParamValidation.Invalid) {
                    try {
                        tentativePoint = function.ssqEvaluate(np);
                        tentativeObjective = tentativePoint.getSsqE();
                        double dF = currentObjective - tentativeObjective;
                        if (dF > 0.0) {
                            if (status == ParamValidation.Changed) {
                                // we have a new starting point (better than the current point).
                                // restart the processing. 
                                currentPoint = tentativePoint;
                                currentObjective = tentativeObjective;
                                currentE = DataBlock.of(currentPoint.getE());
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
                                boolean end = dF <= eps3 * scale2;
                                currentPoint = tentativePoint;
                                currentObjective = tentativeObjective;
                                currentE = DataBlock.of(currentPoint.getE());
                                if (end) {
                                    // clear the variance
                                    H = null;
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
                mu = tau * H.diagonal().max();
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
        H = null;
        iter = 0;
        nu = 4;
        mu = 0;
        currentE = DataBlock.of(currentPoint.getE());
        currentObjective = currentPoint.getSsqE();
        scale2 = currentObjective;
        scale = Math.sqrt(currentObjective);
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
