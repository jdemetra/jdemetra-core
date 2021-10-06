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

import java.util.Collections;
import nbbrd.design.Development;
import java.util.List;

/**
 * Generic description of a group of diagnostics
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface Diagnostics {

    /**
     * Gets the name of the group of diagnostics
     * @return 
     */
    String getName();

    /**
     * Get the list of the tests
     * @return A non empty list of tests.
     */
    List<String> getTests();

    /**
     * Gets the quality of a specified diagnostic
     * @param test
     * @return 
     */
    ProcQuality getDiagnostic(String test);

    /**
     * Gets the value of the diagnostic, if any.
     * Double.Nan is returned if no value is available
     * @param test
     * @return 
     */
    double getValue(String test);

    /**
     * Gets all the warnings related to the given tests.
     * @return The list of warnings.
     */
    default List<String> getWarnings(){
        return Collections.emptyList();
    }
}
