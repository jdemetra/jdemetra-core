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
import demetra.information.InformationSet;
import demetra.modelling.io.information.InterventionVariableMapping;
import demetra.modelling.io.information.OutlierDefinition;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.Variable;
import demetra.tramo.CalendarSpec;
import demetra.tramo.RegressionSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class RegressionSpecMapping {

    final String CALENDAR = "calendar",
            OUTLIERS = "outliers",
            USER = "user", USERS = "user*", RAMPS = "ramps",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*",
            COEFF = "coefficients", FCOEFF = "fixedcoefficients";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OUTLIERS), String[].class);
        dic.put(InformationSet.item(prefix, RAMPS), String[].class);
        CalendarSpecMapping.fillDictionary(InformationSet.item(prefix, CALENDAR), dic);
        InterventionVariableMapping.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
//        TsContextVariableMapping.fillDictionary(InformationSet.item(prefix, USERS), dic);
    }

    Parameter coefficientOf(InformationSet regInfo, String name) {
        InformationSet scoefs = regInfo.getSubSet(RegressionSpecMapping.COEFF);
        if (scoefs != null) {
            double[] coef = scoefs.get(name, double[].class);
            if (coef != null) {
                if (coef.length == 1) {
                    return Parameter.estimated(coef[0]);
                } else {
                    return null;
                }
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

    void add(InformationSet regInfo, String name, Parameter p) {
        if (p == null) {
            return;
        }
        InformationSet scoefs = regInfo.subSet(p.isFixed() ? FCOEFF
                : COEFF);
        scoefs.add(name, new double[]{p.getValue()});
    }

    void add(InformationSet regInfo, String name, Parameter[] p) {
        if (p == null) {
            return;
        }
        // TODO Split in case of partially fixed parameters
        InformationSet scoefs = regInfo.subSet(Parameter.hasFixedParameters(p) ? FCOEFF
                : COEFF);
        scoefs.add(name, Parameter.values(p));
    }
    
    void read(InformationSet regInfo, RegressionSpec.Builder builder){
        
        CalendarSpec cspec = CalendarSpecMapping.read(regInfo);
        builder.calendar(cspec);
        // LEGACY
        String[] outliers = regInfo.get(OUTLIERS, String[].class);
        if (outliers != null) {
            for (int i = 0; i < outliers.length; ++i) {
                OutlierDefinition o = OutlierDefinition.fromString(outliers[i]);
                if (o != null) {
                    Parameter c = RegressionSpecMapping.coefficientOf(regInfo, outliers[i]);
                    builder.outlier(Variable.variable(outliers[i], outlier(o)).withCoefficient(c));
                } 
            }
        }
    }
    
    IOutlier outlier(OutlierDefinition def){
        switch (def.getCode()){
            case "AO":
            case "ao":
                return new AdditiveOutlier(def.getPosition().atStartOfDay());
            case "LS":
            case "ls":
                return new LevelShift(def.getPosition().atStartOfDay(), true);
            case "TC":
            case "tc":
                return new TransitoryChange(def.getPosition().atStartOfDay(), .7);
            case "SO":
            case "s0":
                return new PeriodicOutlier(def.getPosition().atStartOfDay(), 0, true);
            default:
                return null;
        }
    }

//    public InformationSet write(RegressionSpec spec, boolean verbose) {
//        if (!isUsed()) {
//            return null;
//        }
//        InformationSet specInfo = new InformationSet();
//        if (verbose || !calendar_.isDefault()) {
//            InformationSet cinfo = calendar_.write(verbose);
//            if (cinfo != null) {
//                specInfo.add(CALENDAR, cinfo);
//            }
//        }
//        if (!outliers_.isEmpty()) {
//            String[] outliers = new String[outliers_.size()];
//            for (int i = 0; i < outliers.length; ++i) {
//                outliers[i] = outliers_.get(i).toString();
//            }
//            specInfo.add(OUTLIERS, outliers);
//        }
//        if (!ramps_.isEmpty()) {
//            String[] ramps = new String[ramps_.size()];
//            for (int i = 0; i < ramps.length; ++i) {
//                ramps[i] = ramps_.get(i).toString();
//            }
//            specInfo.add(RAMPS, ramps);
//        }
//        int idx = 1;
//        for (TsVariableDescriptor desc : users_) {
//            InformationSet cur = desc.write(verbose);
//            if (cur != null) {
//                specInfo.add(USER + Integer.toString(idx++), cur);
//            }
//        }
//        idx = 1;
//        for (InterventionVariable ivar : interventions_) {
//            InformationSet cur = ivar.write(verbose);
//            if (cur != null) {
//                specInfo.add(INTERVENTION + Integer.toString(idx++), cur);
//            }
//        }
//        if (!fcoeff.isEmpty()) {
//            InformationSet icoeff = specInfo.subSet(FCOEFF);
//            fcoeff.forEach((s, c) -> icoeff.set(s, c.length == 1 ? c[0] : c));
//        }
//        if (!coeff.isEmpty()) {
//            InformationSet icoeff = specInfo.subSet(COEFF);
//            coeff.forEach((s, c) -> icoeff.set(s, c.length == 1 ? c[0] : c));
//        }
//        return specInfo;
//    }
//
//    public RegressionSpec read(InformationSet info) {
//         InformationSet cinfo = info.getSubSet(CALENDAR);
//        if (cinfo != null) {
//            boolean tok = calendar_.read(cinfo);
//            if (!tok) {
//                return false;
//            }
//        }
//        String[] outliers = info.get(OUTLIERS, String[].class);
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                OutlierDefinition o = OutlierDefinition.fromString(outliers[i]);
//                if (o != null) {
//                    outliers_.add(o);
//                } else {
//                    return false;
//                }
//            }
//        }
//        String[] ramps = info.get(RAMPS, String[].class);
//        if (ramps != null) {
//            for (int i = 0; i < ramps.length; ++i) {
//                Ramp r = Ramp.fromString(ramps[i]);
//                if (r != null) {
//                    ramps_.add(r);
//                } else {
//                    return false;
//                }
//            }
//        }
//        List<Information<InformationSet>> usel = info.select(USERS, InformationSet.class);
//        usel.forEach((item) -> {
//            TsVariableDescriptor cur = new TsVariableDescriptor();
//            if (cur.read(item.value)) {
//                users_.add(cur);
//            }
//        });
//        List<Information<InformationSet>> isel = info.select(INTERVENTIONS, InformationSet.class);
//        isel.forEach((item) -> {
//            InterventionVariable cur = new InterventionVariable();
//            if (cur.read(item.value)) {
//                interventions_.add(cur);
//            }
//        });
//        InformationSet ifcoeff = info.getSubSet(FCOEFF);
//        if (ifcoeff != null) {
//            TradingDaysSpec td = calendar_.getTradingDays();
//            List<Information<double[]>> all = ifcoeff.select(double[].class);
//
//            all.forEach((item) -> {
//                //Version 2.2.0 fixed regressors for user defined calendar
//                if (td != null && td.getUserVariables() != null && "td".equals(item.name) && item.value.length == td.getUserVariables().length) {
//                    for (int j = 0; j < item.value.length; j++) {
//                        fcoeff.put(ITsVariable.validName("td|" + td.getUserVariables()[j]), new double[]{item.value[j]});
//                    }
//                } else {
//                    fcoeff.put(item.name, item.value);
//                }
//
//            });
//
//            List<Information<Double>> sall = ifcoeff.select(Double.class);
//            sall.forEach((item) -> {
//                //Version 2.2.0 fixed regressors for user defined calendar
//                if (td != null && td.getUserVariables() != null && "td".equals(item.name) && 1 == td.getUserVariables().length) {
//                    fcoeff.put(ITsVariable.validName("td|" + td.getUserVariables()[0]), new double[]{item.value});
//                } else {
//                    fcoeff.put(item.name, new double[]{item.value});
//                }
//            });
//
//        }
//        InformationSet icoeff = info.getSubSet(COEFF);
//        if (icoeff != null) {
//            List<Information<double[]>> all = icoeff.select(double[].class);
//            all.stream().forEach(reg -> coeff.put(reg.name, reg.value));
//            List<Information<Double>> sall = icoeff.select(Double.class);
//            sall.stream().forEach(reg -> coeff.put(reg.name, new double[]{reg.value}));
//        }
//        return true;
//    }
//
}
