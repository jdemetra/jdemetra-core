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
package demetra.maths;

import demetra.util.Arrays2;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ComplexUtility {

    /**
     * The static method finds the roots present in lr but not in rr
     *
     * @param lr An array of complex roots
     * @param lrlength The number of roots in lroots to take into account
     * @param rr An array of complex roots
     * @param rrlength The number of roots in lroots to take into account
     * @param epsilon
     * @return An array containing the difference between lr and rr
     */
    public Complex[] difference(final Complex[] lr, final int lrlength,
            final Complex[] rr, final int rrlength, final double epsilon) {
        boolean[] flags = new boolean[lrlength];
        int rem = 0;
        for (int i = 0; i < rrlength; i++) {
            for (int j = 0; j < lrlength; j++) {
                if (!flags[j] && (lr[j].distance(rr[i]) <= epsilon)) {
                    flags[j] = true;
                    rem++;
                }
            }
        }

        Complex[] remroots = new Complex[lrlength - rem];
        for (int i = 0, j = 0; i < lrlength; i++) {
            if (!flags[i]) {
                remroots[j++] = lr[i];
            }
        }

        return remroots;
    }

    /**
     * The static method finds the common roots in subsets of the arrays passed
     * as parameters
     *
     * @param lnroots The number of roots in lroots to take into account
     * @param lroots An array of complex roots
     * @param rnroots The number of roots in rr to take into account
     * @param rroots An array of complex roots
     * @param epsilon Contains the number of items in the intersection on return
     * @return An array containing the common roots of lr and rr
     */
    public Complex[] intersection(final int lnroots,
            final Complex[] lroots, final int rnroots, final Complex[] rroots,
            final double epsilon) {
        if ((lnroots == 0) || (rnroots == 0)) {
            return null;
        }

        int croots = 0;
        boolean[] lflags = new boolean[lnroots];
        boolean[] rflags = new boolean[rnroots];

        // determine common roots
        for (int i = 0; i < lnroots; i++) {
            for (int j = 0; j < rnroots; j++) {
                if (!rflags[j] && (lroots[i].distance(rroots[j]) <= epsilon)) {
                    lflags[i] = true;
                    rflags[j] = true;
                    croots++;
                    break;
                }
            }
        }

        Complex[] outroots = new Complex[croots];
        for (int i = 0, k = 0; i < lnroots; i++) {
            if (lflags[i]) {
                outroots[k++] = lroots[i];
            }
        }

        return outroots;
    }

    /**
     * The static method forms a union of the roots passed as parameters
     *
     * @param lr An array of complex roots
     * @param rr An array of complex roots
     * @return An array containing the union of lr and rr
     */
    public static Complex[] union(final Complex[] lr, final Complex[] rr) {
        if (lr == null) {
            return rr;
        } else if (rr == null) {
            return lr;
        } else {
            return union(lr, lr.length, rr, rr.length);
        }
    }

    /*
     * The static method forms a union of a subset of the roots passed as
     * parameters
     * 
     * @param lr
     *            An array of complex roots
     * @param lrlength
     *            The number of roots in lr to take into account
     * @param rr
     *            An array of complex roots
     * @param rrlength
     *            The number of roots in rr to take into account
     * @return An array containing the union of lr and rr
     */
    public Complex[] union(final Complex[] lr, final int lrlength,
            final Complex[] rr, final int rrlength) {
        if (Arrays2.isNullOrEmpty(lr)) {
            if (Arrays2.isNullOrEmpty(rr)) {
                return null;
            }
            return Arrays2.copyOf(rr);
        }

        if (Arrays2.isNullOrEmpty(rr)) {
            return Arrays2.copyOf(lr);
        }

        Complex[] rslt = new Complex[lrlength + rrlength];

        System.arraycopy(lr, 0, rslt, 0, lrlength);
        System.arraycopy(rr, 0, rslt, lrlength, rrlength);
//	for (int i = 0; i < lrlength; ++i)
//	    rslt[i] = lr[i];
//	for (int i = 0; i < rrlength; i++)
//	    rslt[i + lrlength] = rr[i];

        return rslt;
    }

    /**
     *
     * @param xin
     */
    public void lejaOrder(final Complex[] xin) {
        if (xin == null) {
            return;
        }
        int n = xin.length;
        if (n == 0) {
            return;
        }

        double dist = 0;
        int m = 0;
        for (int i = 0; i < n; ++i) {
            double tmp = xin[i].absSquare();
            if (dist < tmp) {
                dist = tmp;
                m = i;
            }
        }
        Arrays2.swap(xin, 0, m);

        for (int i = 1; i < n; ++i) {
            dist = 0;
            m = i;
            for (int j = i; j < n; ++j) {
                double tmp = 1;
                for (int k = 0; k < i; ++k) {
                    double tre = xin[k].getRe() - xin[j].getRe();
                    double tim = xin[k].getIm() - xin[j].getIm();
                    tmp *= tre * tre + tim * tim;
                }
                if (dist < tmp) {
                    dist = tmp;
                    m = j;
                }
            }
            Arrays2.swap(xin, i, m);
        }

    }

    /**
     *
     * @param c
     * @param nRoots
     * @return
     */
    public Complex[] roots(final Complex c, final int nRoots) {
        if (nRoots <= 0) {
            return null;
        }
        Complex[] roots = unitRoots(nRoots);
        double arg = c.arg();
        double abs = c.abs();
        double p = 1;
        p /= nRoots;
        abs = Math.pow(abs, p);
        arg *= p;
        Complex tmp = Complex.polar(abs, arg);
        for (int i = 0; i < nRoots; ++i) {
            roots[i] = roots[i].times(tmp);
        }
        return roots;
    }

    private final Complex[] MROOTS = ur(12), QROOTS = ur(4);

    /**
     *
     * @param nRoots
     * @return
     */
    public Complex[] unitRoots(final int nRoots) {
        if (nRoots <= 0) {
            return null;
        } else if (nRoots == 4) {
            return QROOTS.clone();
        } else if (nRoots == 12) {
            return MROOTS.clone();
        } else {
            return ur(nRoots);
        }
    }

    private static Complex[] ur(final int nRoots) {
        Complex[] roots = new Complex[nRoots];
        roots[0] = Complex.ONE;
        if (nRoots == 2) {
            roots[1] = Complex.NEG_ONE;
        } else if (nRoots == 4) {
            roots[1] = Complex.I;
            roots[2] = Complex.NEG_ONE;
            roots[3] = Complex.NEG_I;
        } else if (nRoots != 1) {
            // cos (k+1)z = 2*cos z*cos kz-cos(k-1)z
            // sin (k+1)z = 2*cos z*sin kz-sin(k-1)z
            // if cos z ==w, we have
            // k=0 : 1, 0
            // k=1 : w, sqrt(1-w*w)
            // k=2 : 2*w*w - 1, 2w*sqrt(1-w*w)
            // k=3 : ...
            double v = 2 * Math.PI / nRoots;
//	    double z = Math.cos(v);
//	    double x0 = 1, x1 = z;
//	    double y0 = 0, y1 = Math.sin(v);
//	    roots[1] = Complex.cart(x1, y1);
            for (int q = 1; q < nRoots; ++q) {
                double w = v * q;
                roots[q] = Complex.cart(Math.cos(w), Math.sin(w));
//		double xtmp = 2 * z * x1 - x0;
//		double ytmp = 2 * z * y1 - y0;
//		roots[q] = Complex.cart(xtmp, ytmp);
//		x0 = x1;
//		y0 = y1;
//		x1 = xtmp;
//		y1 = ytmp;
            }
        }
        return roots;
    }

}
