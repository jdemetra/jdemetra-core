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
package jdplus.math.functions.minpack;

import jdplus.data.DataBlock;
import nbbrd.design.Development;
import jdplus.math.functions.FunctionException;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.SymmetricMatrix;

/**
 * Base class for implementing estimators. This base class handles the
 * boilerplates methods associated to thresholds settings, jacobian and error
 * estimation.
 *
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractEstimator implements IEstimator {

    /**
     * Array of measurements.
     */
    // protected WeightedMeasurement[] m_measurements;
    /**
     * Array of parameters.
     */
    // protected EstimatedParameter[] m_parameters;
    /**
     * Jacobian matrix.
     * <p>
     * This matrix is in canonical form just after the calls to
     * {@link #updateJacobian()}, but may be modified by the solver in the
     * derived class (the {@link LevenbergMarquardtEstimator Levenberg-Marquardt
     * estimator} does this).
     * </p>
     */
    protected double[] m_jacobian;
    /**
     * Number of columns of the jacobian matrix.
     */
    protected int m_cols;
    /**
     * Number of rows of the jacobian matrix.
     */
    protected int m_rows;
    /**
     * Residuals array.
     * <p>
     * This array is in canonical form just after the calls to
     * {@link #updateJacobian()}, but may be modified by the solver in the
     * derived class (the {@link LevenbergMarquardtEstimator Levenberg-Marquardt
     * estimator} does this).
     * </p>
     */
    protected double[] m_residuals;
    /**
     * Cost value (square root of the sum of the residuals).
     */
    protected double m_cost;
    /**
     * Maximal allowed number of cost evaluations.
     */
    private int m_maxCostEval;
    /**
     * Number of cost evaluations.
     */
    private int m_costEvaluations;
    /**
     * Number of jacobian evaluations.
     */
    private int m_jacobianEvaluations;

    /**
     * Build an abstract estimator for least squares problems.
     */
    protected AbstractEstimator() {
    }

    /**
     * Get the Chi-Square value.
     *
     * @param problem estimation problem
     * @return chi-square value
     */
    public double chiSquare(IEstimationProblem problem) {
        /*
         * WeightedMeasurement[] wm = problem.Measurements; double chiSquare =
         * 0; for (int i = 0; i < wm.Length; ++i) { double residual =
         * wm[i].Residual; chiSquare += residual * residual / wm[i].Weight; }
         * return chiSquare;
         */
        int n = problem.getMeasurementsCount();
        double chi = 0;
        for (int i = 0; i < n; ++i) {
            double residual = problem.getResidual(i);
            chi += residual * residual / problem.getMeasurementWheight(i);
        }
        return chi;
    }

    @Override
    public Matrix covariance(IEstimationProblem problem) {
        Matrix cur = curvature(problem);
        try {
            return SymmetricMatrix.inverse(cur);
        } catch (MatrixException ex) {
            return null;
        }
    }

    /**
     * Get the covariance matrix of estimated parameters.
     *
     * @param problem estimation problem
     * @return covariance matrix
     */
    @Override
    public Matrix curvature(IEstimationProblem problem) {

        // set up the jacobian
        updateJacobian(problem);

        // compute transpose(J).J, avoiding building big intermediate matrices
        /*
         * int rows = problem.Measurements.Length; int cols =
         * problem.AllParameters.Length; int Max = cols * rows; Matrix jTj = new
         * Matrix(cols, cols); for (int i = 0; i < cols; ++i) { for (int j = i;
         * j < cols; ++j) { double sum = 0; for (int k = 0; k < Max; k += cols)
         * { sum += m_jacobian[k + i] * m_jacobian[k + j]; } jTj[i, j] = sum;
         * jTj[j, i] = sum; }
         */
        int rows = problem.getMeasurementsCount();
        int cols = problem.getParametersCount();
        int Max = cols * rows;
        Matrix jTj = Matrix.square(cols);
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                double sum = 0;
                for (int k = 0; k < Max; k += cols) {
                    sum += m_jacobian[k + i] * m_jacobian[k + j];
                }
                jTj.set(i, j, sum);
                jTj.set(j, i, sum);
            }
        }

        return jTj;
    }

    /**
     * Solve an estimation problem.
     *
     * <p>
     * The method should set the parameters of the problem to several trial
     * values until it reaches convergence. If this method returns normally
     * (i.e. without throwing an exception), then the best estimate of the
     * parameters can be retrieved from the problem itself, through the      {@link EstimationProblem#getAllParameters
     * EstimationProblem.getAllParameters} method.
     * </p>
     *
     * @param problem estimation problem to solve
     *
     */
    @Override
    public abstract void estimate(IEstimationProblem problem);

    /**
     * Get the number of cost evaluations.
     *
     * @return number of cost evaluations
     *
     */
    public int getCostEvaluations() {
        return m_costEvaluations;
    }

    /**
     * Get the number of jacobian evaluations.
     *
     * @return number of jacobian evaluations
     *
     */
    public int getJacobianEvaluations() {
        return m_jacobianEvaluations;
    }

    /**
     * Set the maximal number of cost evaluations allowed.
     *
     * @return
     * @see #estimate
     */
    public int getMaxCostEval() {
        return m_maxCostEval;
    }

    /**
     * Guess the errors in estimated parameters.
     * <p>
     * Guessing is covariance-based, it only gives rough order of magnitude.
     * </p>
     *
     * @param problem estimation problem
     * @return errors in estimated parameters
     */
    @Override
    public double[] guessParametersErrors(IEstimationProblem problem) {
        /*
         * int m = problem.Measurements.Length; int p =
         * problem.AllParameters.Length; if (m <= p) { throw new
         * FunctionException("no degrees of freedom"); } double[] errors = new
         * double[problem.AllParameters.Length]; double c =
         * Math.Sqrt(ChiSquare(problem) / (m - p)); Matrix covar =
         * Covariance(problem); RC diag = covar.Diagonal(); for (int i = 0; i <
         * errors.Length; ++i) { errors[i] = Math.Sqrt(diag[i]) * c; } return
         * errors;
         */
        int m = problem.getMeasurementsCount();
        int p = problem.getParametersCount();
        if (m <= p) {
            throw new FunctionException("no degrees of freedom");
        }
        double[] errors = new double[p];
        double c = Math.sqrt(chiSquare(problem) / (m - p));
        Matrix covar = covariance(problem);
        DataBlock diag = covar.diagonal();
        for (int i = 0; i < errors.length; ++i) {
            errors[i] = Math.sqrt(diag.get(i)) * c;
        }
        return errors;
    }

    /**
     * Increment the jacobian evaluations counter.
     */
    protected void incrementJacobianEvaluationsCounter() {
        ++m_jacobianEvaluations;
    }

    /**
     * Initialization of the common parts of the estimation.
     * <p>
     * This method <em>must</em> be called at the start of the
     * {@link #estimate(EstimationProblem) estimate} method.
     * </p>
     *
     * @param problem estimation problem to solve
     */
    protected void initializeEstimate(IEstimationProblem problem) {

        // reset counters
        m_costEvaluations = 0;
        m_jacobianEvaluations = 0;

        // retrieve the equations and the parameters
        /*
         * m_measurements = problem.Measurements; m_parameters =
         * problem.UnboundParameters;
         *
         * // arrays shared with the other private methods m_rows =
         * m_measurements.Length; m_cols = m_parameters.Length; m_jacobian = new
         * double[m_rows * m_cols]; m_residuals = new double[m_rows];
         *
         * m_cost = Double.PositiveInfinity;
         */
        // arrays shared with the other private methods
        m_rows = problem.getMeasurementsCount();
        m_cols = problem.getUnboundParametersCount();
        m_jacobian = new double[m_rows * m_cols];
        m_residuals = new double[m_rows];

        m_cost = Double.POSITIVE_INFINITY;

    }

    /**
     * Get the Root Mean Square value. Get the Root Mean Square value, i.e. the
     * root of the arithmetic mean of the square of all weighted residuals. This
     * is related to the criterion that is minimized by the estimator as
     * follows: if <em>c</em> if the criterion, and <em>n</em> is the number of
     * measurements, then the RMS is <em>Sqrt (c/n)</em>.
     *
     * @param problem estimation problem
     * @return RMS value
     */
    @Override
    public double rms(IEstimationProblem problem) {
        /*
         * WeightedMeasurement[] wm = problem.Measurements; double criterion =
         * 0; for (int i = 0; i < wm.Length; ++i) { double residual =
         * wm[i].Residual; criterion += wm[i].Weight * residual * residual; }
         * return Math.Sqrt(criterion / wm.Length);
         */

        int n = problem.getMeasurementsCount();
        double criterion = 0;
        for (int i = 0; i < n; ++i) {
            double residual = problem.getResidual(i);
            criterion += problem.getMeasurementWheight(i) * residual * residual;
        }
        return Math.sqrt(criterion / n);
    }

    /**
     *
     * @param value
     */
    public void setMaxCostEval(int value) {
        this.m_maxCostEval = value;
    }

    /**
     * Update the jacobian matrix.
     */
    protected void updateJacobian(IEstimationProblem problem) {
        incrementJacobianEvaluationsCounter();
        for (int i = 0; i < m_jacobian.length; ++i) {
            m_jacobian[i] = 0;
        }
        for (int i = 0, index = 0; i < m_rows; i++) {
            double factor = -Math.sqrt(problem.getMeasurementWheight(i));
            for (int j = 0; j < m_cols; ++j) {
                m_jacobian[index++] = factor
                        * problem.getMeasurementParialDerivative(i, j);
            }
        }
    }

    /**
     * Update the residuals array and cost function value.
     *
     * @param problem
     * @return
     */
    protected boolean updateResidualsAndCost(IEstimationProblem problem) {
//        if (++m_costEvaluations > m_maxCostEval) {
//            throw new FunctionException("MinPack: "+FunctionException.MAXITER_ERR);
//        }
        if (!problem.compute()) {
            return false;
        }

        m_cost = 0;
        for (int i = 0; i < m_rows; i++) {
            double residual = problem.getResidual(i);
            double weight = problem.getMeasurementWheight(i);
            m_residuals[i] = Math.sqrt(weight) * residual;
            m_cost += weight * residual * residual;
        }
        m_cost = Math.sqrt(m_cost);
        return true;
    }
}
