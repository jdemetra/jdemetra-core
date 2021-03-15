/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.timeseries.regression.modelling.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.modelling.ComponentInformation;
import demetra.modelling.TransformationType;
import demetra.sa.ComponentType;
import demetra.sa.EstimationPolicy;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.seats.DecompositionSpec;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.tramo.RegressionTestType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.timeseries.regression.Variable;
import demetra.tramo.AutoModelSpec;
import demetra.tramo.CalendarSpec;
import demetra.tramo.EasterSpec;
import demetra.tramo.OutlierSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.Arrays;
import java.util.Optional;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;
import jdplus.seats.SeatsResults;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.TrendConstant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.seats.diagnostics.SeatsDiagnosticsConfiguration;
import jdplus.seats.diagnostics.SeatsDiagnosticsFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public class TramoSeatsFactory implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults> {

    public static final TramoSeatsFactory INSTANCE = new TramoSeatsFactory();

    private final List<SaDiagnosticsFactory<TramoSeatsResults>> diagnostics = new CopyOnWriteArrayList<>();

    public TramoSeatsFactory() {
        CoherenceDiagnosticsFactory<TramoSeatsResults> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            return new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<TramoSeatsResults> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing().getModel());
        SaResidualsDiagnosticsFactory<TramoSeatsResults> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<TramoSeatsResults> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.DEFAULT,
                        r -> r.getPreprocessing());
        SeatsDiagnosticsFactory<TramoSeatsResults> seats
                = new SeatsDiagnosticsFactory<>(SeatsDiagnosticsConfiguration.DEFAULT,
                        r -> r.getDecomposition());
        AdvancedResidualSeasonalityDiagnosticsFactory<TramoSeatsResults> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            boolean mul = r.getPreprocessing().isLogTransformation();
                            TsData sa = r.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                            TsData irr = r.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                            return new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, irr);
                        }
                );
        ResidualTradingDaysDiagnosticsFactory<TramoSeatsResults> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT,
                        (TramoSeatsResults r) -> {
                            boolean mul = r.getPreprocessing().isLogTransformation();
                            TsData sa = r.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                            TsData irr = r.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                            return new ResidualTradingDaysDiagnostics.Input(mul, sa, irr);
                        }
                );

        diagnostics.add(coherence);
        diagnostics.add(residuals);
        diagnostics.add(outofsample);
        diagnostics.add(outliers);
        diagnostics.add(seats);
        diagnostics.add(advancedResidualSeasonality);
        diagnostics.add(residualTradingDays);

    }

    @Override
    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, TramoSeatsResults estimation) {
        if (spec instanceof TramoSeatsSpec && estimation instanceof TramoSeatsResults) {
            TramoSeatsSpec espec = (TramoSeatsSpec) spec;
            TramoSeatsResults rslts = (TramoSeatsResults) estimation;

            TramoSpec ntspec = update(espec.getTramo(), rslts.getPreprocessing());
            DecompositionSpec nsspec = update(espec.getSeats(), rslts.getDecomposition());

            return espec.toBuilder()
                    .tramo(ntspec)
                    .seats(nsspec)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid specification");
        }
    }

    @Override
    public SaSpecification refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, EstimationPolicy policy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DecompositionSpec update(DecompositionSpec seats, SeatsResults rslts) {
        // Nothing to do (for the time being)
        return seats;
    }

    private void update(TransformSpec transform, ModelEstimation rslts, TramoSpec.Builder builder) {
        if (transform.getFunction() == TransformationType.Auto) {
            TransformSpec ntransform = transform.toBuilder()
                    .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                    .build();
            builder.transform(ntransform);
        }
    }

    private void update(SarimaSpec sarima, ModelEstimation rslts, TramoSpec.Builder builder) {
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

    private void updateArima(ModelEstimation rslts, TramoSpec.Builder builder) {
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

    private void update(AutoModelSpec ami, ModelEstimation rslts, TramoSpec.Builder builder) {
        // Disable ami
        AutoModelSpec nami = ami.toBuilder()
                .enabled(false)
                .build();
        builder.autoModel(nami);
    }

    private TramoSpec update(TramoSpec regarima, ModelEstimation rslts) {
        TramoSpec.Builder builder = regarima.toBuilder();
        update(regarima.getTransform(), rslts, builder);
        update(regarima.getArima(), rslts, builder);
        update(regarima.getAutoModel(), rslts, builder);
        update(regarima.getOutliers(), rslts, builder);
        update(regarima.getRegression(), rslts, builder);

        return builder.build();
    }

    private void update(OutlierSpec outliers, ModelEstimation rslts, TramoSpec.Builder builder) {
        if (outliers.isUsed()) {    // Disable outliers
            builder.outliers(
                    outliers.toBuilder()
                            .ao(false)
                            .ls(false)
                            .tc(false)
                            .so(false)
                            .build());
        }
    }

    private void update(RegressionSpec regression, ModelEstimation rslts, TramoSpec.Builder builder) {
        // The huge part
        RegressionSpec.Builder rbuilder = regression.toBuilder();
        // all the coefficients (fixed or free) of the variables have already been filled
        Variable[] variables = rslts.getVariables();
        updateMean(variables, rbuilder);
        update(regression.getCalendar(), variables, rbuilder);
        updateOutliers(variables, rbuilder);
        builder.regression(rbuilder.build());
    }

    private void updateMean(Variable[] vars, RegressionSpec.Builder builder) {
        Optional<Variable> fc = Arrays.stream(vars)
                .filter(v -> v.getName().equals(TrendConstant.NAME)).findFirst();
        if (fc.isPresent()) {
            builder.mean(fc.get().getCoefficient(0));
        } else {
            builder.mean(null);
        }
    }

    private void updateOutliers(Variable[] vars, RegressionSpec.Builder builder) {
        // we keep the information that it has been previously estimated automatically
        Arrays.stream(vars)
                .filter(v -> ModellingUtility.isOutlier(v))
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .forEach(v -> builder.outlier(v.replaceAttribute(ModellingUtility.AMI, ModellingUtility.AMI_PREVIOUS, "tramo")));
    }

    private void update(CalendarSpec cspec, Variable[] variables, RegressionSpec.Builder builder) {
        CalendarSpec.Builder cbuilder = CalendarSpec.builder();
        update(cspec.getTradingDays(), variables, cbuilder);
        update(cspec.getEaster(), variables, cbuilder);
        builder.calendar(cbuilder.build());
    }

    private void update(TradingDaysSpec tdspec, Variable[] vars, CalendarSpec.Builder builder) {
        // Nothing to do
        if (!tdspec.isUsed() || !(tdspec.isTest() || tdspec.isAutomatic())) {
            return;
        }
        // leap year
        Optional<Variable> flp = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .filter(v -> ModellingUtility.isLengthOfPeriod(v)).findFirst();
        Optional<Variable> ftd = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .filter(v -> ModellingUtility.isTradingDays(v)).findFirst();

        TradingDaysSpec ntdspec = TradingDaysSpec.none();

        LengthOfPeriodType lp = LengthOfPeriodType.None;
        Parameter clp = null;
        if (flp.isPresent()) {
            Variable v = flp.get();
            lp = tdspec.getLengthOfPeriodType();
            clp = v.getCoefficient(0);
        }
        TradingDaysType td = TradingDaysType.None;
        Parameter[] ctd = null;
        if (ftd.isPresent()) {
            Variable v = ftd.get();
            if (tdspec.isAutomatic()) {
                switch (v.getCore().dim()){
                    case 1: 
                        td=TradingDaysType.WorkingDays;
                        break;
                    case 6: 
                        td=TradingDaysType.TradingDays;
                        break;
                }
            } else {
                td = tdspec.getTradingDaysType();
            }
            ctd = v.getCoefficients();
        }

        if (ftd.isPresent() || flp.isPresent()) {
            if (tdspec.isStockTradingDays()) {
                int ntd = tdspec.getStockTradingDays();
                ntdspec = TradingDaysSpec.stockTradingDays(ntd, ctd);
            } else if (tdspec.isHolidays()) {
                ntdspec = TradingDaysSpec.holidays(tdspec.getHolidays(),
                        td, lp, ctd, clp);
            } else if (tdspec.isUserDefined()) {
                ntdspec = TradingDaysSpec.userDefined(tdspec.getUserVariables(), ctd);
            } else { //normal case
                ntdspec = TradingDaysSpec.td(td, lp, ctd, clp);
            }
        }
        builder.tradingDays(ntdspec);
    }

    private void update(EasterSpec espec, Variable[] vars, CalendarSpec.Builder builder) {
        // Nothing to do
        if (!espec.isUsed() || !espec.isTest()) {
            return;
        }
        // Search for an optional easter variable
        Optional<Variable> fe = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .filter(v -> ModellingUtility.isEaster(v)).findFirst();
        if (fe.isPresent()) {
            Variable ev = fe.get();
            EasterVariable evar = (EasterVariable) ev.getCore();
            espec = espec.toBuilder()
                    .test(false)
                    .duration(evar.getDuration())
                    .coefficient(ev.getCoefficient(0))
                    .build();
        } else {
            espec = EasterSpec.none();
        }
        builder.easter(espec);
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
    public SaProcessor processor(TramoSeatsSpec spec) {
        return (s, cxt, log) -> TramoSeatsKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public TramoSeatsSpec decode(SaSpecification spec) {
        if (spec instanceof TramoSeatsSpec) {
            return (TramoSeatsSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof TramoSeatsSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<TramoSeatsResults>> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<TramoSeatsResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<TramoSeatsResults> olddiag, SaDiagnosticsFactory<TramoSeatsResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

}
