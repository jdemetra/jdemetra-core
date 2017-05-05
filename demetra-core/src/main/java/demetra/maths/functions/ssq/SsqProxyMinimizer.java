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
package demetra.maths.functions.ssq;

import demetra.design.Development;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqProxyMinimizer implements ISsqFunctionMinimizer {
    
    private final IFunctionMinimizer minimizer;

    /**
     *
     * @param min
     */
    public SsqProxyMinimizer(IFunctionMinimizer min) {
        minimizer = min;
    }
    
    @Override
    public ISsqFunctionMinimizer exemplar() {
        return new SsqProxyMinimizer(minimizer.exemplar());
    }

    /**
     *
     * @return
     */
    @Override
    public double getConvergenceCriterion() {
        return minimizer.getConvergenceCriterion();
    }

    /**
     *
     * @return
     */
    @Override
    public Matrix getCurvature() {
        return minimizer.getCurvature();
    }

    /**
     *
     * @return
     */
    @Override
    public Doubles getGradient() {
        return minimizer.getGradient();
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return minimizer.getIterCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return minimizer.getMaxIter();
    }
    
    @Override
    public ISsqFunctionPoint getResult() {
        SsqProxyFunctionPoint rslt = (SsqProxyFunctionPoint) minimizer
                .getResult();
        return rslt.fx;
    }
    
    @Override
    public double getObjective() {
        return minimizer.getObjective();
    }

    /**
     *
     * @param function
     * @param start
     * @return
     */
    @Override
    public boolean minimize(ISsqFunctionPoint start) {
        return minimizer.minimize(new SsqProxyFunctionPoint(start));
    }

    /**
     *
     * @param value
     */
    @Override
    public void setConvergenceCriterion(double value) {
        minimizer.setConvergenceCriterion(value);
    }

    /**
     *
     * @param n
     */
    @Override
    public void setMaxIter(int n) {
        minimizer.setMaxIter(n);
    }
    
    @Override
    public double getPrecision() {
        return minimizer.getPrecision();
    }
    
    @Override
    public void setPrecision(double value) {
        minimizer.setPrecsion(value);
    }
}
