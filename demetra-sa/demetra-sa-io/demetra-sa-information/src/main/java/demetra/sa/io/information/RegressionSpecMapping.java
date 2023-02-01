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
package demetra.sa.io.information;

import demetra.data.Parameter;
import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.modelling.io.information.VariableMapping;
import demetra.modelling.regular.CalendarSpec;
import demetra.modelling.regular.RegressionSpec;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class RegressionSpecMapping {

    final String AICDIFF = "aicdiff",
            MU = "mu", CHECKMU = "checkmu",
            TD = "tradingdays", EASTER = "easter",
            OUTLIER = "outlier", OUTLIERS = "outlier*",
            RAMP = "ramp", RAMPS = "ramp*",
            USER = "user", USERS = "user*",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*",
            COEFF = "coefficients", FCOEFF = "fixedcoefficients";

    RegressionSpec read(InformationSet info) {
        if (info == null) {
            return RegressionSpec.DEFAULT;
        }
        CalendarSpec cal = CalendarSpec.builder()
                .tradingDays(TradingDaysSpecMapping.read(info.getSubSet(TD)))
                .easter(EasterSpecMapping.read(info.getSubSet(EASTER)))
                .build();
        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(info.get(MU, Parameter.class))
                .calendar(cal);
        Double aic = info.get(AICDIFF, Double.class);
        if (aic != null) {
            builder.aicDiff(aic);
        }

        Boolean cmu = info.get(CHECKMU, Boolean.class);
        if (cmu != null) {
            builder.checkMu(cmu);
        }

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

        if (verbose || spec.getAicDiff() != RegressionSpec.DEF_AICCDIFF) {
            info.set(AICDIFF, spec.getAicDiff());
        }
        Parameter mean = spec.getMean();
        if (mean != null) {
            info.set(MU, mean);
        }
        if (verbose || spec.isCheckMu() != RegressionSpec.DEF_CHECKMU) {
            info.set(CHECKMU, spec.isCheckMu());
        }
        InformationSet tdinfo = TradingDaysSpecMapping.write(spec.getCalendar().getTradingDays(), verbose);
        if (tdinfo != null) {
            info.set(TD, tdinfo);
        }
        InformationSet einfo = EasterSpecMapping.write(spec.getCalendar().getEaster(), verbose);
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

}
