/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.spi;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixDecompositions;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = MatrixDecompositions.Processor.class)
public class MatrixDecompositionsProcessor implements MatrixDecompositions.Processor {

    @Override
    public Matrix cholesky(Matrix matrix) {
        CanonicalMatrix M=CanonicalMatrix.of(matrix);
        SymmetricMatrix.lcholesky(M, 1e-9);
        return M.unmodifiable();
    }
    
}
