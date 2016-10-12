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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum TsAggregationType implements IntValue {
    /**
     *
     */
    None(0),
    /**
     * 
     */
    Sum(1),
    /**
     * 
     */
    Average(2),
    /**
     *
     */
    First(3),
    /**
     *
     */
    Last(4),
    /**
     * 
     */
    Min(5),
    /**
     *
     */
    Max(6);

    /**
     * 
     * @param value
     * @return
     */
    public static TsAggregationType valueOf(int value) {
        return IntValue.valueOf(TsAggregationType.class, value).orElse(null);
    }

    private final int value;

    TsAggregationType(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this TsAggregationType as an int.
     * @return
     */
    @Override
    public int intValue() {
	return value;
    }

}
