/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x12;

import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.IEasterVariable;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.ModellingContext;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import demetra.regarima.regular.AICcComparator;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.x12.X12Preprocessor.AmiOptions;
import javax.annotation.Nonnull;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
final class X12SpecDecoder {

    private final X12Preprocessor.Builder builder = X12Preprocessor.builder();

    X12SpecDecoder(@Nonnull RegArimaSpec spec, ModellingContext context) {
        if (context == null) {
            context = ModellingContext.getActiveContext();
        }

        readTransformation(spec);
        if (spec.isUsingAutoModel()) {
            readAutoModel(spec);
        }
        readOutliers(spec);
        builder.modelBuilder(new X12ModelBuilder(spec, context));
        readRegression(spec, context);
        readAmiOptions(spec);
    }

    X12Preprocessor buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final RegArimaSpec spec) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .comparator(tspec.getAICDiff())
                    .estimationPrecision(espec.getTol())
                    .adjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriod() : LengthOfPeriodType.None)
                    .build());
        }
    }

    private void readAutoModel(final RegArimaSpec spec) {
        AutoModelSpec amiSpec = spec.getAutoModel();
        DifferencingModule diff = DifferencingModule.builder()
                .cancel(amiSpec.getCancelationLimit())
                .ub1(amiSpec.getInitialUnitRootLimit())
                .ub2(amiSpec.getFinalUnitRootLimit())
                .build();
        ArmaModule arma = new ArmaModule();
        builder.differencing(diff)
                .arma(arma);
    }

    private void readOutliers(final RegArimaSpec spec) {
        OutlierSpec outliers = spec.getOutliers();
        if (!outliers.isUsed()) {
            return;
        }
        RegularOutliersDetectionModule.Builder obuilder = RegularOutliersDetectionModule.builder();
        SingleOutlierSpec[] types = outliers.getTypes();
        for (int i = 0; i < types.length; ++i) {
            switch (types[i].getType()) {
                case AdditiveOutlier.CODE:
                    obuilder.ao(true);
                    break;
                case LevelShift.CODE:
                    obuilder.ls(true);
                    break;
                case TransitoryChange.CODE:
                    obuilder.tc(true);
                    break;
                case PeriodicOutlier.CODE:
                    obuilder.so(true);
                    break;
            }
        }
        builder.outliers(
                obuilder.span(outliers.getSpan())
                        .maxRound(outliers.getMaxIter())
                        .tcrate(outliers.getMonthlyTCRate())
                        .build());
    }

    private void readRegression(final RegArimaSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        AICcComparator comparator = new AICcComparator(spec.getRegression().getAICCDiff());
        if (tdspec.getTest() != RegressionTestSpec.None) {
            CalendarEffectsDetectionModule cal = CalendarEffectsDetectionModule.builder()
                    .tradingDays(X12ModelBuilder.tradingDays(spec, context))
                    .leapYear(X12ModelBuilder.leapYear(tdspec))
                    .adjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriod() : LengthOfPeriodType.None)
                    .modelComparator(comparator)
                    .build();
            builder.calendarTest(cal);
        }
        MovingHolidaySpec espec = spec.getRegression().getEaster();
        if (espec != null && espec.getTest() != RegressionTestSpec.None) {
            int[] w;
            if (espec.getTest() == RegressionTestSpec.Remove) {
                w = new int[]{espec.getW()};
            } else {
                w = new int[]{1, 8, 15};
            }
            IEasterVariable[] easters = new IEasterVariable[w.length];
            for (int i = 0; i < easters.length; ++i) {
                easters[i] = X12ModelBuilder.easter(espec.getType(), w[i]);
            }
            EasterDetectionModule e = EasterDetectionModule.builder()
                    .easters(easters)
                    .modelComparator(comparator)
                    .build();
            builder.easterTest(e);
        }
    }

    private void readAmiOptions(RegArimaSpec spec) {
        AutoModelSpec ami = spec.getAutoModel();
        builder.options(
                AmiOptions.builder()
                        .precision(spec.getEstimate().getTol())
                        .va(spec.getOutliers().getDefaultCriticalValue())
                        .reduceVa(ami.getPercentReductionCV())
                        .ljungBoxLimit(ami.getLjungBoxLimit())
                        .checkMu(spec.isUsingAutoModel())
                        .build());

    }

}
