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

import demetra.design.Development;
import jdplus.math.matrices.Matrix;

/**
 *
 */
@Development(status = Development.Status.Preliminary)
public interface IEstimator {

    // / <summary>
    // / Get the covariance matrix of estimated parameters.
    // / </summary>
    // / <param name="problem"></param>
    // / <returns></returns>
    /**
     * 
     * @param problem
     * @return
     */
    Matrix covariance(IEstimationProblem problem);

    /**
     * 
     * @param problem
     * @return
     */
    Matrix curvature(IEstimationProblem problem);

    // / <summary>
    // / Solve an estimation problem.
    // / </summary>
    // / <p>The method should set the parameters of the problem to several
    // / trial values until it reaches convergence. If this method returns
    // / normally (i.e. without throwing an exception), then the best
    // / estimate of the parameters can be retrieved from the problem
    // / itself, through the EstimationProblem.AllParameters property.</p>
    // / <param name="problem">estimation problem to solve</param>
    /**
     * 
     * @param problem
     */
    void estimate(IEstimationProblem problem);

    // / <summary>
    // / Guess the errors in estimated parameters.
    // / </summary>
    // / <param name="problem"></param>
    // / <returns></returns>
    /**
     * 
     * @param problem
     * @return
     */
    double[] guessParametersErrors(IEstimationProblem problem);

    // / <summary>
    // / Get the Root Mean Square value.
    // / Get the Root Mean Square value, i.e. the root of the arithmetic
    // / mean of the square of all weighted residuals. This is related to the
    // / criterion that is minimized by the estimator as follows: if
    // / <em>c</em> is the criterion, and <em>n</em> is the number of
    // / measurements, then the RMS is <em>Sqrt (c/n)</em>.
    // / </summary>
    // / <param name="problem"></param>
    // / <returns></returns>
    /**
     * 
     * @param problem
     * @return
     */
    double rms(IEstimationProblem problem);
}
