/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.matrices.MatrixType;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics.Innovations;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(MatrixType T, MatrixType V, MatrixType S){
        return new TimeInvariantDynamics(FastMatrix.of(T), new Innovations(FastMatrix.of(V), FastMatrix.of(S)));
    }
}
