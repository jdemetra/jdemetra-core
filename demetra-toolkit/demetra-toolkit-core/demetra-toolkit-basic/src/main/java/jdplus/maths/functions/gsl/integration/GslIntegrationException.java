/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.gsl.integration;

import demetra.DemetraException;
import demetra.design.Development;

@Development(status = Development.Status.Alpha)
public class GslIntegrationException extends DemetraException {

    /**
     *
     */
    public GslIntegrationException() {
    }

    /**
     *
     * @param msg
     */
    public GslIntegrationException(final String msg) {
        // super(g_origin, g_rmgr.GetString(msg));
        super(msg);
    }

    /**
     *
     * @param message
     * @param innerException
     */
    public GslIntegrationException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
