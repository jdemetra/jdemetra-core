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
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ProxyMinimizer implements IFunctionMinimizer {
    
    private final ISsqFunctionMinimizer minimizer;

    /**
     *
     * @param min
     */
    public ProxyMinimizer(ISsqFunctionMinimizer min) {
        minimizer = min;
    }
    
    public ISsqFunctionMinimizer getCore() {
        return minimizer;
    }
    
    @Override
    public IFunctionMinimizer exemplar() {
        return new ProxyMinimizer(minimizer.exemplar());
    }

    /**
     *
     * @return
     */
    @Override
    public double getFunctionPrecision() {
        return minimizer.getFunctionPrecision();
    }
    
    @Override
    public Matrix curvatureAtMinimum() {
        return minimizer.curvatureAtMinimum();
    }
    
    @Override
    public DoubleSequence gradientAtMinimum() {
        return minimizer.gradientAtMinimum();
    }
    
    @Override
    public double getObjective() {
        return minimizer.getObjective();
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
    public IFunctionPoint getResult() {
        return (IFunctionPoint) minimizer.getResult();
    }

    /**
     *
     * @param function
     * @param start
     * @return
     */
    @Override
    public boolean minimize(IFunction function) {
        if (! (function instanceof ISsqFunction))
            return false;
        ISsqFunction fn = (ISsqFunction) function;
        ISsqFunctionPoint s = fn.ssqEvaluate(function.getDomain().getDefault());
        return minimizer.minimize(s);
     }

    @Override
    public boolean minimize(IFunctionPoint start) {
        IFunction function = start.getFunction();
        if (! (function instanceof ISsqFunction))
            return false;
        ISsqFunction fn = (ISsqFunction) function;
        ISsqFunctionPoint s = fn.ssqEvaluate(start.getParameters());
        return minimizer.minimize(s);
     }
    /**
     *
     * @param value
     */
    @Override
    public void setFunctionPrecision(double value) {
        minimizer.setFunctionPrecision(value);
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
    public double getParametersPrecision() {
        return minimizer.getParametersPrecision();
    }
    
    @Override
    public void setParametersPrecsion(double value) {
        minimizer.setParametersPrecision(value);
    }
}
