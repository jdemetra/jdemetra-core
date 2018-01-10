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


package demetra.maths.functions;

import demetra.design.Development;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FunctionException extends RuntimeException {

    // private static final long serialVersionUID = -7244734237040488179L;

    /**
	 * 
	 */
    private static final long serialVersionUID = -2180798689166385013L;

    /**
         *
         */
    /**
         *
         */
    public static final String D_ERR = "Error in computing derivatives",
	    STEP_ERR = "Error in computing optimization step",
	    BOUND_ERR = "Boundaries error",
	    MAXITER_ERR = "Maximal number of evaluations exceeded", 
            DIM_ERR = "Incompatible dimensions in the paramerers";

    /**
     *
     */
    /**
     *
     */
    public static final int D_ID = 1, STEP_ID = 2, BOUND_ID = 3,
	    MAXITER_ID = 99;

    /**
     * 
     * @param msg
     * @return
     */
    public static int check(String msg) {
	if (msg.equals(D_ERR))
	    return D_ID;
	if (msg.equals(STEP_ERR))
	    return STEP_ID;
	if (msg.equals(BOUND_ERR))
	    return BOUND_ID;
	if (msg.equals(MAXITER_ERR))
	    return MAXITER_ID;
	return 0;
    }

    /**
     *
     */
    public FunctionException() {
    }

    /**
     * 
     * @param msg
     */
    public FunctionException(final String msg) {
	super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public FunctionException(final String message,
	    final Exception innerException) {
	super(message, innerException);
    }

 }
