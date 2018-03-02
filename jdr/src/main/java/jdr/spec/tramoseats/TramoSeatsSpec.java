/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import jdr.spec.sa.SaBenchmarkingSpec;

/**
 *
 * @author Kristof Bayens
 */
public class TramoSeatsSpec {

    final TramoSeatsSpecification core;

    public static TramoSeatsSpec of(String spec){
        TramoSeatsSpecification rspec=TramoSeatsSpecification.fromString(spec).clone();
        return new TramoSeatsSpec(rspec);
    }

    public TramoSeatsSpec(TramoSeatsSpecification spec) {
        core = spec;
    }

     public TramoSeatsSpecification getCore() {
        return core;
    }

    public BasicSpec getBasic() {
        return new BasicSpec(core.getTramoSpecification());
    }

    public RegressionSpec getRegression() {
        return new RegressionSpec(core.getTramoSpecification());
    }

    public TransformSpec getTransform() {
        return new TransformSpec(core.getTramoSpecification());
    }

    public ArimaSpec getArima() {
        return new ArimaSpec(core.getTramoSpecification());
    }

    public OutlierSpec getOutlier() {
        return new OutlierSpec(core.getTramoSpecification());
    }

    public EstimateSpec getEstimate() {
        return new EstimateSpec(core.getTramoSpecification());
    }

    public SeatsSpec getSeats() {
        return new SeatsSpec(core.getSeatsSpecification());
    }

    public SaBenchmarkingSpec getBenchmarking() {
        return new SaBenchmarkingSpec(core.getBenchmarkingSpecification());
    }

}
