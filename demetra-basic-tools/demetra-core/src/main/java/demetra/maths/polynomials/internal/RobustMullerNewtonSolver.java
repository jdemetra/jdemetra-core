/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.maths.polynomials.internal;

import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Robust;
import demetra.design.Development;
import demetra.design.VisibleForTesting;
import demetra.maths.Complex;
import demetra.maths.polynomials.LeastSquaresDivision;
import demetra.maths.polynomials.Polynomial;
import demetra.util.Ref;
import demetra.util.Ref.BooleanRef;
import demetra.util.Ref.DoubleRef;
import demetra.util.Ref.IntRef;
import demetra.maths.polynomials.spi.RootsSolver;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm=RootsSolver.class, feature=Robust)
public class RobustMullerNewtonSolver implements RootsSolver {

    private double[] polynomial;
    private double[] reducedPolynomial;
    private Complex[] roots;
    private Polynomial remainder;
    private int startIdx, degree;
    private double maxError;
    /**
     * max. number ofInternal iteration steps
     */
    private final static int MITERMAX = 150;
    /**
     * halve q2, when |P(x2)/P(x1)|^2 > CONVERGENCE
     */
    private final static int MCONVERGENCE = 100;
    /**
     * max. relative change ofInternal distance between x-values allowed in one
     * step
     *
     */
    private final static double MMAXDIST = 1e3;
    /**
     * if |f2|<FACTOR*macc and (x2-x1)/x2<FACTOR*macc then root is determined;
     * end routine
     *
     */
    private final static double MFACTOR = 1e5;
    /**
     * halve distance between old and new x2 max. KITERMAX times in case
     * ofInternal possible overflow
     *
     */
    private final static double MKITERMAX = 1e3;
    /**
     * initialisation ofInternal |P(x)|^2
     */
    private final static double MFVALUE = 1e36;
    /**
     * improve convergence in case ofInternal small changes
     */
    private final static double MBOUND1 = 1.01;
    /**
     * ofInternal |P(x)|^2
     */
    private final static double MBOUND2 = 0.99;
    private final static double MBOUND3 = 0.01;
    /**
     * if |P(x2).r|+|P(x2).i|>BOUND4 => suppress overflow ofInternal |P(x2)|^2
     */
    private final double MBOUND4;
    /**
     * if |x2|^nred>10^BOUND6 => suppress overflow ofInternal P(x2)
     */
    private final double MBOUND6;
    /**
     * relative distance between determined root and real root bigger than
     * BOUND7 => 2. iteration
     */
    private final static double MBOUND7 = 1e-5;
    /**
     * when noise starts counting
     */
    private final double MNOISESTART;
    /**
     * if noise>NOISEMAX: terminate iteration
     */
    private final static double MNOISEMAX = 5;
    private static final double ISQRT2 = 1.0 / Math.sqrt(2.0);

    /**
     * max. number ofInternal iterations with no better value
     */
    private final static int NNOISEMAX = 5;
    /**
     * smallest such that 1.0+DBL_EPSILON != 1.0
     */
    private final static double DBL_EPSILON = 2.2204460492503131e-016;
    /**
     * common points [x0,f(x0)=P(x0)], ... [x2,f(x2)]
     */
    Complex x0, x1, x2;
    /**
     * distance between x2 and x1
     */
    Complex h1, h2;
    /**
     * smaller root of parabola
     */
    Complex q2;
    Complex f0, f1, f2;
    /* ofInternal parabola and polynomial */

    int iter;

    /* iteration counter */
    /**
     * Default constructor
     */
    public RobustMullerNewtonSolver() {
        MBOUND4 = Math.sqrt(Double.MAX_VALUE) / 1e4;
        MBOUND6 = Math.log10(MBOUND4) - 4;
        MNOISESTART = DBL_EPSILON * 1e2;
        x0 = Complex.ZERO;
        x1 = Complex.ZERO;
        x2 = Complex.ZERO;
        h1 = Complex.ZERO;
        h2 = Complex.ZERO;
        q2 = Complex.ZERO;
        f0 = Complex.ZERO;
        f1 = Complex.ZERO;
        f2 = Complex.ZERO;
    }

