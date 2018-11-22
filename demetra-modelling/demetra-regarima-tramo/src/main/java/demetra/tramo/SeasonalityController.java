/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.design.Development;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaSpecification;
import demetra.stats.tests.StatisticalTest;
import demetra.timeseries.TsData;

/**
 *
 * @author palatej
 */
/**
 * This module corresponds to the routine testXLSeas of TRAMO
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeasonalityController extends ModelController {

    private StatisticalTest ftest_;
    private SeasonalityTests stests;
    private ModelStatistics mstats;

    public SeasonalityController() {
    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
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
        SarimaSpecification spec = getReferenceModel().getDescription().getSpecification();
//        int del = spec.getD() + spec.getBD();
//        del = Math.max(Math.min(2, del), 1);
        int del = 1;
        stests = new SeasonalityTests();
        stests.test(lin, del, true);
        mstats = ModelStatistics.of(getReferenceModel());
    }

    private boolean hasSeasonality(RegArimaModelling context) {
        int  period = context.getDescription().getAnnualFrequency();
        if (stests == null) {
            return false;
        }
        int score = 0, nscore=0;
        if (mstats.getSeasonalLjungBoxPvalue() < .01) {
            ++score;
            ++nscore;
        }
        StatisticalTest np = stests.getNonParametricTest();
        if (np != null && np.isSignificant(.01)) // 24.725 at the 0.01 level (freq=12)
        {
            ++score;
        }
        int n = stests.getDifferencing().getDifferenced().length();
        if (n >= MSHORT || (period != 12 && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(stests.getSpectralPeaks())) {
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
        return fs || mstats.seasLjungBoxPvalue < .01;

    }

    /**
     * This module corresponds to the routine testXLSeas of TRAMO
     *
     * @param context
     * @return
     */
    private ProcessingResult computeReferenceModel(RegArimaModelling context) {
        PreprocessingModel model = context.build();
        setReferenceModel(model);
        computeSTests();
        boolean seas = hasSeasonality(context);
        SarimaSpecification spec = model.getDescription().getSpecification();

        boolean schanged = false;
        if (!seas && spec.hasSeasonalPart()) {
            spec.airline(false);
            spec.setBq(1);
            schanged = true;
        } else if (!context.hasseas && seas) {
            context.hasseas = true;
            return ProcessingResult.Changed;
        }
        if (!context.hasseas && (mstats.seasLjungBoxPvalue < 0.05 || mstats.ljungBoxPvalue < 0.05)) {
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

    private ProcessingResult compareReferenceModels(RegArimaModelling context) {
        // compare with the previous reference model
        PreprocessingModel refmodel = getReferenceModel();
        ModelComparator.Preference pref = ModelComparator.Preference.BIC;
        if (!refmodel.getDescription().getSpecification().equals(context.getDescription().getSpecification())) {
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
