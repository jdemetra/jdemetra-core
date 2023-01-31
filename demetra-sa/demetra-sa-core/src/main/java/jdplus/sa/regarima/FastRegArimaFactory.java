/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.regarima;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.Range;
import demetra.modelling.TransformationType;
import demetra.modelling.regular.CalendarSpec;
import demetra.modelling.regular.EasterSpec;
import demetra.modelling.regular.ModellingSpec;
import demetra.modelling.regular.OutlierSpec;
import demetra.modelling.regular.RegressionSpec;
import demetra.modelling.regular.TradingDaysSpec;
import demetra.modelling.regular.TransformSpec;
import demetra.sa.ComponentType;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaVariable;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jdplus.modelling.GeneralLinearModel;
import jdplus.regarima.ami.ModellingUtility;

/**
 *
 * @author palatej
 */
public class FastRegArimaFactory {

    private static final FastRegArimaFactory INSTANCE = new FastRegArimaFactory();

    public static FastRegArimaFactory getInstance() {
        return INSTANCE;
    }

    public ModellingSpec generateSpec(ModellingSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {
        if (desc == null) {
            return spec;
        }
        ModellingSpec.Builder builder = spec.toBuilder();
        update(spec.getTransform(), desc, builder);
        update(desc, builder);
        update(spec.getOutliers(), builder);
        update(spec.getRegression(), desc, builder);

        return builder.build();
    }

    public ModellingSpec refreshSpec(ModellingSpec currentSpec, ModellingSpec domainSpec, EstimationPolicyType policy, TsDomain frozenDomain) {
        ModellingSpec.Builder builder = currentSpec.toBuilder();
        switch (policy) {
            case Complete -> {
                return domainSpec;
            }
            case Outliers_StochasticComponent -> {
                resetArima(domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case Outliers -> {
                clearArima(currentSpec, domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case LastOutliers -> {
                clearArima(currentSpec, domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case FreeParameters -> {
                freeArima(currentSpec, domainSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case FixedAutoRegressiveParameters -> {
                fixAR(currentSpec, domainSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case FixedParameters -> {
                fixArima(currentSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case Fixed, Current -> {
                fixArima(currentSpec, builder);
                fixVariables(currentSpec.getRegression(), builder, frozenDomain);
            }
            default -> {
                return currentSpec;
            }
        }
        return builder.build();
    }

    private void update(TransformSpec transform, GeneralLinearModel.Description<SarimaSpec> rslts, ModellingSpec.Builder builder) {
        if (transform.getFunction() == TransformationType.Auto) {
            TransformSpec ntransform = transform.toBuilder()
                    .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                    .adjust(rslts.getLengthOfPeriodTransformation())
                    .build();
            builder.transform(ntransform);
        }
    }

    private void update(GeneralLinearModel.Description<SarimaSpec> rslts, ModellingSpec.Builder builder) {
        SarimaSpec nspec = rslts.getStochasticComponent();
        builder.arima(nspec);
    }

    private void update(OutlierSpec outliers, ModellingSpec.Builder builder) {
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

    private void update(RegressionSpec regression, GeneralLinearModel.Description<SarimaSpec> rslts, ModellingSpec.Builder builder) {
        // The huge part. 
        RegressionSpec.Builder rbuilder = regression.toBuilder();
        // all the coefficients (fixed or free) of the variables have already been filled
        Variable[] variables = rslts.getVariables();
        updateMean(variables, rbuilder);
        update(regression.getCalendar(), variables, rbuilder);
        updateOutliers(variables, rbuilder);
        updateUserVariables(variables, rbuilder);
        builder.regression(rbuilder.build());
    }

    private void updateMean(Variable[] vars, RegressionSpec.Builder builder) {
        Optional<Variable> fc = Arrays.stream(vars)
                .filter(v -> v.getName().equals(TrendConstant.NAME)).findFirst();
        builder.checkMu(false);
        if (fc.isPresent()) {
            builder.mean(fc.get().getCoefficient(0));
        } else {
            builder.mean(null);
        }
    }

    private void updateOutliers(Variable[] vars, RegressionSpec.Builder builder) {
        builder.clearOutliers();
        Arrays.stream(vars)
                .filter(v -> ModellingUtility.isOutlier(v))
                .forEach(v -> builder.outlier(v.removeAttribute(ModellingUtility.AMI)));
    }

    private void updateUserVariables(Variable[] vars, RegressionSpec.Builder builder) {

        builder.clearInterventionVariables();
        Arrays.stream(vars)
                .filter(v -> v.getCore() instanceof InterventionVariable)
                .forEach(v -> builder.interventionVariable(v));
        builder.clearRamps();
        Arrays.stream(vars)
                .filter(v -> v.getCore() instanceof Ramp)
                .forEach(v -> builder.ramp(v));
        builder.clearUserDefinedVariables();
        Arrays.stream(vars)
                .filter(v -> ModellingUtility.isUser(v))
                .filter(v -> !(v.getCore() instanceof InterventionVariable))
                .filter(v -> !(v.getCore() instanceof Ramp))
                .map(v -> v.withCore(TsContextVariable.of(v.getCore())))
                .forEach(v -> builder.userDefinedVariable(v));
    }

    private void update(CalendarSpec cspec, Variable[] variables, RegressionSpec.Builder builder) {
        CalendarSpec.Builder cbuilder = CalendarSpec.builder();
        update(cspec.getTradingDays(), variables, cbuilder);
        update(cspec.getEaster(), variables, cbuilder);
        builder.calendar(cbuilder.build());
    }

    private void update(TradingDaysSpec tdspec, Variable[] vars, CalendarSpec.Builder builder) {
        // leap year
        Optional<Variable> flp = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isLengthOfPeriod(v)).findFirst();
        Optional<Variable> ftd = Arrays.stream(vars)
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
                ITradingDaysVariable tdv = (ITradingDaysVariable) v.getCore();
                td = tdv.getTradingDaysType();
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
        // Search for an optional easter variable
        Optional<Variable> fe = Arrays.stream(vars)
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

    private void resetArima(ModellingSpec domainSpec, ModellingSpec.Builder builder) {
        builder.arima(domainSpec.getArima());
    }

    private RegressionSpec removeOutliers(ModellingSpec currentSpec, ModellingSpec domainSpec, ModellingSpec.Builder builder, TsDomain frozen) {
        OutlierSpec ospec = domainSpec.getOutliers();
        if (frozen != null) {
            ospec = ospec.toBuilder().span(TimeSelector.from(frozen.getEndPeriod().start())).build();
        }
        builder.outliers(ospec);

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
        return rbuilder.build();
    }

    private static boolean belongsTo(Variable<IOutlier> outlier, List<Variable<IOutlier>> defoutliers) {
        return defoutliers.stream()
                .filter(o -> o.getCore().getCode().equals(outlier.getCore().getCode()))
                .anyMatch(o -> o.getCore().getPosition().equals(outlier.getCore().getPosition()));
    }

    private void freeArima(ModellingSpec currentSpec, ModellingSpec domainSpec, ModellingSpec.Builder builder) {
        builder.arima(currentSpec.getArima().freeParameters(domainSpec.getArima()));
    }

    private void clearArima(ModellingSpec currentSpec, ModellingSpec domainSpec, ModellingSpec.Builder builder) {
        builder.arima(currentSpec.getArima().resetParameters(domainSpec.getArima()));
    }

    private void fixAR(ModellingSpec currentSpec, ModellingSpec domainSpec, ModellingSpec.Builder builder) {
        SarimaSpec arima = currentSpec.getArima();
        Parameter[] phi = Parameter.fixParameters(arima.getPhi());
        Parameter[] bphi = Parameter.fixParameters(arima.getBphi());
        SarimaSpec.Builder abuilder = arima.toBuilder()
                .phi(phi)
                .bphi(bphi);

        SarimaSpec refarima = domainSpec.getArima();
        abuilder.theta(Parameter.freeParameters(arima.getTheta(), refarima.getTheta()))
                .btheta(Parameter.freeParameters(arima.getTheta(), refarima.getBtheta()));
        builder.arima(abuilder.build());
    }

    private void fixArima(ModellingSpec currentSpec, ModellingSpec.Builder builder) {
        builder.arima(currentSpec.getArima().fixParameters());
    }

    private void freeVariables(RegressionSpec reg, ModellingSpec domainSpec, ModellingSpec.Builder builder) {
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
            tdc = Parameter.freeParameters(tdc, dreg.getCalendar().getTradingDays().getTdCoefficients());
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

    private static Map<String, String> ao_attributes() {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(ModellingUtility.AMI, "demetra");
        attributes.put(SaVariable.REGEFFECT, ComponentType.Irregular.name());
        return attributes;
    }

    private static final Map<String, String> IV_AO = ao_attributes();

    private void fixVariables(RegressionSpec reg, ModellingSpec.Builder builder, TsDomain frozenDomain) {
        RegressionSpec.Builder rbuilder = reg.toBuilder();
        Parameter mean = reg.getMean();
        if (mean != null && mean.isDefined()) {
            mean = Parameter.fixed(mean.getValue());
        }

        List<Variable<InterventionVariable>> iv = reg.getInterventionVariables();
        List<Variable<InterventionVariable>> niv = new ArrayList<>();
        iv.forEach(v -> {
            String n = v.getName();
            if (!n.startsWith(EstimationPolicyType.IV_AO)) {
                niv.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
            } else {
                niv.add(v);
            }
        });
        if (frozenDomain != null) {
            // Current AO: Add IV (ao for the frozen period)
            for (int i = 0; i < frozenDomain.getLength(); ++i) {
                TsPeriod period = frozenDomain.get(i);
                LocalDateTime day = period.start();
                InterventionVariable ao = InterventionVariable.builder()
                        .sequence(Range.of(day, day))
                        .build();
                niv.add(Variable.<InterventionVariable>builder()
                        .name(EstimationPolicyType.IV_AO + period.display())
                        .attributes(IV_AO)
                        .core(ao)
                        .build());
            }
        }

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
