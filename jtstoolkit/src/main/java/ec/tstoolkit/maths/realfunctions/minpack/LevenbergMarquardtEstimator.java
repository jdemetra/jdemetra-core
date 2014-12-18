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
package ec.tstoolkit.maths.realfunctions.minpack;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.FunctionException;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LevenbergMarquardtEstimator extends AbstractEstimator {

    /**
     * Number of solved variables.
     */
    private int solvedCols;

    /**
     * Diagonal elements of the R matrix in the Q.R. decomposition.
     */
    private double[] diagR;

    /**
     * Norms of the columns of the jacobian matrix.
     */
    private double[] jacNorm;

    /**
     * Coefficients of the Householder transforms vectors.
     */
    private double[] beta;

    /**
     * Columns permutation array.
     */
    private int[] permutation;

    /**
     * Rank of the jacobian matrix.
     */
    private int rank;

    /**
     * Levenberg-Marquardt parameter.
     */
    private double lmPar;

    /**
     * Parameters evolution direction associated with lmPar.
     */
    private double[] lmDir;

    /**
     * Positive input variable used in determining the initial step bound.
     */
    private double m_initialStepBoundFactor;

    /**
     * Desired relative error in the sum of squares.
     */
    private double m_costRelativeTolerance;

    /**
     * Desired relative error in the approximate solution parameters.
     */
    private double m_parRelativeTolerance;

    /**
     * Desired Max cosine on the orthogonality between the function vector and
     * the columns of the jacobian.
     */
    private double m_orthoTolerance;

    private int maxIter = 100;

    private int iterCount;

    private ILmHook hook_;

    /**
     * Build an estimator for least squares problems.
     * <p>
     * The default values for the algorithm settings are:
     * <ul>
     * <li>{@link #setInitialStepBoundFactor initial step bound factor}:
     * 100.0</li>
     * <li>{@link #setMaxCostEval maximal cost evaluations}: 1000</li>
     * <li>{@link #setCostRelativeTolerance cost relative tolerance}:
     * 1.0e-10</li>
     * <li>{@link #setParRelativeTolerance parameters relative tolerance}:
     * 1.0e-10</li>
     * <li>{@link #setOrthoTolerance orthogonality tolerance}: 1.0e-10</li>
     * </ul>
     * </p>
     */
    public LevenbergMarquardtEstimator() {

        // set up the superclass with a default Max cost evaluations setting
        setMaxCostEval(1000);

        // default values for the tuning parameters
        m_initialStepBoundFactor = 100.0;
        m_costRelativeTolerance = 1.0e-7;
        m_parRelativeTolerance = 1.0e-7;
        m_orthoTolerance = 1.0e-7;
    }

    public void setHook(ILmHook hook) {
        hook_ = hook;
    }

    /**
     * Solve a*x = b and d*x = 0 in the least squares sense.
     * <p>
     * This implementation is a translation in Java of the MINPACK <a
     * href="http://www.netlib.org/minpack/qrsolv.f">qrsolv</a> routine.
     * </p>
     * <p>
     * This method sets the lmDir and lmDiag attributes.
     * </p>
     * <p>
     * The authors of the original fortran function are:
     * </p>
     * <ul>
     * <li>Argonne National Laboratory. MINPACK project. March 1980</li>
     * <li>Burton S. Garbow</li>
     * <li>Kenneth E. Hillstrom</li>
     * <li>Jorge J. More</li>
     * </ul>
     * <p>
     * Luc Maisonobe did the Java translation.
     * </p>
     *
     * @param qy array containing qTy
     * @param diag diagonal matrix
     * @param lmDiag diagonal elements associated with lmDir
     * @param work work array
     */
    private void determineLMDirection(double[] qy, double[] diag,
            double[] lmDiag, double[] work) {

	// copy R and Qty to preserve input and initialize s
        // in particular, save the diagonal elements of R in lmDir
        for (int j = 0; j < solvedCols; ++j) {
            int pj = permutation[j];
            for (int i = j + 1; i < solvedCols; ++i) {
                m_jacobian[i * m_cols + pj] = m_jacobian[j * m_cols
                        + permutation[i]];
            }
            lmDir[j] = diagR[pj];
            work[j] = qy[j];
        }

        // eliminate the diagonal matrix d using a Givens rotation
        for (int j = 0; j < solvedCols; ++j) {

	    // prepare the row of d to be eliminated, locating the
            // diagonal element using p from the Q.R. factorization
            int pj = permutation[j];
            double dpj = diag[pj];
            if (dpj != 0) {
                for (int k = j + 1; k < lmDiag.length; ++k) {
                    lmDiag[k] = 0;
                }
            }
            lmDiag[j] = dpj;

	    // the transformations to eliminate the row of d
            // modify only a single element of Qty
            // beyond the first n, which is initially zero.
            double qtbpj = 0;
            for (int k = j; k < solvedCols; ++k) {
                int pk = permutation[k];

		// determine a Givens rotation which eliminates the
                // appropriate element in the current row of d
                if (lmDiag[k] != 0) {

                    double sin, cos;
                    double rkk = m_jacobian[k * m_cols + pk];
                    if (Math.abs(rkk) < Math.abs(lmDiag[k])) {
                        double cotan = rkk / lmDiag[k];
                        sin = 1.0 / Math.sqrt(1.0 + cotan * cotan);
                        cos = sin * cotan;
                    } else {
                        double tan = lmDiag[k] / rkk;
                        cos = 1.0 / Math.sqrt(1.0 + tan * tan);
                        sin = cos * tan;
                    }

		    // compute the modified diagonal element of R and
                    // the modified element of (Qty,0)
                    m_jacobian[k * m_cols + pk] = cos * rkk + sin * lmDiag[k];
                    double temp = cos * work[k] + sin * qtbpj;
                    qtbpj = -sin * work[k] + cos * qtbpj;
                    work[k] = temp;

                    // accumulate the tranformation in the row of s
                    for (int i = k + 1; i < solvedCols; ++i) {
                        double rik = m_jacobian[i * m_cols + pk];
                        temp = cos * rik + sin * lmDiag[i];
                        lmDiag[i] = -sin * rik + cos * lmDiag[i];
                        m_jacobian[i * m_cols + pk] = temp;
                    }

                }
            }

	    // store the diagonal element of s and restore
            // the corresponding diagonal element of R
            int index = j * m_cols + permutation[j];
            lmDiag[j] = m_jacobian[index];
            m_jacobian[index] = lmDir[j];

        }

	// solve the triangular system for z, if the system is
        // singular, then obtain a least squares solution
        int nSing = solvedCols;
        for (int j = 0; j < solvedCols; ++j) {
            if ((lmDiag[j] == 0) && (nSing == solvedCols)) {
                nSing = j;
            }
            if (nSing < solvedCols) {
                work[j] = 0;
            }
        }
        if (nSing > 0) {
            for (int j = nSing - 1; j >= 0; --j) {
                int pj = permutation[j];
                double sum = 0;
                for (int i = j + 1; i < nSing; ++i) {
                    sum += m_jacobian[i * m_cols + pj] * work[i];
                }
                work[j] = (work[j] - sum) / lmDiag[j];
            }
        }

        // permute the components of z back to components of lmDir
        for (int j = 0; j < lmDir.length; ++j) {
            lmDir[permutation[j]] = work[j];
        }

    }

    /**
     * Determine the Levenberg-Marquardt parameter.
     * <p>
     * This implementation is a translation in Java of the MINPACK <a
     * href="http://www.netlib.org/minpack/lmpar.f">lmpar</a> routine.
     * </p>
     * <p>
     * This method sets the lmPar and lmDir attributes.
     * </p>
     * <p>
     * The authors of the original fortran function are:
     * </p>
     * <ul>
     * <li>Argonne National Laboratory. MINPACK project. March 1980</li>
     * <li>Burton S. Garbow</li>
     * <li>Kenneth E. Hillstrom</li>
     * <li>Jorge J. More</li>
     * </ul>
     * <p>
     * Luc Maisonobe did the Java translation.
     * </p>
     *
     * @param qy array containing qTy
     * @param delta upper bound on the euclidean norm of diagR * lmDir
     * @param diag diagonal matrix
     * @param work1 work array
     * @param work2 work array
     * @param work3 work array
     */
    private void determineLMParameter(double[] qy, double delta, double[] diag,
            double[] work1, double[] work2, double[] work3) {

	// compute and store in x the gauss-newton direction, if the
        // jacobian is rank-deficient, obtain a least squares solution
        for (int j = 0; j < rank; ++j) {
            lmDir[permutation[j]] = qy[j];
        }
        for (int j = rank; j < m_cols; ++j) {
            lmDir[permutation[j]] = 0;
        }
        for (int k = rank - 1; k >= 0; --k) {
            int pk = permutation[k];
            double ypk = lmDir[pk] / diagR[pk];
            for (int i = 0, index = pk; i < k; ++i, index += m_cols) {
                lmDir[permutation[i]] -= ypk * m_jacobian[index];
            }
            lmDir[pk] = ypk;
        }

	// evaluate the function at the origin, and test
        // for acceptance of the Gauss-Newton direction
        double dxNorm = 0;
        for (int j = 0; j < solvedCols; ++j) {
            int pj = permutation[j];
            double s = diag[pj] * lmDir[pj];
            work1[pj] = s;
            dxNorm += s * s;
        }
        dxNorm = Math.sqrt(dxNorm);
        double fp = dxNorm - delta;
        if (fp <= 0.1 * delta) {
            lmPar = 0;
            return;
        }

	// if the jacobian is not rank deficient, the Newton step provides
        // a lower bound, parl, for the zero of the function,
        // otherwise set this bound to zero
        double sum2, parl = 0;
        if (rank == solvedCols) {
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                work1[pj] *= diag[pj] / dxNorm;
            }
            sum2 = 0;
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                double sum = 0;
                for (int i = 0, index = pj; i < j; ++i, index += m_cols) {
                    sum += m_jacobian[index] * work1[permutation[i]];
                }
                double s = (work1[pj] - sum) / diagR[pj];
                work1[pj] = s;
                sum2 += s * s;
            }
            parl = fp / (delta * sum2);
        }

        // calculate an upper bound, paru, for the zero of the function
        sum2 = 0;
        for (int j = 0; j < solvedCols; ++j) {
            int pj = permutation[j];
            double sum = 0;
            for (int i = 0, index = pj; i <= j; ++i, index += m_cols) {
                sum += m_jacobian[index] * qy[i];
            }
            sum /= diag[pj];
            sum2 += sum * sum;
        }
        double gNorm = Math.sqrt(sum2);
        double paru = gNorm / delta;
        if (paru == 0) // 2.2251e-308 is the smallest positive real for IEE754
        {
            paru = 2.2251e-308 / Math.min(delta, 0.1);
        }

	// if the input par lies outside of the interval (parl,paru),
        // set par to the closer endpoint
        lmPar = Math.min(paru, Math.max(lmPar, parl));
        if (lmPar == 0) {
            lmPar = gNorm / dxNorm;
        }

        for (int countdown = 10; countdown >= 0; --countdown) {

            // evaluate the function at the current value of lmPar
            if (lmPar == 0) {
                lmPar = Math.max(2.2251e-308, 0.001 * paru);
            }
            double sPar = Math.sqrt(lmPar);
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                work1[pj] = sPar * diag[pj];
            }
            determineLMDirection(qy, work1, work2, work3);

            dxNorm = 0;
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                double s = diag[pj] * lmDir[pj];
                work3[pj] = s;
                dxNorm += s * s;
            }
            dxNorm = Math.sqrt(dxNorm);
            double previousFP = fp;
            fp = dxNorm - delta;

	    // if the function is small enough, accept the current value
            // of lmPar, also test for the exceptional cases where parl is zero
            if ((Math.abs(fp) <= 0.1 * delta)
                    || ((parl == 0) && (fp <= previousFP) && (previousFP < 0))) {
                return;
            }

            // compute the Newton correction
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                work1[pj] = work3[pj] * diag[pj] / dxNorm;
            }
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                work1[pj] /= work2[j];
                double tmp = work1[pj];
                for (int i = j + 1; i < solvedCols; ++i) {
                    work1[permutation[i]] -= m_jacobian[i * m_cols + pj] * tmp;
                }
            }
            sum2 = 0;
            for (int j = 0; j < solvedCols; ++j) {
                double s = work1[permutation[j]];
                sum2 += s * s;
            }
            double correction = fp / (delta * sum2);

            // depending on the sign of the function, update parl or paru.
            if (fp > 0) {
                parl = Math.max(parl, lmPar);
            } else if (fp < 0) {
                paru = Math.min(paru, lmPar);
            }

            // compute an improved estimate for lmPar
            lmPar = Math.max(parl, lmPar + correction);

        }
    }

    /**
     * Solve an estimation problem using the Levenberg-Marquardt algorithm.
     * <p>
     * The algorithm used is a modified Levenberg-Marquardt one, based on the
     * MINPACK <a href="http://www.netlib.org/minpack/lmder.f">lmder</a>
     * routine. The algorithm settings must have been set up before this method
     * is called with the {@link #setInitialStepBoundFactor},
     * {@link #setMaxCostEval}, {@link #setCostRelativeTolerance},
     * {@link #setParRelativeTolerance} and {@link #setOrthoTolerance} methods.
     * If these methods have not been called, the default values set up by the
     * {@link #LevenbergMarquardtEstimator() constructor} will be used.
     * </p>
     * <p>
     * The authors of the original fortran function are:
     * </p>
     * <ul>
     * <li>Argonne National Laboratory. MINPACK project. March 1980</li>
     * <li>Burton S. Garbow</li>
     * <li>Kenneth E. Hillstrom</li>
     * <li>Jorge J. More</li>
     * </ul>
     * <p>
     * Luc Maisonobe did the Java translation.
     * </p>
     *
     * @param problem estimation problem to solve
     * @see #setInitialStepBoundFactor
     * @see #setCostRelativeTolerance
     * @see #setParRelativeTolerance
     * @see #setOrthoTolerance
     */
    @Override
    public void estimate(IEstimationProblem problem) {

        initializeEstimate(problem);

        // arrays shared with the other private methods
        solvedCols = Math.min(m_rows, m_cols);
        diagR = new double[m_cols];
        jacNorm = new double[m_cols];
        beta = new double[m_cols];
        permutation = new int[m_cols];
        lmDir = new double[m_cols];

        // local variables
        double delta = 0, xNorm = 0;
        double[] diag = new double[m_cols];
        double[] oldX = new double[m_cols];
        double[] oldRes = new double[m_rows];
        double[] work1 = new double[m_cols];
        double[] work2 = new double[m_cols];
        double[] work3 = new double[m_cols];

        // evaluate the function at the starting point and calculate its norm
        updateResidualsAndCost(problem);

        // outer loop
        lmPar = 0;
        boolean firstIteration = true;
        iterCount = 0;
        while (iterCount++ < maxIter) {

            // compute the Q.R. decomposition of the jacobian matrix
            updateJacobian(problem);
            qrDecomposition();

            // compute Qt.res
            qTy(m_residuals);

	    // now we don't need Q anymore,
            // so let jacobian contain the R matrix with its diagonal elements
            for (int k = 0; k < solvedCols; ++k) {
                int pk = permutation[k];
                m_jacobian[k * m_cols + pk] = diagR[pk];
            }

            if (firstIteration) {

		// scale the variables according to the norms of the columns
                // of the initial jacobian
                xNorm = 0;
                for (int k = 0; k < m_cols; ++k) {
                    double dk = jacNorm[k];
                    if (dk == 0) {
                        dk = 1.0;
                    }
                    double xk = dk * problem.getUnboundParameterEstimate(k);
                    xNorm += xk * xk;
                    diag[k] = dk;
                }
                xNorm = Math.sqrt(xNorm);

                // initialize the step bound delta
                delta = (xNorm == 0) ? m_initialStepBoundFactor
                        : (m_initialStepBoundFactor * xNorm);

            }

            // check orthogonality between function vector and jacobian columns
            double maxCosine = 0;
            if (m_cost != 0) {
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    double s = jacNorm[pj];
                    if (s != 0) {
                        double sum = 0;
                        for (int i = 0, index = pj; i <= j; ++i, index += m_cols) {
                            sum += m_jacobian[index] * m_residuals[i];
                        }
                        maxCosine = Math.max(maxCosine, Math.abs(sum)
                                / (s * m_cost));
                    }
                }
            }
            if (maxCosine <= m_orthoTolerance) {
                return;
            }

            // rescale if necessary
            for (int j = 0; j < m_cols; ++j) {
                diag[j] = Math.max(diag[j], jacNorm[j]);
            }

            // inner loop
            for (double ratio = 0; ratio < 1.0e-4;) {

                // save the state
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    oldX[pj] = problem.getUnboundParameterEstimate(pj);
                }
                double previousCost = m_cost;
                double[] tmpVec = m_residuals;
                m_residuals = oldRes;
                oldRes = tmpVec;

                // determine the Levenberg-Marquardt parameter
                determineLMParameter(oldRes, delta, diag, work1, work2, work3);

                // compute the new point and the norm of the evolution direction
                double lmNorm = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    lmDir[pj] = -lmDir[pj];
                    problem.setUnboundParameterEstimate(pj, oldX[pj]
                            + lmDir[pj]);
                    // m_parameters[pj].Estimate=oldX[pj] + lmDir[pj];
                    double s = diag[pj] * lmDir[pj];
                    lmNorm += s * s;
                }
                lmNorm = Math.sqrt(lmNorm);

                // on the first iteration, adjust the initial step bound.
                if (firstIteration) {
                    delta = Math.min(delta, lmNorm);
                }

                // evaluate the function at x + p and calculate its norm
                updateResidualsAndCost(problem);

                // compute the scaled actual reduction
                double actRed = -1.0;
                if (0.1 * m_cost < previousCost) {
                    double r = m_cost / previousCost;
                    actRed = 1.0 - r * r;
                }

		// compute the scaled predicted reduction
                // and the scaled directional derivative
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    double dirJ = lmDir[pj];
                    work1[j] = 0;
                    for (int i = 0, index = pj; i <= j; ++i, index += m_cols) {
                        work1[i] += m_jacobian[index] * dirJ;
                    }
                }
                double coeff1 = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    coeff1 += work1[j] * work1[j];
                }
                double pc2 = previousCost * previousCost;
                coeff1 = coeff1 / pc2;
                double coeff2 = lmPar * lmNorm * lmNorm / pc2;
                double preRed = coeff1 + 2 * coeff2;
                double dirDer = -(coeff1 + coeff2);

                // ratio of the actual to the predicted reduction
                ratio = (preRed == 0) ? 0 : (actRed / preRed);

                // update the step bound
                if (ratio <= 0.25) {
                    double tmp = (actRed < 0) ? (0.5 * dirDer / (dirDer + 0.5 * actRed))
                            : 0.5;
                    if ((0.1 * m_cost >= previousCost) || (tmp < 0.1)) {
                        tmp = 0.1;
                    }
                    delta = tmp * Math.min(delta, 10.0 * lmNorm);
                    lmPar /= tmp;
                } else if ((lmPar == 0) || (ratio >= 0.75)) {
                    delta = 2 * lmNorm;
                    lmPar *= 0.5;
                }

                // test for successful iteration.
                if (ratio >= 1.0e-4) {
                    // successful iteration, update the norm
                    firstIteration = false;
                    xNorm = 0;
                    for (int k = 0; k < m_cols; ++k) {
                        double xK = diag[k]
                                * problem.getUnboundParameterEstimate(k);
                        // double xK = diag[k] * m_parameters[k].Estimate;
                        xNorm += xK * xK;
                    }
                    xNorm = Math.sqrt(xNorm);
                    if (hook_ != null) {
                        hook_.hook(problem, true);
                    }
                } else {
                    // failed iteration, reset the previous values
                    m_cost = previousCost;
                    for (int j = 0; j < solvedCols; ++j) {
                        int pj = permutation[j];
                        problem.setUnboundParameterEstimate(pj, oldX[pj]);
                        // m_parameters[pj].Estimate=oldX[pj];
                    }
                    tmpVec = m_residuals;
                    m_residuals = oldRes;
                    oldRes = tmpVec;
                    if (hook_ != null) {
                        hook_.hook(problem, false);
                    }
                }

                // tests for convergence.
                if (((Math.abs(actRed) <= m_costRelativeTolerance)
                        && (preRed <= m_costRelativeTolerance) && (ratio <= 2.0))
                        || (delta <= m_parRelativeTolerance * xNorm)) {
                    return;
                }

		// tests for termination and stringent tolerances
                // (2.2204e-16 is the machine epsilon for IEEE754)
                if ((Math.abs(actRed) <= 2.2204e-16) && (preRed <= 2.2204e-16)
                        && (ratio <= 2.0)) {
                    throw new FunctionException(
                            "cost relative tolerance is too small", "MinPack");
                } else if (delta <= 2.2204e-16 * xNorm) {
                    throw new FunctionException(
                            "parameters relative tolerance is too small",
                            "MinPack");
                } else if (maxCosine <= 2.2204e-16) {
                    throw new FunctionException(
                            "orthogonality tolerance is too small", "MinPack");
                }

            }

        }

    }

    /**
     * Set the desired relative error in the sum of squares.
     *
     * @return
     * @see #estimate
     */
    public double getCostRelativeTolerance() {
        return m_costRelativeTolerance;
    }

    /**
     * Set the positive input variable used in determining the initial step
     * bound. This bound is set to the product of initialStepBoundFactor and the
     * euclidean norm of diag*x if nonzero, or else to initialStepBoundFactor
     * itself. In most cases factor should lie in the interval (0.1, 100.0).
     * 100.0 is a generally recommended value
     *
     * @return
     * @see #estimate
     */
    public double getInitialStepBoundFactor() {
        return m_initialStepBoundFactor;
    }

    /**
     * @return the iterCount
     */
    public int getIterCount() {
        return iterCount;
    }

    /**
     * @return the maxIter
     */
    public int getMaxIter() {
        return maxIter;
    }

    /**
     * Set the desired Max cosine on the orthogonality.
     *
     * @return
     * @see #estimate
     */
    public double getOrthogonalTolerance() {
        return m_orthoTolerance;
    }

    /**
     * Set the desired relative error in the approximate solution parameters.
     *
     * @return
     * @see #estimate
     */
    public double getParametersRelativeTolerance() {
        return m_parRelativeTolerance;
    }

    /**
     * Decompose a matrix A as A.P = Q.R using Householder transforms.
     * <p>
     * As suggested in the P. Lascaux and R. Theodor book <i>Analyse
     * num&eacute;rique matricielle appliqu&eacute;e &agrave; l'art de
     * l'ing&eacute;nieur</i> (Masson, 1986), instead of representing the
     * Householder transforms with u<sub>k</sub> unit vectors such that:
     *
     * <pre>
     * H&lt;sub&gt;k&lt;/sub&gt; = I - 2u&lt;sub&gt;k&lt;/sub&gt;.u&lt;sub&gt;k&lt;/sub&gt;&lt;sup&gt;t&lt;/sup&gt;
     * </pre>
     *
     * we use <sub>k</sub> non-unit vectors such that:
     *
     * <pre>
     * H&lt;sub&gt;k&lt;/sub&gt; = I - beta&lt;sub&gt;k&lt;/sub&gt;v&lt;sub&gt;k&lt;/sub&gt;.v&lt;sub&gt;k&lt;/sub&gt;&lt;sup&gt;t&lt;/sup&gt;
     * </pre>
     *
     * where v<sub>k</sub> = a<sub>k</sub> - alpha<sub>k</sub> e<sub>k</sub>.
     * The beta<sub>k</sub> coefficients are provided upon exit as recomputing
     * them from the v<sub>k</sub> vectors would be costly.
     * </p>
     * <p>
     * This decomposition handles rank deficient cases since the tranformations
     * are performed in non-increasing columns norms order thanks to columns
     * pivoting. The diagonal elements of the R matrix are therefore also in
     * non-increasing absolute values order.
     * </p>
     */
    private void qrDecomposition() {

        // initializations
        for (int k = 0; k < m_cols; ++k) {
            permutation[k] = k;
            double norm2 = 0;
            for (int index = k; index < m_jacobian.length; index += m_cols) {
                double akk = m_jacobian[index];
                norm2 += akk * akk;
            }
            jacNorm[k] = Math.sqrt(norm2);
        }

        // transform the matrix column after column
        for (int k = 0; k < m_cols; ++k) {

            // select the column with the greatest norm on active components
            int nextColumn = -1;
            double ak2 = Double.NEGATIVE_INFINITY;
            for (int i = k; i < m_cols; ++i) {
                double norm2 = 0;
                int iDiag = k * m_cols + permutation[i];
                for (int index = iDiag; index < m_jacobian.length; index += m_cols) {
                    double aki = m_jacobian[index];
                    norm2 += aki * aki;
                }
                if (norm2 > ak2) {
                    nextColumn = i;
                    ak2 = norm2;
                }
            }
            if (ak2 == 0) {
                rank = k;
                return;
            }
            int pk = permutation[nextColumn];
            permutation[nextColumn] = permutation[k];
            permutation[k] = pk;

            // choose alpha such that Hk.u = alpha ek
            int kDiag = k * m_cols + pk;
            double akk = m_jacobian[kDiag];
            double alpha = (akk > 0) ? -Math.sqrt(ak2) : Math.sqrt(ak2);
            double betak = 1.0 / (ak2 - akk * alpha);
            beta[pk] = betak;

            // transform the current column
            diagR[pk] = alpha;
            m_jacobian[kDiag] -= alpha;

            // transform the remaining columns
            for (int dk = m_cols - 1 - k; dk > 0; --dk) {
                int dkp = permutation[k + dk] - pk;
                double gamma = 0;
                for (int index = kDiag; index < m_jacobian.length; index += m_cols) {
                    gamma += m_jacobian[index] * m_jacobian[index + dkp];
                }
                gamma *= betak;
                for (int index = kDiag; index < m_jacobian.length; index += m_cols) {
                    m_jacobian[index + dkp] -= gamma * m_jacobian[index];
                }
            }

        }

        rank = solvedCols;

    }

    /**
     * Compute the product Qt.y for some Q.R. decomposition.
     *
     * @param y vector to multiply (will be overwritten with the result)
     */
    private void qTy(double[] y) {
        for (int k = 0; k < m_cols; ++k) {
            int pk = permutation[k];
            int kDiag = k * m_cols + pk;
            double gamma = 0;
            for (int i = k, index = kDiag; i < m_rows; ++i, index += m_cols) {
                gamma += m_jacobian[index] * y[i];
            }
            gamma *= beta[pk];
            for (int i = k, index = kDiag; i < m_rows; ++i, index += m_cols) {
                y[i] -= gamma * m_jacobian[index];
            }
        }
    }

    /**
     *
     * @param value
     */
    public void setCostRelativeTolerance(double value) {
        m_costRelativeTolerance = value;
    }

    /**
     *
     * @param value
     */
    public void setInitialStepBoundFactor(double value) {
        m_initialStepBoundFactor = value;
    }

    /**
     * @param maxIter the maxIter to set
     */
    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }

    /**
     *
     * @param value
     */
    public void setOrthogonalTolerance(double value) {
        m_orthoTolerance = value;
    }

    /**
     *
     * @param value
     */
    public void setParametersRelativeTolerance(double value) {
        m_parRelativeTolerance = value;
    }

}
