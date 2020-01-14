/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.math.matrices.MatrixType;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class FractionalAirlineSpec {
    // series
    private double[] y;
    // regression variables
    private boolean meanCorrection;
    private MatrixType X;
    
    // Periodic airline model
    private double[] periodicities;
    
    // automatic outliers detection
    private String[] outliers;
    private double criticalValue;
    
    // operational
    
}
