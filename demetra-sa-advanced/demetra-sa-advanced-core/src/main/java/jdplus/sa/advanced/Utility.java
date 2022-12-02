/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sa.advanced;

import demetra.arima.SarimaSpec;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionTestSpec;
import demetra.regarima.SingleOutlierSpec;
import demetra.sa.advanced.PreprocessingSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.CalendarSpec;
import demetra.tramo.EasterSpec;
import demetra.tramo.OutlierSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.RegressionTestType;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import jdplus.regsarima.regular.RegSarimaProcessor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {
    public RegSarimaProcessor preprocessor(PreprocessingSpec spec){
        switch (spec.getMethod()){
            case TRAMO:
                // create the corresponding TramoSpec
                TramoSpec.builder()
                        .transform(TransformSpec
                                .builder()
                                .span(spec.getSpan())
                                .function(spec.getTransform())
                                .build())
                        .arima(SarimaSpec.airline())
                        .regression(RegressionSpec.builder()
                                .calendar(CalendarSpec.builder()
                                        .tradingDays(TradingDaysSpec
                                                .td(spec.getDtype(), spec.getLtype(), spec.pretest ? RegressionTestType.Joint_F : RegressionTestType.None, false))
                                        .easter(spec.easter ?
                                                EasterSpec.builder()
                                                        .type(EasterSpec.Type.IncludeEaster)
                                                        .test(spec.isPretest())
                                                        .build()
                                                :EasterSpec.DEFAULT_UNUSED)
                                        .build())
                                .build())
                        .outliers(OutlierSpec
                                .builder()
                                .ao(spec.isAo())
                                .ls(spec.isLs())
                                .tc(spec.isTc())
                                .so(spec.isSo())
                                .build())
                        .build();
                
            case REGARIMA:
                 RegArimaSpec.builder()
                        .basic(demetra.regarima.BasicSpec
                                .builder()
                                .span(spec.getSpan())
                                .build())
                        .transform(demetra.regarima.TransformSpec
                                .builder()
                                .function(spec.getTransform())
                                .build())
                        .arima(SarimaSpec.airline())
                        .regression(demetra.regarima.RegressionSpec.builder()
                                .tradingDays(demetra.regarima.TradingDaysSpec.
                                        td(spec.getDtype(), spec.getLtype(), spec.pretest ? RegressionTestSpec.Remove : RegressionTestSpec.None, true))
                                .easter(spec.easter ?
                                        demetra.regarima.EasterSpec.builder()
                                                .type(demetra.regarima.EasterSpec.Type.Easter)
                                                .test(spec.isPretest() ? RegressionTestSpec.Add : RegressionTestSpec.None)
                                                .build()
                                        :demetra.regarima.EasterSpec.DEFAULT_UNUSED)
                                .build())
                        .outliers(
                                demetra.regarima.OutlierSpec.of(spec.isAo(), 
                                        spec.isLs(), spec.isTc(), spec.isSo())
                                .build())
                        .build();
                
                
            default:
                return null;
        }
    }
}
