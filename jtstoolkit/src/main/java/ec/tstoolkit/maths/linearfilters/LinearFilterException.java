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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LinearFilterException extends BaseException
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 3845976478247215076L;

    /**
         *
         */
    public static final String InvalidSFilter = "lf_err_sfilter";

    /**
     *
     */
    public LinearFilterException() {
	super();
    }

    /**
     * 
     * @param msg
     */
    public LinearFilterException(final String msg) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public LinearFilterException(final String message,
	    final Exception innerException) {
	super(message, innerException);
    }

    /**
     * 
     * @param msg
     * @param origin
     */
    public LinearFilterException(final String msg, final String origin) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg, origin, 0);
    }

    // TODO: resourcemanager
    // private static readonly System.Resources.ResourceManager g_rmgr = new
    // System.Resources.ResourceManager("Nbb.Maths.Properties.Resources",
    // System.Reflection.Assembly.GetCallingAssembly());

}
