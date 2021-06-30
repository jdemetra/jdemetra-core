/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ISarimaModel {
    
    static final String NAME="sarima";
    
    default String getName(){
        return NAME;
    }
    
    int getPeriod();
    /**
     * Regular differencing order
     */
    int getD();
    /**
     * Seasonal differencing order
     */
    int getBd();
    /**
     * Regular auto-regressive parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    double[] getPhi();
    /**
     * Regular moving average parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    double[] getTheta();
    /**
     * Seasonal auto-regressive parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    double[] getBphi();
    /**
     * Seasonal moving average parameters (true signs, 1 excluded)
     */
    @lombok.NonNull
    double[] getBtheta();

    int getP();

    int getBp();

    int getQ();

    int getBq();

    /**
     * Gets the underlying specification
     * @return
     */
    SarimaOrders orders();
    /**
     * Gets all the parameters in the following order
     * phi, bphi, theta, btheta
     * 
     * The sign of the coefficients corresponds to the
     * sign in the backshift polynomials. 
     */
    DoubleSeq parameters();
    
}
