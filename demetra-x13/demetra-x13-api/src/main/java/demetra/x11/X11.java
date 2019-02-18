/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.design.ServiceDefinition;
import demetra.timeseries.TsData;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Thomas Witthohn
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class X11 {

    @Algorithm
    @ServiceDefinition
    public static interface Processor {

        X11Results process(TsData timeSeries, X11Spec spec);

    }

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public X11Results process(TsData timeSeries, X11Spec spec) {
        return PROCESSOR.get().process(timeSeries, spec);
    }

}
