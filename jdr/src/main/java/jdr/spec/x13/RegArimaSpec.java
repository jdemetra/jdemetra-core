/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.descriptors.IObjectDescriptor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class RegArimaSpec {

    final RegArimaSpecification core;

    public RegArimaSpec(RegArimaSpecification spec) {
        core=spec;
    }

    public RegArimaSpecification getCore() {
        return core;
    }

    public BasicSpec getBasic() {
        return new BasicSpec(core);
    }

    public RegressionSpec getRegression() {
        return new RegressionSpec(core);
    }

    public TransformSpec getTransform() {
        return new TransformSpec(core);
    }

    public ArimaSpec getArima() {
        return new ArimaSpec(core);
    }

    public OutlierSpec getOutliers() {
        return new OutlierSpec(core);
    }

    public EstimateSpec getEstimate() {
        return new EstimateSpec(core);
    }

}
