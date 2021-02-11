/*
 * Copyright 2021 National Bank of Belgium.
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
package demetra.likelihood;

import nbbrd.design.Development;

/**
 * Estimation of a missing value in an array of doubles in the context of 
 * maximum likelihood estimation
 * @author Jean Palate <jean.palate@nbb.be>
 * 
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class MissingValueEstimation {
    
    /**
     * 0-based position of the missing value in the array
     */
    private int position;
    /**
     * Estimated value
     */
    private double value;
    /**
     * Standard deviation of the estimated value
     */
    private double standardError;
   
}
