/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.data.DoubleSeq;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import jdplus.math.functions.CubicSpline;
import jdplus.math.functions.DefaultDomain;
import jdplus.math.functions.GenericCubicSpline;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.matrices.MatrixException;
import jdplus.math.polynomials.Polynomial;

/**
 * Temporal disaggregation by means of Cubic splines
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AggregationCubicSpline {

    public double[] disaggregate(double[] fx, int ratio) {
        double[] rslt = new double[fx.length * ratio];
        double[] xi = new double[fx.length + 1];
        for (int i = 0, j = 0; i < xi.length; ++i, j += ratio) {
            xi[i] = j;
        }
        GenericCubicSpline agg = aggregationSplineOf(xi, fx);
        for (int i = 0, j = 0; i < fx.length; ++i) {
            DoubleSeq c = agg.polynomial(i);
            double m0, m1 = 0;
            for (int k = 1; k <= ratio; ++k) {
                m0 = m1;
                m1 = c.get(0) * k + c.get(1) * k * k / 2 + c.get(2) * k * k * k / 3 + c.get(3) * k * k * k * k / 4;
                rslt[j++] = m1 - m0;
            }
        }
        return rslt;
    }

    public double[] disaggregateByCumul(double[] fx, int ratio) {
        double[] rslt = new double[fx.length * ratio];
        double[] cfi = new double[fx.length + 1];
        double[] xi = new double[fx.length + 1];
        for (int i = 1; i < xi.length; ++i) {
            xi[i] = xi[i - 1] + ratio;
            cfi[i] = cfi[i - 1] + fx[i - 1];
        }

        DoubleUnaryOperator cs = CubicSpline.of(xi, cfi);
        double m1 = cs.applyAsDouble(0), m0;
        for (int i = 0; i < rslt.length; ++i) {
            m0 = m1;
            m1 = cs.applyAsDouble(i + 1);
            rslt[i] = m1 - m0;
        }
        return rslt;
    }

    public double[] disaggregateByCumul(double[] fx, int ratio, double[] x, int offset) {
        double[] rslt = new double[x.length];
        double[] cfi = new double[fx.length + 1];
        double[] cxi = new double[fx.length + 1];
        for (int j=0; j<offset; ++j){
            cxi[0]+=x[j];
        }
        for (int i = 1, j=offset; i < cfi.length; ++i) {
            cxi[i]=cxi[i-1];
            for (int k=0; k<ratio; ++k, ++j){
                cxi[i]+=x[j];
            }
            cfi[i] = cfi[i - 1] + fx[i - 1];
        }

        DoubleUnaryOperator cs = CubicSpline.of(cxi, cfi);
        double m1 = cs.applyAsDouble(0), m0;
        double cx=0;
        for (int i = 0; i < rslt.length; ++i) {
            cx+=x[i];
            m0 = m1;
            m1 = cs.applyAsDouble(cx);
            rslt[i] = m1 - m0;
        }
        return rslt;
    }
//    public double[] disaggregateIteratively(double[] fx, int ratio) {
//        double[] xi = new double[6];
//        for (int i = 0, j = 0; i < 6; ++i, j += ratio) {
//            xi[i] = j;
//        }
//        double[] rslt = new double[fx.length * ratio];
//        // starting
//        GenericCubicSpline agg = aggregationSplineOf(xi, Arrays.copyOf(fx, 5));
//        int idx = 0;
//        DoubleSeq c = null;
//        for (int i = 0; i < 2; ++i) {
//            c = agg.polynomial(i);
//            double m0, m1 = 0;
//            for (int k = 1; k <= ratio; ++k) {
//                m0 = m1;
//                m1 = c.get(0) * k + c.get(1) * k * k / 2 + c.get(2) * k * k * k / 3 + c.get(3) * k * k * k * k / 4;
//                rslt[idx++] = m1 - m0;
//            }
//        }
//        // next steps;
//        xi = new double[5];
//        for (int i = 0, j = 0; i < 5; ++i, j += ratio) {
//            xi[i] = j;
//        }
//        int fend = 6;
//        while (fend <= fx.length) {
//            int fcur = fend - 4;
//            double x = ratio, x2 = x * x, x3 = x2 * x;
//            double f = c.get(0) + c.get(1) * x + c.get(2) * x2 + c.get(3) * x3;
//            double df = c.get(1) + 2 * c.get(2) * x + 3 * c.get(3) * x2;
//            double d2f = 2 * c.get(2) + 6 * c.get(3) * x;
//
//            agg = GenericCubicSpline.ofAggregation(xi, Arrays.copyOfRange(fx, fcur, fend),
//                    new GenericCubicSpline.BoundaryConstraints(f, df, Double.NaN), GenericCubicSpline.NATURAL);
//            c = agg.polynomial(0);
//            double m0, m1 = 0;
//            for (int k = 1; k <= ratio; ++k) {
//                m0 = m1;
//                m1 = c.get(0) * k + c.get(1) * k * k / 2 + c.get(2) * k * k * k / 3 + c.get(3) * k * k * k * k / 4;
//                rslt[idx++] = m1 - m0;
//            }
//            ++fend;
//        }
//        // end
//
//        for (int i = 1; i < agg.getPolynomialsCount(); ++i) {
//            c = agg.polynomial(i);
//            double m0, m1 = 0;
//            for (int k = 1; k <= ratio; ++k) {
//                m0 = m1;
//                m1 = c.get(0) * k + c.get(1) * k * k / 2 + c.get(2) * k * k * k / 3 + c.get(3) * k * k * k * k / 4;
//                rslt[idx++] = m1 - m0;
//            }
//        }
//        return rslt;
//    }
//

    public GenericCubicSpline aggregationSplineOf(double[] xi, double[] fx) {
        AggregationFunction fn = new AggregationFunction(xi, fx);
        int n = xi.length - 1;
        double f = fx[n - 1] / (xi[n] - xi[n - 1]);
        Bfgs bfgs = Bfgs.builder().build();
        bfgs.minimize(fn.evaluate(DoubleSeq.of(f)));
        AggregationFunction.Point rslt = (AggregationFunction.Point) bfgs.getResult();
        return rslt.spline();
    }

    static class AggregationFunction implements IFunction {

        private final double[] xi, fx;
        private final double range;

        AggregationFunction(double[] xi, double[] fx) {
            this.fx = fx;
            this.xi = xi;
            double min = fx[0] / (xi[1] - xi[0]), max = min;
            for (int i = 1; i < fx.length; ++i) {
                double c = fx[i] / (xi[i + 1] - xi[i]);
                if (c < min) {
                    min = c;
                } else if (c > max) {
                    max = c;
                }
            }
            range = max - min;
        }

        @Override
        public IFunctionPoint evaluate(DoubleSeq parameters) {
            return new Point(parameters.get(0));
        }

        @Override
        public IParametersDomain getDomain() {
            return new DefaultDomain(1, range == 0 ? 1e-6 : range * 1e-6);
        }

        class Point implements IFunctionPoint {

            private final double fn;
            private final GenericCubicSpline spline;

            GenericCubicSpline spline() {
                return spline;
            }

            Point(double fn) {
                this.fn = fn;
                GenericCubicSpline cs;
                try {
                    cs = GenericCubicSpline.ofAggregation(xi, fx, null, new GenericCubicSpline.BoundaryConstraints(fn, Double.NaN, 0));
                } catch (MatrixException err) {
                    cs = null;
                }
                spline = cs;
            }

            @Override
            public IFunction getFunction() {
                return AggregationFunction.this;
            }

            @Override
            public DoubleSeq getParameters() {
                return DoubleSeq.of(fn);
            }

            @Override
            public double getValue() {
                if (spline == null) {
                    return Double.NaN;
                }
                // Computes the smoothness criterion
                double s = 0;
                for (int i = 0; i < xi.length - 1; ++i) {
                    DoubleSeq p = spline.polynomial(i);
                    double dx = xi[i + 1] - xi[i], dx2 = dx * dx, dx3 = dx * dx2;
                    double a2 = p.get(2), a3 = p.get(3);
                    s += a2 * a2 * dx + 3 * a2 * a3 * dx2 + 3 * a3 * a3 * dx3;
                }
                return s;
            }

        }

    }

}
