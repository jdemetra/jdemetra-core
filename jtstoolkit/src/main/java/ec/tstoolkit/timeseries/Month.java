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
@Development(status = Development.Status.Alpha)
public enum Month implements IntValue {
    // / <summary>January (=1)</summary>

    /**
     *
     */
    January(0),
    // / <summary>February (=2)</summary>
    /**
     *
     */
    February(1),
    // / <summary>March (=3)</summary>
    /**
     *
     */
    March(2),
    // / <summary>April (=4)</summary>
    /**
     *
     */
    April(3),
    // / <summary>May (=5)</summary>
    /**
     *
     */
    May(4),
    // / <summary>June (=6)</summary>
    /**
     *
     */
    June(5),
    // / <summary>July (=7)</summary>
    /**
     *
     */
    July(6),
    // / <summary>August (=8)</summary>
    /**
     *
     */
    August(7),
    // / <summary>September (=9)</summary>
    /**
     *
     */
    September(8),
    // / <summary>October (=10)</summary>
    /**
     *
     */
    October(9),
    // / <summary>November (=11)</summary>
    /**
     *
     */
    November(10),
    // / <summary>December (=12)</summary>
    /**
     *
     */
    December(11);
    /**
     * 
     * @param value
     * @return
     */
    public static Month valueOf(final int value) {
	for (Month option : EnumSet.allOf(Month.class))
	    if (option.intValue() == value)
		return option;
	return null;
    }

    private final int value;

    Month(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this Month as an int.
     * @return
     */
    @Override
    public int intValue() {
	return value;
    }
}
