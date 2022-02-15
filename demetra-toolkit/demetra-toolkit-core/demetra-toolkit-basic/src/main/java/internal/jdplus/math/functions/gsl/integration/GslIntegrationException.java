/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.jdplus.math.functions.gsl.integration;

import demetra.DemetraException;
import nbbrd.design.Development;

@Development(status = Development.Status.Alpha)
public class GslIntegrationException extends DemetraException {

    public GslIntegrationException() {
    }

    public GslIntegrationException(final String msg) {
        super(msg);
    }

    public GslIntegrationException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