    /**
     * *** is the new x2 the best approximation? ****
     */
    private void check_x_value(final Ref<Complex> xb,
            final DoubleRef f2absqb, final BooleanRef rootd,
            final double f1absq, final double f2absq, final double epsilon,
            final IntRef noise) /* Complex *xb; best x-value */ /* double *f2absqb, f2absqb |P(xb)|^2 */ /* f1absq, f1absq = |f1|^2 */ /* f2absq, f2absq = |f2|^2 */ /* epsilon; bound for |q2| */ /* int *rootd, *rootd = 1 => root determined */ /* *rootd = 0 => no root determined */ /* *noise; noisecounter */ {
        if ((f2absq <= (MBOUND1 * f1absq)) && (f2absq >= (MBOUND2 * f1absq))) /* function-value changes slowly */ {
            if (h2.abs() < MBOUND3) {
                /* if |h[2]| is small enough => */
                q2 = q2.times(2);
                /* double q2 and h[2] */
                h2 = h2.times(2);
            } else {
                /* otherwise: |q2| = 1 and */
 /* h[2] = h[2]*q2 */
                q2 = getComplexForIterationCounter(iter);
                h2 = h2.times(q2);
            }
        } else if (f2absq < f2absqb.val) {
            f2absqb.val = f2absq;
            /* the new function value is the */
            xb.val = x2;
            /* best approximation */
            noise.val = 0;
            /* reset noise counter */
            if ((Math.sqrt(f2absq) < epsilon)
                    && (x2.minus(x1).div(x2).abs() < epsilon)) {
                rootd.val = true;
                /* root determined */
            }
        }
    }

    public void clear() {
        roots = null;
        remainder = null;
    }

    /**
     * *** compute P(x2) and make some checks ****
     */
    private void compute_function(final double f1absq,
            final DoubleRef f2absq, final double epsilon) /* Complex *pred; coefficient vector ofInternal the deflated polynomial */ /* int nred; the highest exponent ofInternal the deflated polynomial */ /* double f1absq, f1absq = |f1|^2 */ /* *f2absq, f2absq = |f2|^2 */ /* epsilon; bound for |q2| */ {
        // overflow = 1 => overflow occures
        // overflow = 0 => no overflow occures
        final IntRef overflow = new IntRef(0);

        do {
            /* initial estimation: no overflow */
            overflow.val = 0;

            /* suppress overflow */
            suppress_overflow();

            /* calculate new value => result in f2 */
            PolynomialComputer fn = new PolynomialComputer(Polynomial.of(reducedPolynomial, startIdx, reducedPolynomial.length));
            f2 = fn.compute(x2).f();

            /* check ofInternal too big function values */
            too_big_functionvalues(f2absq);

            /* increase iterationcounter */
            iter++;

            /* Muller's modification to improve convergence */
            convergence_check(overflow, f1absq, f2absq.val, epsilon);
        } while (overflow.val != 0);
    }

    /**
     * *** Muller's modification to improve convergence ****
     */
    private void convergence_check(final IntRef overflow,
            final double f1absq, final double f2absq, final double epsilon) /* double f1absq, f1absq = |f1|^2 */ /* f2absq, f2absq = |f2|^2 */ /* epsilon; bound for |q2| */ /* int *overflow; *overflow = 1 => overflow occures */ /* *overflow = 0 => no overflow occures */ {
        if ((f2absq > (MCONVERGENCE * f1absq)) && (q2.abs() > epsilon)
                && (iter < MITERMAX)) {
            q2 = q2.times(.5);
            /* in case ofInternal overflow: */
            h2 = h2.times(.5);
            /* halve q2 and h2; compute new x2 */
            x2 = x2.minus(h2);
            overflow.val = 1;
        }
    }

    @Override
    public boolean factorize(final Polynomial p) {
        // if (p == null)
        // throw new ArgumentNullException("p");
        // initialization ...
        startIdx = 0;
        degree = p.degree();
        while ((degree > 0) && (p.get(degree) == 0)) {
            --degree;
        }
        if (degree == 0) {
            return false;
        }
        roots = new Complex[degree];
        polynomial = p.toArray();
        reducedPolynomial = polynomial.clone();
        if (!newtonnull()) {
            return false;
        }
        for (int j = 1; j < degree; j++) {
            // Sort roots by their real parts by straight insertion.
            final Complex tmp = roots[j];
            int i = j - 1;
            for (; i >= 0; i--) {
                if (roots[i].getRe() <= tmp.getRe()) {
                    break;
                }
                roots[i + 1] = roots[i];
            }
            roots[i + 1] = tmp;
        }
        remainder = Polynomial.valueOf(p.get(p.degree()));
        return true;
    }

