/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sigex;

import jdplus.maths.matrices.CanonicalMatrix;
import demetra.maths.matrices.internal.LDLDecomposition;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MatrixOperations {
    CanonicalMatrix[] gcd(FastMatrix x, int rank){
        LDLDecomposition ldl=new LDLDecomposition();
        ldl.decompose(x, 1e-9);
        return new CanonicalMatrix[]{ldl.L(), ldl.D()};
    }
}
