/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;

import demetra.maths.matrices.Matrix;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.TimeSeriesDomain;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Regression {

    private final RegressionLoader.Processor PROCESSOR = new RegressionLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public <D extends TimeSeriesDomain> Matrix variables(List<ITsVariable> vars, D domain) {
        return PROCESSOR.get().variables(vars, domain);
    }

    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Processor {

        <D extends TimeSeriesDomain> Matrix variables(List<ITsVariable> vars, D domain);

    }
}