    /**
     * initializing routine
     *
     * @param xb
     * @param epsilon
     */
    private void initialize(final Ref<Complex> xb, final DoubleRef epsilon) /* Complex *pred, coefficient vector ofInternal the deflated polynomial */ /* *xb; best x-value */ /* double *epsilon; bound for |q2| */ {
        /* initial estimations for x0,...,x2 and its values */
 /* ml, 12-21-94 changed */

        x0 = Complex.ZERO;
        /* x0 = 0 + j*1 */
        x1 = Complex.cart(-ISQRT2, -ISQRT2);
        /* x1 = 0 - j*1 */
        x2 = Complex.cart(ISQRT2, ISQRT2);
        /* x2 = (1 + j*1)/Sqrt(2) */

        h1 = x1.minus(x0);
        /* h1 = x1 - x0 */
        h2 = x2.minus(x1);
        /* h2 = x2 - x1 */
        q2 = h2.div(h1);
        /* q2 = h2/h1 */

        xb.val = x2;
        /* best initial x-value = zero */
        epsilon.val = MFACTOR * DBL_EPSILON;/* accuracy for determined root */
        iter = 0;
        /* reset iteration counter */
    }

    /**
     * main iteration equation: x2 = h2*q2 + x2
     *
     * @param h2abs
     */
    private void iteration_equation(final DoubleRef h2abs) /* double *h2abs; Absolute value ofInternal the old distance */ {
        h2 = h2.times(q2);
        // distance between old and new x2
        double h2absnew = h2.abs();

        if (h2absnew > h2abs.val * MMAXDIST) {
            /* maximum relative change */
            double help = MMAXDIST / h2absnew;
            h2 = h2.times(help);
            q2 = q2.times(help);
        }

        h2abs.val = h2absnew;
        /* actualize old distance for next iteration */

        x2 = x2.plus(h2);
    }

    /**
     * **** lin_or_quad() calculates roots ofInternal lin. or quadratic
     * equation ****
     */
    private boolean lin_or_quad() /* Complex *pred, coefficient vector ofInternal the deflated polynomial */ /* *root; determined roots */ /* int nred; highest exponent ofInternal the deflated polynomial */ {
        int nred = reducedPolynomial.length - startIdx - 1;
        if (nred == 1) {
            /* root = -p0/p1 */
            roots[startIdx] = Complex.cart(-reducedPolynomial[startIdx] / reducedPolynomial[startIdx + 1]);
            return true;
            /* and return no error */
        } else if (nred == 2) {
            /* quadratic polynomial */
            quadratic();
            return true;
            /* return no error */
        }
        return false;
        /* nred>2 => no roots were calculated */
    }

