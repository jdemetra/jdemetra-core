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


package ec.tstoolkit.algorithm;

import ec.tstoolkit.ICustomizable;
import ec.tstoolkit.design.Development;

/**
 * Generic interface for the creation of output generating tool.
 * The factory should be a singleton
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IOutputFactory<D extends IProcDocument<?, ?, ?>> extends ICustomizable{
    
    /**
     * Called when the factory is no longer used. Most implementations will be empty.
     */
    void dispose();
    /**
     * Gets the name of the factory
     * @return 
     */
    String getName();

    /**
     * Gets the description of the factory
     * @return 
     */
    String getDescription();

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
     * @param properties The properties of the generating tool. Can be null; 
     * in that case, default properties will be used.
     * @return 
     */
    IOutput<D> create();

}
