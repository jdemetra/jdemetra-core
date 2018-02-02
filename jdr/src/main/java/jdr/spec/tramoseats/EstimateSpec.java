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
public class EstimateSpec extends BaseTramoSpec {

    @Override
    public String toString() {
        return "";
    }

    private ec.tstoolkit.modelling.arima.tramo.EstimateSpec inner() {
        return core.getEstimate();
    }

    EstimateSpec(TramoSpecification spec) {
        super(spec);
    }

    public SpanSelector getSpan() {
        return new SpanSelector(inner().getSpan());
    }

    public boolean isEml() {
        return inner().isEML();
    }

    public void setEml(boolean value) {
        inner().setEML(value);
    }

    public double getTol() {
        return inner().getTol();
    }

    public void setTol(double value) {
        inner().setTol(value);
    }

    public double getUbp() {
        return inner().getUbp();
    }

    public void setUbp(double u) {
        inner().setUbp(u);
    }

}