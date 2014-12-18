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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.IntValue;
import java.util.EnumSet;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public enum PeriodKind implements IntValue
{

    /**
     *
     */
    Undefined(0),
    /**
     *
     */
    Day(1),
    /**
     * 
     */
    Week(2),
    /**
     *
     */
    Regular(3);

    /**
     * 
     * @param value
     * @return
     */
    public static PeriodKind valueOf(final int value) {
	for (PeriodKind periodKind : EnumSet.allOf(PeriodKind.class))
	    if (periodKind.intValue() == value)
		return periodKind;
	return null;
    }

    private final int value;

    PeriodKind(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this PeriodKind as an int.
     * @return
     */
    @Override
    public int intValue() {
	return value;
    }

}
