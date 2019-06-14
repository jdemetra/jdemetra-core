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

import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.PreprocessingModel;
import jdplus.regarima.regular.ProcessingResult;
import jdplus.regarima.regular.RegArimaModelling;
import demetra.arima.SarimaSpecification;


/**
 *
 * @author Jean Palate
 */
class SeasonalUnderDifferencingTest2 extends ModelController {

    private static final double DEF_SBOUND = .91;

    @Override
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
        ModelDescription desc = modelling.getDescription();
        int period=desc.getAnnualFrequency();
        if (period == 1) {
            return ProcessingResult.Unprocessed;
        }
        SarimaSpecification spec = desc.getSpecification();
        if (spec.getBd() == 1 || spec.getBq() == 1 || context.originalSeasonalityTest == 0) {
            return ProcessingResult.Unchanged;
        }
        if (spec.getBp() == 1) {
            
            boolean hastdmh = desc.variables()
                    .filter(var->var.isCalendar() || var.isMovingHolidays())
                    .findAny()
                    .isPresent();

            if (!hastdmh) {
                return ProcessingResult.Unchanged;
            }
        }
        // check seasonal quasi-unit roots
//        if (!isUnderDiff(context)) {
//            return ProcessingResult.Unchanged;
//        }
        RegArimaModelling scontext=buildNewModel(modelling);
        PreprocessingModel smodel = scontext.build();
        ModelComparator cmp = ModelComparator.builder().build();
        if (cmp.compare(smodel, modelling.build()) < 0) {
//            setReferenceModel(smodel);
            transferInformation(scontext, modelling);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

//    private boolean isUnderDiff(ModellingContext context) {
//        SeasonalityTests tests = SeasonalityTests.
//                residualSeasonalityTest(context.estimation.getLikelihood().getResiduals(),
//                        TsFrequency.valueOf(context.description.getFrequency()));
//        return tests.getScore() >= 1;
//    }
//
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

}
