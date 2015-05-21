/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.maths.special;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class DirichletKernelTest {
    
    public DirichletKernelTest() {
    }

    @Test
    public void testSomeMethod() {
        int N=180;
        DirichletKernel dk=new DirichletKernel(N);
        double step=Math.PI*2/N;
        double z=0;
        for (int i=1; i<20; ++i){
            double k=dk.fn(step*i);
            z+=k*k*step*Math.PI*2;
        }
//        double dn=N;
//        for (int i=20; i<N/2; ++i){
//            double k=dk.fn(step*i);
//            z+=k*k*step*Math.PI*2;
//            System.out.print(z);
//            System.out.print('\t');
//            System.out.println(i/dn);
//        }
    }
    
}
