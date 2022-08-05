/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.data;

import java.util.function.DoubleUnaryOperator;
import nbbrd.design.Development;

/**
 * Weight function, defined in [-1,1].
 * The function is positive, even and equals 1 (=max) at zero
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
public enum WeightFunction {
    UNIFORM,
    TRIANGULAR,
    EPANECHNIKOV,
    TRICUBE,
    TRIWEIGHT,
    BIWEIGHT;

    public DoubleUnaryOperator asFunction() {
        return switch (this) {
            case EPANECHNIKOV -> x -> 1.0 - x * x;
            case TRIANGULAR -> x -> 1.0 - Math.abs(x);
            case BIWEIGHT -> x -> {
                double z = 1.0 - x * x;
                return z * z;
            };
            case TRIWEIGHT -> x -> {
                double z = 1.0 - x * x;
                return z * z * z;
            };
            case TRICUBE -> x -> {
                double v = Math.abs(x);
                double z = 1.0 - v * v * v;
                return z * z * z;
            };
            default -> x -> 1;
        };
    }
    
}
