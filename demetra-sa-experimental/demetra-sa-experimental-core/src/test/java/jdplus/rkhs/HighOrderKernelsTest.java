/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import java.util.function.DoubleUnaryOperator;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.maths.linearfilters.LocalPolynomialFilters;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.stats.Kernel;
import jdplus.stats.Kernels;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HighOrderKernelsTest {
    
    public HighOrderKernelsTest() {
    }

    @Test
    public void testBiWeight() {
        CanonicalMatrix H = HighOrderKernels.hankel(jdplus.stats.Kernels.BIWEIGHT, 0, 5);
        assertEquals(SymmetricMatrix.determinant(H), 2.243734e-05, 1e-12);
    }
    
    @Test
    public void testBiWeight1() {
        Kernel K=Kernels.BIWEIGHT;
        System.out.println("BiWeight");
        Polynomial p = HighOrderKernels.p(K, 4);
        int m=11;
        SymmetricFilter sf = LocalPolynomialFilters.of(m, 3, DiscreteKernel.biweight(m));
        System.out.println(p);
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 4);
        double step=1.0/(m+1);
        for (int i=0; i<=m; ++i){
            System.out.print(sf.weights().applyAsDouble(i));
            System.out.print('\t');
            System.out.print(p.evaluateAt(step*i));
            System.out.print('\t');
            System.out.print(K.asFunction().applyAsDouble(step*i));
            System.out.print('\t');
            System.out.println(kernel.applyAsDouble(step*i));
        }
    }

    @Test
    public void testTriWeight() {
        CanonicalMatrix H = HighOrderKernels.hankel(jdplus.stats.Kernels.TRIWEIGHT, 0, 5);
        assertEquals(SymmetricMatrix.determinant(H), 6.765031e-06, 1e-12);
    }
    
    @Test
    public void testTriWeight1() {
        Kernel K=Kernels.TRIWEIGHT;
        System.out.println("TriWeight");
        Polynomial p = HighOrderKernels.p(K, 4);
        int m=11;
        SymmetricFilter sf = LocalPolynomialFilters.of(m, 3, DiscreteKernel.triweight(m));
        System.out.println(p);
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 4);
        double step=1.0/(m+1);
        for (int i=0; i<=m; ++i){
            System.out.print(sf.weights().applyAsDouble(i));
            System.out.print('\t');
            System.out.print(p.evaluateAt(step*i));
            System.out.print('\t');
            System.out.print(K.asFunction().applyAsDouble(step*i));
            System.out.print('\t');
            System.out.println(kernel.applyAsDouble(step*i));
        }
    }
    
    @Test
    public void testParabolic() {
        Kernel K=Kernels.EPANECHNIKOV;
        System.out.println("Epanechnikov");
        Polynomial p = HighOrderKernels.p(K, 4);
        int m=111;
        SymmetricFilter sf = LocalPolynomialFilters.of(m, 3, DiscreteKernel.epanechnikov(m));
        System.out.println(p);
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 4);
        double step=1.0/(m+1);
        for (int i=0; i<=m; ++i){
            System.out.print(sf.weights().applyAsDouble(i));
            System.out.print('\t');
            System.out.print(p.evaluateAt(step*i));
            System.out.print('\t');
            System.out.print(K.asFunction().applyAsDouble(step*i));
            System.out.print('\t');
            System.out.println(kernel.applyAsDouble(step*i));
        }
    }
    
    @Test
    public void testHenderson() {
        int m=6;
        Kernel K=Kernels.henderson(m);
        System.out.println("Henderson-11");
        System.out.println(Kernels.phenderson(m));
        System.out.println(HighOrderKernels.p(K, 3));
        System.out.println(HighOrderKernels.p(K, 4));
        Polynomial p = HighOrderKernels.p(K, 4).times(Kernels.phenderson(m));
        SymmetricFilter sf = LocalPolynomialFilters.of(m, 3, DiscreteKernel.henderson(m));
        System.out.println(p);
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 4);
        double step=1.0/(m+1);
        for (int i=0; i<=m; ++i){
            System.out.print(sf.weights().applyAsDouble(i));
            System.out.print('\t');
            System.out.print(p.evaluateAt(step*i));
            System.out.print('\t');
            System.out.print(K.asFunction().applyAsDouble(step*i));
            System.out.print('\t');
            System.out.println(kernel.applyAsDouble(step*i));
        }
    }
    
}
