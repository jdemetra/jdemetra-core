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

import demetra.data.DoubleSequence;
import demetra.modelling.regression.ModellingContext;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
class SeasonalUnderDifferencingTest extends ModelController {

    private static final double DEF_SBOUND = .91;

    @Override
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
        int period = modelling.getDescription().getAnnualFrequency();
        if (period == 1) {
            return ProcessingResult.Unprocessed;
        }
        if (fixSeasonalRoots(modelling)) {
            return ProcessingResult.Changed;
        }
        // check seasonal quasi-unit roots
        if (!isUnderDiff(modelling, context)) {
            return ProcessingResult.Unchanged;
        }
        RegArimaModelling scontext = buildNewModel(modelling);
        PreprocessingModel smodel = scontext.build();
        if (smodel == null) {
            return ProcessingResult.Failed;
        }
        ModelComparator cmp = ModelComparator.builder()
                .build();
        if (cmp.compare(smodel, modelling.build()) < 0) {
//            setReferenceModel(smodel);
            transferInformation(scontext, modelling);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    private boolean isUnderDiff(RegArimaModelling modelling, TramoProcessor.Context context) {
        DoubleSequence res = modelling.getEstimation().getConcentratedLikelihood().e();
        SeasonalityTests tests
                = SeasonalityTests.residualSeasonalityTest(res, modelling.getDescription().getAnnualFrequency());
        return tests.getScore() > 1 || (tests.getScore() == 1 && context.seasonal);
    }

    private RegArimaModelling buildNewModel(RegArimaModelling context) {
        RegArimaModelling ncontext = new RegArimaModelling();
        ModelDescription ndesc = new ModelDescription(context.getDescription());
        SarimaSpecification spec = ndesc.getSpecification();
        spec.setBp(0);
        spec.setBd(1);
        spec.setBq(1);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        ncontext.setDescription(ndesc);
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return null;
        }
        return ncontext;
    }

    private boolean fixSeasonalRoots(RegArimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaModel model = description.arima();
        SarimaSpecification spec = model.specification();
        if (spec.getBd() != 0 || spec.getBp() != 1 || model.bphi(1) >= -DEF_SBOUND) {
            return false;
        }
        spec.setBp(0);
        spec.setBd(1);
        spec.setBq(1);
        RegArimaModelling ncontext = new RegArimaModelling();
        ModelDescription ndesc = new ModelDescription(description);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        ncontext.setDescription(ndesc);
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return false;
        } else {
            transferInformation(ncontext, context);
            return true;
        }
    }
}
