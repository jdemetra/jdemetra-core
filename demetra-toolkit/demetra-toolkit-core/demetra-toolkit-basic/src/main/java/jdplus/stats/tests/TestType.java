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
package jdplus.stats.tests;

import demetra.design.Development;

/**
 * An enumeration representing the way tests are conducted
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public enum TestType {
    /**
     *
     */
    Undefined,
    /** test_Lower : test that a statistic is below some value */
    Lower,
    /** test_Upper : test that a statistic is above some value */
    Upper,
    /**
     * test_TwoSided : test that a statistic is below some value and above
     * another value
     */
    TwoSided;
}
