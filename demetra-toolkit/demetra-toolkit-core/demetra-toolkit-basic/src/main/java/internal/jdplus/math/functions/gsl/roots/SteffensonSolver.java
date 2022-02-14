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
 * This is Newton's method with an Aitken "delta-squared"
 * acceleration of the iterates. This can improve the convergence on
 * multiple roots where the ordinary Newton algorithm is slow.
 *
 * x[i+1] = x[i] - f(x[i]) / f'(x[i])
 *
 * x_accelerated[i] = x[i] - (x[i+1] - x[i])**2 / (x[i+2] - 2*x[i+1] + x[i])
 *
 * We can only use the accelerated estimate after three iterations,
 * and use the unaccelerated value until then.
 *
 * @author Mats Maggi
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fdfsolver_steffenson
 */
public class SteffensonSolver extends FDFSolver {

    private double x;
    private double x1 = 0.0;
    private double x2 = 0.0;
    private int count = 1;

    public SteffensonSolver(DoubleUnaryOperator fn, DoubleUnaryOperator dfn, double root) {
        this.function = fn;
        this.functionDf = dfn;
        this.root = root;
        this.f = fn.applyAsDouble(root);
        this.df = dfn.applyAsDouble(root);
        this.x = root;
    }

    @Override
    public void iterate() {
        double x = this.x;
        double x1 = this.x1;
        double x_new, f_new, df_new;

        if (df == 0.0) {
            throw new GslRootException("Derivative is zero");
        }

        x_new = x - (f / df);
        f_new = function.applyAsDouble(x_new);
        df_new = functionDf.applyAsDouble(x_new);
        this.x2 = x1;
        this.x1 = x;
        this.x = x_new;

        this.f = f_new;
        this.df = df_new;

        if (!Double.isFinite(f_new)) {
            throw new GslRootException("Function value is not finite");
        }

        if (this.count < 3) {
            this.root = x_new;
            this.count++;
        } else {
            double u = (x - x1);
            double v = (x_new - 2 * x + x1);

            if (v == 0) {
                root = x_new; // avoid division by zero
            } else {
                root = x1 - u * u / v;  // accelerated value
            }
        }
    }
}
