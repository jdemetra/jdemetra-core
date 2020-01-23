/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import java.util.Arrays;
import java.util.Random;
import jdplus.math.functions.GenericCubicSpline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class AggregationCubicSplineTest {
    
    public AggregationCubicSplineTest() {
    }
    
    @Test
    public void testAggregation() {
        double[] xi = new double[20];
        for (int i = 0; i < xi.length; ++i) {
            xi[i] = 4 * i;
        }
        double[] fx = new double[xi.length - 1];
        Random rnd = new Random(0);
        double cumul = 10 + rnd.nextDouble();
        for (int i = 0; i < xi.length - 1; ++i) {
            cumul += rnd.nextDouble();
            fx[i] = cumul;
        }
        GenericCubicSpline gspline = GenericCubicSpline.ofAggregation(xi, fx, new GenericCubicSpline.BoundaryConstraints(2.5, Double.NaN, 0), null);
        GenericCubicSpline aspline = AggregationCubicSpline.aggregationSplineOf(xi, fx);
        for (int i = 0; i < gspline.getPolynomialsCount(); ++i) {
            DoubleSeq c = gspline.polynomial(i);
            DoubleSeq a = aspline.polynomial(i);
            double m0 = 0, m1 = 0;
            double a0 = 0, a1 = 0;
            for (int j = 1; j <= 4; ++j) {
                m0 = m1;
                m1 = c.get(0) * j + c.get(1) * j * j / 2 + c.get(2) * j * j * j / 3 + c.get(3) * j * j * j * j / 4;
//                System.out.print(m1 - m0);
//                System.out.print('\t');
                a0 = a1;
                a1 = a.get(0) * j + a.get(1) * j * j / 2 + a.get(2) * j * j * j / 3 + a.get(3) * j * j * j * j / 4;
//                System.out.println(a1 - a0);
            }
            assertEquals(m1, fx[i], 1e-6);
            assertEquals(a1, fx[i], 1e-6);
        }
    }
    
    public static void disaggregation() {
        double[] d = AggregationCubicSpline.disaggregate(Data.PCRA, 4);
        System.out.println(DoubleSeq.of(d));
        d = AggregationCubicSpline.disaggregateByCumul(Data.PCRA, 4);
        System.out.println(DoubleSeq.of(d));
        DentonSpec spec = DentonSpec.builder()
                .differencing(1).build();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);
        double[] d2 = denton.process(DoubleSeq.of(Data.PCRA));
        System.out.println(DoubleSeq.of(d2));
    }
    
     public static void disaggregationWithIndicator(){
        double[] d = AggregationCubicSpline.disaggregateByCumul(Arrays.copyOf(Data.PCRA, 16), 4, Data.IND_PCR, 4);
        System.out.println(DoubleSeq.of(d));
        DentonSpec spec = DentonSpec.builder()
                .differencing(1).build();
        MatrixDenton denton = new MatrixDenton(spec, 4, 4);
        double[] d2 = denton.process(DoubleSeq.of(Data.IND_PCR), DoubleSeq.of(Data.PCRA).range(0, 16));
        System.out.println(DoubleSeq.of(d2));
        
    }
     
     public static void main(String[] args){
         disaggregation();
         disaggregationWithIndicator();
     }
    
}
