/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import demetra.data.DoubleSeq;
import demetra.maths.Complex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolverTest {

    public FastEigenValuesSolverTest() {

    }

    @Test
    public void testSmall() {
        int n=10;
        Polynomial P = Polynomial.ofInternal(DoubleSeq.onMapping(n, i -> n / (i + 1.0)).toArray());
        FastEigenValuesSolver solver = new FastEigenValuesSolver();
//        long t0 = System.currentTimeMillis();
        boolean ok = solver.factorize(P);
        assertTrue(ok);
        Complex[] nr = solver.roots();
        for (int i=0; i<nr.length; ++i){
            assertTrue(nr[i]!= null);
//            System.out.print(nr[i].getRe());
//            System.out.print('\t');
//            System.out.println(nr[i].getIm());
        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    public static void main(String[] arg) {
        int N = 5000;
        int K = 1;
        double[] p=new double[N];
        p[0]=-1;
        p[N-1]=1;
        Polynomial P = Polynomial.ofInternal(p);
        Complex[] roots = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
   //        roots=P.roots(new EigenValuesSolver());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            FastEigenValuesSolver solver = new FastEigenValuesSolver();
            solver.factorize(P);
            roots=solver.roots();
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        
        for (int i=0; i<roots.length; ++i){
            System.out.println(roots[i].abs());
        }
    }

}
