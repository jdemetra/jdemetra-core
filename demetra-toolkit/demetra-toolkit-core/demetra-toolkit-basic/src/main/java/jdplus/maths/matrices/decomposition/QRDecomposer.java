  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.decomposition;

import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.MatrixException;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */

public interface QRDecomposer {
    
        QRDecomposition decompose(FastMatrix A)throws MatrixException;       
}
