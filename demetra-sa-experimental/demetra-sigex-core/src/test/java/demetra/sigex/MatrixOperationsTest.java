/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sigex;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixOperationsTest {
    
    public MatrixOperationsTest() {
    }

    @Test
    public void testGcd() {
        Matrix m=Matrix.make(30,15 );
        double[] storage = m.getStorage();
        for (int i=0; i<storage.length; ++i)
            storage[i]=(i+1);
        Matrix s=SymmetricMatrix.XXt(m);
        Matrix[] gcd = MatrixOperations.gcd(s, 30);
        System.out.println(gcd[0]);
        System.out.println(gcd[1]);
    }
    
}
