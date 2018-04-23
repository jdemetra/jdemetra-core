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

package demetra.modelling;

import demetra.design.Development;
import demetra.design.IntValue;


/**
 * Series = Trend + Seasonal + Irregular + CalendarEffect
 * SeasonallyAdjusted = Trend + Seasonal + CalendarEffect
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum ComponentType implements IntValue {


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
    /**
     * 
     * @param value
     * @return
     */
    public static ComponentType valueOf(int value)
    {
        return IntValue.valueOf(ComponentType.class, value).orElse(null);
    }

    private final int value;

    ComponentType(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this ComponentType as an int.
     * @return
     */
    @Override
    public int intValue()
    {
	return value;
    }
}
