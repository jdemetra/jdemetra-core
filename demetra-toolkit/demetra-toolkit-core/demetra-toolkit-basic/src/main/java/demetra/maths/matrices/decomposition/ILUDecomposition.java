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
package demetra.maths.matrices.decomposition;

import demetra.design.Development;
import demetra.data.DataBlock;
import demetra.data.LogSign;
import demetra.maths.matrices.Matrix;
import demetra.maths.MatrixException;

/**
 * Computes the L-U decomposition of a matrix M = L * U where L is a lower
 * triangular matrix with 1 on the main diagonal and U is an upper triangular
 * matrix. Once a matrix has been decomposed in L * U, it can be easily used for
 * solving M x = L U x = b (solve L y = b and U x = y) or for computing the
 * determinant (product of the diagonal of U.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ILUDecomposition {

    void setPrecision(double eps);
    
    double getPrecision();

    /**
     * @param m A square matrix
     */
    void decompose(Matrix m)throws MatrixException;
    
    /**
     * if M = L*U, solves M * x = b
     * @param b in/out parameter. Contains the result after the processing
     */
    void solve(DataBlock b);
    
    default void solve(Matrix B){
        B.applyByColumns(col->solve(col));
    }

    LogSign determinant();
}

