/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.math.matrices.Matrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.implementations.TimeInvariantDynamics;
import jdplus.ssf.implementations.TimeInvariantDynamics.Innovations;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(MatrixType T, MatrixType V, MatrixType S){
        return new TimeInvariantDynamics(Matrix.of(T), new Innovations(Matrix.of(V), Matrix.of(S)));
    }
}
