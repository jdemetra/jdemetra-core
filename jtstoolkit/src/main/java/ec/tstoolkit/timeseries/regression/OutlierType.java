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


package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.IntValue;
import java.util.EnumSet;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum OutlierType implements IntValue {
    // / <summary>
    // / Undefined
    // / </summary>
    /**
     *
     */
    Undefined(0),
    // / <summary>
    // / Additive outlier
    // / </summary>
    /**
         *
         */
    AO(1),
    // / <summary>
    // / Level shift
    // / </summary>
    /**
         *
         */
    LS(2),
    // / <summary>
    // / Transitory change
    // / </summary>
    /**
         *
         */
    TC(3),
    // / <summary>
    // / Seasonal outlier
    // / </summary>
    /**
         *
         */
    SO(4),
    // non standard kinds
    // / <summary>
    // / Innovation outlier
    // / </summary>
    /**
         *
         */
    IO(5),
    // / <summary>
    // / Seasonal level shift
    // / </summary>
    /**
         *
         */
    SLS(6),
    // / <summary>
    // / Displacement outlier
    // / </summary>
    /**
         *
         */
    WO(7),
    // / <summary>
    // / Temporary level shift
    // / </summary>
    /**
     *
     */
    TLS(8);

    /**
     * 
     * @param value
     * @return
     */
    public static OutlierType valueOf(final int value) {
	for (OutlierType periodKind : EnumSet.allOf(OutlierType.class))
	    if (periodKind.intValue() == value)
		return periodKind;
	return null;
    }

    private final int value;

    OutlierType(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this OutlierType as an int.
     * @return
     */
    @Override
    public int intValue() {
	return value;
    }

}
