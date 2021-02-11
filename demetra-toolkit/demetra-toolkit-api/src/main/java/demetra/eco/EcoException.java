/*
 * Copyright 2021 National Bank of Belgium.
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
package demetra.eco;

import demetra.DemetraException;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class EcoException extends DemetraException {

    public static final String INV_VAR = "Invalid variance";
    public static final String NEG_VAR = "Negative variance";
    public static final String OLS_FAILED = "Ols failed";
    public static final String GLS_FAILED = "Gls failed";
    public static final String UNEXPECTEDOPERATION="Unexpected operation";
    public static final String NOT_ENOUGH_OBS="Not enough observations";

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
