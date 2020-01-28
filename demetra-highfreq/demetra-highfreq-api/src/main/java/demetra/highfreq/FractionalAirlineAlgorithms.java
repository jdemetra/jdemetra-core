/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.design.Algorithm;
import demetra.design.Development;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class FractionalAirlineAlgorithms {

    private final FractionalAirlineAlgorithmsLoader.Processor PROCESSOR = new FractionalAirlineAlgorithmsLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    /**
     *
     * @param s Time series being decomposed
     * @param airline
     *
     * @return
     */
    public FractionalAirlineDecomposition process(double[] s, FractionalAirline airline) {
        return PROCESSOR.get().process(s, airline);
    }

    public FractionalAirlineEstimation process(FractionalAirlineSpec spec) {
        return PROCESSOR.get().process(spec);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public interface Processor {

        FractionalAirlineDecomposition process(double[] s, FractionalAirline airline);

        FractionalAirlineEstimation process(FractionalAirlineSpec spec);
    }

}
