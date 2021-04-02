/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.jdplus.maths.functions.gsl.roots;

import internal.jdplus.maths.functions.gsl.roots.RootStatus;
import internal.jdplus.maths.functions.gsl.roots.GslRootException;
import internal.jdplus.maths.functions.gsl.roots.FDFSolver;
import internal.jdplus.maths.functions.gsl.roots.FSolver;
import java.util.function.DoubleUnaryOperator;
import internal.jdplus.maths.functions.gsl.Utility;
import static org.junit.Assert.*;

/**
 *
 * @author Mats Maggi
 */
public class SolverTestUtils {

    private static final double EPSREL = Math.pow(Utility.GSL_DBL_EPSILON, .5);
    private static final double EPSABS = EPSREL;
    
    private static final int MAX_ITERATIONS = 150;

    public static void test(FSolver s, String desc, double correctRoot) {
        System.out.println(String.format("%s => expected : %f", desc, correctRoot));
        int iterations = 0;
        RootStatus status;
        do {
            iterations++;
            s.iterate();

            if (s.lower > s.upper) {
                fail(String.format("Interval is invalid (%f %f)", s.lower, s.upper));
            }

            if (s.root < s.lower || s.root > s.upper) {
                fail(String.format("r lies outside interval %f (%f %f)", s.root, s.lower, s.upper));
            }

            // Test interval a b
            status = testInterval(s.lower, s.upper, EPSABS, EPSREL);
        } while (status == RootStatus.CONTINUE && iterations < MAX_ITERATIONS);

        if (iterations == MAX_ITERATIONS) {
            fail("Exceeded maximum number of iterations");
        }

        // Check the validity of the returned result
        System.out.println(String.format("\t===> result : %f\t(%d iterations)", s.root, iterations));
        assertTrue(withinTol(s.root, correctRoot, EPSREL, EPSABS));
    }

    public static void testFDF(FDFSolver s, String desc, double correctRoot) {
        System.out.println(String.format("%s => expected : %f", desc, correctRoot));
        int iterations = 0;
        double prev;
        RootStatus status;

        do {
            iterations++;
            prev = s.root;
            s.iterate();
            status = testDelta(s.root, prev, EPSABS, EPSREL);

        } while (status == RootStatus.CONTINUE && iterations < MAX_ITERATIONS);

        if (iterations == MAX_ITERATIONS) {
            fail("Exceeded maximum number of iterations");
        }

        // Check the validity of the returned result
        System.out.println(String.format("\t===> result : %f\t(%d iterations)", s.root, iterations));
        assertTrue(withinTol(s.root, correctRoot, EPSREL, EPSABS));
    }

    private static RootStatus testInterval(double xLower, double xUpper, double epsAbs, double epsRel) {
        final double absLower = Math.abs(xLower);
        final double absUpper = Math.abs(xUpper);

        double minAbs, tolerance;
        if (epsRel < 0.0) {
            throw new GslRootException("Relative tolerance is negative");
        }

        if (epsAbs < 0.0) {
            throw new GslRootException("Absolute tolerance is negative");
        }

        if (xLower > xUpper) {
            throw new GslRootException("Lower bound larger than upper bound");
        }

        if ((xLower > 0.0 && xUpper > 0.0) || (xLower < 0.0 && xUpper < 0.0)) {
            minAbs = Math.min(absLower, absUpper);
        } else {
            minAbs = 0;
        }

        tolerance = epsAbs + epsRel * minAbs;

        if (Math.abs(xUpper - xLower) < tolerance) {
            return RootStatus.SUCCESS;
        }

        return RootStatus.CONTINUE;
    }

    private static RootStatus testDelta(double x1, double x0, double epsAbs, double epsRel) {
        double tolerance = epsAbs + epsRel * Math.abs(x1);
        if (epsRel < 0.0) {
            throw new GslRootException("Relative tolerance is negative");
        }

        if (epsAbs < 0.0) {
            throw new GslRootException("Absolute tolerance is negative");
        }

        if (Math.abs(x1 - x0) < tolerance || x1 == x0) {
            return RootStatus.SUCCESS;
        }
        return RootStatus.CONTINUE;
    }

    public static boolean withinTol(double a, double b, double epsRel, double epsAbs) {
        return Math.abs(a - b) < (epsRel * Math.min(Math.abs(a), Math.abs(b)) + epsAbs);
    }

