/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.stl;

import demetra.data.WeightFunction;
import java.util.function.DoubleUnaryOperator;

/**
 * Defines a Loess filter. The specification contains - window: the length of
 * the estimation window (should be odd) - degree: the degree of the
 * interpolating polynomial (0 for constant, 1 for linear trend) - jump
 * (optimization option): the number of jumps between two successive estimations
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class LoessSpec {

    /**
     * The length of the full(two-sided) estimation window (should be odd)
     */
    private int window;
    /**
     * The degree of the regression polynomial (0 for constant, 1 for linear
     * trend)
     */
    private int degree;
    /**
     * The number of jumps between two successive estimations (0 when the filter
     * is applied on each point)
     */
    private int jump;

    /**
     * WeightFunction
     */
    @lombok.NonNull
    private WeightFunction loessFunction;

    public static final WeightFunction DEF_WEIGHTS = WeightFunction.TRICUBE;

    /**
     * Default trend length (=1.5*period/(1-1.5/swindow)) For example, for
     * period = 12, we have: | seasonal | trend | |----------|-------| | 3 | 37
     * | | 5 | 25 | | 7 | 23 | | 9 | 21 |
     *
     * @param period periodicity of the series
     * @param swindow the length of the two-sided seasonal filter (should be
     * odd)
     * @return
     */
    public static LoessSpec defaultTrend(int period, int swindow, boolean nojump) {
        int win = defaultTrendWindow(period, swindow);
        return of(win, 1, nojump ? 0 : (int) Math.ceil(0.1 * win), null);
    }

    public static int defaultTrendWindow(int period) {
        return defaultTrendWindow(period, 7);
    }

    public static int defaultTrendWindow(int period, int swindow) {
        int win = (int) Math.ceil((1.5 * period) / (1 - 1.5 / swindow));
        if (win % 2 == 0) {
            ++win;
        }
        return win;
    }

    public static LoessSpec defaultTrend(int period, boolean nojump) {
        int win = (int) Math.ceil((1.5 * period) / (1 - 1.5 / 7));
        if (win % 2 == 0) {
            ++win;
        }
        return of(win, 1, nojump ? 0 : (int) Math.ceil(0.1 * win), null);
    }

    /**
     * The window is the smallest odd integer greater than or equal to period
     *
     * @param period periodicity of the series
     * @param nojump
     * @return
     */
    public static LoessSpec defaultLowPass(int period, boolean nojump) {
        int win = period;
        if (win % 2 == 0) {
            ++win;
        }
        return of(win, 1, nojump ? 0 : (int) Math.ceil(0.1 * win), null);
    }

    /**
     * By default, win is the smaller odd number greater or equal to swin, and
     * degree = 0
     *
     * @param swin The seasonal window. In normal use, should be odd and at
     * least 7.
     * @param nojump
     * @return
     */
    public static LoessSpec defaultSeasonal(int swin, boolean nojump) {
        if (swin % 2 == 0) {
            ++swin;
        }
        return of(swin, 0, nojump ? 0 : (int) Math.ceil(0.1 * swin), null);
    }

    public static LoessSpec defaultSeasonal(boolean nojump) {
        return of(7, 0, nojump ? 0 : 1, null);
    }

    public static LoessSpec of(int window, int degree, boolean nojump) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpec(window, degree, nojump ? 0 : (int) Math.ceil(0.1 * window), null);
    }

    /**
     *
     * @param window
     * @param degree
     * @param jump
     * @param fn If null, the default weights are used (Tri-cubes). Use UNIFORM
     * if you don't want weighting
     * @return
     */
    public static LoessSpec of(int window, int degree, int jump, WeightFunction fn) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        if (jump < 0) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpec(window, degree, jump, fn);
    }

    private LoessSpec(int window, int degree, int jump, WeightFunction fn) {
        this.window = window;
        this.degree = degree;
        this.jump = jump;
        this.loessFunction = fn == null ? DEF_WEIGHTS : fn;
    }

    /**
     * @return the loessFunction
     */
    public DoubleUnaryOperator weights() {
        return loessFunction.asFunction();
    }

}
