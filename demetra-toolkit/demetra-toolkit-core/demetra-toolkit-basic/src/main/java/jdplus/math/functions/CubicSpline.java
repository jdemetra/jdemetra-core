/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.functions;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CubicSpline {

    /**
     * see Burden and Faires (1997), algorithm 3:4.
     *
     * @param xi set of points
     * @param fxi values at xi
     * @return The corresponding natural cubic spline
     */
    public DoubleUnaryOperator of(double[] xi, double[] fxi) {
        return new CubicSplineFunction(xi, fxi);
    }

    static class CubicSplineFunction implements DoubleUnaryOperator {

        final int n;
        final double[] xi;
        final double[] a, b, c, d;

        CubicSplineFunction(final double[] xi, final double[] fxi) {
            this.xi = xi.clone();
            this.a = fxi.clone();
            // compute the polynomials
            n = xi.length - 1;
            b = new double[n];
            c = new double[n+1];
            d = new double[n];
            double[] h = new double[n];

            for (int i = 0; i < n; ++i) {
                h[i] = xi[i + 1] - xi[i];
            }

            double[] m = new double[n], z = new double[n];
            for (int i = 1; i < n; ++i) {
                double l=2*(xi[i+1]-xi[i-1])-h[i-1]*m[i-1];
                m[i]=h[i]/l;
                double alpha = 3 / h[i] * (a[i + 1] - a[i]) - 3 / h[i - 1] * (a[i] - a[i - 1]);
                z[i]=(alpha-h[i-1]*z[i-1])/l;
            }
            // STEP 5
            for (int i=n-1; i>=0; --i){
                c[i]=z[i]-m[i]*c[i+1];
                b[i]=(a[i+1]-a[i])/h[i]-h[i]*(c[i+1]+2*c[i])/3;
                d[i]=(c[i+1]-c[i])/(3*h[i]);
            }
        }

        private int find(double x) {
            if (x <= xi[0])
                return -1;
            else if (x >= xi[n])
                return n;
            int pos=Arrays.binarySearch(xi, x);
            if (pos >= 0)
                return pos;
            else
                return -pos-2;
        }

        private double compute(double x, int p) {
            double dx = x - xi[p], dx2 = dx * dx, dx3 = dx2 * dx;
            return a[p] + b[p] * dx + c[p] * dx2 + d[p] * dx3;
        }

        private double compute0(double x) {
            double df = b[0];
            return a[0] + (x - xi[0]) * df;
        }

        private double computen(double x) {
            double dx = xi[n] - xi[n - 1], dx2 = dx * dx;
            double df = b[n - 1] + 2 * c[n - 1] * dx + 3 * d[n - 1] * dx2;
            return a[n] + (x - xi[n]) * df;
        }

        @Override
        public double applyAsDouble(double value) {
            int pos = find(value);
            if (pos < 0) {
                return compute0(value);
            } else if (pos >= n) {
                return computen(value);
            } else {
                return compute(value, pos);
            }
        }

    }
}
