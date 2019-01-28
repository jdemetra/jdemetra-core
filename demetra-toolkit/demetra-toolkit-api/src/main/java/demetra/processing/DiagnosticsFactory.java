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

import java.util.List;

/**
 *
 * @author Jean Palate
 */
public interface DiagnosticsFactory <R extends ProcResults>{

    void dispose();

    String getName();

    String getDescription();

    boolean isEnabled();

    void setEnabled(boolean enabled);
    
    /**
     * Get the list of the tests
     * @return A non empty list of tests.
     */
    List<String> getTestDictionary();
    

    /**
     * The properties object should be designed to appear in a property grid.
     * It describes the default parameters used to make a new instance.
     * It should be possible to modify the parameters (through a property grid)
     * before using them to create a new instance.
     * @return
     */
    Object getProperties();

    void setProperties(Object obj);

    Diagnostics create(R rslts);
}
