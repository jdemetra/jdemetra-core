/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demetra.processing;

import demetra.design.Development;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public enum ProcQuality {

    /**
     * The results have been checked and accepted
     */
    Accepted(-1),
    /**
     * The quality is undefined: meaningless test, failure in the computation of the test...
     */
    Undefined(0),
    /**
     * There is an error in the results. The processing should be rejected
     */
    Error(1),
    /**
     * There is no logical error in the results but they should be accepted with caution.
     */
    Severe(2),
    /**
     * The quality of the results is bad, but there is no actual error.
     */
    Bad(3),
    /**
     * The quality of the results is uncertain
     */
    Uncertain(4),
    /**
     * The results are compatible with the test.
     */
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
