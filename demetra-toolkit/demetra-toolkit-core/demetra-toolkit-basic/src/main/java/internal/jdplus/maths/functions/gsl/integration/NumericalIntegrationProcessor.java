/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.maths.functions.gsl.integration;

import java.util.function.DoubleUnaryOperator;
import internal.jdplus.maths.functions.gsl.integration.NumericalIntegration;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(jdplus.math.functions.NumericalIntegration.Processor.class)
public class NumericalIntegrationProcessor implements jdplus.math.functions.NumericalIntegration.Processor {

    @Override
    public double integrate(DoubleUnaryOperator fn, double a, double b) {
        return NumericalIntegration.integrate(fn, a, b);
    }
}
