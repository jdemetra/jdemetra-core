/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.data.Doubles;

/**
 * A = U*S*V'
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ISingularValueDecomposition {
    void decompose(Matrix A)throws MatrixException;
    
    Matrix U();
    
    Doubles S();
    
    Matrix V();
}
