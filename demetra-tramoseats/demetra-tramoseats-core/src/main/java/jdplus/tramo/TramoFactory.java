/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.modelling.TransformationType;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsDomain;
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
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.TsContextVariable;
import java.util.ArrayList;
import java.util.List;
import jdplus.modelling.GeneralLinearModel;
import demetra.timeseries.regression.ModellingUtility;

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
        update(spec.getArima(), desc, builder);
        update(spec.getAutoModel(), desc, builder);
        update(spec.getOutliers(), desc, builder);
        update(spec.getRegression(), desc, builder);

        return builder.build();
    }

    public TramoSpec refreshSpec(TramoSpec currentSpec, TramoSpec domainSpec, EstimationPolicyType policy, TsDomain frozenDomain) {
        TramoSpec.Builder builder = currentSpec.toBuilder();
        switch (policy) {
            case Complete:
                return domainSpec;
            case Outliers_StochasticComponent:
                resetArima(currentSpec, domainSpec, builder);
                removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case Outliers:
                clearArima(currentSpec, domainSpec, builder);
                removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case LastOutliers:
                clearArima(currentSpec, domainSpec, builder);
                removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case FreeParameters:
                freeArima(currentSpec, domainSpec, builder);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case FixedAutoRegressiveParameters:
                fixAR(currentSpec, domainSpec, builder);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case FixedParameters:
                fixArima(currentSpec, domainSpec, builder);
                freeVariables(currentSpec, domainSpec, builder);
                break;
            case Fixed:
            case Current:
                fixArima(currentSpec, domainSpec, builder);
                fixVariables(currentSpec, domainSpec, builder);
                break;
            default:
                return currentSpec;
        }
        return builder.build();
    }

    private void update(TransformSpec transform, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        if (transform.getFunction() == TransformationType.Auto) {
            TransformSpec ntransform = transform.toBuilder()
                    .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                    .build();
            builder.transform(ntransform);
        }
    }

    private void update(SarimaSpec arima, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        SarimaSpec nspec = rslts.getStochasticComponent();
        builder.arima(nspec);
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
                .forEach(v -> builder.outlier(v.removeAttribute(ModellingUtility.AMI)));
//                .forEach(v -> builder.outlier(v.replaceAttribute(ModellingUtility.AMI, ModellingUtility.AMI_PREVIOUS, "tramo")));
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
        TradingDaysType td = TradingDaysType.NONE;
        Parameter[] ctd = null;
        if (ftd.isPresent()) {
            Variable v = ftd.get();
            if (tdspec.isAutomatic()) {
                switch (v.getCore().dim()) {
                    case 1:
                        td = TradingDaysType.TD2;
                        break;
                    case 6:
                        td = TradingDaysType.TD7;
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

    private void resetArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(domainSpec.getArima());
        builder.autoModel(domainSpec.getAutoModel());
    }

    private void removeOutliers(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder, TsDomain frozen) {
        builder.outliers(domainSpec.getOutliers());
        // remove existing automatic outliers...
        List<Variable<IOutlier>> outliers = currentSpec.getRegression().getOutliers();
        List<Variable<IOutlier>> defoutliers = domainSpec.getRegression().getOutliers();

        RegressionSpec.Builder rbuilder = currentSpec.getRegression().toBuilder()
                .clearOutliers();
        // use frozen outliers and outliers specified in the domain spec (avoid doubles)
        defoutliers.forEach(outlier -> {
            rbuilder.outlier(outlier);
        });

        outliers.stream()
                .filter(outlier -> !belongsTo(outlier, defoutliers))
                .filter(outlier -> (frozen != null && frozen.contains(outlier.getCore().getPosition())))
                .forEachOrdered(outlier -> {
                    rbuilder.outlier(outlier);
                });
        builder.regression(rbuilder.build());
    }

    private static boolean belongsTo(Variable<IOutlier> outlier, List<Variable<IOutlier>> defoutliers) {
        return defoutliers.stream()
                .filter(o -> o.getCore().getCode().equals(outlier.getCore().getCode()))
                .anyMatch(o -> o.getCore().getPosition().equals(outlier.getCore().getPosition()));
    }

    private void freeArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().freeParameters(domainSpec.isUsingAutoModel() ? null : domainSpec.getArima()));
    }

    private void clearArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().resetParameters(domainSpec.isUsingAutoModel() ? null : domainSpec.getArima()));
    }
    
    private void fixAR(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        SarimaSpec arima = currentSpec.getArima();
        Parameter[] phi = Parameter.fixParameters(arima.getPhi());
        Parameter[] bphi = Parameter.fixParameters(arima.getBphi());
        SarimaSpec.Builder abuilder = arima.toBuilder()
                .phi(phi)
                .bphi(bphi);
        if (domainSpec.isUsingAutoModel()) {
            abuilder.theta(Parameter.freeParameters(arima.getTheta()))
                    .btheta(Parameter.freeParameters(arima.getTheta()));
        } else {
            SarimaSpec refarima = domainSpec.getArima();
            abuilder.theta(Parameter.freeParameters(arima.getTheta(), refarima.getTheta()))
                    .btheta(Parameter.freeParameters(arima.getTheta(), refarima.getBtheta()));
        }
        builder.arima(abuilder.build());
    }

    private void fixArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().fixParameters());
    }

    private void freeVariables(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        RegressionSpec reg = currentSpec.getRegression();
        RegressionSpec dreg = domainSpec.getRegression();
        RegressionSpec.Builder rbuilder = reg.toBuilder();
        Parameter mean = reg.getMean();
        if (mean != null && mean.isFixed()) {
            Parameter dc = dreg.getMean();
            if (dc == null || !dc.isFixed()) {
                mean = Parameter.initial(mean.getValue());
            }
        }

        List<Variable<InterventionVariable>> iv = reg.getInterventionVariables();
        List<Variable<InterventionVariable>> niv = new ArrayList<>();
        iv.forEach(v -> {
            niv.add(v.withCoefficients(freeCoefficients(v, dreg.getInterventionVariables())));
        });

        List<Variable<IOutlier>> o = reg.getOutliers();
        List<Variable<IOutlier>> no = new ArrayList<>();
        o.forEach(v -> {
            no.add(v.withCoefficients(freeCoefficients(v, dreg.getOutliers())));
        });

        List<Variable<Ramp>> r = reg.getRamps();
        List<Variable<Ramp>> nr = new ArrayList<>();
        r.forEach(v -> {
            nr.add(v.withCoefficients(freeCoefficients(v, dreg.getRamps())));
        });

        List<Variable<TsContextVariable>> u = reg.getUserDefinedVariables();
        List<Variable<TsContextVariable>> nu = new ArrayList<>();
        u.forEach(v -> {
            nu.add(v.withCoefficients(freeCoefficients(v, dreg.getUserDefinedVariables())));
        });

        EasterSpec easter = reg.getCalendar().getEaster();
        Parameter c = easter.getCoefficient();
        if (c != null && c.isFixed()) {
            Parameter dc = dreg.getCalendar().getEaster().getCoefficient();
            if (dc == null || !dc.isFixed()) {
                c = Parameter.initial(c.getValue());
                easter = easter.toBuilder()
                        .coefficient(c)
                        .build();
            }
        }
        TradingDaysSpec td = reg.getCalendar().getTradingDays();
        c = td.getLpCoefficient();
        Parameter[] tdc = td.getTdCoefficients();
        if (c != null || tdc != null) {
            if (c != null && c.isFixed()) {
                Parameter dc = dreg.getCalendar().getTradingDays().getLpCoefficient();
                if (dc == null || !dc.isFixed()) {
                    c = Parameter.initial(c.getValue());
                }
            }
            tdc=Parameter.freeParameters(tdc, dreg.getCalendar().getTradingDays().getTdCoefficients());
            td = td.withCoefficients(tdc, c);
        }

        builder.regression(rbuilder
                .mean(mean)
                .clearInterventionVariables().interventionVariables(niv)
                .clearOutliers().outliers(no)
                .clearRamps().ramps(nr)
                .clearUserDefinedVariables().userDefinedVariables(nu)
                .calendar(CalendarSpec.builder()
                        .easter(easter)
                        .tradingDays(td)
                        .build())
                .build());
    }

    private static <S extends ITsVariable> Parameter[] freeCoefficients(Variable<S> var, List<Variable<S>> ref) {
        Parameter[] c = var.getCoefficients();
        if (c == null) {
            return null;
        }
        Optional<Variable<S>> rvar = ref.stream().filter(v -> v.getName().equals(var.getName())).findFirst();
        if (rvar.isPresent()) {
            return Parameter.freeParameters(c, rvar.get().getCoefficients());
        } else {
            return Parameter.freeParameters(c);
        }
    }

    private void fixVariables(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        RegressionSpec reg = currentSpec.getRegression();
        RegressionSpec.Builder rbuilder = reg.toBuilder();
        Parameter mean = reg.getMean();
        if (mean != null && mean.isDefined()) {
            mean = Parameter.fixed(mean.getValue());
        }

        List<Variable<InterventionVariable>> iv = reg.getInterventionVariables();
        List<Variable<InterventionVariable>> niv = new ArrayList<>();
        iv.forEach(v -> {
            niv.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        List<Variable<IOutlier>> o = reg.getOutliers();
        List<Variable<IOutlier>> no = new ArrayList<>();
        o.forEach(v -> {
            no.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        List<Variable<Ramp>> r = reg.getRamps();
        List<Variable<Ramp>> nr = new ArrayList<>();
        r.forEach(v -> {
            nr.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        List<Variable<TsContextVariable>> u = reg.getUserDefinedVariables();
        List<Variable<TsContextVariable>> nu = new ArrayList<>();
        u.forEach(v -> {
            nu.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        EasterSpec easter = reg.getCalendar().getEaster();
        Parameter c = easter.getCoefficient();
        if (c != null) {
            easter = easter.toBuilder()
                    .coefficient(Parameter.fixed(c.getValue()))
                    .build();
        }
        TradingDaysSpec td = reg.getCalendar().getTradingDays();
        c = td.getLpCoefficient();
        Parameter[] tdc = td.getTdCoefficients();
        if (c != null || tdc != null) {
            td = td.withCoefficients(Parameter.fixParameters(tdc), c == null ? null : Parameter.fixed(c.getValue()));
         }

        builder.regression(rbuilder
                .mean(mean)
                .clearInterventionVariables().interventionVariables(niv)
                .clearOutliers().outliers(no)
                .clearRamps().ramps(nr)
                .clearUserDefinedVariables().userDefinedVariables(nu)
                .calendar(CalendarSpec.builder()
                        .easter(easter)
                        .tradingDays(td)
                        .build())
                .build());

    }

}
