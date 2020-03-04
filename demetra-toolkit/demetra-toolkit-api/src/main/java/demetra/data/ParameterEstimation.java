/*
 * Copyright 2020 National Bank of Belgium.
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
package demetra.data;

import demetra.design.Development;

/**
 * TODO Is it necessary?
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@Development(status = Development.Status.Preliminary)
public class ParameterEstimation {
    /**
     * Value of the parameter
     */
    private double value;
    /**
     * Standard deviation of the parameter. 0 if the parameter is fixed or NaN if
     * the standard deviation was not computed.
     */
    private double standardError;
    /**
     * P-Value of the test H0: p=0. NaN if the parameter is not estimated (fixed parameter).
     * In many cases, the test is P(T>|t|) where T is the T distribution and t is
     * the value of the T-stat of the parameter.
     */
    private double pvalue;
    /**
     * Any suitable information/description
     */
    private String description;
   
}
