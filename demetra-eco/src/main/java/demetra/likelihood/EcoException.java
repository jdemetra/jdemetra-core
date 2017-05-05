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
package demetra.likelihood;

/**
 * 
 * @author Jean Palate
 */
public class EcoException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2481221517066844109L;
    /**
         *
         */
    public static final String EMPTY = "err_vempty";

    /**
         *
         */
    public static final String LDET = "det_err";

    /**
	 * 
	 */
    // private static final long serialVersionUID = -4153626303926617998L;
    public EcoException() {
    }

    /**
     * 
     * @param msg
     */
    public EcoException(final String msg) {
	// super(g_origin, g_rmgr.GetString(msg))
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public EcoException(final String message, final Exception innerException) {
	super(message, innerException);
    }

}
