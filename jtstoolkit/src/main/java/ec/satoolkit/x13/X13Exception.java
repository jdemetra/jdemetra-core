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


package ec.satoolkit.x13;

import ec.satoolkit.SaException;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X13Exception extends SaException {
    public static final String ERR_CONTEXT = "Invalid context";

    /**
     * 
     */
    public X13Exception()
    {
    }

    // / <summary>
    // / Constructor for a time series exception with a specific message
    // / </summary>
    // / <param name="msg">Message of the exception</param>
    /**
     * 
     * @param msg
     */
    public X13Exception(String msg) {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public X13Exception(final String message, final Exception innerException) {
	super(message, innerException);
    }

    /**
     * 
     * @param msg
     * @param origin
     */
    public X13Exception(final String msg, final String origin) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg, origin);
    }
}
