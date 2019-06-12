/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.spi;

import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.modelling.regsarima.RegSarimaDescription;
import demetra.modelling.regsarima.RegSarimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegArima {
    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);
    
    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }
    
    public ConcentratedLikelihoodWithMissing computeConcentratedLikelihood(RegArimaModel model){
        return PROCESSOR.get().computeConcentratedLikelihood(model);
    }
    
    public RegSarimaEstimation compute(RegSarimaDescription model){
        return PROCESSOR.get().compute(model);
    }
    
    public static interface Processor{
 
        /**
         * 
         * @param model
         * @return 
         */
        ConcentratedLikelihoodWithMissing computeConcentratedLikelihood(RegArimaModel model);
        
        /**
         * 
         * @param model
         * @return 
         */
        RegSarimaEstimation compute(RegSarimaDescription model);
    }
    
}
