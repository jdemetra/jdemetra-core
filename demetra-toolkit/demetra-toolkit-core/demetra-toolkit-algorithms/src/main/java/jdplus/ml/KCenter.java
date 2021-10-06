/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

import demetra.util.IntList;
import java.util.Random;
import java.util.function.IntFunction;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class KCenter {
    
    private final Random rnd=new Random();
    
    public <Z> IntList kcenter(int ksize, IntFunction<Z> points, int psize, DistanceMeasure<Z> measure){
        IntList S=new IntList();
        int s0 = rnd.nextInt(psize);
        S.add(s0);
        double[] dmin=new double[psize];
        for (int j=1; j<ksize; ++j){
            double cmax=0;
            int imax=0;
            Z cur=points.apply(S.get(j-1));
            for (int k=0; k<psize; ++k){
                double dcur=measure.compute(cur, points.apply(k));
                if (j == 1 || dcur<dmin[k]){
                    dmin[k]=dcur;
                }
                if (dmin[k]>cmax){
                    cmax=dmin[k];
                    imax=k;
                }
            }
            S.add(imax);
        }
        return S;
    }
}
