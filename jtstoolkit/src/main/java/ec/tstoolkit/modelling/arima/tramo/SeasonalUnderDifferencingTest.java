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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class SeasonalUnderDifferencingTest extends AbstractModelController {

    private static final double DEF_SBOUND = .91;

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (context.description.getFrequency() == 1) {
            return ProcessingResult.Unprocessed;
        }
        if (fixSeasonalRoots(context)) {
            return ProcessingResult.Changed;
        }
        // check seasonal quasi-unit roots
        if (!isUnderDiff(context)) {
            return ProcessingResult.Unchanged;
        }
        ModellingContext scontext=buildNewModel(context);
        PreprocessingModel smodel = scontext.tmpModel();
        if (smodel == null) {
            return ProcessingResult.Failed;
        }
        if (new ModelComparator().compare(smodel, context.tmpModel()) < 0) {
//            setReferenceModel(smodel);
            transferInformation(scontext, context);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    private boolean isUnderDiff(ModellingContext context) {
        SeasonalityTests tests = SeasonalityTests.
                residualSeasonalityTest(context.estimation.getLikelihood().getResiduals(),
                TsFrequency.valueOf(context.description.getFrequency()));
        return tests.getScore() >1 || (tests.getScore()== 1 && context.hasseas);
    }

    private ModellingContext buildNewModel(ModellingContext context) {
        ModellingContext ncontext = new ModellingContext();
        ModelDescription ndesc = context.description.clone();
        SarimaSpecification spec = ndesc.getSpecification();
        spec.setBP(0);
        spec.setBD(1);
        spec.setBQ(1);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        ncontext.description = ndesc;
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return null;
        }
        return ncontext;
    }

    private boolean fixSeasonalRoots(ModellingContext context) {
        SarimaModel model = context.estimation.getRegArima().getArima();
        SarimaSpecification spec = model.getSpecification();
        if (spec.getBD() != 0 || spec.getBP() != 1 || model.bphi(1) >= -DEF_SBOUND) {
            return false;
        }
        spec.setBP(0);
        spec.setBD(1);
        spec.setBQ(1);
        ModellingContext ncontext = new ModellingContext();
        ModelDescription ndesc = context.description.clone();
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        ncontext.description = ndesc;
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return false;
        } else {
            transferInformation(ncontext, context);
            return true;
        }
    }
}
