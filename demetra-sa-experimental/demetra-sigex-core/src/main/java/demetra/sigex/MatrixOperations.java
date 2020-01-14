/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sigex;

import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.LDLDecomposition;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MatrixOperations {
    Matrix[] gcd(Matrix x, int rank){
        LDLDecomposition ldl=new LDLDecomposition();
        ldl.decompose(x, 1e-9);
        return new Matrix[]{ldl.L(), ldl.D()};
    }
}
