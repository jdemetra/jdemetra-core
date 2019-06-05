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
package jdplus.maths.linearfilters;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LinearFilterException extends RuntimeException
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 3845976478247215076L;

    /**
         *
         */
    public static final String SFILTER = "lf_err_sfilter", LENGTH="incorrect buffer length";

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

}
