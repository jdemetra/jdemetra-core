/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import demetra.modelling.TransformationType;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.ModellingContext;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import demetra.timeseries.calendars.DayClustering;
import demetra.tramo.TradingDaysSpec.AutoMethod;
import demetra.tramo.TramoProcessor.AmiOptions;
import javax.annotation.Nonnull;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
final class TramoSpecDecoder {

    private final TramoProcessor.Builder builder = TramoProcessor.builder();

    TramoSpecDecoder(@Nonnull TramoSpec spec, ModellingContext context) {
        if (context == null) {
            context = ModellingContext.getActiveContext();
        }

        readTransformation(spec);
        if (spec.isUsingAutoModel()) {
            readAutoModel(spec);
        }
        builder.modelBuilder(new TramoModelBuilder(spec, context));
        readOutliers(spec);
        readRegression(spec, context);
        readAmiOptions(spec);
       
    }

    TramoProcessor buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final TramoSpec spec) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .logPreference(Math.log(tspec.getFct()))
                    .estimationPrecision(espec.getTol())
                    .build());
        }
    }

    private void readAutoModel(final TramoSpec spec) {
        AutoModelSpec amiSpec = spec.getAutoModel();
        DifferencingModule diff = DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(amiSpec.getUb1())
                .ub2(amiSpec.getUb2())
                .build();
        ArmaModule arma = new ArmaModule();
        builder.seasonalityDetector(new SeasonalityDetector())
                .differencing(diff)
                .arma(arma);
    }

    private void readRegression(final TramoSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (tdspec.isAutomatic()) {
            if (tdspec.getAutomaticMethod() == AutoMethod.FTest) {
                AutomaticFRegressionTest test = AutomaticFRegressionTest.builder()
                        .easter(TramoModelBuilder.easter(spec))
                        .leapYear(TramoModelBuilder.leapYear(tdspec))
                        .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, context))
                        .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, context))
                        .testMean(spec.isUsingAutoModel())
                        .fPValue(tdspec.getProbabibilityForFTest())
                        .build();
                builder.regressionTest(test);
            }else{
                AutomaticWaldRegressionTest test = AutomaticWaldRegressionTest.builder()
                        .easter(TramoModelBuilder.easter(spec))
                        .leapYear(TramoModelBuilder.leapYear(tdspec))
                        .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, context))
                        .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, context))
                        .testMean(spec.isUsingAutoModel())
                        .fPValue(tdspec.getProbabibilityForFTest())
                        .PConstraint(tdspec.getProbabibilityForFTest())
                        .build();
                builder.regressionTest(test);
                
            }
        } else {
            DefaultRegressionTest test = DefaultRegressionTest.builder()
                    .easter(TramoModelBuilder.easter(spec))
                    .leapYear(TramoModelBuilder.leapYear(tdspec))
                    .tradingDays(TramoModelBuilder.tradingDays(spec, context))
                    .testMean(spec.isUsingAutoModel())
                    .build();
            builder.regressionTest(test);
        }
    }

    private void readOutliers(final TramoSpec spec) {
        OutlierSpec outliers = spec.getOutliers();
        if (!outliers.isUsed()) {
            return;
        }
        RegularOutliersDetectionModule.Builder obuilder = RegularOutliersDetectionModule.builder();
        String[] types = outliers.getTypes();
        for (int i = 0; i < types.length; ++i) {
            switch (types[i]) {
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
                        .tcrate(outliers.getDeltaTC())
                        .maximumLikelihood(outliers.isMaximumLikelihood())
                        .build());
    }

    private void readAmiOptions(TramoSpec spec) {
        AutoModelSpec ami = spec.getAutoModel();
        builder.options(
                AmiOptions.builder()
                        .precision(spec.getEstimate().getTol())
                        .va(spec.getOutliers().getCriticalValue())
                        .reduceVa(ami.getPc())
                        .checkMu(spec.isUsingAutoModel())
                        .ljungBoxLimit(ami.getPcr())
                        .acceptAirline(ami.isAcceptDefault())
                        .build());

    }
}
