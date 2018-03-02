/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;

/**
 *
 * @author Jean Palate
 */
public class TramoSpec extends BaseTramoSpec {

    public static TramoSpec of(String spec){
        TramoSpecification rspec=TramoSpecification.fromString(spec).clone();
        return new TramoSpec(rspec);
    }

    public TramoSpec(TramoSpecification spec) {
        super(spec);
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

    public OutlierSpec getOutlier() {
        return new OutlierSpec(core);
    }

    public ArimaSpec getArima() {
        return new ArimaSpec(core);
    }

    public EstimateSpec getEstimate() {
        return new EstimateSpec(core);
    }

}
