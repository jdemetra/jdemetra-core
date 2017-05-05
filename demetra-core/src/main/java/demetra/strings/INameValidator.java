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

package demetra.strings;

/**
 *
 * @author Jean Palate
 */
public interface INameValidator {
    /**
     * Check is a name is valid.
     * @param name The name to be checked
     * @return True if the name was accepted
     */
    boolean accept(String name);
    
    /**
     * Gets the last error message.
     * The message is reset to null after an accepted name
     * @return The error if the last call to accept failed, null otherwise
     */
    String getLastError();
}
