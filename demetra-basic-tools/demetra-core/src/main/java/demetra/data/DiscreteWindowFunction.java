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
package demetra.data;

import demetra.design.IntValue;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
public enum DiscreteWindowFunction {

    Square,
    Welch,
    Tukey,
    Bartlett,
    Hamming,
    Parzen;

    /**
     * Returns the normalized window function, defined on [-1, 1].
     * We must have that f(-1)=f(1)=0
     * The window function is even, so that f(-x)=f(x)3
     * It should be noted that the functions don't check the validity of the input
     *
     * @return
     */
    public DoubleUnaryOperator window() {
        switch (this) {
            case Welch:
                return x -> 1.0 - x * x;
            case Tukey:
                return x -> 0.5 * (1 + Math.cos(Math.PI * x));
            case Bartlett:
                return x -> x < 0 ? 1 + x : 1 - x;
            case Hamming:
                return x -> 0.54 + 0.46 * Math.cos(Math.PI * x);
            case Parzen:
                return x -> {
                    double x1 = x < 0 ? -x : x;
                    if (x <= .5) {
                        double x2 = x1 * x1, x3 = x1 * x2;
                        return 1.0 - 6.0 * x2 + 6.0 * x3;
                    } else {
                        double y = 1 - x1;
                        return 2.0 * y * y * y;
                    }
                };
            case Square:
                return x -> 1;
        }
        return null;
    }

    /**
     * Computes w[i]=f(i/m)
     *
     * @param m The length of the half-window. 
     * @return The returned array contains exactly m elements
     * they correspond to wnd(0)... f(m-1/m)
     */
    public double[] discreteWindow(int m) {
        double[] win = new double[m];
        double dlen = m;
        DoubleUnaryOperator fn = window();
        for (int i = 0; i < m; ++i) {
            win[i] = fn.applyAsDouble(i / dlen);
        }
        return win;
    }

    /**
     *
     * @param fn The input series, which
     * @param N The window length. Should be odd
     *
     * @return computes sum(fn(i)*w(i)).
     */
    public double compute(IntToDoubleFunction fn, int N) {
        if (N % 2 == 0) {
            throw new IllegalArgumentException("Window length should be odd");
        }
        int win2 = N / 2;
        double dlen = win2;
        DoubleUnaryOperator wfn = window();

        double v = fn.applyAsDouble(0) * wfn.applyAsDouble(0);
        for (int i = 1; i < win2; ++i) {
            v += wfn.applyAsDouble(i / dlen) * (fn.applyAsDouble(i) + fn.applyAsDouble(-i));
        }
        return v;
    }

}
