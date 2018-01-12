/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class DiscreteKernels {

    public IntToDoubleFunction uniform() {
        return i -> 1;
    }

    public IntToDoubleFunction triangular(final int h) {
        final double u = 1.0 / (h + 1);
        return i -> (i < 0 ? 1 + i * u : 1 - i * u);
    }

    public IntToDoubleFunction triweight(final int h) {
        final double q = 1.0 / (h + 1);
        return i -> {
            double x = i * q;
            double t = 1 - x * x;
            return t * t * t;
        };
    }

    public IntToDoubleFunction biweight(final int h) {
        final double q = 1.0 / (h + 1);
        return i -> {
            double x = i * q;
            double t = 1 - x * x;
            return t * t;
        };
    }

    public IntToDoubleFunction tricube(final int h) {
        final double q = 1.0 / (h + 1);
        return i -> {
            double x = i >= 0 ? i * q : -i * q;
            double t = 1 - x * x * x;
            return t * t * t;
        };
    }

    public IntToDoubleFunction parabolic(final int h) {
        final double q = 1.0 / (h + 1);
        return i -> {
            double x = i * q;
            return 1 - x * x;
        };
    }

    public IntToDoubleFunction henderson(final int h) {
        double x = h + 1;
        double d = (x * x) * (x + 1) * (x + 1) * (x + 2) * (x + 2);
        return i -> (x * x - i * i) * ((x + 1) * (x + 1) - i * i) * ((x + 2) * (x + 2) - i * i) / d;
    }

    public IntToDoubleFunction gaussian(final double v) {
        return i -> Math.exp(-i * i / v);
    }
}
