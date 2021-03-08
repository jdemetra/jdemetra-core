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

import demetra.information.InformationSet;
import demetra.modelling.io.information.InterventionVariableMapping;
import demetra.tramo.RegressionSpec;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionSpecMapping {

    public final String CALENDAR = "calendar",
            OUTLIERS = "outliers",
            USER = "user", USERS = "user*", RAMPS = "ramps",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*",
            COEFF = "coefficients", FCOEFF = "fixedcoefficients";
    
    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OUTLIERS), String[].class);
        dic.put(InformationSet.item(prefix, RAMPS), String[].class);
        CalendarSpecMapping.fillDictionary(InformationSet.item(prefix, CALENDAR), dic);
        InterventionVariableMapping.fillDictionary(InformationSet.item(prefix, INTERVENTIONS), dic);
//        TsContextVariableMapping.fillDictionary(InformationSet.item(prefix, USERS), dic);
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
