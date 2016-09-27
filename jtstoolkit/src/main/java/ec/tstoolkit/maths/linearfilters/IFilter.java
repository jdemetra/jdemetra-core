/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.realfunctions.RealFunction;
import java.util.function.DoubleFunction;

/**
 * Generic interface of linear filter with real coefficients. A linear filter W
 * is a sequence (perhaps infinite) of real weights (..., w(-n),...,w(m),...)
 * that can be applied on an other sequence of values (y(t)). so that W(y(t)) =
 * ... + w(-n)y(t-n) + ... + w(m) y(t+m)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFilter {

    /**
     * Computes the response in frequency of the filter. It is the Fourier
     * transform of the filter evaluated at a given frequency
     *
     * @param freq The frequency
     * @return A complex number that provides the amplitude and the phase
     * effects.
     */
    Complex frequencyResponse(double freq);

    /**
     * Gets a specific weight of the filter
     *
     * @param pos Position of the weight (relative to the current data)
     * @return The requested weight
     */
    double getWeight(int pos);

    /**
     * Checks that the filter has a lower bound.
     *
     * @return True if the filter is finite on the left, false otherwise
     */
    boolean hasLowerBound();

    /**
     * Checks that the filter has an upper bound.
     *
     * @return True if the filter is finite on the right, false otherwise
     */
    boolean hasUpperBound();

    default DoubleFunction<Complex> frequencyResponse() {
        return (x -> frequencyResponse(x));
    }

    default RealFunction gainFunction() {
        return (x -> frequencyResponse(x).abs());
    }

    default RealFunction squaredGainFunction() {
        return (x -> frequencyResponse(x).absSquare());
    }

    default RealFunction phaseFunction() {
        return (x -> {
            Complex c = frequencyResponse(x);
            if (c.getIm() == 0) {
                return 0;
            } else {
                return c.arg();
            }
        });
    }

}
