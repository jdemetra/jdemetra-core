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

import ec.satoolkit.diagnostics.FriedmanTest;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ISeasonalityDetector;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityDetector implements ISeasonalityDetector, IPreprocessingModule {

    private static final String SEASONALITY_TESTS = "Seasonality tests";

    private SeasonalityTests tests;
    private int ost;

    public SeasonalityTests getTests() {
        return tests;
    }

    @Override
    public boolean hasSeasonality(ModelDescription desc) {
        TsData y = desc.transformedOriginal();
        return hasSeasonality(y);
    }

    public boolean hasSeasonality(TsData y) {
        ost = 0;
        if (y.getFrequency() == TsFrequency.Yearly) {
            return false;
        }
        tests = new SeasonalityTests();
        tests.test(y, 1, true);
        int ost95 = 0;
        int cqs = 0, cnp = 0;
        StatisticalTest qs = tests.getQs();
        FriedmanTest np = tests.getNonParametricTest();
        if (qs.getPValue() < .01) {
            cqs = 2;
            ++ost;
            ++ost95;
        } else if (qs.getPValue() < .05) {
            cqs = 1;
            ++ost95;
        }
        if (np.getPValue() < .01) {
            cnp = 2;
            ++ost;
            ++ost95;
        } else if (np.getPValue() < .05) {
            cnp = 1;
            ++ost95;
        }
//        BlackmanTukeySpectrum spectrum=new BlackmanTukeySpectrum();
//        spectrum.setData(tests.getDifferencing().differenced.getValues().internalStorage());
//        int ifreq=y.getFrequency().intValue();
//        int len=(spectrum.getData().length*3/4/ifreq)*ifreq;
//        spectrum.setWindowLength(Math.min(len, 10*ifreq));
//        StatisticalTest ltest=spectrum.getAverageSpectrumTest(ifreq);
//        boolean ok=ltest.isSignificant();
        return cqs == 2 || ost95 == 2;

    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        context.hasseas = hasSeasonality(context.description);
        context.originalSeasonalityTest = ost;
        addTestsInfo(context);
        addSeasInfo(context);
        return ProcessingResult.Changed;
    }

    private void addTestsInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            StatisticalTest qs = tests.getQs();
//            FriedmanTest np = tests.getNonParametricTest();
//            builder.append("OST=").append(ost).append(" (");
//            builder.append("QS-PValue=").append(qs.getPValue()).append((", "));
//            builder.append("Friedman-PValue=").append(np.getPValue()).append(')');
//
//            context.processingLog.add(ProcessingInformation.info(SEASONALITY_TESTS,
//                    LogLevelTest.class.getName(), builder.toString(), null));
//        }
    }

    private void addSeasInfo(ModellingContext context) {
//        if (context.processingLog != null) {
//            context.processingLog.add(ProcessingInformation.info(SEASONALITY_TESTS,
//                    SeasonalityDetector.class.getName(), context.hasseas ? SMODEL : NSMODEL, null));
//        }
    }

    private static final String SMODEL = "Model with seasonal part", NSMODEL = "Model without seasonal part";
}
