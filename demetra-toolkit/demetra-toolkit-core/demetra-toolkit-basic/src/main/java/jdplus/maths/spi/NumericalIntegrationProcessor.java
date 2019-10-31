/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.spi;

import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.integration.NumericalIntegration;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(demetra.maths.functions.NumericalIntegration.Processor.class)
public class NumericalIntegrationProcessor implements demetra.maths.functions.NumericalIntegration.Processor {

    @Override
    public double integrate(DoubleUnaryOperator fn, double a, double b) {
        return NumericalIntegration.integrate(fn, a, b);
    }
}
