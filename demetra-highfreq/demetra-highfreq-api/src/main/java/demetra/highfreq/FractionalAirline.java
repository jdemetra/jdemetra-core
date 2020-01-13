/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.math.matrices.MatrixType;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class FractionalAirline {
    private final FractionalAirlineLoader.Processor PROCESSOR = new FractionalAirlineLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    /**
     * 
     * @param s Time series being decomposed
     * @param period Periodicity of the series
     * @param adjust If true, adjust the periodicity to the nearest integer, otherwise use 
     * fractional model
     * @param sn If true, consider a simple signal/noise model; all the noise is added to the first component (leaving the seasonal component as smooth as possible).
     * Otherwise, a separate noise is created. The seasonal component is the same in both cases.
     * @return 
     */
    public FractionalAirlineDecomposition process(double[] s, double period, boolean adjust, boolean sn){
        return PROCESSOR.get().process(s, period, adjust, sn);
    }

   public FractionalAirlineModel process(FractionalAirlineSpec spec){
        return PROCESSOR.get().process(spec);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public interface Processor {

        FractionalAirlineDecomposition process(double[] s, double period, boolean adjust, boolean sn);
        FractionalAirlineModel process(FractionalAirlineSpec spec);
    }
    
}
