/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramo;

import demetra.design.Development;
import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.PreprocessingModel;
import jdplus.regarima.regular.ProcessingResult;
import jdplus.regarima.regular.RegArimaModelling;
import demetra.arima.SarimaSpecification;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class ModelBenchmarking extends ModelController {

    public ModelBenchmarking() {
    }

    @Override
    public ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {

        PreprocessingModel current = modelling.build();

        SarimaSpecification spec = current.getDescription().specification();
        if (spec.isAirline(context.seasonal)) {
            return ProcessingResult.Unchanged;
        }
        ModelVerifier verifier = new ModelVerifier();
        if (verifier.accept(modelling)) {
            return ProcessingResult.Unchanged;
        }

        // compute the corresponding airline model.
        RegArimaModelling nmodelling = new RegArimaModelling();
        ModelDescription ndesc=new ModelDescription(current.getDescription()); 
        ndesc.setAirline(context.seasonal);
        ndesc.setMean(context.seasonal ? modelling.getDescription().isMean() : true);
        ndesc.removeVariable(var->var.isOutlier(false));
        nmodelling.setDescription(ndesc);

        if (!estimate(nmodelling, true)) {
            return ProcessingResult.Failed;
        }


        PreprocessingModel nmodel = nmodelling.build();
        ModelComparator mcmp = ModelComparator.builder().build();
        int cmp = mcmp.compare(current, nmodel);
        if (cmp < 1) {
            return ProcessingResult.Unchanged;
        } else {
            transferInformation(nmodelling, modelling);
            return ProcessingResult.Changed;
        }
    }
}
