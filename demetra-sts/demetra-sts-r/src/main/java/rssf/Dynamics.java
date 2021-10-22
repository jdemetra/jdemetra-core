/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.implementations.TimeInvariantDynamics;
import jdplus.ssf.implementations.TimeInvariantDynamics.Innovations;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(Matrix T, Matrix V, Matrix S){
        return new TimeInvariantDynamics(FastMatrix.of(T), new Innovations(FastMatrix.of(V), FastMatrix.of(S)));
    }
}
