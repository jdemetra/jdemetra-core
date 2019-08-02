/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.integration;

import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.gsl.integration.QAGS;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class NumericalIntegration {

    public double integrate(DoubleUnaryOperator fn, double a, double b) {
        try {
            QAGS qags = QAGS.builder()
                    .absoluteTolerance(1e-9)
                    .build();
            qags.integrate(fn, a, b);
            return qags.getResult();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

}
