/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class BayesRegularizedRegressionModel {
    public static enum ModelType {
        GAUSSIAN, LAPLACE, T, BINOMIAL, POISSON, GEOMETRIC;
    }

    public static enum Prior {
        G,
        RIDGE,
        LASSO,
        HORSESHOE,
        HORSESHOEPLUS,
        LOGT
    }
    
    public static enum Tau2Prior{
        HS,
        SB,
        UNIFORM
    }
    
    @lombok.NonNull
    DoubleSeq y;
    @lombok.NonNull
    MatrixType X;

    @lombok.NonNull
    ModelType model;
    
    @lombok.NonNull
    Prior prior;
    
    int tdof;
    
    int[] categoricalVariables;
    @lombok.Singular
    List<int[]> groups;

}
