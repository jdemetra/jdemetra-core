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

package ec.satoolkit.seats;

import ec.satoolkit.SaException;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeatsException extends SaException {

    /**
     * 
     */
    private static final long serialVersionUID = 5655759917791571299L;

    final static String ERR_MODEL = "Invalid model";

    final static String ERR_DECOMP = "Invalid decomposition";

    final static String ERR_ESTIMATION = "Model estimation failed";

    public static final String ERR_LENGTH = "Not enough observations", ERR_MISSING = "Missing values are not allowed";

    /**
     *
     */
    public SeatsException() {
    }

    // / <summary>
    // / Constructor for a time series exception with a specific message
    // / </summary>
    // / <param name="msg">Message of the exception</param>
    /**
     * 
     * @param msg
     */
    public SeatsException(String msg) {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public SeatsException(final String message, final Exception innerException) {
	super(message, innerException);
    }

    /**
     * 
     * @param msg
     * @param origin
     */
    public SeatsException(final String msg, final String origin) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg, origin);
    }
}
