/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;

import demetra.arima.ArimaType;
import demetra.arima.SarimaModel;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.modelling.regarima.RegSarimaProcessing;
import demetra.regarima.RegArimaModel;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegArima {

    private final RegArimaLoader.Processor PROCESSOR = new RegArimaLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public <S extends ArimaType> ConcentratedLikelihoodWithMissing computeConcentratedLikelihood(RegArimaModel.Data<S> model) {
        return PROCESSOR.get().computeConcentratedLikelihood(model);
    }

    public RegArimaModel<SarimaModel> compute(RegSarimaProcessing.Specification model) {
        return PROCESSOR.get().compute(model);
    }

    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Processor {

        /**
         *
         * @param <S>
         * @param model
         * @return
         */
        <S extends ArimaType> ConcentratedLikelihoodWithMissing computeConcentratedLikelihood(RegArimaModel.Data<S> model);

        /**
         *
         * @param model
         * @return
         */
        RegArimaModel<SarimaModel> compute(RegSarimaProcessing.Specification model);
    }

}
