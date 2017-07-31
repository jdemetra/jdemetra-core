/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.eco;

/**
 *
 * @author Jean Palate
 */
public class EcoException extends RuntimeException {

    public static final String INV_VAR = "Invalid variance";
    public static final String NEG_VAR = "Negative variance";
    public static final String OLS_FAILED = "Ols failed";
    public static final String GLS_FAILED = "Gls failed";

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
