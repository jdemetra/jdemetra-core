/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.x13.io.information;

import demetra.data.Parameter;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.modelling.io.information.InterventionVariableMapping;
import demetra.modelling.io.information.OutlierDefinition;
import demetra.modelling.io.information.OutlierMapping;
import demetra.modelling.io.information.RampMapping;
import demetra.modelling.io.information.TsContextVariableMapping;
import demetra.modelling.io.information.VariableMapping;
import demetra.regarima.MeanSpec;
import demetra.regarima.RegressionSpec;
import demetra.sa.SaVariable;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class RegressionSpecMapping {

    final String AICDIFF = "aicdiff",
            MU = "mu", CHECKMU = "checkmu",
            TD = "tradingdays", EASTER = "easter",
            MH = "mh", MHS = "mh*",
            OUTLIER = "outlier", OUTLIERS_LEGACY = "outliers", OUTLIERS = "outlier*",
            RAMP = "ramp", RAMPS_LEGACY = "ramps", RAMPS = "ramp*",
            USER = "user", USERS = "user*",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*",
            COEFF = "coefficients", FCOEFF = "fixedcoefficients";

    Parameter coefficientOf(InformationSet regInfo, String name) {
        InformationSet scoefs = regInfo.getSubSet(RegressionSpecMapping.COEFF);
        if (scoefs != null) {
            Double coef = scoefs.get(name, Double.class);
            if (coef != null) {
                return Parameter.estimated(coef);
            }
        }
        return fixedCoefficientOf(regInfo, name);
    }

    Parameter[] coefficientsOf(InformationSet regInfo, String name) {
        InformationSet scoefs = regInfo.getSubSet(COEFF);
        if (scoefs != null) {
            double[] coef = scoefs.get(name, double[].class);
            if (coef != null) {
                return Parameter.of(coef, demetra.data.ParameterType.Estimated);
            }
        }
        return fixedCoefficientsOf(regInfo, name);
    }

    Parameter fixedCoefficientOf(InformationSet regInfo, String name) {
        InformationSet fcoefs = regInfo.getSubSet(RegressionSpecMapping.FCOEFF);
        if (fcoefs != null) {
            Double coef = fcoefs.get(name, Double.class);
            if (coef != null) {
                return Parameter.fixed(coef);
            }
        }
        return null;
    }

    Parameter[] fixedCoefficientsOf(InformationSet regInfo, String name) {
        InformationSet fcoefs = regInfo.getSubSet(FCOEFF);
        if (fcoefs != null) {
            double[] coef = fcoefs.get(name, double[].class);
            if (coef != null) {
                return Parameter.of(coef, demetra.data.ParameterType.Fixed);
            }
        }
        return null;
    }

    void set(InformationSet regInfo, String name, Parameter p) {
        if (p == null || !p.isDefined()) {
            return;
        }
        InformationSet scoefs = regInfo.subSet(p.isFixed() ? FCOEFF
                : COEFF);
        scoefs.set(name, p.getValue());
    }

    void set(InformationSet regInfo, String name, Parameter[] p) {
        if (p == null || Parameter.isDefault(p)) {
            return;
        }
        // TODO Split in case of partially fixed parameters
        InformationSet scoefs = regInfo.subSet(Parameter.hasFixedParameters(p) ? FCOEFF
                : COEFF);
        scoefs.set(name, Parameter.values(p));
    }

    void readLegacy(InformationSet regInfo, TsDomain context, RegressionSpec.Builder builder) {
        if (regInfo == null) {
            return;
        }

        builder.tradingDays(TradingDaysSpecMapping.readLegacy(regInfo))
                .easter(EasterSpecMapping.readLegacy(regInfo));
        // LEGACY
        String[] outliers = regInfo.get(OUTLIERS_LEGACY, String[].class);
        if (outliers != null) {
            for (int i = 0; i < outliers.length; ++i) {
                OutlierDefinition o = OutlierDefinition.fromString(outliers[i]);
                if (o != null) {
                    Parameter c = RegressionSpecMapping.coefficientOf(regInfo, o.name(context));
                    if (c == null)
                        c=RegressionSpecMapping.coefficientOf(regInfo, o.name(null));
                    IOutlier io = OutlierMapping.from(o);
                    String name = OutlierMapping.name(io, context);
                    Variable<IOutlier> v=Variable.<IOutlier>builder()
                            .name(name)
                            .core(io)
                            .attribute(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(io).name())
                            .coefficients(c == null ? null : new Parameter[]{c})
                            .build();
                    builder.outlier(v);
                }
            }
        }
        String[] ramps = regInfo.get(RAMPS_LEGACY, String[].class);
        if (ramps != null) {
            for (int i = 0; i < ramps.length; ++i) {
                Ramp r = RampMapping.parseLegacy(ramps[i], context);
                if (r != null) {
                    Parameter c = RegressionSpecMapping.coefficientOf(regInfo, ramps[i]);
                    builder.ramp(Variable.variable(ramps[i], r).withCoefficient(c));
                }
            }
        }
        List<Information<InformationSet>> sel = regInfo.select(INTERVENTIONS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                InformationSet sinfo = sub.getValue();
                InterventionVariable iv = InterventionVariableMapping.read(sub.getValue());
                String name=sinfo.get(InterventionVariableMapping.NAME_LEGACY, String.class);
                Variable<InterventionVariable> v=Variable.<InterventionVariable>builder()
                        .core(iv)
                        .name(name)
                        .coefficients(coefficientsOf(regInfo, name))
                        .attribute(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(iv).name())
                        .build();
                 builder.interventionVariable(v);
            }
        }
        sel = regInfo.select(USERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                List<Variable<TsContextVariable>> v = TsContextVariableMapping.readLegacy(sub.getValue());
                if (!v.isEmpty()) {
                    Parameter[] c = coefficientsOf(regInfo, v.get(0).getName());
                    int j = 0;
                    if (c != null) {
                        v.forEach(var -> builder.userDefinedVariable(var.withCoefficient(c[j])));
                    } else {
                        v.forEach(var -> builder.userDefinedVariable(var));
                    }
                }
            }
        }
    }

    RegressionSpec read(InformationSet info) {
        if (info == null) {
            return RegressionSpec.DEFAULT;
        }
        MeanSpec mean = MeanSpec.DEFAULT_UNUSED;
        Parameter mu = info.get(MU, Parameter.class);
        if (mu != null) {
            Boolean tmu = info.get(CHECKMU, Boolean.class);
            mean = MeanSpec.builder()
                    .trendConstant(true)
                    .test(tmu == null ? false : tmu)
                    .coefficient(mu)
                    .build();
        }

        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(mean)
                .tradingDays(TradingDaysSpecMapping.read(info.getSubSet(TD)))
                .easter(EasterSpecMapping.read(info.getSubSet(EASTER)));
        List<Information<InformationSet>> sel = info.select(OUTLIERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<IOutlier> v = VariableMapping.readO(sub.getValue());
                builder.outlier(v);
            }
        }
        sel = info.select(RAMPS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<Ramp> v = VariableMapping.readR(sub.getValue());
                builder.ramp(v);
            }
        }
        sel = info.select(INTERVENTIONS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<InterventionVariable> v = VariableMapping.readIV(sub.getValue());
                builder.interventionVariable(v);
            }
        }
        sel = info.select(USERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<TsContextVariable> v = VariableMapping.readT(sub.getValue());
                builder.userDefinedVariable(v);
            }
        }
        return builder.build();
    }

    InformationSet write(RegressionSpec spec, TsDomain context, boolean verbose) {
        if (!spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        MeanSpec mean = spec.getMean();
        if (mean.isUsed()) {
            info.set(MU, mean.getCoefficient());
            if (verbose || mean.isTest()) {
                info.set(CHECKMU, mean.isTest());
            }
        }
        InformationSet tdinfo = TradingDaysSpecMapping.write(spec.getTradingDays(), verbose);
        if (tdinfo != null) {
            info.set(TD, tdinfo);
        }
        InformationSet einfo = EasterSpecMapping.write(spec.getEaster(), verbose);
        if (einfo != null) {
            info.set(EASTER, einfo);
        }
        List<Variable<IOutlier>> voutliers = spec.getOutliers();
        if (!voutliers.isEmpty()) {
            int idx = 1;
            for (Variable<IOutlier> v : voutliers) {
                InformationSet w = VariableMapping.writeO(v, verbose);
                info.set(OUTLIER + (idx++), w);
            }
        }
        List<Variable<Ramp>> vramps = spec.getRamps();
        if (!vramps.isEmpty()) {
            int idx = 1;
            for (Variable<Ramp> v : vramps) {
                InformationSet w = VariableMapping.writeR(v, verbose);
                info.set(RAMP + (idx++), w);
            }
        }
        List<Variable<TsContextVariable>> vusers = spec.getUserDefinedVariables();
        if (!vusers.isEmpty()) {
            int idx = 1;
            for (Variable<TsContextVariable> v : vusers) {
                InformationSet w = VariableMapping.writeT(v, verbose);
                info.set(USER + (idx++), w);
            }
        }
        List<Variable<InterventionVariable>> viv = spec.getInterventionVariables();
        if (!viv.isEmpty()) {
            int idx = 1;
            for (Variable<InterventionVariable> v : viv) {
                InformationSet w = VariableMapping.writeIV(v, verbose);
                info.set(INTERVENTION + (idx++), w);
            }
        }
        return info;
    }

    InformationSet writeLegacy(RegressionSpec spec, TsDomain context, boolean verbose) {
        if (!spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        TradingDaysSpecMapping.writeLegacy(info, spec.getTradingDays(), verbose);
        EasterSpecMapping.writeLegacy(info, spec.getEaster(), verbose);
        List<Variable<IOutlier>> voutliers = spec.getOutliers();
        if (!voutliers.isEmpty()) {
            String[] outliers = new String[voutliers.size()];
            for (int i = 0; i < outliers.length; ++i) {
                Variable<IOutlier> v = voutliers.get(i);
                outliers[i] = OutlierMapping.format(v.getCore());
                Parameter p = v.getCoefficient(0);
                set(info, OutlierMapping.name(v.getCore(), context), p);
            }
            info.set(OUTLIERS_LEGACY, outliers);
        }
        List<Variable<Ramp>> vramps = spec.getRamps();
        if (!vramps.isEmpty()) {
            String[] ramps = new String[vramps.size()];
            for (int i = 0; i < ramps.length; ++i) {
                Variable<Ramp> v = vramps.get(i);
                ramps[i] = RampMapping.format(v.getCore());
                Parameter p = v.getCoefficient(0);
                set(info, ramps[i], p);
            }
            info.set(RAMPS_LEGACY, ramps);
        }
        List<Variable<TsContextVariable>> vusers = spec.getUserDefinedVariables();
        if (!vusers.isEmpty()) {
            int idx = 1;
            for (Variable<TsContextVariable> v : vusers) {
                InformationSet cur = TsContextVariableMapping.writeLegacy(v, verbose);
                if (cur != null) {
                    info.set(USER + Integer.toString(idx++), cur);
                    Parameter p = v.getCoefficient(0);
                    set(info, v.getName(), p);
                }
            }
        }
        List<Variable<InterventionVariable>> viv = spec.getInterventionVariables();
        if (!viv.isEmpty()) {
            int idx = 1;
            for (Variable<InterventionVariable> v : viv) {
                InformationSet cur = InterventionVariableMapping.writeLegacy(v, verbose);
                if (cur != null) {
                    info.set(INTERVENTION + Integer.toString(idx++), cur);
                    Parameter p = v.getCoefficient(0);
                    set(info, v.getName(), p);
                }
            }
        }
        return info;
    }

    private Map<String, String> attributes(IOutlier o) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(o).name());
        return attributes;
    }

    private Map<String, String> attributes(InterventionVariable iv) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(iv).name());
        return attributes;
    }
}
