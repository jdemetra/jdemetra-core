/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;

import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import demetra.modelling.regression.ITsVariable;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import demetra.util.ServiceLookup;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Regression {

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }
    
    public <D extends TimeSeriesDomain> Matrix variables(List<ITsVariable> vars, D domain){
        return PROCESSOR.get().variables(vars, domain);
    }

    public static interface Processor {

        <D extends TimeSeriesDomain> Matrix variables(List<ITsVariable> vars, D domain);

    }
}
