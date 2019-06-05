/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.data.analysis;

import jdplus.maths.IntUtility;
import java.util.function.IntToDoubleFunction;
import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status=Development.Status.Release)
public enum DiscreteKernel {

    Uniform,
    Triangular,
    Parabolic,
    Biweight,
    Triweight,
    Tricube,
    Henderson;

    public IntToDoubleFunction asFunction(int h) {
        switch (this) {
            case Uniform:
                return uniform(h);
            case Triangular:
                return triangular(h);
            case Parabolic:
                return parabolic(h);
            case Biweight:
                return biweight(h);
            case Triweight:
                return triweight(h);
            case Tricube:
                return tricube(h);
            case Henderson:
                return henderson(h);
        }
        return null;
    }

    public static IntToDoubleFunction uniform(final int h) {
        return i -> 1.0 / (2 * h + 1);
    }

    public static IntToDoubleFunction triangular(final int h) {

        final double u = 1.0 / (h + 1);
        return i -> (i < 0 ? 1 + i * u : 1 - i * u) * u;
    }

    public static IntToDoubleFunction triweight(final int h) {
        double H = h + 1, H2 = H * H, H4 = H2 * H2, H6 = H2 * H4;
        final double q = 1.0 + 2 * (h - 3 * IntUtility.sumOfPowers(2, h) / H2 + 3 * IntUtility.sumOfPowers(4, h) / H4 - IntUtility.sumOfPowers(6, h) / H6);
        return i -> {
            double x = i / H;
            double t = 1 - x * x;
            return t * t * t / q;
        };
    }

    public static IntToDoubleFunction biweight(final int h) {
        double H = h + 1, H2 = H * H, H4 = H2 * H2;
        final double q = 1 + 2 * (h - 2 * IntUtility.sumOfPowers(2, h) / H2 + IntUtility.sumOfPowers(4, h) / H4);
        return i -> {
            double x = i / H;
            double t = 1 - x * x;
            return t * t / q;
        };
    }

    public static IntToDoubleFunction tricube(final int h) {
        double H = h + 1, H3 = H * H * H, H6 = H3 * H3, H9 = H3 * H6;
        final double q = 1.0 + 2 * (h - 3 * IntUtility.sumOfPowers(3, h) / H3 + 3 * IntUtility.sumOfPowers(6, h) / H6 - IntUtility.sumOfPowers(9, h) / H9);
        return i -> {
            double x = i >= 0 ? i / H : -i / H;
            double t = 1 - x * x * x;
            return t * t * t / q;
        };
    }

    public static IntToDoubleFunction parabolic(final int h) {
        double H = h + 1, H2 = H * H;
        final double q = 1 + 2 * (h - IntUtility.sumOfPowers(2, h) / H2);
        return i -> {
            double x = i / H;
            return (1 - x * x) / q;
        };
    }

    public static IntToDoubleFunction henderson(final int h) {
        double A = h + 1, A2 = A * A;
        double B = h + 2, B2 = B * B;
        double C = h + 3, C2 = C * C;
        // (A2 - i2)(B2 - i2)(C2 - i2)
        // A2*B2*C2 - (A2*B2+A2*C2+B2*C2)i2 + (A2+B2+C2)i4 - i6
        final double q = A2 * B2 * C2 * (1 + 2 * h) - 2 * ((A2 * B2 + A2 * C2 + B2 * C2) * IntUtility.sumOfPowers(2, h)
                - (A2 + B2 + C2) * IntUtility.sumOfPowers(4, h) + IntUtility.sumOfPowers(6, h));
        return i -> {
            double i2 = i * i;
            return (A2 - i2) * (B2 - i2) * (C2 - i2) / q;
        };

    }

    public static IntToDoubleFunction gaussian(final double v) {
        return i -> Math.exp(-i * i / v);
    }

    public double distance(DiscreteKernel other, int horizon) {
        return distance(asFunction(horizon), other.asFunction(horizon), horizon);
    }

    public static double distance(IntToDoubleFunction k1, IntToDoubleFunction k2, int horizon) {
        double s = 0;
        for (int i = -horizon; i <= horizon; ++i) {
            double d = k1.applyAsDouble(i) - k2.applyAsDouble(i);
            s += d * d;
        }
        return Math.sqrt(s);
    }
}
