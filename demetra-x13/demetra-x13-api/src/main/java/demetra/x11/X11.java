/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.design.Algorithm;
import nbbrd.design.Development;
import nbbrd.service.ServiceDefinition;
import demetra.timeseries.TsData;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;

/**
 *
 * @author Thomas Witthohn
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class X11 {

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Processor {

        X11Results process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec);

    }

    private final X11Loader.Processor PROCESSOR = new X11Loader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public X11Results process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec) {
        return PROCESSOR.get().process(timeSeries, spec);
    }

}
