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

package ec.tstoolkit.modelling;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.IntValue;
import java.util.EnumSet;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public enum ComponentType implements IntValue {

    // / <summary>
    // / Undefined component
    // / </summary>

    /**
     *
     */
    Undefined(0),
    // / <summary>
    // / Complete series
    // / </summary>
    /**
     *
     */
    Series(1),
    // / <summary>
    // / Trend-cycle
    // / </summary>
    /**
     *
     */
    Trend(2),
    // / <summary>
    // / Seasonal component. Contains the calendar effects
    // / </summary>
    /**
     *
     */
    Seasonal(3),
    // / <summary>
    // / Seasonally adjusted component
    // / </summary>
    /**
     *
     */
    SeasonallyAdjusted(4),
    // / <summary>
    // / Transitory-irregular component
    // / </summary>
    /**
     *
     */
    Irregular(5),
    // / <summary>
    // / Effects related to irregularities in trading/days
    // / (and captured by a preadjustment of the series)
    // / </summary>
    /**
     *
     */
    CalendarEffect(6);
    /**
     * 
     * @param value
     * @return
     */
    public static ComponentType valueOf(final int value)
    {
	for (ComponentType type : EnumSet.allOf(ComponentType.class))
	    if (type.intValue() == value)
		return type;
	return null;
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
