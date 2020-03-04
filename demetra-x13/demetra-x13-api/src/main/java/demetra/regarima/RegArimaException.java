/*
* Copyright 2019 National Bank of Belgium
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
package demetra.regarima;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaException extends RuntimeException {

    private static final long serialVersionUID = -8351066904274802554L;

    final static String ATIP_E = "Failure in the outliers identification procedure";

    final static String ATIP_MANY = "Too many outliers";

    final static String ATIP_ITER = "Too many iterations in the outliers identification procedure";

    final static String IDDIF_E = "Failure in the identification of the differencing orders";

    /**
     *
     */
    public RegArimaException() {
    }

    /**
     * Constructor for a time series exception with a specific message
     *
     * @param msg Message of the exception
     */
    public RegArimaException(String msg) {
        super(msg);
    }

    /**
     * Constructor for a time series exception with a specific message and inner
     * exception
     *
     * @param message Message of the exception
     * @param innerException Inner exception
     */
    public RegArimaException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
