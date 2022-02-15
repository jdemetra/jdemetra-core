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
package internal.jdplus.math.functions.gsl.roots;

import java.util.function.DoubleUnaryOperator;

/**
 * The secant algorithm is a variant of the Newton algorithm with the
 * derivative term replaced by a numerical estimate from the last two
 * function evaluations.
 *
 * x[i+1] = x[i] - f(x[i]) / f'_est
 *
 * where f'_est = (f(x[i]) - f(x[i-1])) / (x[i] - x[i-1])
 *
 * The exact derivative is used for the initial value of f'_est.
 *
 * @author Mats Maggi
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fdfsolver_secant
 */
public class SecantSolver extends FDFSolver {

    public SecantSolver(DoubleUnaryOperator fn, DoubleUnaryOperator dfn, double root) {
        this.function = fn;
        this.functionDf = dfn;
        this.root = root;
        this.f = fn.applyAsDouble(root);
        this.df = dfn.applyAsDouble(root);
    }

    @Override
    public void iterate() {
        double x = root;
        double f = this.f;
        double df = this.df;
        double x_new, f_new, df_new;
        
        if (f==0.0) {
            return;
        }
        
        if (df == 0.0) {
            throw new GslRootException("Derivative is zero");
        }
        
        x_new = x - (f / df);
        f_new = function.applyAsDouble(x_new);
        df_new = df * ((f - f_new) / f);
        
        root = x_new;
        
        this.f = f_new;
        this.df = df_new;

        if (!Double.isFinite(f_new)) {
            throw new GslRootException("Function value is not finite");
        }

        if (!Double.isFinite(df_new)) {
            throw new GslRootException("Derivative value is not finite");
        }
    }
}
