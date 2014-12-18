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

import ec.tstoolkit.modelling.arima.ModelStatistics;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.AbstractModelController;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import static ec.tstoolkit.modelling.arima.tramo.SeasonalityTests.MSHORT;
import static ec.tstoolkit.modelling.arima.tramo.SeasonalityTests.SHORT;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 * This module corresponds to the routine testXLSeas of TRAMO
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeasonalityController extends AbstractModelController {

    private FTest ftest_;
    private SeasonalityTests stests_;
    private ModelStatistics mstats_;

    public SeasonalityController() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (!context.automodelling || context.description.getFrequency() == 1) {
            return ProcessingResult.Unprocessed;
        }
        ProcessingResult result;
        if (getReferenceModel() == null) {
            result = computeReferenceModel(context);
        } else {
            result = compareReferenceModels(context);
        }

        return result;
    }

    private void computeSTests() {
        TsData lin = getReferenceModel().linearizedSeries(false);
        SarimaSpecification spec = getReferenceModel().description.getSpecification();
//        int del = spec.getD() + spec.getBD();
//        del = Math.max(Math.min(2, del), 1);
        int del = 1;
        stests_ = new SeasonalityTests();
        stests_.test(lin, del, true);
        mstats_ = new ModelStatistics(getReferenceModel());
    }

    private boolean hasSeasonality(ModellingContext context) {
        TsFrequency freq = stests_.getDifferencing().original.getFrequency();
        if (stests_ == null) {
            return false;
        }
        int score = 0, nscore=0;
        if (mstats_.seasLjungBoxPvalue < .01) {
            ++score;
            ++nscore;
        }
        FriedmanTest np = stests_.getNonParametricTest();
        if (np != null && np.isSignificant()) // 24.725 at the 0.01 level (freq=12)
        {
            ++score;
        }
        int n = stests_.getDifferencing().differenced.getLength();
        if (n >= MSHORT || (freq != TsFrequency.Monthly && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(stests_.getSpectralPeaks())) {
                ++score;
            }
        }
        ftest_ = new FTest();
        ftest_.test(getReferenceModel().description);
        boolean fs = ftest_.getFTest().isSignificant();
        if (fs) {
            ++score;
            ++nscore;
        }
        context.originalSeasonalityTest=score;
        if (score > 1 || nscore > 0) {
            return true;
        }
        return fs || mstats_.seasLjungBoxPvalue < .01;

    }

    /**
     * This module corresponds to the routine testXLSeas of TRAMO
     *
     * @param context
     * @return
     */
    private ProcessingResult computeReferenceModel(ModellingContext context) {
        PreprocessingModel model = context.current(false);
        setReferenceModel(model);
        computeSTests();
        boolean seas = hasSeasonality(context);
        SarimaSpecification spec = model.description.getSpecification();

        boolean schanged = false;
        if (!seas && spec.hasSeasonalPart()) {
            spec.airline(false);
            spec.setBQ(1);
            schanged = true;
        } else if (!context.hasseas && seas) {
            context.hasseas = true;
            return ProcessingResult.Changed;
        }
        if (!context.hasseas && (mstats_.seasLjungBoxPvalue < 0.05 || mstats_.ljungBoxPvalue < 0.05)) {
            context.hasseas = true;
            spec.airline(false);
            spec.setBQ(1);
            schanged = true;
        }

        if (schanged) {
            ModellingContext ncontext = new ModellingContext();
            ncontext.description = context.description.clone();
            ncontext.description.setSpecification(spec);

            if (estimate(ncontext, false)) {
                transferInformation(ncontext, context);
                setReferenceModel(context.current(false));
                return ProcessingResult.Changed;
            }
        }
        return ProcessingResult.Unchanged;
    }

    private ProcessingResult compareReferenceModels(ModellingContext context) {
        // compare with the previous reference model
        PreprocessingModel refmodel = getReferenceModel();
        ModelComparator.Preference pref = ModelComparator.Preference.BIC;
        if (!refmodel.description.getSpecification().equals(context.description.getSpecification())) {
            SeasonalOverDifferencingTest overseas = new SeasonalOverDifferencingTest();
            switch (overseas.test(context)) {
                case 1:
                    pref = ModelComparator.Preference.First;
                    break;
                case 2:
                    pref = ModelComparator.Preference.Second;
                    break;
            }
        }
        ModelComparator cmp = new ModelComparator(pref);
        PreprocessingModel cur = context.current(false);
        int icmp = cmp.compare(cur, refmodel);
        if (icmp < 0) {
            setReferenceModel(cur);
            return ProcessingResult.Unchanged;
        } else if (icmp > 0) {
            context.description = refmodel.description.clone();
            context.estimation = refmodel.estimation;
            context.information.clear();
            if (refmodel.info_ != null) {
                context.information.copy(refmodel.info_);
            }
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }
}
