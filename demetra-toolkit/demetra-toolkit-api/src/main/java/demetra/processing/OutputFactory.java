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
 * Generic interface for the creation of output generating tool.
 * The factory should be a singleton
 * @author Jean Palate
 * @param <D> Document type
 */
@Development(status = Development.Status.Alpha)
public interface OutputFactory<D>{
    
    /**
     * Gets the name of the factory
     * @return 
     */
    String getName();

    /**
     * Controls the availability of the factory
     * @return 
     */
    boolean isAvailable();

    /**
     * Checks that the factory is enabled
     * @return 
     */
    boolean isEnabled();

    /**
     * Enables/disables the factory
     * @param enabled 
     */
    void setEnabled(boolean enabled);

    /**
     * Creates the output generating tool.
     * @return 
     */
    Output<D> create();

}
