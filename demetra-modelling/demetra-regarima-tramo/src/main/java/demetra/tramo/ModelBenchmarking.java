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
package demetra.tramo;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelBenchmarking extends AbstractModelController {

    public ModelBenchmarking() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {

        PreprocessingModel current = context.tmpModel();

        SarimaSpecification spec = current.description.getSpecification();
        if (spec.isAirline(context.hasseas)) {
            return ProcessingResult.Unchanged;
        }
        ModelVerifier verifier = new ModelVerifier();
        if (verifier.accept(context)) {
            return ProcessingResult.Unchanged;
        }

        // compute the corresponding airline model.
        ModellingContext scontext = new ModellingContext();
        scontext.description = current.description.clone();
        scontext.description.setAirline(context.hasseas);
        scontext.description.setMean(context.hasseas ? context.description.isMean() : true);
        scontext.description.setOutliers(null);

        if (!estimate(scontext, true)) {
            return ProcessingResult.Failed;
        }


        PreprocessingModel smodel = scontext.tmpModel();
        int cmp = new ModelComparator().compare(current, smodel);
        if (cmp < 1) {
//            setReferenceModel(current);
            return ProcessingResult.Unchanged;
        } else {
//            setReferenceModel(smodel);
            transferInformation(scontext, context);
            return ProcessingResult.Changed;
        }
    }
}
