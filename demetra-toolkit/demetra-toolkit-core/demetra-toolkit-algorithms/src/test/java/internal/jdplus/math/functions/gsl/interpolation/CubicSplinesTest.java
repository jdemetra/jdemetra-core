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
        double[] xi = new double[]{5, 10, 15, 20, 30, 50, 60, 75, 90, 120,130};
        double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 4, -3};
        CubicSplines.Spline natural = CubicSplines.natural(DoubleSeq.of(xi), DoubleSeq.of(fxi));
        CubicSplines.Spline periodic = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(fxi));
        DoubleUnaryOperator fn = CubicSpline.of(xi, fxi);
        for (int i = 0; i <= 135; ++i) {
            double f0=fn.applyAsDouble(i);
            double f1 = natural.applyAsDouble(i);
            double f2 = periodic.applyAsDouble(i);
            assertEquals(f0, f1, 1e-6);
        }
    }
    
}
