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

package demetra.maths.functions.minpack;

import demetra.design.Development;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MinPackMinimizer implements ISsqFunctionMinimizer {

    private LevenbergMarquardtEstimator m_estimator = new LevenbergMarquardtEstimator();

    private SsqEstimationProblem m_problem;
    
    @Override
    public ISsqFunctionMinimizer exemplar() {
	return new MinPackMinimizer();
    }

    /**
     * 
     * @return
     */
    @Override
    public double getFunctionPrecision() {
	return m_estimator.getCostRelativeTolerance();
    }

    /**
     * 
     * @return
     */
    @Override
    public Matrix curvatureAtMinimum() {
        try{
	return m_estimator.curvature(m_problem);
        }
        catch(Exception err){
            return null;
        }
    }
    
    @Override
    public DoubleSequence gradientAtMinimum(){
        return this. m_problem.gradient();
    }
            

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
	return m_estimator.getIterCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
	return m_estimator.getMaxIter();
    }

    @Override
    public ISsqFunctionPoint getResult() {
	return m_problem.getResult();
    }

     @Override
    public double getObjective() {
	return m_problem.getResult() == null ? Double.NaN : m_problem.getResult().getSsqE();
    }
 
    @Override
    public boolean minimize(ISsqFunctionPoint start) {
	m_problem = new SsqEstimationProblem(start);
	try {
	    m_estimator.estimate(m_problem);
	    return m_estimator.getIterCount() < m_estimator.getMaxIter();
	} catch (RuntimeException err) {
	    return false;
	}
    }

    @Override
    public void setFunctionPrecision(double value) {
	m_estimator.setCostRelativeTolerance(value);
    }

    /**
     *
     * @param n
     */
    @Override
    public void setMaxIter(int n) {
	m_estimator.setMaxIter(n);
    }

    @Override
    public double getParametersPrecision() {
 return m_estimator.getCostRelativeTolerance();   }

    @Override
    public void setParametersPrecision(double value) {
        m_estimator.setCostRelativeTolerance(value);
    }
}
