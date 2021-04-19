/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import demetra.data.DoubleSeq;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class BayesRegularizedRegressionResults {
    @lombok.Value
    public static class Result {

        DoubleSeq b;
        double b0;
        double tau2;
    }
    
    @lombok.Singular
    List<Result> samples;
    
}
