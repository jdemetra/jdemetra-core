/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import nbbrd.design.Development;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.regular.SeasonalFTest;
import demetra.arima.SarimaOrders;
import jdplus.stats.tests.StatisticalTest;
import demetra.timeseries.TsData;
import static jdplus.tramo.SeasonalityTests.MSHORT;
import static jdplus.tramo.SeasonalityTests.SHORT;

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
class SeasonalityController extends ModelController {

    private StatisticalTest ftest;
    private SeasonalityTests stests;
    private ModelStatistics mstats;

    public SeasonalityController() {
    }

    @Override
    ProcessingResult process(RegSarimaModelling modelling, TramoContext context) {
        ProcessingResult result;
        if (getReferenceModel() == null) {
            result = computeReferenceModel(modelling, context);
        } else {
            result = compareReferenceModels(modelling);
        }

        return result;
    }

    private void computeSTests() {
        ModelEstimation refestimation = getReferenceModel().build();
        TsData lin = refestimation.linearizedSeries();
        SarimaOrders spec = refestimation.specification();
//        int del = spec.getD() + spec.getBD();
//        del = Math.max(Math.min(2, del), 1);
        int del = 1;
        stests = new SeasonalityTests();
        stests.test(lin.getValues(), lin.getAnnualFrequency(), del, true);
        mstats = ModelStatistics.of(refestimation);
    }

    private boolean hasSeasonality(RegSarimaModelling modelling, TramoContext context) {
        int period = modelling.getDescription().getAnnualFrequency();
        if (stests == null) {
            return false;
        }
        int score = 0, nscore = 0;
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
        SeasonalFTest f = new SeasonalFTest();
        f.test(getReferenceModel().getDescription());
        ftest = f.getFTest();
        boolean fs = ftest.getPValue() < .01;
        if (fs) {
            ++score;
            ++nscore;
        }
        context.originalSeasonalityTest = score;
        if (score > 1 || nscore > 0) {
            return true;
        }
        return fs || mstats.getSeasonalLjungBoxPvalue() < .01;

    }

    /**
     * This module corresponds to the routine testXLSeas of TRAMO
     *
     * @param modelling
     * @return
     */
    private ProcessingResult computeReferenceModel(RegSarimaModelling modelling, TramoContext context) {
        ModelEstimation model = modelling.build();
        setReferenceModel(modelling);
        computeSTests();
        boolean seas = hasSeasonality(modelling, context);
        SarimaOrders spec = model.specification();
        SarimaOrders nspec = null;
        if (!seas && spec.isSeasonal()) {
            nspec = SarimaOrders.m011(spec.getPeriod());
            nspec.setBq(1);
        } else if (!context.seasonal && seas) {
            context.seasonal = true;
            return ProcessingResult.Changed;
        }
        if (!context.seasonal && (mstats.getSeasonalLjungBoxPvalue() < 0.05 || mstats.getLjungBoxPvalue() < 0.05)) {
            context.seasonal = true;
            nspec = SarimaOrders.m011(spec.getPeriod());
            nspec.setBq(1);
        }

        if (nspec != null) {
            ModelDescription desc = ModelDescription.copyOf(modelling.getDescription());
            desc.setSpecification(spec);
            RegSarimaModelling ncontext = RegSarimaModelling.of(desc);
            if (estimate(ncontext, false)) {
                transferInformation(ncontext, modelling);
                setReferenceModel(modelling);
                return ProcessingResult.Changed;
            }
        }
        return ProcessingResult.Unchanged;
    }

    private ProcessingResult compareReferenceModels(RegSarimaModelling context) {
        // compare with the previous reference model
        RegSarimaModelling referenceModel = getReferenceModel();
        ModelEstimation refestimation = referenceModel.build();
        ModelComparator.Preference pref = ModelComparator.Preference.BIC;
        if (!refestimation.specification().equals(context.getDescription().specification())) {
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
        ModelComparator cmp = ModelComparator.builder()
                .preference(pref)
                .build();
        ModelEstimation cur = context.build();
        int icmp = cmp.compare(cur, refestimation);
        if (icmp <= 0) {
            setReferenceModel(context);
            return ProcessingResult.Unchanged;
        } else {
            this.transferInformation(referenceModel, context);
            return ProcessingResult.Changed;
        } 
    }
}
