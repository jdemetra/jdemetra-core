/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.functions;

import demetra.data.DoubleSeq;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class GenericCubicSplineTest {
    
    public GenericCubicSplineTest() {
    }

   @Test
    public void testSimple() {
        double[] xi = new double[]{5, 10, 15, 20, 30, 50, 60, 75, 90, 130};
        double[] fxi = new double[]{-3, 20, -10, 5, 11, -3, 20, -10, 5, 11};
        DoubleUnaryOperator fn = CubicSpline.of(xi, fxi);
        DoubleUnaryOperator fn2 = GenericCubicSpline.of(xi, fxi, null, null);
        DoubleUnaryOperator fn3 = GenericCubicSpline.of(xi, fxi, GenericCubicSpline.STABLE, null);
        for (int i = 0; i < 135; ++i) {
            double f = fn.applyAsDouble(i);
            double f2 = fn2.applyAsDouble(i);
            double f3 = fn3.applyAsDouble(i);
//            System.out.print(f);
//            System.out.print('\t');
//            System.out.println(f3);
            assertEquals(f, f2, 1e-12);
        }
    }
    
    @Test
    public void testAggregation() {
        double[] xi = new double[20];
        for (int i=0; i<xi.length; ++i)
            xi[i]=4*i;
        double[] fx = new double[xi.length-1];
        Random rnd=new Random(0);
        double cumul=10+rnd.nextDouble();
        for (int i=0; i<xi.length-1; ++i){
            cumul+=rnd.nextDouble();
            fx[i]=cumul;
        }
        GenericCubicSpline gspline = GenericCubicSpline.ofAggregation(xi, fx, new GenericCubicSpline.BoundaryConstraints(fx[0]/(xi[1]-xi[0]), Double.NaN, 0), null);
        for (int i=0; i<gspline.getPolynomialsCount(); ++i){
            DoubleSeq c = gspline.polynomial(i);
            double m0=0, m1=0;
            for (int j=1; j<=4; ++j){
                m0=m1;
                m1=c.get(0)*j+c.get(1)*j*j/2+c.get(2)*j*j*j/3+c.get(3)*j*j*j*j/4;
//                System.out.println(m1-m0);
            }
            assertEquals(m1, fx[i], 1e-6);
        }
    }
    
}
