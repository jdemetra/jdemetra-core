/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.design.Development;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.regarima.regular.SeasonalFTest;
import demetra.arima.SarimaSpecification;
import demetra.stats.tests.StatisticalTest;
import demetra.timeseries.TsData;
import static demetra.tramo.SeasonalityTests.MSHORT;
import static demetra.tramo.SeasonalityTests.SHORT;

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
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
        ProcessingResult result;
        if (getReferenceModel() == null) {
            result = computeReferenceModel(modelling, context);
        } else {
            result = compareReferenceModels(modelling);
        }

        return result;
    }
    
    private void computeSTests() {
        TsData lin = getReferenceModel().linearizedSeries();
        SarimaSpecification spec = getReferenceModel().getDescription().getSpecification();
//        int del = spec.getD() + spec.getBD();
//        del = Math.max(Math.min(2, del), 1);
        int del = 1;
        stests = new SeasonalityTests();
        stests.test(lin, del, true);
        mstats = ModelStatistics.of(getReferenceModel());
    }
    
    private boolean hasSeasonality(RegArimaModelling modelling, TramoProcessor.Context context) {
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
    private ProcessingResult computeReferenceModel(RegArimaModelling modelling, TramoProcessor.Context context) {
        PreprocessingModel model = modelling.build();
        setReferenceModel(model);
        computeSTests();
        boolean seas = hasSeasonality(modelling, context);
        SarimaSpecification spec = model.getDescription().getSpecification();
        
        boolean schanged = false;
        if (!seas && spec.isSeasonal()) {
            spec.airline(false);
            spec.setBq(1);
            schanged = true;
        } else if (!context.seasonal && seas) {
            context.seasonal = true;
            return ProcessingResult.Changed;
        }
        if (!context.seasonal && (mstats.getSeasonalLjungBoxPvalue() < 0.05 || mstats.getLjungBoxPvalue() < 0.05)) {
            context.seasonal = true;
            spec.airline(false);
            spec.setBq(1);
            schanged = true;
        }
        
        if (schanged) {
            RegArimaModelling ncontext = new RegArimaModelling();
            ModelDescription desc = new ModelDescription(modelling.getDescription());
            desc.setSpecification(spec);
            ncontext.setDescription(desc);
            if (estimate(ncontext, false)) {
                transferInformation(ncontext, modelling);
                setReferenceModel(modelling.build());
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
        ModelComparator cmp = ModelComparator.builder()
                .preference(pref)
                .build();
        PreprocessingModel cur = context.build();
        int icmp = cmp.compare(cur, refmodel);
        if (icmp < 0) {
            setReferenceModel(cur);
            return ProcessingResult.Unchanged;
        } else if (icmp > 0) {
            this.transferInformation(refmodel, context);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }
}
