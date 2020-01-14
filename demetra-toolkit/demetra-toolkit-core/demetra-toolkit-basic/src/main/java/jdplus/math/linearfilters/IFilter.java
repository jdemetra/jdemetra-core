/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.math.linearfilters;

import demetra.design.Development;
import demetra.math.Complex;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

/**
 * Generic interface of time invariant linear filter with real coefficients. 
 * A linear filter W is a sequence (perhaps infinite) of real weights (..., w(-n),...,w(m),...)
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

    default DoubleFunction<Complex> frequencyResponseFunction() {
        return (x -> frequencyResponse(x));
    }

    default DoubleUnaryOperator gainFunction() {
        return (x -> frequencyResponse(x).abs());
    }

    default DoubleUnaryOperator squaredGainFunction() {
        return (x -> frequencyResponse(x).absSquare());
    }

    default DoubleUnaryOperator phaseFunction() {
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
