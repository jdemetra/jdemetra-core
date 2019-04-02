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
package demetra.linearmodel;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class Coefficient {

    /**
     * Value of the coefficient
     */
    private double value;
    /**
     * Std error of the coefficient. Could be 0 (fixed coeff)
     */
    private double stdError;
    /**
     * P-value (T-Stat)
     */
    private double pValue;
    /**
     * Description of the coefficient
     */
    private String description;

    /**
     * T-Stat
     *
     * @return
     */
    public double getTStat() {
        return stdError == 0 ? Double.NaN : value / stdError;
    }
}
