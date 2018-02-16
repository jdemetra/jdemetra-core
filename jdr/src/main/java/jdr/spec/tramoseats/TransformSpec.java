/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Jean Palate
 */
public class TransformSpec extends BaseTramoSpec {

    @Override
    public String toString() {
        return "";
    }

    private ec.tstoolkit.modelling.arima.tramo.TransformSpec inner() {
        return core.getTransform();
    }

    public TransformSpec(TramoSpecification spec) {
        super(spec);
    }

    public String getFunction() {
        return inner().getFunction().name();

    }

    public void setFunction(String value) {
        inner().setFunction(DefaultTransformationType.valueOf(value));
    }

    public SpanSelector getSpan() {
        return new SpanSelector(inner().getSpan());
    }

    public double getFct() {
        return inner().getFct();
    }

    public void setFct(double value) {
        inner().setFct(value);
    }

    public boolean isUnits() {
        return inner().isUnits();
    }

    public void setUnits(boolean value) {
        inner().setUnits(true);
    }
}
