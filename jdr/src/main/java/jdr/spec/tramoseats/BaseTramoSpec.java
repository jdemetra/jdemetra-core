/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.descriptors.IObjectDescriptor;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;

/**
 *
 * @author Jean Palate
 */
public abstract class BaseTramoSpec{

    final TramoSpecification core;

    public BaseTramoSpec(TramoSpecification spec) {
      core = spec;
    }

    public TramoSpecification getCore() {
        return core;
    }
    
}
