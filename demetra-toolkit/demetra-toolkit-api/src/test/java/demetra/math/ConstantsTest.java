/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ConstantsTest {

    public ConstantsTest() {
    }

//    @Test
//    public void testLamc4() {
//        Lam lam = new Lam();
//        System.out.println(lam.base);
//        System.out.println(lam.mantissa);
//        System.out.println(lam.ieee);
//        System.out.println(lam.rnd);
//        System.out.println(lam.emin);
//        System.out.println(lam.emax);
//        System.out.println(lam.nbits);
//        System.out.println(lam.rmin);
//        System.out.println(lam.rmax);
//        System.out.println(lam.eps);
//        System.out.println(lam.sfmin);
//        System.out.println(lam.small);
//        System.out.println(Double.MAX_EXPONENT);
//        System.out.println(Double.MIN_EXPONENT);
//        System.out.println(Double.MIN_NORMAL);
//        System.out.println(Double.MIN_VALUE);
//        System.out.println(Double.MAX_VALUE);
//        System.out.println(Constants.getEpsilon());
//        System.out.println(Constants.getSafeMin());
//
//    }
}

//strictfp class Lam {
//
//    private static final double zero = 0.0, one = 1.0, two = 2.0;
//
//    int base, mantissa;
//    boolean ieee, rnd;
//    int emin, emax;
//    int nbits;
//    double rmax, rmin;
//    double eps, prec, small, sfmin;
//
//    Lam() {
//        lamc2();
//        if (rnd) {
//            eps = pow(base, (1 - mantissa)) / 2;
//        } else {
//            eps = pow(base, (1 - mantissa));
//        }
//        prec = eps * base;
//        small = 1 / rmax;
//        sfmin=rmin;
//        if (small >= sfmin) {
//            sfmin = small * (1 + eps);
//        }
//    }
//
//    double pow(double x, int e) {
//        if (e < 0) {
//            double z = 1 / x;
//            for (int i = -1; i > e; --i) {
//                z /= x;
//            }
//            return z;
//        } else {
//            double z = x;
//            for (int i = 1; i < e; ++i) {
//                z *= x;
//            }
//            return z;
//        }
//    }
//
//    private void lamc1() {
//        double a = one, c = one;
//        while (c == 1) {
//            a = 2 * a;
//            c = lamc3(a, one);
//            c = lamc3(c, -a);
//        }
//
//        double b = 1;
//        c = lamc3(a, b);
//        while (c == a) {
//            b = 2 * b;
//            c = lamc3(a, b);
//        }
//        double qtr = one / 4;
//        double savec = c;
//        c = lamc3(c, -a);
//        base = (int) (c + qtr);
//
//        b = base;
//        double f = lamc3(b / 2, -b / 100);
//        c = lamc3(f, a);
//        rnd = c == a;
//        f = lamc3(b / 2, b / 100);
//        c = lamc3(f, a);
//        if (rnd && c == a) {
//            rnd = false;
//        }
//        double t1 = lamc3(b / 2, a);
//        double t2 = lamc3(b / 2, savec);
//        ieee = t1 == a && t2 > savec && rnd;
//
//        mantissa = 0;
//        a = 1;
//        c = 1;
//        while (c == one) {
//            ++mantissa;
//            a = a * b;
//            c = lamc3(a, one);
//            c = lamc3(c, -a);
//        }
//    }
//
//    private void lamc2() {
//        lamc1();
//        double b = base;
//        double a = pow(b, -mantissa);
//        eps = a;
//        b = two / 3;
//        double half = one / 2;
//        double sixth = lamc3(b, -half);
//        double third = lamc3(sixth, sixth);
//        b = lamc3(third, -half);
//        b = lamc3(b, sixth);
//        b = Math.abs(b);
//        if (b < eps) {
//            b = eps;
//        }
//        eps = one;
//        double c;
//        while (eps > 1 && b > zero) {
//            eps = b;
//            c = lamc3(half * eps, pow(two, 5) * eps * eps);
//            c = lamc3(half, -c);
//            b = lamc3(half, c);
//            c = lamc3(half, -b);
//            b = lamc3(half, c);
//        }
//        if (a < eps) {
//            eps = a;
//        }
//        double rbase = one / base;
//        small = one;
//        for (int i = 1; i <= 3; ++i) {
//            small = lamc3(small * rbase, zero);
//        }
//        a = lamc3(one, small);
//        int ngpmin = lamc4(1, base);
//        int ngnmin = lamc4(-1, base);
//        int gpmin = lamc4(a, base);
//        int gnmin = lamc4(-a, base);
//
//        boolean lieee = false;
//        if (ngpmin == ngnmin && gpmin == gnmin) {
//            if (ngpmin == gpmin) {
//                emin = ngpmin;
//            } else if (gpmin - ngpmin == 3) {
//                emin = ngpmin - 1 + mantissa;
//                lieee = true;
//            } else {
//                emin = Math.min(ngpmin, gpmin);
//            }
//        } else if (ngpmin == gpmin && ngnmin == gnmin) {
//            if (Math.abs(ngpmin - ngnmin) == 1) {
//                emin = Math.max(ngpmin, ngnmin);
//            } else {
//                emin = Math.min(ngpmin, ngnmin);
//            }
//        } else if (Math.abs(ngpmin - ngnmin) == 1 && gpmin == gnmin) {
//            if (gpmin - Math.min(gnmin, ngpmin) == 3) {
//                emin = Math.max(ngpmin, ngnmin) - 1 + mantissa;
//            } else {
//                emin = Math.min(ngpmin, ngnmin);
//            }
//        } else {
//            emin = Math.min(Math.min(ngpmin, ngnmin), Math.min(gpmin, gnmin));
//        }
//        ieee = ieee || lieee;
//
//        lamc5();
//        rmin = 1;
//        for (int i = 1; i <= 1 - emin; ++i) {
//            rmin = lamc3(rmin * rbase, zero);
//        }
//        lamc5();
//    }
//
//    private double lamc3(double a, double b) {
//        return a + b;
//    }
//
//    private int lamc4(final double start, final int base) {
//        int lemin = 1;
//        double a = start, rbase = one / base;
//        double b1 = lamc3(a * rbase, zero), b2;
//        double c1 = a, c2 = a, d1 = a, d2 = a;
//        while (c1 == a && c2 == a && d1 == a && d2 == a) {
//            --lemin;
//            a = b1;
//            b1 = lamc3(a / base, zero);
//            c1 = lamc3(b1 * base, zero);
//            d1 = zero;
//            for (int i = 1; i <= base; ++i) {
//                d1 += b1;
//            }
//            b2 = lamc3(a * rbase, zero);
//            c2 = lamc3(b2 / rbase, zero);
//            d2 = zero;
//            for (int i = 1; i <= base; ++i) {
//                d2 += b2;
//            }
//        }
//        return lemin;
//    }
//
//    private void lamc5() {
//        int lexp = 1;
//        int exbits = 1;
//
//        int tr;
//        do {
//            tr = lexp * 2;
//            if (tr <= -emin) {
//                lexp = tr;
//                exbits++;
//            } else {
//                break;
//            }
//        } while (true);
//
//        int uexp;
//        if (lexp == -emin) {
//            uexp = lexp;
//        } else {
//            uexp = tr;
//            exbits++;
//        }
//        int expsum;
//        if (uexp + emin > -lexp - emin) {
//            expsum = 2 * lexp;
//        } else {
//            expsum = 2 * uexp;
//        }
//        emax = expsum + emin - 1;
//        nbits = 1 + exbits + mantissa;
//
//        if (nbits % 2 == 1 && base == 2) {
//            --emax;
//        }
//        if (ieee) {
//            --emax;
//        }
//
//        double recbas = one / base;
//        double z = base - one, y = zero;
//
//        double oldy = 0;
//        for (int i = 1; i <= mantissa; ++i) {
//            z *= recbas;
//            if (y < one) {
//                oldy = y;
//            }
//            y = lamc3(y, z);
//        }
//        if (y >= one) {
//            y = oldy;
//        }
//
//        for (int i = 1; i <= emax; ++i) {
//            y = lamc3(y * base, zero);
//        }
//        rmax = y;
//    }
//
//}
