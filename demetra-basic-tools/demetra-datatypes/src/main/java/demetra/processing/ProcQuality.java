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

package demetra.processing;

import demetra.design.Development;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public enum ProcQuality {

    Accepted(-1),
    /// <summary>
    /// The quality is undefined: meaningless test, failure in the computation of the test...
    /// </summary>
    Undefined(0),
    /// <summary>
    /// There is an error in the results. The processing should be rejected
    /// </summary>
    Error(1),
    /// <summary>
    /// There is no logical error in the results but they should not be accepted.
    /// </summary>
    Severe(2),
    /// <summary>
    /// The quality of the results is bad, but there is no actual error.
    /// </summary>
    Bad(3),
    /// <summary>
    /// The result of the test is uncertain
    /// </summary>
    Uncertain(4),
    /// <summary>
    /// The results are compatible with the test.
    /// </summary>
    Good(5);

    public static ProcQuality valueOf(int value) {
        ProcQuality[] values = ProcQuality.values();
        for (int i=0; i<values.length; ++i){
            if (values[i].value == value)
                return values[i];
        }
        return null;
    }

    public boolean isAtLeast(ProcQuality quality){
        return value > 0 && value >= quality.value;
    }
    
    public boolean isLower(ProcQuality quality){
        return value > 0 && value < quality.value;
    }

    private final int value;

    ProcQuality(final int value) {
        this.value = value;
    }

    /**
     * Integer representation of the frequency
     *
     * @return The number of events by year
     */
    public int intValue() {
        return value;
    }
}
