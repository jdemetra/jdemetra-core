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
package demetra.stats;

import nbbrd.design.Development;

/**
 * An enumeration of the types of probability.
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public enum ProbabilityType {
    /** prob_Point : the probability of x = value */
    Point,
    /** prob_Lower : the probability of x .LE. value */
    Lower,
    /** prob_Upper : the probability of x .GE. value */
    Upper;
}
