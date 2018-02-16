/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;

/**
 *
 * @author Kristof Bayens
 */
public abstract class BaseRegArimaSpec {

    final RegArimaSpecification core;

    public BaseRegArimaSpec(RegArimaSpecification spec) {
        core = spec;
    }

    public RegArimaSpecification getCore() {
        return core;
    }

}
