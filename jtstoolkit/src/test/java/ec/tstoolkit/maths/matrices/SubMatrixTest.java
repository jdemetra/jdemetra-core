/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SubMatrixTest {
    
    public SubMatrixTest() {
    }

    /**
     * Test of add method, of class SubMatrix.
     */
    @Test
    public void testAdd_double() {
    }

    /**
     * Test of add method, of class SubMatrix.
     */
    @Test
    public void testAdd_3args() {
    }

    /**
     * Test of add method, of class SubMatrix.
     */
    @Test
    public void testAdd_SubMatrix() {
    }

    /**
     * Test of chs method, of class SubMatrix.
     */
    @Test
    public void testChs() {
    }

    /**
     * Test of clone method, of class SubMatrix.
     */
    @Test
    public void testClone() {
    }

    /**
     * Test of column method, of class SubMatrix.
     */
    @Test
    public void testColumn() {
    }

    /**
     * Test of columns method, of class SubMatrix.
     */
    @Test
    public void testColumns() {
    }

    /**
     * Test of copy method, of class SubMatrix.
     */
    @Test
    public void testCopy() {
    }

    /**
     * Test of setAY method, of class SubMatrix.
     */
    @Test
    public void testSetAY() {
    }

    /**
     * Test of addAY method, of class SubMatrix.
     */
    @Test
    public void testAddAY() {
    }

    /**
     * Test of diagonal method, of class SubMatrix.
     */
    @Test
    public void testDiagonal() {
    }

    /**
     * Test of difference method, of class SubMatrix.
     */
    @Test
    public void testDifference() {
    }

    /**
     * Test of extract method, of class SubMatrix.
     */
    @Test
    public void testExtract_4args() {
    }

    /**
     * Test of extract method, of class SubMatrix.
     */
    @Test
    public void testExtract_6args() {
    }

    /**
     * Test of get method, of class SubMatrix.
     */
    @Test
    public void testGet() {
    }

    /**
     * Test of getColumnsCount method, of class SubMatrix.
     */
    @Test
    public void testGetColumnsCount() {
    }

    /**
     * Test of getRowsCount method, of class SubMatrix.
     */
    @Test
    public void testGetRowsCount() {
    }

    /**
     * Test of move method, of class SubMatrix.
     */
    @Test
    public void testMove() {
    }

    /**
     * Test of mul method, of class SubMatrix.
     */
    @Test
    public void testMul_double() {
    }

    /**
     * Test of mul method, of class SubMatrix.
     */
    @Test
    public void testMul_3args() {
    }

    /**
     * Test of product method, of class SubMatrix.
     */
    @Test
    public void testProduct() {
    }

    /**
     * Test of kronecker method, of class SubMatrix.
     */
    @Test
    public void testKronecker() {
    }

    /**
     * Test of row method, of class SubMatrix.
     */
    @Test
    public void testRow() {
    }

    /**
     * Test of rows method, of class SubMatrix.
     */
    @Test
    public void testRows() {
    }

    /**
     * Test of set method, of class SubMatrix.
     */
    @Test
    public void testSet_double() {
    }

    /**
     * Test of set method, of class SubMatrix.
     */
    @Test
    public void testSet_3args() {
    }

    /**
     * Test of shift method, of class SubMatrix.
     */
    @Test
    public void testShift() {
    }

    /**
     * Test of sub method, of class SubMatrix.
     */
    @Test
    public void testSub() {
    }

    /**
     * Test of subDiagonal method, of class SubMatrix.
     */
    @Test
    public void testSubDiagonal() {
    }

    /**
     * Test of sum method, of class SubMatrix.
     */
    @Test
    public void testSum_0args() {
    }

    /**
     * Test of sum method, of class SubMatrix.
     */
    @Test
    public void testSum_SubMatrix_SubMatrix() {
    }

    /**
     * Test of transpose method, of class SubMatrix.
     */
    @Test
    public void testTranspose() {
    }

    /**
     * Test of isEmpty method, of class SubMatrix.
     */
    @Test
    public void testIsEmpty() {
    }

    /**
     * Test of isNull method, of class SubMatrix.
     */
    @Test
    public void testIsNull() {
    }

    /**
     * Test of toString method, of class SubMatrix.
     */
    @Test
    public void testToString() {
    }

    /**
     * Test of rowSum method, of class SubMatrix.
     */
    @Test
    public void testRowSum() {
        Matrix x = new Matrix(10, 10);
        x.set(1);
        x.row(0).cumul();
        for (int i = 0; i < 10; ++i) {
            x.column(i).cumul();
        }
        DataBlock rowSum = x.subMatrix().rowSum();
        for (int i = 0; i < 10; ++i) {
            assertTrue(Math.abs(rowSum.get(i) - 55 - i * 10) < 1e-9);
        }
        
    }

    /**
     * Test of columnSum method, of class SubMatrix.
     */
    @Test
    public void testColumnSum() {
    }

    /**
     * Test of xmy method, of class SubMatrix.
     */
    @Test
    public void testXmy() {
        Matrix x = new Matrix(10, 10);
        Matrix m = new Matrix(10, 10);
        Matrix y = new Matrix(10, 10);
        x.randomize();
        m.randomize();
        y.randomize();
        Matrix z = x.times(m).times(y);
        m.subMatrix().xmy(x.subMatrix(), y.subMatrix());
        
        Matrix d = m.minus(z);
        assertTrue(d.nrm2() < 1e-9);
    }
    
    @Test
    public void testCopyTo() {
        Matrix x = new Matrix(10, 10);
        Matrix m = new Matrix(10, 10);
        x.randomize();
        m.randomize();
        Matrix y = x.clone();
        SubMatrix sx = x.subMatrix();
        SubMatrix sy = y.subMatrix();
        SubMatrix sm = m.subMatrix(3, 6, 2, 4);
        
        sx.extract(2, 5, 3, 5).copy(sm);
        sm.copyTo(sy, 2, 3);
        
        sx.extract(1, 3, 5, 8).copy(sm.transpose());
        sm.transpose().copyTo(sy, 1, 5);
        
        Matrix d = x.minus(y);
        assertTrue(d.nrm2() < 1e-15);
    }
    
    @Test
    public void testAddSubTo() {
        Matrix x = new Matrix(10, 10);
        Matrix m = new Matrix(10, 10);
        x.randomize();
        m.randomize();
        Matrix y = x.clone();
        SubMatrix sx = x.subMatrix();
        SubMatrix sy = y.subMatrix();
        SubMatrix sm = m.subMatrix(3, 6, 2, 4);
        
        sx.extract(2, 5, 3, 5).add(sm);
        sm.addTo(sy, 2, 3);
        
        sx.extract(1, 3, 5, 8).sub(sm.transpose());
        sm.transpose().subTo(sy, 1, 5);
        
        Matrix d = x.minus(y);
        assertTrue(d.nrm2() < 1e-15);
    }
    
    @Test
    public void testIterators() {
        Matrix x = new Matrix(8, 8);
        SubMatrix cur = x.topLeft();
        for (int i = 0; i < 4; ++i) {
            cur.next(2, 2);
            cur.set(i + 1);
        }
        for (int i = 0; i < 4; ++i) {
            cur.add(i-4);
            cur.previous();
        }
        assertTrue(x.nrm2()<1e-9);
        
        SubMatrix y = x.subMatrix(2,4,0,8);
        cur = y.topLeft(2, 0);
        for (int i = 0; i < 4; ++i) {
            cur.hnext(2);
            cur.set(i + 1);
        }
        for (int i = 0; i < 4; ++i) {
            cur.add(i-4);
            cur.hprevious();
        }
        assertTrue(x.nrm2()<1e-9);
        SubMatrix z = x.subMatrix(0,8,2,4);
        cur = z.topLeft(0, 1);
        for (int i = 0; i < 4; ++i) {
            cur.vnext(2);
            cur.set(i + 1);
        }
        for (int i = 0; i < 4; ++i) {
            cur.add(i-4);
            cur.vprevious();
        }
        assertTrue(x.nrm2()<1e-9);
    }
    
}
