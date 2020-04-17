/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.parametric;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class Bias {
    /**
     * Number of observations used
     */
    int n;
    /**
     * Estimated bias and its standard deviation
     */
    double mu, sigma;

    /**
     * T-Statistic and corresponding PValue
     */
    double t, tPvalue;
    
    /**
     * Auto-regressive coefficient on residuals
     */
    double ar;
    
    /**
     * Adjusted standard deviation
     */
    double adjustedSigma;
    
    /**
     * Adjusted T-statistic
     */
    double adjustedT, adjustedTPvalue;
    
}
