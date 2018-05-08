/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x12;

import demetra.modelling.TransformationType;
import demetra.modelling.regression.ModellingContext;
import demetra.timeseries.calendars.DayClustering;
import demetra.tramo.TradingDaysSpec.AutoMethod;
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
        builder.modelBuilder(new X12ModelBuilder(spec, context));
        readRegression(spec, context);
    }

    X12Preprocessor buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final RegArimaSpec spec) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .comparator(tspec.getAICDiff())
                    .estimationPrecision(espec.getTol())
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

    private void readRegression(final RegArimaSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        // TODO
    }

}
