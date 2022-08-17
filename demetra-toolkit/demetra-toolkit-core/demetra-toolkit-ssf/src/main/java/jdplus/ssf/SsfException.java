/*
* Copyright 2016 National Bank of Belgium
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
package jdplus.ssf;

import demetra.DemetraException;
import nbbrd.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfException extends DemetraException  {

    /**
     *
     */
    public static final String STATIONARY = "Non stationary model";
    /**
     *
     */
    public static final String STOCH = "Invalid stochastic model";
    /**
     *
     */
    public static final String COMPOSITE = "Invalid composite model";
    /**
     *
     */
    public static final String DIFFUSE = "Error in diffuse initialization";
    /**
     *
     */
    public static final String FASTFILTER = "Invalid fast filter";
    public static final String TYPE = "Invalid type";
    public static final String INCONSISTENT = "Inconsistent constraints in the model";
    public static final String STATUS = "Invalid status for the state vector";
    public static final String INITIALIZATION = "Invalid initialization";
    public static final String ERRORS="Errors in measurement are not supported yet for this algorithm";
    public static final String MODEL="Invalid model definition";

    /**
     *
     */
    public SsfException() {
        super();
    }

    /**
     *
     * @param msg
     */
    public SsfException(final String msg) {
        // super(g_origin, g_rmgr.GetString(msg));
        super(msg);
    }

    /**
     *
     * @param message
     * @param innerException
     */
    public SsfException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
