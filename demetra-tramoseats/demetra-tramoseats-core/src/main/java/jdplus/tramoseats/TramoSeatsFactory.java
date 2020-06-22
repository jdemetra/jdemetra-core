/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.modelling.TransformationType;
import demetra.processing.ProcResults;
import demetra.sa.EstimationPolicy;
import demetra.sa.SaProcessor;
import demetra.sa.SaProcessorFactory;
import demetra.sa.SaSpecification;
import demetra.seats.DecompositionSpec;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.RegressionTestType;
import demetra.timeseries.regression.TradingDaysType;
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

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessorFactory.class)
public class TramoSeatsFactory implements SaProcessorFactory<TramoSeatsSpec, TramoSeatsResults> {

    public static final TramoSeatsFactory INSTANCE = new TramoSeatsFactory();

    @Override
    public TramoSeatsSpec of(TramoSeatsSpec spec, TramoSeatsResults estimation) {
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
    public SaSpecification refreshSpecification(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, EstimationPolicy policy) {
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

    private TramoSpec update(TramoSpec tramo, ModelEstimation rslts) {
        TramoSpec.Builder builder = tramo.toBuilder();
        update(tramo.getTransform(), rslts, builder);
        AutoModelSpec ami = tramo.getAutoModel();
        if (ami.isEnabled()) {
            update(tramo.getArima(), rslts, builder);
            update(ami, rslts, builder);
        } else {
            updateArima(rslts, builder);
        }

        update(tramo.getOutliers(), rslts, builder);
        update(tramo.getRegression(), rslts, builder);

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
        EasterSpec espec = regression.getCalendar().getEaster();
        TradingDaysSpec tdspec = regression.getCalendar().getTradingDays();

        if (tdspec.isUsed() && (tdspec.isTest() || tdspec.isAutomatic())) {
            // leap year
            ILengthOfPeriodVariable lp = null;
            ITradingDaysVariable td = null;
            Optional<Variable> flp = Arrays.stream(variables).filter(v -> v.isLengthOfPeriod()).findFirst();
            if (flp.isPresent()) {
                lp = (ILengthOfPeriodVariable) flp.get().getVariable();
            }
            Optional<Variable> ftd = Arrays.stream(variables).filter(v -> v.isTradingDays()).findFirst();
            if (ftd.isPresent()) {
                td = (ITradingDaysVariable) ftd.get().getVariable();
            }
            if (lp != null || td != null) {
                if (tdspec.isStockTradingDays()) {
                    int ntd = tdspec.getStockTradingDays();
                    tdspec = TradingDaysSpec.stockTradingDays(ntd, RegressionTestType.None);
                } else if (tdspec.isHolidays()) {
                    tdspec = TradingDaysSpec.holidays(tdspec.getHolidays(),
                            tdspec.getTradingDaysType(), lp != null, RegressionTestType.None);
                } else if (tdspec.isUserDefined()) {
                    tdspec = TradingDaysSpec.userDefined(tdspec.getUserVariables(), RegressionTestType.None);
                } else { //normal case
                    tdspec = TradingDaysSpec.td(td.dim() == 6 ? TradingDaysType.TradingDays : TradingDaysType.WorkingDays,
                            lp != null, RegressionTestType.None);
                }
            } else {
                tdspec = TradingDaysSpec.none();
            }
        }

        if (espec.isUsed() && espec.isTest()) {
            Optional<Variable> fe = Arrays.stream(variables).filter(v -> v.isEaster()).findFirst();
            if (fe.isPresent()) {
                espec = espec.toBuilder()
                        .test(false)
                        .build();
            } else {
                espec = EasterSpec.none();
            }
        }
        rbuilder.calendar(
                CalendarSpec.builder()
                        .tradingDays(tdspec)
                        .easter(espec)
                        .build());
        builder.regression(rbuilder.build());
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
        if (spec instanceof TramoSeatsSpec)
            return (TramoSeatsSpec) spec;
        else
            return null;
    }

}
