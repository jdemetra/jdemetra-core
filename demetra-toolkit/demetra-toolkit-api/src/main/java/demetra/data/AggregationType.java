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
package demetra.data;

import demetra.design.Development;
import java.util.EnumSet;
import java.util.function.IntSupplier;

/**
 * Describes the way a set of ordered observations are aggregated
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public enum AggregationType implements IntSupplier {
    /**
     * No transformation
     */
    None(0),
    /**
     * Sum
     */
    Sum(1),
    /**
     * Average
     */
    Average(2),
    /**
     * First observation
     */
    First(3),
    /**
     * Last observation
     */
    Last(4),
    /**
     * Minimum
     */
    Min(5),
    /**
     * Maximum
     */
    Max(6);

    /**
     * Enum correspondence to an integer
     *
     * @param value Integer representation of the TsAggregationType
     * @return Enum representation of the TsAggregationType
     */
    public static AggregationType valueOf(int value) {
        for (AggregationType option : EnumSet.allOf(AggregationType.class)) {
            if (option.getAsInt() == value) {
                return option;
            }
        }
        return null;
    }

    private final int value;

    AggregationType(final int value) {
        this.value = value;
    }

    /**
     * Returns the value of this TsAggregationType as an int.
     *
     * @return
     */
    @Override
    public int getAsInt() {
        return value;
    }
}