    public static void testAll(String name, Factory factory) {
        System.out.println("\n" + name);
        SolverTestUtils.test(factory.build(Math::sin, 3.0, 4.0), "sin(x) [3, 4]", Math.PI);
        SolverTestUtils.test(factory.build(Math::sin, -4.0, -3.0), "sin(x) [-4, 3]", -Math.PI);
        SolverTestUtils.test(factory.build(Math::sin, -1.0 / 3.0, 1.0), "sin(x) [-1/3, 1]", 0.0);
        SolverTestUtils.test(factory.build(Math::cos, 0.0, 3.0), "cos(x) [0, 3]", Math.PI / 2.0);
        SolverTestUtils.test(factory.build(Math::cos, -3, 0.0), "cos(x) [-3, 0]", -Math.PI / 2.0);
        SolverTestUtils.test(factory.build(x -> Math.sqrt(Math.abs(x)) * Math.signum(x), -1.0 / 3.0, 1.0), "sqrt(|x|)*sgn(x) [-1/3, 1]", 0.0);
        SolverTestUtils.test(factory.build(x -> x * x - 1e-8, 0.0, 1.0), "x^2 - 1e-8 [0, 1]", Math.sqrt(1e-8));
        SolverTestUtils.test(factory.build(x -> x * Math.exp(-x), -1.0 / 3.0, 2.0), "x exp(-x) [-1/3, 2]", 0.0);
        SolverTestUtils.test(factory.build(x -> Math.pow((x - 1), 7), 0.9995, 1.0002), "(x - 1)^7 [0.9995, 1.0002]", 1.0);
    }

    public static void testAll(String name, FactoryFDF factoryFDF) {
        System.out.println("\n" + name);
        SolverTestUtils.testFDF(factoryFDF.build(Math::sin, Math::cos, 3.4), "sin(x) {3.4}", Math.PI);
        SolverTestUtils.testFDF(factoryFDF.build(Math::sin, Math::cos, -3.3), "sin(x) {-3.3}", -Math.PI);
        SolverTestUtils.testFDF(factoryFDF.build(Math::sin, Math::cos, 0.5), "sin(x) {0.5}", 0.0);
        SolverTestUtils.testFDF(factoryFDF.build(Math::cos, x -> -Math.sin(x), 0.6), "cos(x) {0.6}", Math.PI / 2.0);
        SolverTestUtils.testFDF(factoryFDF.build(Math::cos, x -> -Math.sin(x), -2.5), "sin(x) {-3.3}", -Math.PI / 2.0);
        SolverTestUtils.testFDF(factoryFDF.build(x -> Math.pow(x, 20) - 1, x -> 20.0 * Math.pow(x, 19.0), 0.9), "x^20 {0.9}", 1.0);
        SolverTestUtils.testFDF(factoryFDF.build(x -> Math.pow(x, 20) - 1, x -> 20.0 * Math.pow(x, 19.0), 1.1), "x^20 {1.1}", 1.0);
        SolverTestUtils.testFDF(factoryFDF.build(x -> Math.sqrt(Math.abs(x)) * Math.signum(x), x -> 1.0 / Math.sqrt(Math.abs(x)), 0.001), "sqrt(|x|)*sgn(x) {0.001}", 0.0);
        SolverTestUtils.testFDF(factoryFDF.build(x -> x * x - 1e-8, x -> 2 * x, 1.0), "x^2 - 1e-8 {1}", Math.sqrt(1e-8));
        SolverTestUtils.testFDF(factoryFDF.build(x -> x * Math.exp(-x), x -> Math.exp(-x) - x * Math.exp(-x), -2.0), "x exp(-x) {-2}", 0.0);
        SolverTestUtils.testFDF(factoryFDF.build(x -> -Math.PI * x + Math.E, x -> -Math.PI, 1.5), "-pi * x + e {1.5}", Math.E / Math.PI);
    }

    @FunctionalInterface
    public interface Factory {

        FSolver build(DoubleUnaryOperator fn, double xLower, double xUpper);

    }

    @FunctionalInterface
    public interface FactoryFDF {

        FDFSolver build(DoubleUnaryOperator f, DoubleUnaryOperator fDf, double root);
    }
}
