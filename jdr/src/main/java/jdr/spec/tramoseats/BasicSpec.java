/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Jean Palate
 */
public class BasicSpec extends BaseTramoSpec {

    BasicSpec(TramoSpecification spec) {
        super(spec);
    }

    public SpanSelector getSpan() {
        return new SpanSelector(core.getTransform().getSpan());
    }

    public boolean isPreliminaryCheck() {
        return core.getTransform().isPreliminaryCheck();
    }

    public void setPreliminaryCheck(boolean value) {
        core.getTransform().setPreliminaryCheck(value);
    }

}
