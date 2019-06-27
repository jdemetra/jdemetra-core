/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import jdplus.maths.matrices.CanonicalMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolver2Test {
    
    public FastEigenValuesSolver2Test() {
    }

    @Test
    public void testQ() {
        int N=8;
        FastEigenValuesSolver2.Reflector r=new FastEigenValuesSolver2.Reflector(0, 1);
        CanonicalMatrix Q=r.asMatrix(N,0);
        for (int i=1; i<N-2; ++i){
            Q=Q.times(r.asMatrix(N,i));
        }
        System.out.println("Reflectors");
        System.out.println(Q);
        System.out.println();
        
        CanonicalMatrix R=CanonicalMatrix.square(N);
        R.diagonal().drop(0,2).set(1);
        R.column(N-2).drop(0,2).set(i-> -i-1.1);
        R.set(N-2, N-2, -.1);
        R.set(N-2, N-1,1);
        System.out.println(Q.times(R));
        System.out.println();
    }
    
    @Test
    public void testQ2() {
        int N=8;
        FastEigenValuesSolver2.Rotator r=new FastEigenValuesSolver2.Rotator(0, 1);
        CanonicalMatrix Q=r.asMatrix(N,0);
        for (int i=1; i<N-2; ++i){
            Q=Q.times(r.asMatrix(N,i));
        }
        System.out.println("Rotators");
        System.out.println(Q);
        System.out.println();
        
        CanonicalMatrix R=CanonicalMatrix.square(N);
        R.diagonal().drop(0,2).set(-1);
        R.column(N-2).drop(0,2).set(i-> i+1.1);
        R.set(N-2, N-2, -.1);
        R.set(N-2, N-1,1);
        System.out.println(Q.times(R));
        System.out.println();
    }
}
