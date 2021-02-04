/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x13;

import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DefaultSaDiagnostics;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.stats.TestResult;
import demetra.timeseries.TsData;
import demetra.x11.X11Results;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sa.StationaryVarianceComputer;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.stats.tests.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class X13Diagnostics {

    private Mstatistics mstatistics;
    private DefaultSaDiagnostics saDiagnostics;

    public static X13Diagnostics of(X13Results rslts) {
        ModelEstimation preprocessing = rslts.getPreprocessing();
        X11Results xrslts = rslts.getDecomposition();
            Mstatistics mstats = Mstatistics.of(rslts.getPreadjustment(), xrslts, rslts.getFinals());
        DefaultSaDiagnostics.Builder sadiags = DefaultSaDiagnostics.builder()
                .varianceDecomposition(varDecomposition(mstats));
        boolean mul = preprocessing.isLogTransformation();
        TsData sa = xrslts.getD11();
        TsData i = xrslts.getD13();
        AdvancedResidualSeasonalityDiagnostics.Input input = new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, i);
        AdvancedResidualSeasonalityDiagnostics rseas = AdvancedResidualSeasonalityDiagnostics.of(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT, input);
        ResidualTradingDaysDiagnostics.Input tdinput = new ResidualTradingDaysDiagnostics.Input(mul, sa, i);
        ResidualTradingDaysDiagnostics rtd = ResidualTradingDaysDiagnostics.of(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT, tdinput);
        if (rtd != null) {
            sadiags.tdFTestOnI(summary(rtd.FTestOnI()))
                    .tdFTestOnSa(summary(rtd.FTestOnSa()));
        }
        return new X13Diagnostics(mstats, sadiags.build());
    }

    private static TestResult summary(StatisticalTest t) {
        return t == null ? null : t.toSummary();
    }

    private static StationaryVarianceDecomposition varDecomposition(Mstatistics m) {
        return StationaryVarianceDecomposition.builder()
                .C(m.getVarC())
                .S(m.getVarS())
                .I(m.getVarI())
                .Calendar(m.getVarTD())
                .P(m.getVarP())
                .trendType(StationaryVarianceDecomposition.TrendType.Linear)
                .build();
        
    }
}
    
