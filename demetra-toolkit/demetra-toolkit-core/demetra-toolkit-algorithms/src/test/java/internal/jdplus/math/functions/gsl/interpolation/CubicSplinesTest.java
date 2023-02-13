/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package internal.jdplus.math.functions.gsl.interpolation;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.math.functions.CubicSpline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class CubicSplinesTest {

    public CubicSplinesTest() {
    }

    @Test
    public void testSimple() {
        double[] xi = new double[]{5, 10, 15, 20, 30, 50, 60, 75, 90, 120, 130};
        double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 4, -3};
        CubicSplines.Spline natural = CubicSplines.natural(DoubleSeq.of(xi), DoubleSeq.of(fxi));
        CubicSplines.Spline periodic = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(fxi));
        DoubleUnaryOperator fn = CubicSpline.of(xi, fxi);
        for (int i = 0; i <= 135; ++i) {
            double f0 = fn.applyAsDouble(i);
            double f1 = natural.applyAsDouble(i);
            double f2 = periodic.applyAsDouble(i);
            assertEquals(f0, f1, 1e-6);
        }
    }

    @Test
    public void testAdditive() {
        double[] xi = new double[]{0, 10, 15, 20, 30, 50, 60, 75, 90, 120, 130};
        double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 4, -3};
        CubicSplines.Spline periodic = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(fxi));
        CubicSplines.Spline[] nodes = new CubicSplines.Spline[xi.length - 1];
        for (int i = 0; i < nodes.length; ++i) {
            double[] f = new double[xi.length];
            if (i == 0) {
                f[0] = 1;
                f[nodes.length] = 1;
            } else {
                f[i] = 1;
            }
            nodes[i] = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(f));
        }
        for (int i = 0; i <= 130; ++i) {
            double f = periodic.applyAsDouble(i);
            double s = 0;
            for (int j = 0; j < nodes.length; ++j) {
                double z = nodes[j].applyAsDouble(i);
                s += z * fxi[j];
                System.out.print(z);
                System.out.print('\t');
            }
            System.out.print(s);
            System.out.print('\t');
            System.out.println(f);

        }
    }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        for (int j = 0; j < 10000; ++j) {
            double[] xi = new double[]{5, 10, 15, 20, 30, 50, 60, 75, 90, 120, 130};
            double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 4, -3};
            CubicSplines.Spline periodic = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(fxi));
            for (int i = 0; i <= 13500; ++i) {
                double f = periodic.applyAsDouble(0.01 * i);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
