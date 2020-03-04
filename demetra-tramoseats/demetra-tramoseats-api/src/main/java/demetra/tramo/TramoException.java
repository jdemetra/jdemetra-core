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
package demetra.tramo;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoException extends RuntimeException {

    public final static String ATIP_E = "Failure in the outliers identification procedure";

    public final static String ATIP_MANY = "Too many outliers";

    public final static String ATIP_ITER = "Too many iterations in the outliers identification procedure";

    public final static String IDDIF_E = "Failure in the identification of the differencing orders";

    /**
     *
     */
    public TramoException() {
    }

    /**
     * Constructor for a time series exception with a specific message
     *
     * @param msg Message of the exception
     */
    public TramoException(String msg) {
        super(msg);
    }

}
