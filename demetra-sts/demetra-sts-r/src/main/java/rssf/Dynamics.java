/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jd.maths.matrices.CanonicalMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics.Innovations;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(Matrix T, Matrix V, Matrix S){
        return new TimeInvariantDynamics(CanonicalMatrix.of(T), new Innovations(CanonicalMatrix.of(V), CanonicalMatrix.of(S)));
    }
}
