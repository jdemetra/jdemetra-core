/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import java.util.Random;

import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class KCenterTest {
    
    public KCenterTest() {
    }
  
    public static void main(String[] args) {
        Random rnd = new Random(0);
        int n = 10000, m = 5;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
            if (i % 3 == 1) {
                data[i] *= Math.sqrt(.1);
            }
        }
        for (int i = 100*m; i < 100*m+m; ++i) {
            data[i] = 3.3;
        }
        Matrix M = Matrix.of(data, m, n);
        DistanceMeasure<DoubleSeq> d = (l, r) -> l.distance(r);
        
        long t0=System.currentTimeMillis();
        IntList kcenter = KCenter.kcenter(100, i -> M.column(i), n, d);
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        for (int i=0; i<100; ++i){
            System.out.print(kcenter.get(i));
            System.out.print('\t');
        }
    }
    
}
