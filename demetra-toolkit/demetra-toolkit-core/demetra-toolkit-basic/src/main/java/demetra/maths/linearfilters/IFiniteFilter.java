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
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.design.Development;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleVector;
import demetra.data.DoubleVectorCursor;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFiniteFilter extends IFilter, ILinearProcess {

    /**
     * Length of the filter
     *
     * @return
     */
    default int length() {
        return getUpperBound() - getLowerBound() + 1;
    }

    @Override
    default int getOutputLength(int inputLength) {
        return inputLength - getUpperBound() - getLowerBound();
    }

    // FiniteFilterDecomposition Decompose();
    /**
     * Lower bound of the filter (included)
     *
     * @return
     */
    int getLowerBound();

    /**
     * Upper bound of the filter (included)
     *
     * @return
     */
    int getUpperBound();

    /**
     * Weights of the filter; the function is defined for index ranging from the
     * lower bound to the upper bound (included)
     *
     * @return
     */
    IntToDoubleFunction weights();

    /**
     * Returns all the weights, from lbound to ubound
     *
     * @return
     */
    default double[] weightsToArray() {
        double[] w = new double[length()];
        IntToDoubleFunction weights = weights();
        for (int i = 0, j = getLowerBound(); i < w.length; ++i, ++j) {
            w[i] = weights.applyAsDouble(j);
        }
        return w;
    }

    /**
     * If this filter is w(l)B^(-l)+...+w(u)F^u Its mirror is
     * w(-u)B^(u)+...+w(-l)F^(-l)
     *
     * @return A new filter is returned
     */
    IFiniteFilter mirror();

    /**
     * Apply the filter on the input and store the results in the output The
     * range of the input is implicitly defined by the filter and by the output.
     * If the filter is defined by w{lb)...w(ub) and the filter output is
     * defined for [start, end[, the input should be defined for [start-lb,
     * end+ub[
     *
     * @param in
     * @param out
     */
    default void apply(IntToDoubleFunction in, IFilterOutput out) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        for (int i = out.getStart(); i < out.getEnd(); ++i) {
            double s = 0;
            for (int j = lb; j <= ub; ++j) {
                s += weights.applyAsDouble(j) * in.applyAsDouble(i + j);
            }
            out.set(i, s);
        }
    }

    /**
     * Applies the filter on the input
     *
     * @param in The input, which must have the same length as the filter
     * @return The product of the filter and of the input
     */
    double apply(DoubleSeq in);

    /**
     * Applies the filter on the input y(t)
     *
     * @param in The buffer containing the input
     * @param pos The position of y(t) in the buffer
     * @param inc The increment between two successive inputs.
     * y(t+k)=buffer[pos+k*inc]
     * @return sum(w(k)* buffer[pos+k*inc]), k in [-lb, ub]
     */
    default double apply(double[] in, int pos, int inc) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        double s = 0;
        if (inc == 1) {
            for (int k = lb, t = pos + lb; k <= ub; ++k, ++t) {
                s += in[t] * weights.applyAsDouble(k);
            }
        } else {
            for (int k = lb, t = pos + lb * inc; k <= ub; ++k, t += inc) {
                s += in[t] * weights.applyAsDouble(k);
            }
        }
        return s;
    }

    /**
     * Filters in and sets the result in out. More exactly, in contains x(-lb,
     * n+ub) and out contains y(0, n) with y(t) = f(t-lb,...,t+ub).
     *
     * @param in Input. Should not be modified
     * @param out Output
     */
    default void filter(DataBlock in, DoubleVector out) {
        double[] w = weightsToArray();
        double[] xin = in.getStorage();
        int start = in.getStartPosition(), inc = in.getIncrement();
        int n = out.length();
        DoubleVectorCursor cursor = out.cursor();
        if (inc == 1) {
            for (int i = 0, j = start; i < n; ++i, ++j) {
                double s = 0;
                for (int k = 0, t = j; k < w.length; ++k, ++t) {
                    s += xin[t] * w[k];
                }
                cursor.setAndNext(s);
            }
        } else {
            for (int i = 0, j = start; i < n; ++i, j+=inc) {
                double s = 0;
                for (int k = 0, t = j; k < w.length; ++k, t+=inc) {
                    s += xin[t] * w[k];
                }
                cursor.setAndNext(s);
            }
        }
    }

    default void apply2(DoubleSeq in, DoubleVector out) {
        double[] w = weightsToArray();
        int n = out.length();
        DoubleVectorCursor cursor = out.cursor();
        DoubleSeqCursor icur = in.cursor();
            for (int i = 0; i < n; ++i) {
                icur.moveTo(i);
                double s = 0;
                for (int k = 0; k < w.length; ++k) {
                    s += icur.getAndNext() * w[k];
                }
                cursor.setAndNext(s);
            }
    }

}
