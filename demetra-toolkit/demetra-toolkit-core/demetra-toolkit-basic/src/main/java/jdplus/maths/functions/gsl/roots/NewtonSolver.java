/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.maths.functions.gsl.roots;

import java.util.function.DoubleUnaryOperator;

/**
 * Newton root finding algorithm.
 * This is the classical Newton-Raphson iteration.
 *
 * x[i+1] = x[i] - f(x[i])/f'(x[i])
 *
 * @author Mats Maggi
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fdfsolver_newton
 */
public class NewtonSolver extends FDFSolver {

    public NewtonSolver(DoubleUnaryOperator fn, DoubleUnaryOperator dfn, double root) {
        this.function = fn;
        this.functionDf = dfn;
        this.root = root;

        this.f = fn.applyAsDouble(root);
        this.df = dfn.applyAsDouble(root);
    }

    @Override
    public void iterate() {
        if (this.df == 0.0) {
            throw new GslRootException("Derivative is zero");
        }

        root = root - (f / df);

        this.f = function.applyAsDouble(root);
        this.df = functionDf.applyAsDouble(root);

        if (!Double.isFinite(f)) {
            throw new GslRootException("Function value is not finite");
        }
    }
}
