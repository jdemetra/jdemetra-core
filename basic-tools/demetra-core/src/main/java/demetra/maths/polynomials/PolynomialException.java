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
package demetra.maths.polynomials;

import demetra.design.Development;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PolynomialException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4381169988217677067L;

    /**
         *
         */
    public final static String CONJUGATE = "p_err_conj";

    /**
     *
     */
    public final static String DIVISION = "p_err_div";

    /**
         *
         */
    public final static String POLE = "rf_err_pole";

    /**
     *
     */
    public PolynomialException() {
    }

    /**
     * 
     * @param msg
     */
    public PolynomialException(final String msg) {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public PolynomialException(final String message,
	    final Exception innerException) {
	super(message, innerException);
    }

    // TODO:
    // private static final System.Resources.ResourceManager g_rmgr = new
    // System.Resources.ResourceManager("Nbb.Maths.Properties.Resources",
    // System.Reflection.Assembly.GetCallingAssembly());
    // }

}
