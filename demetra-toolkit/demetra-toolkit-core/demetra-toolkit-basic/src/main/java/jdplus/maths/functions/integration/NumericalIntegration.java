/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.maths.functions.integration;

import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.gsl.integration.QAGS;
import jdplus.maths.functions.gsl.integration.QK15;
import nbbrd.service.ServiceProvider;

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

    /**
     * QAGI: evaluate an integral over an infinite range using the
     * transformation :
     *
     * integrate(f(x),-Inf,Inf) = integrate((f((1-t)/t) + f(-(1-t)/t))/t^2,0,1)
     *
     * @param fn Function to transform
     * @return integral result
     */
    public double integrateQAGI(DoubleUnaryOperator fn) {
        try {
            QAGS qags = QAGS.builder()
                    .absoluteTolerance(1e-9)
                    .integrationRule(QK15.rule())
                    .build();
            qags.integrate((double d) -> {
                double x = (1 - d) / d;
                double y = fn.applyAsDouble(x) + fn.applyAsDouble(-x);
                return (y / d) / d;
            }, 0.0, 1.0);
            return qags.getResult();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    /**
     * QAGIL: Evaluate an integral over an infinite range using the
     * transformation :
     *
     * integrate(f(x),-Inf,b) = integrate(f(b-(1-t)/t)/t^2,0,1)
     *
     * @param fn Function to transform
     * @param b Upper bound
     * @return
     */
    public double integrateQAGIL(DoubleUnaryOperator fn, double b) {
        try {
            QAGS qags = QAGS.builder()
                    .absoluteTolerance(1e-9)
                    .integrationRule(QK15.rule())
                    .build();
            qags.integrate((double d) -> {
                double x = b - (1 - d) / d;
                double y = fn.applyAsDouble(x);
                return (y / d) / d;
            }, 0.0, 1.0);
            return qags.getResult();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    /**
     * QAGIU: Evaluate an integral over an infinite range using the
     * transformation :
     *
     * integrate(f(x),a,Inf) = integrate(f(a+(1-t)/t)/t^2,0,1)
     *
     * @param fn Function to transform
     * @param a Lower bound
     * @return
     */
    public double integrateQAGIU(DoubleUnaryOperator fn, double a) {
        try {
            QAGS qags = QAGS.builder()
                    .absoluteTolerance(1e-9)
                    .integrationRule(QK15.rule())
                    .build();
            qags.integrate((double d) -> {
                double x = a + (1 - d) / d;
                double y = fn.applyAsDouble(x);
                return (y / d) / d;
            }, 0.0, 1.0);
            return qags.getResult();
        } catch (Exception err) {
            return Double.NaN;
        }
    }
}
