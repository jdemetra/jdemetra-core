/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.descriptors.stats;

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class MaximumLikelihoodDescriptor {

    private final String VALUE = "value", PARAMS = "parameters", GRADIENT = "gradient", HESSIAN = "hessian";

    private final InformationMapping<MaximumLogLikelihood> MAPPING = new InformationMapping<>(MaximumLogLikelihood.class);

    static {
        MAPPING.set(VALUE, Double.class, source -> source.getValue());
        MAPPING.set(PARAMS, double[].class, source -> 
                source.getParameters() == null ? null : source.getParameters().toArray());
        MAPPING.set(GRADIENT, double[].class, source -> 
                source.getGradient() == null ? null : source.getGradient().toArray());
        MAPPING.set(HESSIAN, MatrixType.class, source -> 
                source.getHessian() == null ? null : source.getHessian());
    }

}
