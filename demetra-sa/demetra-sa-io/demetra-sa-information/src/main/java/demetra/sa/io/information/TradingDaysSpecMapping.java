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
import demetra.information.InformationSet;
import demetra.modelling.regular.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TradingDaysSpecMapping {

    final String MAUTO = "mauto", AUTOPVAL = "autopval", TDOPTION = "option", LPOPTION = "leapyear",
            ADJUST = "autoadjust", HOLIDAYS = "holidays", USER = "user",
            TEST = "test", W = "stocktd",
            LPCOEF = "lpcoef", TDCOEF = "tdcoef";

    InformationSet write(TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet tdInfo = new InformationSet();

        writeProperties(tdInfo, spec, verbose, true);

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

    void writeProperties(InformationSet tdInfo, TradingDaysSpec spec, boolean verbose, boolean v3) {
        if (verbose || spec.isAutomatic()) {
            tdInfo.set(MAUTO, spec.getAutomaticMethod().name());
        }
        if (verbose || spec.getProbabilityForTest() != TradingDaysSpec.DEF_PTD) {
            tdInfo.set(AUTOPVAL, spec.getProbabilityForTest());
        }
        if (verbose || spec.getTradingDaysType() != TradingDaysType.NONE) {
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
        if (verbose || spec.isTest()) {
            tdInfo.add(TEST, spec.isTest());
        }
    }

    TradingDaysSpec read(InformationSet tdInfo) {
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = tdInfo.get(LPCOEF, Parameter.class);
        Parameter[] tdcoef = tdInfo.get(TDCOEF, Parameter[].class);

        String mauto = tdInfo.get(MAUTO, String.class);
        Double pval = tdInfo.get(AUTOPVAL, Double.class);
        TradingDaysType tdtype = TradingDaysType.NONE;
        LengthOfPeriodType lptype = LengthOfPeriodType.None;
        String td = tdInfo.get(TDOPTION, String.class);
        if (td != null) {
            tdtype = TradingDaysType.valueOf(td);
        }
        String lp = tdInfo.get(LPOPTION, String.class);
        if (lp != null) {
            lptype = LengthOfPeriodType.valueOf(lp);
        }
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        boolean adjust = TradingDaysSpec.DEF_ADJUST;
        Boolean adj = tdInfo.get(ADJUST, Boolean.class);
        if (adj != null) {
            adjust = adj;
        }
        TradingDaysSpec.AutoMethod method = TradingDaysSpec.AutoMethod.UNUSED;
        if (mauto != null) {
            method = TradingDaysSpec.AutoMethod.valueOf(mauto);
        }
        if (method != TradingDaysSpec.AutoMethod.UNUSED) {
            if (holidays != null) {
                return TradingDaysSpec.automaticHolidays(holidays, lptype, method,
                        pval == null ? TradingDaysSpec.DEF_PTD : pval, adjust);
            } else {
                return TradingDaysSpec.automatic(lptype, method,
                        pval == null ? TradingDaysSpec.DEF_PTD : pval,
                        adjust);
            }
        }
        String[] users = tdInfo.get(USER, String[].class);
        Integer w = tdInfo.get(W, Integer.class);
        boolean rtest = false;
        Boolean test = tdInfo.get(TEST, Boolean.class);
        if (test != null) {
            rtest = test;
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
        } else if (tdtype == TradingDaysType.NONE && lptype == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        } else if (holidays != null) {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.holidays(holidays, tdtype, lptype, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.holidays(holidays, tdtype, lptype, rtest, adjust);
            }
        } else {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.td(tdtype, lptype, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.td(tdtype, lptype, rtest, adjust);
            }
        }
    }

}
