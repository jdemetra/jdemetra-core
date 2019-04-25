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
package demetra.arima;

import demetra.DemetraException;
import demetra.design.Development;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class ArimaException extends DemetraException {

    /**
     *
     */
    public static final String AR_INVALID = "invalid arima AR";
    /**
     *
     */
    public static final String NONSTATIONARY = "acgf of non stationary model";
    /**
     *
     */
    public static final String INVALID = "invalid ARIMA";
    /**
     *
     */
    public static final String MIN_SPECTRUM = "error in minimizing the spectrum";

    /**
     * 
     */
    public ArimaException() {
    }

    /**
     * 
     * @param msg
     */
    public ArimaException(final String msg) {
        // super(g_origin, g_rmgr.GetString(msg));
        super(msg);
    }

    /**
     * 
     * @param message
     * @param innerException
     */
    public ArimaException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
