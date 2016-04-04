/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LowerTriangularMatrixTest {
    
    public LowerTriangularMatrixTest() {
    }

    @Test
    public void testlmul() {
        Matrix M=Matrix.square(20);
        M.randomize(0);
        M.toLower();
        DataBlock B0=new DataBlock(M.getColumnsCount());
        B0.randomize(1);
        DataBlock B1=B0.deepClone();
        
        LowerTriangularMatrix.lmul(M, B0);
        LowerTriangularMatrix.lmul2(M, B1);
        assertTrue(B0.distance(B1)<1e-9);
    }
    
    @Test
    public void testrmul() {
        Matrix M=Matrix.square(20);
        M.randomize(0);
        M.toLower();
        DataBlock B0=new DataBlock(M.getColumnsCount());
        B0.randomize(1);
        DataBlock B1=B0.deepClone();
        
        LowerTriangularMatrix.rmul(M, B0);
        LowerTriangularMatrix.rmul2(M, B1);
        assertTrue(B0.distance(B1)<1e-9);
    }

    @Test
    public void testlsolve() {
        Matrix M=Matrix.square(20);
        M.randomize(0);
        M.toLower();
        DataBlock B0=new DataBlock(M.getColumnsCount());
        B0.randomize(1);
        DataBlock B1=B0.deepClone();
        
        LowerTriangularMatrix.lsolve(M, B0);
        LowerTriangularMatrix.lsolve2(M, B1);
        assertTrue(B0.distance(B1)<1e-9);
    }
    
    @Test
    public void testrsolve() {
        Matrix M=Matrix.square(20);
        M.randomize(0);
        M.toLower();
        DataBlock B0=new DataBlock(M.getColumnsCount());
        B0.randomize(1);
        DataBlock B1=B0.deepClone();
        
        LowerTriangularMatrix.rsolve(M, B0);
        LowerTriangularMatrix.rsolve2(M, B1);
        assertTrue(B0.distance(B1)<1e-9);
    }
}
