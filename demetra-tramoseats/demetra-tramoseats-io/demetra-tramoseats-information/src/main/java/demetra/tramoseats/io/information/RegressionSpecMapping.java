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
package demetra.tramoseats.io.information;

import demetra.data.Parameter;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.modelling.io.information.InterventionVariableMapping;
import demetra.modelling.io.information.OutlierDefinition;
import demetra.modelling.io.information.OutlierMapping;
import demetra.modelling.io.information.RampMapping;
import demetra.modelling.io.information.TsContextVariableMapping;
import demetra.modelling.io.information.VariableMapping;
import demetra.sa.SaVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import demetra.tramo.CalendarSpec;
import demetra.tramo.RegressionSpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class RegressionSpecMapping {

    final String CALENDAR = "calendar",
            OUTLIERS_LEGACY = "outliers", OUTLIER = "outlier", OUTLIERS = "outlier*",
            USER = "user", USERS = "user*", RAMPS_LEGACY = "ramps", RAMP = "ramp", RAMPS = "ramp*",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*",
            COEFF = "coefficients", FCOEFF = "fixedcoefficients",
            MU = "mu";

//    void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, OUTLIERS_LEGACY), String[].class);
//        dic.put(InformationSet.item(prefix, RAMPS_LEGACY), String[].class);
//        CalendarSpecMapping.fillDictionary(InformationSet.item(prefix, CALENDAR), dic);
//        InterventionVariableMapping.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
////        TsContextVariableMapping.fillDictionary(InformationSet.item(prefix, USERS), dic);
//    }
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
            double[] coef = fcoefs.get(name, double[].class);
            if (coef != null) {
                if (coef.length == 1) {
                    return Parameter.fixed(coef[0]);
                } else {
                    return null;
                }
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

    void readLegacy(InformationSet regInfo, RegressionSpec.Builder builder) {

        CalendarSpec cspec = CalendarSpecMapping.readLegacy(regInfo);
        builder.calendar(cspec);
        // LEGACY
        String[] outliers = regInfo.get(OUTLIERS_LEGACY, String[].class);
        if (outliers != null) {
            for (int i = 0; i < outliers.length; ++i) {
                OutlierDefinition o = OutlierDefinition.fromString(outliers[i]);
                if (o != null) {
                    Parameter c = RegressionSpecMapping.coefficientOf(regInfo, outliers[i]);
                    IOutlier io = OutlierMapping.from(o);
                    builder.outlier(Variable.variable(OutlierMapping.name(io), io, attributes(io)).withCoefficient(c));
                }
            }
        }
        String[] ramps = regInfo.get(RAMPS_LEGACY, String[].class);
        if (ramps != null) {
            for (int i = 0; i < ramps.length; ++i) {
                Ramp r = RampMapping.parse(ramps[i]);
                if (r != null) {
                    Parameter c = RegressionSpecMapping.coefficientOf(regInfo, ramps[i]);
                    builder.ramp(Variable.variable(ramps[i], r).withCoefficient(c));
                }
            }
        }
        List<Information<InformationSet>> sel = regInfo.select(INTERVENTIONS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<InterventionVariable> v = InterventionVariableMapping.readLegacy(sub.getValue());
                builder.interventionVariable(v.withCoefficients(coefficientsOf(regInfo, v.getName())));
            }
        }
        sel = regInfo.select(USERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<TsContextVariable> v = TsContextVariableMapping.readLegacy(sub.getValue());
                builder.userDefinedVariable(v.withCoefficients(coefficientsOf(regInfo, v.getName())));
            }
        }
    }

    RegressionSpec read(InformationSet info) {
        if (info == null) {
            return RegressionSpec.DEFAULT;
        }
        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(info.get(MU, Parameter.class))
                .calendar(CalendarSpecMapping.read(info.getSubSet(CALENDAR)));
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

    InformationSet write(RegressionSpec spec, boolean verbose) {
        if (!spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        Parameter mean = spec.getMean();
        if (mean != null) {
            info.set(MU, mean);
        }
        InformationSet cinfo = CalendarSpecMapping.write(spec.getCalendar(), verbose);
        if (cinfo != null) {
            info.set(CALENDAR, cinfo);
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
                info.set(USER + (idx++), w);
            }
        }
        return info;
    }

    InformationSet writeLegacy(RegressionSpec spec, boolean verbose) {
        if (!spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        CalendarSpecMapping.writeLegacy(info, spec.getCalendar(), verbose);
        List<Variable<IOutlier>> voutliers = spec.getOutliers();
        if (!voutliers.isEmpty()) {
            String[] outliers = new String[voutliers.size()];
            for (int i = 0; i < outliers.length; ++i) {
                Variable<IOutlier> v = voutliers.get(i);
                outliers[i] = OutlierMapping.format(v.getCore());
                Parameter p = v.getCoefficient(0);
                set(info, outliers[i], p);
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

}
