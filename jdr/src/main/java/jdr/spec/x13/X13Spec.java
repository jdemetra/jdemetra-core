/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.satoolkit.x13.X13Specification;
import jdr.spec.sa.SaBenchmarkingSpec;

/**
 *
 */
public class X13Spec  {

    final X13Specification core;

   public static X13Spec of(String spec){
        X13Specification rspec=X13Specification.fromString(spec).clone();
        return new X13Spec(rspec);
   }
   
    public X13Spec(X13Specification spec) {
        core = spec;
    }

    public X13Specification getCore() {
        return core;
    }

    public BasicSpec getBasic() {
        return new BasicSpec(core.getRegArimaSpecification());
    }

    public RegressionSpec getRegression() {
        return new RegressionSpec(core.getRegArimaSpecification());
    }

    public TransformSpec getTransform() {
        return new TransformSpec(core.getRegArimaSpecification());
    }

    public ArimaSpec getArima() {
        return new ArimaSpec(core.getRegArimaSpecification());
    }

    public OutlierSpec getOutliers() {
        return new OutlierSpec(core.getRegArimaSpecification());
    }

    public EstimateSpec getEstimate() {
        return new EstimateSpec(core.getRegArimaSpecification());
    }

    public X11Spec getX11() {
        return new X11Spec(core.getX11Specification(), 0, true);
    }

    public SaBenchmarkingSpec getBenchmarking() {
        return new SaBenchmarkingSpec(core.getBenchmarkingSpecification());
    }

}