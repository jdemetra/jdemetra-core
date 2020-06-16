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
package jdplus.x13.regarima;

import demetra.regarima.SingleOutlierSpec;
import demetra.regarima.EstimateSpec;
import demetra.regarima.OutlierSpec;
import demetra.regarima.AutoModelSpec;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.regarima.TransformSpec;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import jdplus.regarima.AICcComparator;
import jdplus.regsarima.regular.RegressionVariablesTest;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.x13.regarima.RegArimaKernel.AmiOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.timeseries.regression.IEasterVariable;
import demetra.regarima.EasterSpec;
import java.util.List;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
final class X13SpecDecoder {

    private final RegArimaKernel.Builder builder = RegArimaKernel.builder();

    X13SpecDecoder(@NonNull RegArimaSpec spec, ModellingContext context) {
        if (context == null) {
            context = ModellingContext.getActiveContext();
        }

        readTransformation(spec);
        if (spec.isUsingAutoModel()) {
            readAutoModel(spec);
        }
        readOutliers(spec);
        builder.modelBuilder(new X13ModelBuilder(spec, context));
        readRegression(spec, context);
        readAmiOptions(spec);
    }

    RegArimaKernel buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final RegArimaSpec spec) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .aiccLogCorrection(tspec.getAicDiff())
                    .estimationPrecision(espec.getTol())
                    .preadjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriod() : LengthOfPeriodType.None)
                    .build());
        }
    }

    private void readAutoModel(final RegArimaSpec spec) {
        AutoModelSpec amiSpec = spec.getAutoModel();
        DifferencingModule diff = DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(1/amiSpec.getUb1())
                .ub2(1/amiSpec.getUb2())
                .build();
        ArmaModule arma = ArmaModule.builder()
                .balanced(amiSpec.isBalanced())
                .mixed(amiSpec.isMixed())
                .build();

        builder.autoModelling(new AutoModellingModule(diff, arma));
    }

    private void readOutliers(final RegArimaSpec spec) {
        OutlierSpec outliers = spec.getOutliers();
        if (!outliers.isUsed()) {
            return;
        }
        OutliersDetectionModule.Builder obuilder = OutliersDetectionModule.builder();
        List<SingleOutlierSpec> types = outliers.getTypes();
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i).getType()) {
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
        AICcComparator comparator = new AICcComparator(spec.getRegression().getAicDiff());
        if (tdspec.getTest() != RegressionTestSpec.None) {
            CalendarEffectsDetectionModule cal = CalendarEffectsDetectionModule.builder()
                    .tradingDays(X13ModelBuilder.tradingDays(spec, context))
                    .leapYear(X13ModelBuilder.leapYear(tdspec))
                    .adjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriod() : LengthOfPeriodType.None)
                    .modelComparator(comparator)
                    .build();
            builder.calendarTest(cal);
        }
        EasterSpec espec = spec.getRegression().getEaster();
        if (espec != null && espec.getTest() != RegressionTestSpec.None) {
            int[] w;
            if (espec.getTest() == RegressionTestSpec.Remove) {
                w = new int[]{espec.getDuration()};
            } else {
                w = new int[]{1, 8, 15};
            }
            IEasterVariable[] easters = new IEasterVariable[w.length];
            for (int i = 0; i < easters.length; ++i) {
                easters[i] = X13ModelBuilder.easter(espec.getType(), w[i]);
            }
            EasterDetectionModule e = EasterDetectionModule.builder()
                    .easters(easters)
                    .modelComparator(comparator)
                    .build();
            builder.easterTest(e);
        }

        RegressionVariablesTest.Builder rbuilder = RegressionVariablesTest.builder();
        if (tdspec.getTest() != RegressionTestSpec.None) {
            rbuilder.tdTest(RegressionVariablesTest.CVAL, true);
        }
        if (espec != null && espec.getTest() != RegressionTestSpec.None) {
            rbuilder.movingHolidaysTest(RegressionVariablesTest.CVAL);
        }
        if (spec.isUsingAutoModel()) {
            rbuilder.meanTest(RegressionVariablesTest.CVAL);
        }
        builder.initialRegressionTest(rbuilder.build());
        if (spec.isUsingAutoModel()) {
            rbuilder.meanTest(RegressionVariablesTest.TSIG);
        }
        builder.finalRegressionTest(rbuilder.build());

    }

    private void readAmiOptions(RegArimaSpec spec) {
        AutoModelSpec ami = spec.getAutoModel();
        builder.options(
                AmiOptions.builder()
                .precision(spec.getEstimate().getTol())
                .va(spec.getOutliers().getDefaultCriticalValue())
                .reduceVa(ami.getPredcv())
                .ljungBoxLimit(ami.getLjungBoxLimit())
                .checkMu(spec.isUsingAutoModel())
                .mixedModel(ami.isMixed())
                .build());

    }

}
