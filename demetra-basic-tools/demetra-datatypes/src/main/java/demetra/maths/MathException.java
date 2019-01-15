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
package demetra.maths;

import demetra.DemetraException;
import demetra.design.Development;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MathException extends DemetraException {

    /**
         *
         */
    public final static String DIM = "m_err_dim";

    /**
     *
     */
    public final static String SQUARE = "m_err_square";

    /**
     *
     */
    public final static String CHOLESKY = "m_err_chol";

    /**
         *
         */
    public final static String RANK = "m_err_rank";

    /**
         *
         */
    public final static String SINGULAR = "m_err_sing";
    /**
         *
         */
    public final static String NOTIMPL = "err_notimpl";

    /**
     *
     */
    public MathException() {
    }

    /**
     * 
     * @param msg
     */
    public MathException(final String msg) {
	// super(g_origin, g_rmgr.GetString(msg));
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public MathException(final String message, final Exception innerException) {
	super(message, innerException);
    }

}
