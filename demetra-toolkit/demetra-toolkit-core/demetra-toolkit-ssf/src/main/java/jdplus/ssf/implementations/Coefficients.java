/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.implementations;

import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.SsfException;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Coefficients {

    /**
     * Coefficients which follow a random walk
     * @param vars The innovation variances of the coefficients. The number of coefficients
     * are defined by the number of variances
     * @return 
     */
    public StateComponent timeVaryingCoefficients(DoubleSeq vars) {
        int nx = vars.length();
        if (nx == 1) {
            return new StateComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(nx, vars.get(0)));
        } else {
            return new StateComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(vars));
        }
    }

    public StateComponent timeVaryingCoefficient(DoubleSeq std, double scale) {
        return new StateComponent(new ConstantInitialization(1), TimeVaryingDynamics.of(std, scale));
     }

    /**
     * Coefficients which follow a multivariate random walk
     * @param vars The covariance of the innovations. The number of coefficients is
     * defined by the size of the (square) matrix
     * @return 
     */
    public StateComponent timeVaryingCoefficients(FastMatrix vars) {
        int nx = vars.getColumnsCount();
        return new StateComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(vars));
    }

    /**
     * Block of fixed coefficients
     * @param nx The number of coefficients
     * @return 
     */
    public StateComponent fixedCoefficients(int nx) {
        return new StateComponent(new ConstantInitialization(nx), new ConstantDynamics());
    }
}
