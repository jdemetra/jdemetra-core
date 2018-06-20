/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics;
import demetra.ssf.implementations.TimeInvariantDynamics.Innovations;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(Matrix T, Matrix V, Matrix S){
        return new TimeInvariantDynamics(T, new Innovations(V, S));
    }
}
