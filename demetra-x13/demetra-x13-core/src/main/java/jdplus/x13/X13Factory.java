/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.regarima.AutoModelSpec;
import demetra.regarima.EasterSpec;
import demetra.regarima.OutlierSpec;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.regarima.TransformSpec;
import demetra.sa.EstimationPolicy;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.TradingDaysType;
import demetra.timeseries.regression.Variable;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.X13Spec;
import java.util.Arrays;
import java.util.Optional;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsData;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.diagnostics.SaResidualsDiagnosticsFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public class X13Factory implements SaProcessingFactory<X13Spec, X13Results> {

    public static final X13Factory INSTANCE = new X13Factory();

    private final List<SaDiagnosticsFactory<X13Results>> diagnostics = new CopyOnWriteArrayList<>();

    public X13Factory() {
        CoherenceDiagnosticsFactory<X13Results> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            return new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<X13Results> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.DEFAULT,
                        r->r.getPreprocessing().getModel());
        SaResidualsDiagnosticsFactory<X13Results> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.DEFAULT,
                        r->r.getPreprocessing());
        SaOutliersDiagnosticsFactory<X13Results> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.DEFAULT,
                        r->r.getPreprocessing());
        AdvancedResidualSeasonalityDiagnosticsFactory<X13Results> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            boolean mul = r.getPreprocessing().isLogTransformation();
                            TsData sa = r.getDecomposition().getD11();
                            TsData irr = r.getDecomposition().getD13();
                            return new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, irr);
                        }
                );
        ResidualTradingDaysDiagnosticsFactory<X13Results> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT,
                        (X13Results r) -> {
                            boolean mul = r.getPreprocessing().isLogTransformation();
                            TsData sa = r.getDecomposition().getD11();
                            TsData irr = r.getDecomposition().getD13();
                            return new ResidualTradingDaysDiagnostics.Input(mul, sa, irr);
                        }
                );
        
        diagnostics.add(coherence);
        diagnostics.add(residuals);
        diagnostics.add(outofsample);
        diagnostics.add(outliers);
        diagnostics.add(advancedResidualSeasonality);
        diagnostics.add(residualTradingDays);
        
    }

    @Override
    public X13Spec of(X13Spec spec, X13Results estimation) {
        if (spec instanceof X13Spec && estimation instanceof X13Results) {
            X13Spec espec = (X13Spec) spec;
            X13Results rslts = (X13Results) estimation;

            RegArimaSpec ntspec = update(espec.getRegArima(), rslts.getPreprocessing());
            X11Spec nsspec = update(espec.getX11(), rslts.getDecomposition());

            return espec.toBuilder()
                    .regArima(ntspec)
                    .x11(nsspec)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid specification");
        }
    }

    @Override
    public SaSpecification refreshSpecification(X13Spec currentSpec, X13Spec domainSpec, EstimationPolicy policy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private X11Spec update(X11Spec x11, X11Results rslts) {
        // Nothing to do (for the time being)
        return x11;
    }

    private void update(TransformSpec transform, ModelEstimation rslts, RegArimaSpec.Builder builder) {
        TransformSpec ntransform = transform.toBuilder()
                .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                .adjust(rslts.getLpTransformation())
                .build();
        builder.transform(ntransform);
    }

    private void update(SarimaSpec sarima, ModelEstimation rslts, RegArimaSpec.Builder builder) {
        // Update the model (taking into account fixed parameters)
        sarima.getPhi();
        SarimaModel model = rslts.getModel().arima();
        SarimaSpec nspec = sarima.toBuilder()
                .phi(parametersOf(sarima.getPhi(), model.phi()))
                .bphi(parametersOf(sarima.getBphi(), model.bphi()))
                .theta(parametersOf(sarima.getTheta(), model.theta()))
                .btheta(parametersOf(sarima.getBtheta(), model.btheta()))
                .d(model.getRegularDifferenceOrder())
                .bd(model.getSeasonalDifferenceOrder())
                .build();
        builder.arima(nspec);
    }

    private void updateArima(ModelEstimation rslts, RegArimaSpec.Builder builder) {
        // Update completely the model (if AMI, no fixed parameters!)
        SarimaModel model = rslts.getModel().arima();
        SarimaSpec nspec = SarimaSpec.builder()
                .phi(Parameter.of(model.phi(), ParameterType.Estimated))
                .bphi(Parameter.of(model.bphi(), ParameterType.Estimated))
                .theta(Parameter.of(model.theta(), ParameterType.Estimated))
                .btheta(Parameter.of(model.btheta(), ParameterType.Estimated))
                .d(model.getRegularDifferenceOrder())
                .bd(model.getSeasonalDifferenceOrder())
                .build();
        builder.arima(nspec);
    }

    private void update(AutoModelSpec ami, ModelEstimation rslts, RegArimaSpec.Builder builder) {
        // Disable ami
        AutoModelSpec nami = ami.toBuilder()
                .enabled(false)
                .build();
        builder.autoModel(nami);
    }

    private RegArimaSpec update(RegArimaSpec regarima, ModelEstimation rslts) {
        RegArimaSpec.Builder builder = regarima.toBuilder();
        update(regarima.getTransform(), rslts, builder);
        AutoModelSpec ami = regarima.getAutoModel();
        if (ami.isEnabled()) {
            update(regarima.getArima(), rslts, builder);
            update(ami, rslts, builder);
        } else {
            updateArima(rslts, builder);
        }

        update(regarima.getOutliers(), rslts, builder);
        update(regarima.getRegression(), rslts, builder);

        return builder.build();
    }

    private void update(OutlierSpec outliers, ModelEstimation rslts, RegArimaSpec.Builder builder) {
        if (outliers.isUsed()) {    // Disable outliers
            builder.outliers(
                    outliers.toBuilder()
                            .clearTypes()
                            .build());
        }
    }

    private void update(RegressionSpec regression, ModelEstimation rslts, RegArimaSpec.Builder builder) {
        RegressionSpec.Builder rbuilder = regression.toBuilder();
        // fill all the coefficients (either estimated or fixed)
        rbuilder.clearCoefficients();
        Variable[] variables = rslts.getVariables();
        for (int i = 0; i < variables.length; ++i) {
            rbuilder.coefficient(variables[i].getName(), variables[i].getCoefficients());
        }
        // add new outliers in the list of pre-specified outliers
        Arrays.stream(variables).filter(v -> v.isOutlier(false)).forEach(v -> rbuilder.outlier((IOutlier) v.getVariable()));
        // calendar effects
        EasterSpec espec = regression.getEaster();
        TradingDaysSpec tdspec = regression.getTradingDays();

        if (tdspec.isUsed() && (tdspec.getTest() != RegressionTestSpec.None)) {
            // leap year
            LengthOfPeriodType lp = LengthOfPeriodType.None;
            TradingDaysType td = TradingDaysType.None;
            Optional<Variable> flp = Arrays.stream(variables).filter(v -> v.isLengthOfPeriod()).findFirst();
            if (flp.isPresent()) {
                lp = tdspec.getLengthOfPeriod();
            }
            Optional<Variable> ftd = Arrays.stream(variables).filter(v -> v.isTradingDays()).findFirst();
            if (ftd.isPresent()) {
                td = tdspec.getType();
            }

            if (lp != null || td != null) {
                if (tdspec.isStockTradingDays()) {
                    int ntd = tdspec.getStockTradingDays();
                    tdspec = TradingDaysSpec.stockTradingDays(ntd, RegressionTestSpec.None);
                } else if (tdspec.isHolidays()) {
                    tdspec = TradingDaysSpec.holidays(tdspec.getHolidays(),
                            td, lp, RegressionTestSpec.None, false);
                } else if (tdspec.isUserDefined()) {
                    tdspec = TradingDaysSpec.userDefined(tdspec.getUserVariables(), RegressionTestSpec.None);
                } else { //normal case
                    tdspec = TradingDaysSpec.td(td, lp, RegressionTestSpec.None, false);
                }
            } else {
                tdspec = TradingDaysSpec.none();
            }
        }

        if (espec.isUsed() && espec.getTest() != RegressionTestSpec.None) {
            Optional<Variable> fe = Arrays.stream(variables).filter(v -> v.isEaster()).findFirst();
            if (fe.isPresent()) {
                EasterVariable evar = (EasterVariable) fe.get().getVariable();
                espec = espec.toBuilder()
                        .test(RegressionTestSpec.None)
                        .duration(evar.getDuration())
                        .build();
            } else {
                espec = EasterSpec.none();
            }
        }
        builder.regression(rbuilder
                .easter(espec)
                .tradingDays(tdspec)
                .build());
    }

    private Parameter[] parametersOf(Parameter[] phi, double[] vals) {
        if (!Parameter.isFree(phi) && phi.length == vals.length) {
            Parameter[] all = new Parameter[vals.length];
            for (int i = 0; i < all.length; ++i) {
                if (phi[i].isFixed()) {
                    all[i] = phi[i];
                } else {
                    all[i] = Parameter.estimated(vals[i]);
                }
            }
            return all;
        } else {
            return Parameter.of(vals, ParameterType.Estimated);
        }
    }

    @Override
    public SaProcessor processor(X13Spec spec) {
        return (s, cxt, log) -> X13Kernel.of(spec, cxt).process(s, log);
    }

    @Override
    public X13Spec decode(SaSpecification spec) {
        if (spec instanceof X13Spec) {
            return (X13Spec) spec;
        } else {
            return null;
        }
    }

    @Override
    public List<SaDiagnosticsFactory> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<X13Results> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<X13Results> olddiag, SaDiagnosticsFactory<X13Results> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

}
