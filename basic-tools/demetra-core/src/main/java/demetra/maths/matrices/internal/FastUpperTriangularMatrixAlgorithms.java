/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.maths.matrices.internal;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.spi.UpperTriangularMatrixAlgorithms;

/**
 *
 * @author Jean Palate
 */
public class FastUpperTriangularMatrixAlgorithms implements UpperTriangularMatrixAlgorithms{

    public static final FastUpperTriangularMatrixAlgorithms INSTANCE = new FastUpperTriangularMatrixAlgorithms();

    private FastUpperTriangularMatrixAlgorithms() {
    }

    @Override
    public void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve(M.transpose(), x, zero);
    }

    @Override
    public void lsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve(M.transpose(), x, zero);
    }

    @Override
    public void rmul(Matrix M, DataBlock x) {
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul(M.transpose(), x);
    }

    @Override
    public void lmul(Matrix M, DataBlock x) {
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul(M.transpose(), x);
    }
    
}
