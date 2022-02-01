/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package internal.jdplus.maths.functions.gsl.interpolation;

import java.util.function.DoubleUnaryOperator;
import jdplus.math.functions.CubicSpline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class CubicSplinesTest {
    
    public CubicSplinesTest() {
    }

   @Test
    public void testSimple() {
        double[] xi = new double[]{5, 10, 15, 20, 30, 50, 60, 75, 90, 130};
        double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 11};
        CubicSplines.Spline natural = CubicSplines.natural(xi, fxi);
        CubicSplines.Spline periodic = CubicSplines.periodic(xi, fxi);
        DoubleUnaryOperator fn = CubicSpline.of(xi, fxi);
        for (int i = 0; i < 135; ++i) {
            double f1 = natural.eval(i);
            double f2 = periodic.eval(i);
            System.out.print(fn.applyAsDouble(i));
            System.out.print('\t');
            System.out.print(f1);
            System.out.print('\t');
            System.out.println(f2);
        }
    }
    
}
