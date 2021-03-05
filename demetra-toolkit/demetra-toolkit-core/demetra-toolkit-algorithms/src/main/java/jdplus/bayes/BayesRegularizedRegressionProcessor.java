/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

/**
 *
 * @author PALATEJ
 */
public class BayesRegularizedRegressionProcessor {

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Spec {

        int nsamples;
        int burnin;
        int thin;
        int blocksize;
        boolean waic;
    }
    
    private final Spec spec;

    public BayesRegularizedRegressionProcessor(Spec spec) {
        this.spec = spec;
    }

    public BayesRegularizedRegressionResults process(BayesRegularizedRegressionModel model) {
        return null;
    }

}
