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
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.arima.SarimaOrders;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class ModelBenchmarking extends ModelController {

    public ModelBenchmarking() {
    }

    @Override
    public ProcessingResult process(RegSarimaModelling modelling, TramoProcessor.Context context) {

        ModelEstimation current = modelling.build();

        SarimaOrders spec = current.specification();
        if (spec.isAirline(context.seasonal)) {
            return ProcessingResult.Unchanged;
        }
        ModelVerifier verifier = new ModelVerifier();
        if (verifier.accept(modelling)) {
            return ProcessingResult.Unchanged;
        }

        // compute the corresponding airline model.
        ModelDescription ndesc=ModelDescription.copyOf(modelling.getDescription()); 
        ndesc.setAirline(context.seasonal);
        ndesc.setMean(context.seasonal ? modelling.getDescription().isMean() : true);
        ndesc.removeVariable(var->var.isOutlier(false));
        RegSarimaModelling nmodelling = RegSarimaModelling.of(ndesc);

        if (!estimate(nmodelling, true)) {
            return ProcessingResult.Failed;
        }


        ModelEstimation nmodel = nmodelling.build();
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
