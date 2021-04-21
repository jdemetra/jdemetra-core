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
import demetra.data.ParameterType;
import demetra.information.InformationSet;
import demetra.regarima.RegressionTestSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TradingDaysSpecMapping {

    final String TDOPTION = "option", LPOPTION = "leapyear",
            ADJUST = "autoadjust", HOLIDAYS = "holidays", USER = "user",
            TEST = "test", W = "stocktd",
            LPCOEF = "lpcoef", TDCOEF = "tdcoef", CHANGEOFREGIME = "changeofregime";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ADJUST), Boolean.class);
        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
        dic.put(InformationSet.item(prefix, USER), String[].class);
        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
        dic.put(InformationSet.item(prefix, W), Integer.class);
        dic.put(InformationSet.item(prefix, TEST), String.class);
//        dic.put(InformationSet.item(prefix, CHANGEOFREGIME), String.class);
    }

    String lpName() {
        return "lp";
    }

    String tdName() {
        return "td";
    }

    void writeLegacy(InformationSet regInfo, TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return;
        }
        InformationSet tdInfo = regInfo.subSet(RegressionSpecMapping.TD);
        writeProperties(tdInfo, spec, verbose);

        Parameter lcoef = spec.getLpCoefficient();
        RegressionSpecMapping.set(regInfo, lpName(), lcoef);
        Parameter[] tcoef = spec.getTdCoefficients();
        RegressionSpecMapping.set(regInfo, tdName(), tcoef);
    }

    InformationSet write(TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet tdInfo = new InformationSet();

        writeProperties(tdInfo, spec, verbose);

        Parameter lcoef = spec.getLpCoefficient();
        Parameter[] tcoef = spec.getTdCoefficients();
        if (lcoef != null) {
            tdInfo.set(LPCOEF, lcoef);
        }
        if (tcoef != null) {
            tdInfo.set(TDCOEF, tcoef);
        }
        return tdInfo;
    }

    void writeProperties(InformationSet tdInfo, TradingDaysSpec spec, boolean verbose) {
        if (verbose || spec.getTradingDaysType() != TradingDaysType.None) {
            tdInfo.add(TDOPTION, spec.getTradingDaysType().name());
        }
        if (verbose || spec.getLengthOfPeriodType() != LengthOfPeriodType.None) {
            tdInfo.add(LPOPTION, spec.getLengthOfPeriodType().name());
        }
        if (verbose || !spec.isAutoAdjust()) {
            tdInfo.add(ADJUST, spec.isAutoAdjust());
        }
        if (spec.getHolidays() != null) {
            tdInfo.add(HOLIDAYS, spec.getHolidays());
        }
        if (spec.getUserVariables() != null) {
            tdInfo.add(USER, spec.getUserVariables());
        }
        if (verbose || spec.isStockTradingDays()) {
            tdInfo.add(W, spec.getStockTradingDays());
        }
        if (verbose || spec.getRegressionTestType() != RegressionTestSpec.None) {
            tdInfo.add(TEST, spec.getRegressionTestType().name());
        }
    }

    TradingDaysSpec readLegacy(InformationSet regInfo) {
        InformationSet tdInfo = regInfo.getSubSet(RegressionSpecMapping.TD);
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = RegressionSpecMapping.coefficientOf(regInfo, lpName());
        Parameter[] tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, tdName());

        return readProperties(tdInfo, lcoef, tdcoef);
    }

    TradingDaysSpec read(InformationSet tdInfo) {
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = tdInfo.get(LPCOEF, Parameter.class);
        Parameter[] tdcoef = tdInfo.get(TDCOEF, Parameter[].class);

        return readProperties(tdInfo, lcoef, tdcoef);
    }

    TradingDaysSpec readProperties(InformationSet tdInfo, Parameter lcoef, Parameter[] tdcoef) {
        TradingDaysType tdtype = TradingDaysType.None;
        LengthOfPeriodType lptype = LengthOfPeriodType.None;
        String td = tdInfo.get(TDOPTION, String.class);
        if (td != null) {
            tdtype = TradingDaysType.valueOf(td);
        }
        String lp = tdInfo.get(LPOPTION, String.class);
        if (lp != null) {
            lptype = LengthOfPeriodType.valueOf(lp);
        }
        boolean auto = false;
        Boolean adj = tdInfo.get(ADJUST, Boolean.class);
        if (adj != null) {
            auto = adj;
        }
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] users = tdInfo.get(USER, String[].class);
        int ws = 0;
        Integer w = tdInfo.get(W, Integer.class);
        RegressionTestSpec rtest = RegressionTestSpec.None;
        String test = tdInfo.get(TEST, String.class);
        if (test != null) {
            rtest = RegressionTestSpec.valueOf(test);
        }

        if (users != null) {
            if (tdcoef != null) {
                return TradingDaysSpec.userDefined(users, tdcoef);
            } else {
                return TradingDaysSpec.userDefined(users, rtest);
            }
        } else if (w != null && w != 0) {
            if (tdcoef != null) {
                return TradingDaysSpec.stockTradingDays(w, tdcoef);
            } else {
                return TradingDaysSpec.stockTradingDays(w, rtest);
            }
        } else if (tdtype == TradingDaysType.None && lptype == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        } else if (holidays != null) {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.holidays(holidays, tdtype, lptype, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.holidays(holidays, tdtype, lptype, rtest, auto);
            }
        } else {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.td(tdtype, lptype, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.td(tdtype, lptype, rtest, auto);
            }
        }
    }
}
