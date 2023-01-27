/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.sa.regarima;

import demetra.modelling.TransformationType;
import demetra.modelling.regular.EasterSpec;
import demetra.modelling.regular.ModellingSpec;
import demetra.modelling.regular.OutlierSpec;
import demetra.modelling.regular.TradingDaysSpec;
import demetra.modelling.regular.TransformSpec;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regarima.AICcComparator;
import jdplus.regsarima.regular.RegressionVariablesTest;
import demetra.timeseries.calendars.LengthOfPeriodType;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.regression.ITradingDaysVariable;
import jdplus.sa.regarima.FastKernel.AmiOptions;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
final class SpecDecoder {

    private final FastKernel.Builder builder = FastKernel.builder();

    SpecDecoder(@NonNull ModellingSpec spec, ModellingContext context) {
        if (context == null) {
            context = ModellingContext.getActiveContext();
        }

        readTransformation(spec);
        readOutliers(spec);
        builder.modelBuilder(new ModelBuilder(spec, context));
        readRegression(spec, context);
        readAmiOptions(spec);
    }

    FastKernel buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final ModellingSpec spec) {
        TransformSpec tspec = spec.getTransform();
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .aiccLogCorrection(tspec.getAicDiff())
                    .estimationPrecision(spec.getEstimate().getIntermediatePrecision())
                    .preadjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriodType() : LengthOfPeriodType.None)
                    .outliersCorrection(tspec.isOutliersCorrection())
                    .build());
        }
    }

    private void readOutliers(final ModellingSpec spec) {
        OutlierSpec outliers = spec.getOutliers();
        if (!outliers.isUsed()) {
            return;
        }
        OutliersDetectionModule module = OutliersDetectionModule.builder()
                .ao(outliers.isAo())
                .ls(outliers.isLs())
                .tc(outliers.isTc())
                .so(outliers.isSo())
                .span(outliers.getSpan())
                .tcrate(outliers.getDeltaTC())
                .precision(spec.getEstimate().getIntermediatePrecision())
                .build();
        builder.outliers(module);
    }

    private ITradingDaysVariable[] nestedtd(final ModellingSpec spec, ModellingContext context) {
        return new ITradingDaysVariable[]{
            ModelBuilder.td(spec, DayClustering.TD2, context),
            ModelBuilder.td(spec, DayClustering.TD3, context),
            ModelBuilder.td(spec, DayClustering.TD4, context),
            ModelBuilder.td(spec, DayClustering.TD7, context)
        };
    }

    private ITradingDaysVariable[] alltd(final ModellingSpec spec, ModellingContext context) {
        return new ITradingDaysVariable[]{
            ModelBuilder.td(spec, DayClustering.TD2c, context),
            ModelBuilder.td(spec, DayClustering.TD2, context),
            ModelBuilder.td(spec, DayClustering.TD3, context),
            ModelBuilder.td(spec, DayClustering.TD3c, context),
            ModelBuilder.td(spec, DayClustering.TD4, context),
            ModelBuilder.td(spec, DayClustering.TD7, context)
        };
    }

    private void readRegression(final ModellingSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        AICcComparator comparator = new AICcComparator(spec.getRegression().getAicDiff());
        if (tdspec.isAutomatic()) {
            switch (tdspec.getAutomaticMethod()) {
                case AIC:
                    builder.calendarTest(AutomaticTradingDaysRegressionTest.builder()
                            .leapYear(ModelBuilder.leapYear(tdspec))
                            .tradingDays(alltd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .aic()
                            .estimationPrecision(spec.getEstimate().getIntermediatePrecision())
                            .build());
                    break;
                case BIC:
                    builder.calendarTest(AutomaticTradingDaysRegressionTest.builder()
                            .leapYear(ModelBuilder.leapYear(tdspec))
                            .tradingDays(alltd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .estimationPrecision(spec.getEstimate().getIntermediatePrecision())
                            .bic()
                            .build());
                    break;
                case WALD:
                    builder.calendarTest(AutomaticTradingDaysWaldTest.builder()
                            .leapYear(ModelBuilder.leapYear(tdspec))
                            .tradingDays(nestedtd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .estimationPrecision(spec.getEstimate().getIntermediatePrecision())
                            .build());
                    break;

            }
        } else if (tdspec.isTest()) {
            CalendarEffectsDetectionModule cal = CalendarEffectsDetectionModule.builder()
                    .tradingDays(ModelBuilder.tradingDays(spec, context))
                    .leapYear(ModelBuilder.leapYear(tdspec))
                    .adjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriodType() : LengthOfPeriodType.None)
                    .modelComparator(comparator)
                    .build();
            builder.calendarTest(cal);
        }
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        if (espec.getType() != EasterSpec.Type.UNUSED && espec.isTest()) {
            int[] w = new int[]{espec.getDuration()};
            IEasterVariable[] easters = new IEasterVariable[w.length];
            for (int i = 0; i < easters.length; ++i) {
                easters[i] = ModelBuilder.easter(espec.getType(), w[i]);
            }
            EasterDetectionModule e = EasterDetectionModule.builder()
                    .easters(easters)
                    .modelComparator(comparator)
                    .build();
            builder.easterTest(e);
        }

        RegressionVariablesTest.Builder rbuilder = RegressionVariablesTest.builder();
        if (tdspec.isTest()) {
            rbuilder.tdTest(RegressionVariablesTest.CVAL, true);
        }
        if (espec.getType() != EasterSpec.Type.UNUSED && espec.isTest()) {
            rbuilder.movingHolidaysTest(RegressionVariablesTest.CVAL);
        }
        builder.initialRegressionTest(rbuilder.build());
        builder.finalRegressionTest(rbuilder.build());
    }

    private void readAmiOptions(ModellingSpec spec) {
        builder.options(
                FastKernel.AmiOptions.builder()
                        .precision(spec.getEstimate().getPrecision())
                        .intermediatePrecision(spec.getEstimate().getIntermediatePrecision())
                        .va(spec.getOutliers().getCriticalValue())
                        .checkMu(spec.getRegression().isCheckMu())
                        .build());

    }

}
