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

package jdplus.math.functions;

import nbbrd.design.Development;
import nbbrd.design.RepresentableAsInt;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 *
 * @author Jean Palate
 */
@RepresentableAsInt
@lombok.AllArgsConstructor
@Development(status = Development.Status.Alpha)
public enum ParamValidation {

    /**
     *
     */
    Valid(1),
    /**
     *
     */
    Changed(0),
    /**
     *
     */
    Invalid(-1);

    private final int value;

    /**
     * Returns the value of this ParamValidation as an int.
     * @return
     */
    public int toInt() {
	return value;
    }

    public static @NonNull ParamValidation parse(int value) throws IllegalArgumentException {
        for (ParamValidation o : values()) {
            if (o.value == value) {
                return o;
            }
        }
        throw new IllegalArgumentException("Cannot parse " + value);
    }
}
