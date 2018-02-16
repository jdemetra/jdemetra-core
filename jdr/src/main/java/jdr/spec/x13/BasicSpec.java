/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Kristof Bayens
 */
public class BasicSpec extends BaseRegArimaSpec {

    BasicSpec(RegArimaSpecification spec) {
        super(spec);
    }

    public SpanSelector getSpan() {
        return new SpanSelector(core.getBasic().getSpan());
    }

    public boolean isPreprocessing() {
        return core.getBasic().isPreprocessing();
    }

    public void setPreprocessing(boolean value) {
        core.getBasic().setPreprocessing(value);
    }

    public boolean isPreliminaryCheck() {
        return core.getBasic().isPreliminaryCheck();
    }

    public void setPreliminaryCheck(boolean value) {
        core.getBasic().setPreliminaryCheck(value);
    }

}
