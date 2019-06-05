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

import demetra.design.Development;
import demetra.maths.Constants;

/**
 * X(k) = sum (x(j) e(-i 2pi/N *jk))
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status=Development.Status.Release)
public class DFT {

    /**
     * Transforms a symmetric real input
     *
     * @param inr Symmetric input (x(t), t in [0, N]. Only non redundant input
     * are provided We suppose that the full output is given by x(t), t in [-N,
     * N] with x(-t)=x(t) or, equivalently x(t), t in [0, 2N] with
     * x(2N-t)=x(t+1)
     * @return The returned array contains the same number of elements as the
     * input array
     */
    public double[] transformSymmetric(double[] inr) {
        int T = inr.length;
        double[] out = new double[T];
        int N = 2 * T - 1; // full size
        double l = Constants.TWOPI / N;
        double[] cos = new double[T];
        cos[0] = 1;
        for (int i = 1; i < T; ++i) {
            cos[i] = Math.cos(i * l);
        }
        double a;
        for (int i = 0; i < T; ++i) {
            a = inr[0];
            for (int j = 1, k = i; j < T; ++j, k += i) {
                double r = inr[j];
                int q = k % N;
                if (q>=T){
                    q=N -q;
                }
                if (!Double.isNaN(r)) {
                    a += 2 * r * cos[q];
                }
            }
            out[i] = a;
        }
        return out;
    }

    public void transform(double[] inr, double[] outcos, double[] outsin) {
        int T = inr.length;
        double l = Constants.TWOPI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...

        double a, b;
        for (int i = 0; i < T; ++i) {

            a = 0;
            b = 0;
            double c = 1, s = 0;
            for (int j = 0; j < T; ++j) {
                double r = inr[j];
                // compute next c, s ...
                // cos(x+y)=cos(x)cos(y)-sin(x)sin(y)
                // sin(x+y)=cos(x)sin(y)+sin(x)cos(y)
                double ctmp = c;
                double stmp = s;
                s = cos * stmp + sin * ctmp;
                c = cos * ctmp - sin * stmp;
                if (!Double.isNaN(r)) {
                    a += c * r;
                    b += s * r;
                }
            }
            outcos[i] = a;
            outsin[i] = b;
            // compute next cos, sin
            double ctmp = cos, stmp = sin;
            sin = cosl * stmp + sinl * ctmp;
            cos = -sinl * stmp + cosl * ctmp;
        }
    }

    public void transform(double[] inr, double[] ini, double[] outcos, double[] outsin) {
        int T = inr.length;
        double l = Constants.TWOPI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...
        // cos(x+y)=cos(x)cos(y)-sin(x)sin(y)
        // sin(x+y)=cos(x)sin(y)+sin(x)cos(y)

        double a, b;
        for (int i = 0; i < T; ++i) {
            a = 0;
            b = 0;
            double c = 1, s = 0;
            for (int j = 0; j < T; ++j) {
                double r = inr[j];
                double im = ini[j];
                // (c+is)(r+im)=(cr -sm)+i(sr+cm) 
                if (!Double.isNaN(r)) {
                    a += c * r + s * im;
                    b += c * im - s * r;
                }
                // compute next c, s ...
                double ctmp = c;
                double stmp = s;
                c = cos * ctmp - sin * stmp;
                s = cos * stmp + sin * ctmp;
            }
            outcos[i] = a;
            outsin[i] = b;
            // compute next cos, sin
            double ctmp = cos, stmp = sin;
            sin = cosl * stmp + sinl * ctmp;
            cos = -sinl * stmp + cosl * ctmp;
        }
    }

    public void backTransform(double[] inr, double[] ini, double[] outcos, double[] outsin) {
        int T = inr.length;
        double l = Constants.TWOPI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...
        // cos(x+y)=cos(x)cos(y)-sin(x)sin(y)
        // sin(x+y)=cos(x)sin(y)+sin(x)cos(y)

        double a, b;
        for (int i = 0; i < T; ++i) {
            a = 0;
            b = 0;
            double c = 1, s = 0;
            for (int j = 0; j < T; ++j) {
                double r = inr[j];
                double im = ini[j];
                // (c+is)(r+im)=(cr -sm)+i(sr+cm) 
                if (!Double.isNaN(r)) {
                    a += c * r - s * im;
                    b += c * im + s * r;
                }
                // compute next c, s ...
                double ctmp = c;
                double stmp = s;
                c = cos * ctmp - sin * stmp;
                s = cos * stmp + sin * ctmp;
            }
            outcos[i] = a / T;
            outsin[i] = b / T;
            // compute next cos, sin
            double ctmp = cos, stmp = sin;
            sin = cosl * stmp + sinl * ctmp;
            cos = -sinl * stmp + cosl * ctmp;
        }
    }

    public void transform2(double[] inr, double[] ini, double[] outcos, double[] outsin) {
        int T = inr.length;
        double l = Constants.TWOPI / T;
        double[] cos = new double[T], sin = new double[T];
        // compute recuslively all the cos/sin
        cos[0] = 1;
        for (int i = 1; i < T; ++i) {
            cos[i] = Math.cos(i * l);
            sin[i] = Math.sin(i * l);
        }
        double a, b;
        for (int i = 0; i < T; ++i) {
            a = 0;
            b = 0;
            for (int j = 0, k = 0; j < T; ++j, k += i) {
                double r = inr[j];
                double im = ini[j];
                int q = k % T;
                if (!Double.isNaN(r)) {
                    a += cos[q] * r + sin[q] * im;
                    b += cos[q] * im - sin[q] * r;
                }
            }
            outcos[i] = a;
            outsin[i] = b;
            // compute next cos, sin
        }
    }

    public void backTransform2(double[] inr, double[] ini, double[] outcos, double[] outsin) {
        int T = inr.length;
        double l = Constants.TWOPI / T;
        double[] cos = new double[T], sin = new double[T];
        // compute recuslively all the cos/sin
        cos[0] = 1;
        for (int i = 1; i < T; ++i) {
            cos[i] = Math.cos(i * l);
            sin[i] = Math.sin(i * l);
        }
        double a, b;
        for (int i = 0; i < T; ++i) {
            a = 0;
            b = 0;
            for (int j = 0, k = 0; j < T; ++j, k += i) {
                double r = inr[j];
                double im = ini[j];
                int q = k % T;
                if (!Double.isNaN(r)) {
                    a += cos[q] * r - sin[q] * im;
                    b += cos[q] * im + sin[q] * r;
                }
            }
            outcos[i] = a / T;
            outsin[i] = b / T;
            // compute next cos, sin
        }
    }
}
