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
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SymmetricMatrixTest {

    public SymmetricMatrixTest() {
    }

    @Test
    public void testQuadraticForm() {
        Matrix X = new Matrix(5, 8);
        X.randomize();
        Matrix S = SymmetricMatrix.XXt(X);
        DataBlock x = new DataBlock(S.getRowsCount());
        x.randomize();
        assertEquals(SymmetricMatrix.quadraticForm(S, x.getData()), SymmetricMatrix.quadraticForm(S, x), 1e-9);
    }
    
    @Test
    @Ignore
    public void stressTestQuadraticForm() {
        Matrix X = new Matrix(1000, 1000);
        X.randomize();
        Matrix S = SymmetricMatrix.XXt(X);
        DataBlock x = new DataBlock(S.getRowsCount());
        x.randomize();
        long t0=System.currentTimeMillis();
        for (int i=0; i<5000; ++i){
            SymmetricMatrix.quadraticForm(S, x);
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        t0=System.currentTimeMillis();
        for (int i=0; i<5000; ++i){
            SymmetricMatrix.quadraticForm(S, x.getData());
        }
        t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }
    

    @Test
    public void testLSolve() {
        Matrix X = new Matrix(5, 8);
        X.randomize();
        Matrix S = SymmetricMatrix.XXt(X);
        Matrix A = new Matrix(3, 5);
        A.randomize();
        Matrix B1 = A.clone();
        Matrix B2 = Matrix.lsolve(S.subMatrix(), A.subMatrix());
        SymmetricMatrix.lsolve(S, B1.subMatrix(), true);
        Matrix D = B1.minus(B2);
        assertTrue(D.nrm2() < 1e-9);
    }

    @Test
    public void testRSolve() {
        Matrix X = new Matrix(5, 8);
        X.randomize();
        Matrix S = SymmetricMatrix.XXt(X);
        Matrix A = new Matrix(5, 4);
        A.randomize();
        Matrix B1 = A.clone();
        Matrix B2 = Matrix.rsolve(S.subMatrix(), A.subMatrix());
        SymmetricMatrix.rsolve(S, B1.subMatrix(), true);
        Matrix D = B1.minus(B2);
        assertTrue(D.nrm2() < 1e-9);
    }

//    @Test
    public void demoCholesky() {
        int n = 10;
        Matrix X = new Matrix(n, n);
        X.randomize();
        Matrix S = Matrix.lsolve(X.subMatrix(), Matrix.identity(X.getRowsCount()).subMatrix());
        S = SymmetricMatrix.XtX(S);
        for (int i = 0; i < S.getColumnsCount(); ++i) {
            Matrix T = new Matrix(S.subMatrix(0, i + 1, 0, i + 1));
            SymmetricMatrix.lcholesky(T);
            System.out.println(T.diagonal());
        }
        X.set(3, 6, 10);
        S = Matrix.lsolve(X.subMatrix(), Matrix.identity(X.getRowsCount()).subMatrix());
        S = SymmetricMatrix.XtX(S);
        for (int i = 0; i < S.getColumnsCount(); ++i) {
            Matrix T = new Matrix(S.subMatrix(0, i + 1, 0, i + 1));
            SymmetricMatrix.lcholesky(T);
            System.out.println(T);
        }
    }
    
    @Test
    public void testLCholeskySD(){
        Matrix M=new Matrix(50,5);
        M.randomize(0);
        Matrix N=new Matrix(50, 50);
        for (int i=0; i<50; ++i){
            N.column(i).copy(M.subMatrix().columnSum());
        }
        for (int i=1; i<5; ++i){
            N.column(26+i).copy(M.column(i));
        }
        Matrix Q=SymmetricMatrix.XXt(N);
        Matrix R=Q.clone();
        SymmetricMatrix.lcholesky(Q, 1e-9);
        assertTrue(SymmetricMatrix.LLt(Q).distance(R)<1e-9);
    }
}
