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

import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.arima.SarimaOrders;
import jdplus.regarima.ami.Utility;


/**
 *
 * @author Jean Palate
 */
class SeasonalUnderDifferencingTest2 extends ModelController {

    private static final double DEF_SBOUND = .91;

    @Override
    ProcessingResult process(RegSarimaModelling modelling, TramoContext context) {
        ModelDescription desc = modelling.getDescription();
        int period=desc.getAnnualFrequency();
        if (period == 1) {
            return ProcessingResult.Unprocessed;
        }
        SarimaOrders spec = desc.specification();
        if (spec.getBd() == 1 || spec.getBq() == 1 || context.originalSeasonalityTest == 0) {
            return ProcessingResult.Unchanged;
        }
        if (spec.getBp() == 1) {
            
            boolean hastdmh = desc.variables()
                    .filter(var->Utility.isCalendar(var))
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
        RegSarimaModelling scontext=buildNewModel(modelling);
        ModelEstimation smodel = scontext.build();
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
    private RegSarimaModelling buildNewModel(RegSarimaModelling context) {
        ModelDescription ndesc = ModelDescription.copyOf(context.getDescription());
        SarimaOrders spec = ndesc.specification();
        spec.setBp(0);
        spec.setBd(1);
        spec.setBq(1);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        RegSarimaModelling ncontext = RegSarimaModelling.of(ndesc);
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return null;
        }
        return ncontext;
    }

}