    /**
     * *** monic() computes monic polynomial for original polynomial ****
     */
    private void monic() {
        // factor stores absolute value ofInternal the coefficient */
        /* with highest exponent */
        int n = polynomial.length - 1;
        double factor = Math.abs(1 / polynomial[n]);
        /* factor = |1/pn| */
        if (factor != 1) /* get monic pol., when |pn| != 1 */ {
            for (int i = 0; i <= n; i++) {
                polynomial[i] *= factor;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * main routine ofInternal Mueller's method
     *
     * @return
     */
    private Complex muller() {
        double f1absq;
        /* f1absq=|f1|^2 */
        final DoubleRef f2absq = new DoubleRef(MFVALUE);
        /* f2absq=|f2|^2 */
        final DoubleRef f2absqb = new DoubleRef(MFVALUE);
        /*
         * f2absqb=|P(xb)|^
         * 2
         */
        final DoubleRef h2abs = new DoubleRef(0d);
        /* h2abs=|h2| */
        final DoubleRef epsilon = new DoubleRef(0d);
        /* bound for |q2| */
        final IntRef seconditer = new IntRef(0);
        /*
         * second
         * iteration, when
         * root is too bad
         */
        final IntRef noise = new IntRef(0);
        /* noise counter */
        final BooleanRef rootd = new BooleanRef(false);
        /*
         * rootd = 1 => root
         * determined
         */
 /* rootd = 0 => no root determined */
        final Ref<Complex> xb = new Ref<>(Complex.ZERO);
        /*
         * best x-value
         */

 /* initializing routine */
        initialize(xb, epsilon);

        PolynomialComputer fn = new PolynomialComputer(Polynomial.of(reducedPolynomial, startIdx, reducedPolynomial.length));
        f0 = fn.compute(x0).f();
        f1 = fn.compute(x1).f();
        f2 = fn.compute(x2).f();

        do {
            /* loop for possible second iteration */
            do {
                /* main iteration loop */
 /* calculate the roots ofInternal the parabola */
                root_of_parabola();

                /* store values for the next iteration */
                x0 = x1;
                x1 = x2;
                h2abs.val = h2.abs();
                /*
                 * distance between x2 and x1
                 */

 /* main iteration-equation */
                iteration_equation(h2abs);

                /* store values for the next iteration */
                f0 = f1;
                f1 = f2;
                f1absq = f2absq.val;

                /* compute P(x2) and make some checks */
                compute_function(f1absq, f2absq, epsilon.val);

                /* printf("betrag %10.5e %4.2d %4.2d\n",f2absq,iter,seconditer); */

 /* is the new x-value the best approximation? */
                check_x_value(xb, f2absqb, rootd, f1absq, f2absq.val,
                        epsilon.val, noise);

                /* increase noise counter */
                double xb_abs = xb.val.abs();
                if (Math.abs(xb_abs - x2.abs()) / xb_abs < MNOISESTART) {
                    noise.val++;
                }
            } while ((iter < MITERMAX) && (!rootd.val)
                    && (noise.val <= MNOISEMAX));

            seconditer.val++;
            /* increase seconditer */

 /* check, if determined root is good enough */
            root_check(f2absqb.val, seconditer, rootd, noise, xb.val);
        } while (seconditer.val == 2);
        return xb.val;
        /* return best x value */
    }

    private boolean newtonnull() {
        maxError = 0;
        /* initialize max. error ofInternal determined roots */
 /* check input ofInternal the polynomial */

        roots_at_zero();
        if (startIdx == polynomial.length - 1) {
            return true;
        }

        /* polynomial is linear or quadratic */
        if (lin_or_quad()) {
            maxError = DBL_EPSILON;
            return true;
            /* return no error */
        }

        monic();
        /* get monic polynom */

        do {
            final Complex ns = muller();
            /* Newton method */
            NewtonOptimizer optimizer = new NewtonOptimizer(Polynomial.of(polynomial, 0, polynomial.length), true);
            Complex nroot = optimizer.root(ns);
            /* stores max. error ofInternal all roots */
            if (optimizer.getError() > maxError) {
                maxError = optimizer.getError();
            }
            /* deflate polynomial */
            if (nroot.getIm() == 0) {
                update(nroot.getRe(), optimizer.getMultiplicity());
            } else {
                update(nroot, optimizer.getMultiplicity());
            }
        } while (polynomial.length - startIdx > 3);
        /* last one or two roots */
        lin_or_quad();
//	if (m_p.length - m_idx == 3) {
//	    m_roots[m_idx + 1] = newton(m_roots[m_idx + 1], newerr);
//	    if (newerr.val > m_maxerr)
//		m_maxerr = newerr.val;
//	}
//	m_roots[m_idx] = newton(m_roots[m_idx], newerr);
//	if (newerr.val > m_maxerr)
//	    m_maxerr = newerr.val;

        return true;
        /* return no error */
    }

    /**
     * **** quadratic() calculates the roots of a quadratic polynomial ****
     */
    private void quadratic() {
        /* discr = p1^2-4*p2*p0 */
        double a = reducedPolynomial[startIdx + 2], b = reducedPolynomial[startIdx + 1], c = reducedPolynomial[startIdx], aa = 2 * a;
        double rdiscr = b * b - 4 * a * c;
        if (rdiscr < 0) {
            double z = Math.sqrt(-rdiscr);
            Complex r = Complex.cart(-b / aa, +z / aa);
            roots[startIdx] = r;
            roots[startIdx + 1] = r.conj();
        } else {
            double z = Math.sqrt(rdiscr);
            roots[startIdx] = Complex.cart((-b + z) / aa);
            roots[startIdx + 1] = Complex.cart((-b - z) / aa);
        }
    }

    @Override
    public Polynomial remainder() {
        return remainder;
    }

    /**
     * *** check, if determined root is good enough. ****
     */
    private void root_check(final double f2absqb,
            final IntRef seconditer, final BooleanRef rootd,
            final IntRef noise, final Complex xb) /* Complex *pred, coefficient vector ofInternal the deflated polynomial */ /* xb; best x-value */ /* int nred, the highest exponent ofInternal the deflated polynomial */ /* *noise, noisecounter */ /* *rootd, *rootd = 1 => root determined */ /* *rootd = 0 => no root determined */ /* *seconditer; *seconditer = 1 => reader second iteration with */ /* new initial estimations */ /* *seconditer = 0 => end routine */ /* double f2absqb; f2absqb |P(xb)|^2 */ {
        /* df=P'(x0) */
        Complex df;

        if ((seconditer.val == 1) && (f2absqb > 0)) {
            // f2=P(x0), df=P'(x0)
            PolynomialComputer fn = new PolynomialComputer(Polynomial.of(reducedPolynomial, startIdx, reducedPolynomial.length));
            fn.computeAll(xb);
            f2 = fn.f();
            df = fn.df();
            if (f2.abs() / (df.abs() * xb.abs()) > MBOUND7) {
                /* reader second iteration with new initial estimations */
                x0 = Complex.ONE;
                x1 = Complex.NEG_ONE;
                x2 = Complex.ZERO;
                /*   */
                f0 = fn.compute(x0).f();
                f1 = fn.compute(x1).f();
                f2 = fn.compute(x2).f();
                /* f2 = P(x2) */
                iter = 0;
                /* reset iteration counter */
                ++seconditer.val;
                /* increase seconditer */
                rootd.val = false;
                /* no root determined */
                noise.val = 0;
                /* reset noise counter */
            }
        }
    }

    /**
     * calculate smaller root of Muller's parabola
     */
    private void root_of_parabola() {
        /* A2 = q2(f2 - (1+q2)f1 + f0q2) */
 /* B2 = q2[q2(f0-f1) + 2(f2-f1)] + (f2-f1) */
 /* C2 = (1+q2)f[2] */

        final Complex A2 = computeA2(q2, f2, f0, f1);
        final Complex B2 = computeB2(f2, f1, q2, f0);
        final Complex C2 = computeC2(q2, f2);

        /* discr = B2^2 - 4A2C2 */
        final Complex rdiscr = computeDiscr(B2, A2, C2).sqrt();

        /* denominators ofInternal q2 */
        final Complex N1 = B2.minus(rdiscr);
        final Complex N2 = B2.plus(rdiscr);
        double N1_abs = N1.abs();
        double N2_abs = N2.abs();
        /* choose denominater with largest modulus */
        if ((N1_abs > N2_abs) && (N1_abs > DBL_EPSILON)) {
            q2 = C2.times(-2).div(N1);
        } else if (N2_abs > DBL_EPSILON) {
            q2 = C2.times(-2).div(N2);
        } else {
            q2 = getComplexForIterationCounter(iter);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Generated code">
    /**
     * Computes: <code>q2 * ( f2 + ( q2 * f0 ) - ( 1 + q2 ) * f1 )</code><br>
     * RPN: <code>q2 f2 q2 f0 * + 1 q2 + f1 * - *</code>
     */
    @VisibleForTesting
    static Complex computeA2(Complex q2, Complex f2, Complex f0, Complex f1) {
        double tmp;
        double re0, re1, re2;
        double im0, im1, im2;
        /* { q2 } */
        re0 = q2.getRe();
        im0 = q2.getIm();
        /* { f2 } */
        re1 = f2.getRe();
        im1 = f2.getIm();
        /* { q2 f0 * } */
        re2 = q2.getRe() * f0.getRe() - q2.getIm() * f0.getIm();
        im2 = q2.getRe() * f0.getIm() + q2.getIm() * f0.getRe();
        /* { + } */
        re1 += re2;
        im1 += im2;
        /* { 1 q2 + } */
        re2 = 1 + q2.getRe();
        im2 = q2.getIm();
        /* { f1 * } */
        tmp = re2 * f1.getRe() - im2 * f1.getIm();
        im2 = re2 * f1.getIm() + im2 * f1.getRe();
        re2 = tmp;
        /* { - } */
        re1 -= re2;
        im1 -= im2;
        /* { * } */
        tmp = re0 * re1 - im0 * im1;
        im0 = re0 * im1 + im0 * re1;
        re0 = tmp;
        /* .build() */
        return Complex.cart(re0, im0);
    }

    /**
     * Computes:
     * <code>f2 - f1 + q2 * ( q2 * ( f0 - f1 ) + ( f2 - f1 ) * 2 )</code><br>
     * RPN: <code>f2 f1 - q2 q2 f0 f1 - * f2 f1 - 2 * + * +</code>
     */
    @VisibleForTesting
    static Complex computeB2(Complex f2, Complex f1, Complex q2, Complex f0) {
        double tmp;
        double re0, re1, re2, re3;
        double im0, im1, im2, im3;
        /* { f2 f1 - } */
        re0 = f2.getRe() - f1.getRe();
        im0 = f2.getIm() - f1.getIm();
        /* { q2 } */
        re1 = q2.getRe();
        im1 = q2.getIm();
        /* { q2 } */
        re2 = q2.getRe();
        im2 = q2.getIm();
        /* { f0 f1 - } */
        re3 = f0.getRe() - f1.getRe();
        im3 = f0.getIm() - f1.getIm();
        /* { * } */
        tmp = re2 * re3 - im2 * im3;
        im2 = re2 * im3 + im2 * re3;
        re2 = tmp;
        /* { f2 f1 - } */
        re3 = f2.getRe() - f1.getRe();
        im3 = f2.getIm() - f1.getIm();
        /* { 2 * } */
        re3 *= 2;
        im3 *= 2;
        /* { + } */
        re2 += re3;
        im2 += im3;
        /* { * } */
        tmp = re1 * re2 - im1 * im2;
        im1 = re1 * im2 + im1 * re2;
        re1 = tmp;
        /* { + } */
        re0 += re1;
        im0 += im1;
        /* .build() */
        return Complex.cart(re0, im0);
    }

    /**
     * Computes: <code>( 1 + q2 ) * f2</code><br>
     * RPN: <code>1 q2 + f2 *</code>
     */
    @VisibleForTesting
    static Complex computeC2(Complex q2, Complex f2) {
        double tmp;
        double re0;
        double im0;
        /* { 1 q2 + } */
        re0 = 1 + q2.getRe();
        im0 = q2.getIm();
        /* { f2 * } */
        tmp = re0 * f2.getRe() - im0 * f2.getIm();
        im0 = re0 * f2.getIm() + im0 * f2.getRe();
        re0 = tmp;
        /* .build() */
        return Complex.cart(re0, im0);
    }

    /**
     * Computes: <code>B2 * B2 - A2 * C2 * 4</code><br>
     * RPN: <code>B2 B2 * A2 C2 * 4 * -</code>
     */
    @VisibleForTesting
    static Complex computeDiscr(Complex B2, Complex A2, Complex C2) {
        double re0, re1;
        double im0, im1;
        /* { B2 B2 * } */
        re0 = B2.getRe() * B2.getRe() - B2.getIm() * B2.getIm();
        im0 = B2.getRe() * B2.getIm() + B2.getIm() * B2.getRe();
        /* { A2 C2 * } */
        re1 = A2.getRe() * C2.getRe() - A2.getIm() * C2.getIm();
        im1 = A2.getRe() * C2.getIm() + A2.getIm() * C2.getRe();
        /* { 4 * } */
        re1 *= 4;
        im1 *= 4;
        /* { - } */
        re0 -= re1;
        im0 -= im1;
        /* .build() */
        return Complex.cart(re0, im0);
    }
    //</editor-fold>

    @Override
    public Complex[] roots() {
        return roots;
    }

    /**
     * **** poly_check() check the formal correctness ofInternal input ****
     */
    private void roots_at_zero() {
        // find roots at 0
        startIdx = 0;
        while ((startIdx < polynomial.length) && (polynomial[startIdx] == 0)) {
            roots[startIdx++] = Complex.ZERO;
        }
    }

    /**
     * suppress overflow
     */
    private void suppress_overflow() {
        final int nred = reducedPolynomial.length - 1 - startIdx;
        boolean loop;
        int kiter = 0;
        /* reset iteration counter */
        do {
            loop = false;
            /* initial estimation: no overflow */
            final double help = x2.abs();
            /* help = |x2| */
            if ((help > 1) && (Math.abs(nred * Math.log10(help)) > MBOUND6)) {
                kiter++;
                /* if |x2|>1 and |x2|^nred>10^BOUND6 */
                if (kiter < MKITERMAX) {
                    /* then halve the distance between */
                    h2 = h2.times(.5);
                    /* new and old x2 */
                    q2 = q2.times(.5);
                    x2 = x2.minus(h2);
                    loop = true;
                } else {
                    kiter = 0;
                }
            }
        } while (loop);
    }

    /**
     * **** check ofInternal too big function values ****
     */
    private void too_big_functionvalues(final DoubleRef f2absq) /* double *f2absq; f2absq=|f2|^2 */ {
        if ((Math.abs(f2.getRe()) + Math.abs(f2.getIm())) > MBOUND4) {
            f2absq.val = Math.abs(f2.getRe()) + Math.abs(f2.getIm());
        } else {
            f2absq.val = f2.absSquare();
            /* |f2|^2 = f2.r^2+f2.i^2 */
        }
    }

    private boolean lqdiv = false;

    public boolean isLeastSquaresDivision() {
        return lqdiv;
    }

    public void setLeastSquaresDivision(boolean lq) {
        lqdiv = lq;
    }

    private void update(final Complex r0, final int mul) {
        double a = -2 * r0.getRe(), b = r0.absSquare();
        for (int k = 0; k < mul; ++k) {
            roots[startIdx++] = r0;
            roots[startIdx++] = r0.conj();

            if (!lqdiv) {
                reducedPolynomial[degree - 1] -= reducedPolynomial[degree] * a;
                for (int i = degree; i > startIdx; i--) {
                    reducedPolynomial[i - 2] -= a * reducedPolynomial[i - 1] + b * reducedPolynomial[i];
                }
            }
        }

        if (lqdiv) {
            Polynomial num = Polynomial.of(reducedPolynomial, startIdx - 2 * mul, degree + 1);
            Polynomial c = Polynomial.ofInternal(new double[]{b, a, 1});
            Polynomial div = c;
            for (int i = 1; i < mul; ++i) {
                div = div.times(c);
            }
            LeastSquaresDivision lq = new LeastSquaresDivision();
            lq.divide(num, div);
            lq.getQuotient().copyTo(reducedPolynomial, startIdx);
        }
    }

    /**
     * divide by the polynomial x-r0; root to be deflated
     *
     * @param r0
     */
    private void update(final double r0, final int mul) {
        for (int k = 0; k < mul; ++k) {
            roots[startIdx++] = Complex.cart(r0);

            if (!lqdiv) {
                for (int i = degree; i > startIdx; i--) {
                    reducedPolynomial[i - 1] += reducedPolynomial[i] * r0;
                }
            }
        }
        if (lqdiv) {
            Polynomial num = Polynomial.of(reducedPolynomial, startIdx - mul, degree + 1);
            Polynomial c = Polynomial.ofInternal(new double[]{-r0, 1});
            Polynomial div = c;
            for (int k = 1; k < mul; ++k) {
                div = div.times(c);
            }
            LeastSquaresDivision lq = new LeastSquaresDivision();
            lq.divide(num, div);
            lq.getQuotient().copyTo(reducedPolynomial, startIdx);
        }
    }

    private static Complex getComplexForIterationCounter(int iter) {
        return COMPLEX_FOR_ITER[iter];
    }

    private static Complex newComplexForIterationCounter(int iter) {
        return Complex.cart(Math.cos(iter), Math.sin(iter));
    }

    // local cache for all possible values
    private static final Complex[] COMPLEX_FOR_ITER = initComplexForIter(MITERMAX);

    /**
     * Computes all possible values for iter
     *
     * @param maxIter
     * @return
     */
    private static Complex[] initComplexForIter(int maxIter) {
        Complex[] result = new Complex[maxIter + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = newComplexForIterationCounter(i);
        }
        return result;
    }
}
