/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Jean Palate
 */
public class EstimateSpec extends BaseRegArimaSpec {

    EstimateSpec(RegArimaSpecification spec) {
        super(spec);
    }

    private ec.tstoolkit.modelling.arima.x13.EstimateSpec inner() {
        return core.getEstimate();
    }

    public SpanSelector getSpan() {
        return new SpanSelector(inner().getSpan());
    }


    public double getTol() {
        return inner().getTol();
    }

    public void setTol(double value) {
        inner().setTol(value);
    }

}
