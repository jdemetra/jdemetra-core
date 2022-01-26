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

package demetra.sa;

import nbbrd.design.Development;
import nbbrd.design.RepresentableAsInt;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 * Series = Trend + Seasonal + Irregular + CalendarEffect
 * SeasonallyAdjusted = Trend + Seasonal + CalendarEffect
 * @author Jean Palate
 */
@RepresentableAsInt
@lombok.AllArgsConstructor
@Development(status = Development.Status.Alpha)
public enum ComponentType {


    /**
     *
     */
    Undefined(0),
    /**
     *
     */
    Series(1),
    /**
     *
     */
    Trend(2),
    /**
     *
     */
    Seasonal(3),
    /**
     *
     */
    SeasonallyAdjusted(4),
    /**
     *
     */
    Irregular(5),
    /**
     *
     */
    CalendarEffect(6);

    private final int value;

    /**
     * Returns the value of this ComponentType as an int.
     * @return
     */
    public int toInt()
    {
	return value;
    }

    public static @NonNull ComponentType parse(int value) throws IllegalArgumentException {
        for (ComponentType o : values()) {
            if (o.value == value) {
                return o;
            }
        }
        throw new IllegalArgumentException("Cannot parse " + value);
    }
}
