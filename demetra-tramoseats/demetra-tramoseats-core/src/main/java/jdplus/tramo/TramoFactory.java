/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import demetra.modelling.implementations.SarimaSpec;
import demetra.data.Parameter;
import demetra.modelling.TransformationType;
import demetra.sa.EstimationPolicy;
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
import java.util.Arrays;
import java.util.Optional;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import jdplus.regarima.ami.ModellingUtility;

/**
 *
 * @author PALATEJ
 */
//@ServiceProvider(SaProcessingFactory.class)
public class TramoFactory /*implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults>*/ {

    public static final TramoFactory INSTANCE = new TramoFactory();

    public TramoSpec generateSpec(TramoSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {
       TramoSpec.Builder builder = spec.toBuilder();
        update(spec.getTransform(), desc, builder);
        builder.arima(desc.getStochasticComponent());
        update(spec.getAutoModel(), desc, builder);
        update(spec.getOutliers(), desc, builder);
        update(spec.getRegression(), desc, builder);

        return builder.build();
    }

    public TramoSpec refreshSpec(TramoSpec currentSpec, TramoSpec domainSpec, EstimationPolicy policy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    private void update(TransformSpec transform, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        if (transform.getFunction() == TransformationType.Auto) {
            TransformSpec ntransform = transform.toBuilder()
                    .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                    .build();
            builder.transform(ntransform);
        }
    }

    private void update(AutoModelSpec ami, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        // Disable ami
        AutoModelSpec nami = ami.toBuilder()
                .enabled(false)
                .build();
        builder.autoModel(nami);
    }

    private void update(OutlierSpec outliers, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
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

    private void update(RegressionSpec regression, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
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

 
}
