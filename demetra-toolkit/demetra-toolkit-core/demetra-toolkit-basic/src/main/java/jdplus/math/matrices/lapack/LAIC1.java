/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.math.Constants;
import jdplus.math.matrices.DataPointer;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LAIC1 {

    private double sestpr = 1;
    private double s;
    private double c;

    /**
     *
     * @param j Length of the vectors
     * @param x
     * @param sest Estimated singular value of j by j matrix L
     * @param w
     * @param gamma The diagonal element gamma
     */
    public void minSingularValue(int j, DataPointer x, double sest, DataPointer w, double gamma) {
        double eps = Constants.getEpsilon();
        double alpha = x.dot(j, w);
        double aalpha = Math.abs(alpha), agamma = Math.abs(gamma), asest = Math.abs(sest);
        // special cases:
        if (sest == 0) {
            double sine, cosine;
            sestpr = 0;
            if (agamma == 0 && aalpha == 0) {
                sine = 1;
                cosine = 0;
            } else {
                sine = -gamma;
                cosine = alpha;
            }
            double s1 = Math.max(Math.abs(sine), Math.abs(cosine));
            s = sine / s1;
            c = cosine / s1;
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s /= tmp;
            c /= tmp;
        } else if (agamma <= eps * asest) {
            s = 0;
            c = 1;
            sestpr = agamma;
        } else if (aalpha <= eps * asest) {
            double s1 = agamma, s2 = asest;
            if (s1 <= s2) {
                s = 0;
                c = 1;
                sestpr = s1;
            } else {
                s = 1;
                c = 0;
                sestpr = s2;
            }
        } else if (asest <= eps * aalpha || asest <= eps * agamma) {
            double s1 = agamma, s2 = agamma;
            if (s1 <= s2) {
                double tmp = s1 / s2;
                c = Math.sqrt(1 + tmp * tmp);
                sestpr = asest * (tmp / c);
                s = -(gamma / s2) / c;
                c = Math.copySign(1, alpha) / c;
            } else {
                double tmp = s2 / s1;
                s = Math.sqrt(1 + tmp * tmp);
                sestpr = asest / s;
                c = (alpha / s1) / s;
                s = Math.copySign(1, gamma) / s;

            }
        } else {
            double zeta1 = alpha / asest, zeta2 = gamma / asest;
            double norma = Math.max(1 + zeta1 * zeta1 + Math.abs(zeta1 * zeta2),
                    Math.abs(zeta1 * zeta2) + zeta2 * zeta2);
            double test = 1 + 2 * (zeta1 - zeta2) * (zeta1 + zeta2);
            double sine, cosine;
            if (test >= 0) {
                double b = (zeta1 * zeta1 + zeta2 * zeta2 + 1) / 2;
                c = zeta2 * zeta2;
                double t = c / (b + Math.sqrt(Math.abs(b * b - c)));
                sine = zeta1 / (1 - t);
                cosine = -zeta2 / t;
                sestpr = Math.sqrt(1 + 4 * eps * eps * norma) * asest;
            } else {
                double b = (zeta2 * zeta2 + zeta1 * zeta1 - 1) / 2;
                c = zeta1 * zeta1;
                double t;
                if (b >= 0) {
                    t = -c / (b + Math.sqrt(b * b - c));
                } else {
                    t = b - Math.sqrt(b * b + c);
                }
                sine = -zeta1 / t;
                cosine = -zeta2 / (1 + t);
                sestpr = Math.sqrt(1 + t + 4 * eps * eps * norma) * asest;
            }
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s = sine / tmp;
            c = cosine / tmp;
        }
    }

    /**
     *
     * @param j Length of the vectors
     * @param x
     * @param sest Estimated singular value of j by j matrix L
     * @param w
     * @param gamma The diagonal element gamma
     */
    public void maxSingularValue(int j, DataPointer x, double sest, DataPointer w, double gamma) {
        double eps = Constants.getEpsilon();
        double alpha = x.dot(j, w);
        double aalpha = Math.abs(alpha), agamma = Math.abs(gamma), asest = Math.abs(sest);
        // special cases:
        if (sest == 0) {
            double s1 = Math.max(agamma, aalpha);
            if (s1 == 0) {
                s = 0;
                c = 1;
                sestpr = 0;
            } else {
                s = alpha / s1;
                c = gamma / s1;
                double tmp = Math.sqrt(s * s + c * c);
                s /= tmp;
                c /= tmp;
                sestpr = s1 * tmp;
            }
        } else if (agamma <= eps * asest) {
            s = 1;
            c = 0;
            double tmp = Math.max(asest, aalpha);
            double s1 = asest / tmp, s2 = aalpha / tmp;
            sestpr = tmp * Math.sqrt(s1 * s1 + s2 * s2);
        } else if (aalpha <= eps * asest) {
            double s1 = agamma, s2 = asest;
            if (s1 <= s2) {
                s = 1;
                c = 0;
                sestpr = s2;
            } else {
                s = 0;
                c = 1;
                sestpr = s1;
            }
        } else if (asest <= eps * aalpha || asest <= eps * agamma) {
            double s1 = agamma, s2 = aalpha;
            if (s1 <= s2) {
                double tmp = s1 / s2;
                s = Math.sqrt(1 + tmp * tmp);
                sestpr = s2 * s;
                c = (gamma / s2) / s;
                s = Math.copySign(1, alpha) / s;
            } else {
                double tmp = s2 / s1;
                c = Math.sqrt(1 + tmp * tmp);
                sestpr = s1 * c;
                s = (alpha / s1) / c;
                c = Math.copySign(1, gamma) / c;
            }
        } else {
            //normal case
            double zeta1 = alpha / asest, zeta2 = gamma / asest;
            double b = (1 - zeta1 * zeta1 - zeta2 * zeta2) / 2;
            c = zeta1 * zeta1;
            double t;
            if (b > 0) {
                t = c / (b + Math.sqrt(b * b + c));
            } else {
                t = Math.sqrt(b * b + c) - b;
            }
            double sine = -zeta1 / t, cosine = -zeta2 / (1 + t);
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s = sine / tmp;
            c = cosine / tmp;
            sestpr = Math.sqrt(t + 1) * asest;
        }
    }

    /**
     * @return the sestpr
     */
    public double getSestpr() {
        return sestpr;
    }

    /**
     * @return the s
     */
    public double getS() {
        return s;
    }

    /**
     * @return the c
     */
    public double getC() {
        return c;
    }

}
