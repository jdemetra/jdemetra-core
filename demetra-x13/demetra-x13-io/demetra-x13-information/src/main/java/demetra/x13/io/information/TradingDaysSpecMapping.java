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
import demetra.information.InformationSet;
import demetra.regarima.RegressionTestSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.x13.X13Exception;
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

    static final String LEGACY_LP = "lp";
    static final String LEGACY_WD = "td";
    static final String LEGACY_TD = "td#6";
    static final String LEGACY_STOCK = LEGACY_TD;

    static final String legacyUserTradingDaysName(String username) {
        username = username.replace('.', '@');
        if (!username.startsWith("td|")) {
            username = "td|" + username;
        }
        return username;
    }

    static final String[] legacyUserTradingDaysNames(String[] users) {
        if (users == null) {
            return null;
        }
        String[] usersc = new String[users.length];
        for (int i = 0; i < users.length; ++i) {
            usersc[i] = legacyUserTradingDaysName(users[i]);
        }
        return usersc;
    }

    static final String cleanTradingDaysName(String username) {
        if (username.startsWith("td|")) {
            username = username.substring(3);
        }
        return username;
        //return username.replace('@', '.');
    }

    static final String[] cleanTradingDaysNames(String[] users) {
        if (users == null) {
            return null;
        }
        String[] usersc = new String[users.length];
        for (int i = 0; i < users.length; ++i) {
            usersc[i] = cleanTradingDaysName(users[i]);
        }
        return usersc;
    }

    void writeLegacy(InformationSet regInfo, TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return;
        }
        InformationSet tdInfo = regInfo.subSet(RegressionSpecMapping.TD);
        writeProperties(tdInfo, spec, verbose, false);

        Parameter lcoef = spec.getLpCoefficient();
        RegressionSpecMapping.set(regInfo, LEGACY_LP, lcoef);
        Parameter[] tcoef = spec.getTdCoefficients();
        if (tcoef != null) {
            if (spec.isUserDefined()) {
                String[] uv = spec.getUserVariables();
                for (int i = 0; i < uv.length; ++i) {
                    RegressionSpecMapping.set(regInfo, legacyUserTradingDaysName(uv[i]), tcoef[i]);
                }
            }
            if (tcoef.length == 1) {
                RegressionSpecMapping.set(regInfo, LEGACY_WD, tcoef[0]);
            } else {
                RegressionSpecMapping.set(regInfo, LEGACY_TD, tcoef);
            }
        }
    }

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
        if (verbose || spec.getTradingDaysType() != TradingDaysType.NONE) {
            if (v3) {
                tdInfo.add(TDOPTION, spec.getTradingDaysType().name());
            } else {
                tdInfo.add(TDOPTION, tdToString(spec.getTradingDaysType()));
            }
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
        TradingDaysType tdtype = TradingDaysType.NONE;
        LengthOfPeriodType lptype = LengthOfPeriodType.None;
        String td = tdInfo.get(TDOPTION, String.class);
        Parameter lcoef;
        Parameter[] tdcoef;
        if (td != null) {
            tdtype = tdOf(td);
        }
        String lp = tdInfo.get(LPOPTION, String.class);
        if (lp != null) {
            lptype = LengthOfPeriodType.valueOf(lp);
            lcoef = RegressionSpecMapping.coefficientOf(regInfo, LEGACY_LP);
        } else {
            lcoef = null;
        }
        boolean auto = true;
        Boolean adj = tdInfo.get(ADJUST, Boolean.class);
        if (adj != null) {
            auto = adj;
        }
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] users = tdInfo.get(USER, String[].class);
        Integer w = tdInfo.get(W, Integer.class);
        RegressionTestSpec rtest = RegressionTestSpec.None;
        String test = tdInfo.get(TEST, String.class);
        if (test != null) {
            rtest = RegressionTestSpec.valueOf(test);
        }

        if (users != null) {
            // clean users names from "td|"
            users = cleanTradingDaysNames(users);
            tdcoef = new Parameter[users.length];
            boolean ok = false;
            for (int j = 0; j < users.length; ++j) {
                Parameter p = RegressionSpecMapping.coefficientOf(regInfo, legacyUserTradingDaysName(users[j]));
                if (p != null) {
                    ok = true;
                    tdcoef[j] = p;
                }
            }
            if (!ok) {
                tdcoef = null;
            }

            if (tdcoef != null) {
                return TradingDaysSpec.userDefined(users, tdcoef);
            } else {
                return TradingDaysSpec.userDefined(users, rtest);
            }
        } else if (w != null && w != 0) {
            tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, LEGACY_TD);
            if (tdcoef != null) {
                return TradingDaysSpec.stockTradingDays(w, tdcoef);
            } else {
                return TradingDaysSpec.stockTradingDays(w, rtest);
            }
        } else if (tdtype == TradingDaysType.NONE && lptype == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        }
        if (tdtype == TradingDaysType.TD2) {
            Parameter wdcoef = RegressionSpecMapping.coefficientOf(regInfo, LEGACY_WD);
            tdcoef = wdcoef == null ? null : new Parameter[]{wdcoef};
        } else {
            tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, LEGACY_TD);
        }
        if (holidays != null) {
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

    TradingDaysSpec read(InformationSet tdInfo) {
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = tdInfo.get(LPCOEF, Parameter.class);
        Parameter[] tdcoef = tdInfo.get(TDCOEF, Parameter[].class);

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
        boolean auto = true;
        Boolean adj = tdInfo.get(ADJUST, Boolean.class);
        if (adj != null) {
            auto = adj;
        }
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] users = tdInfo.get(USER, String[].class);
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
        } else if (tdtype == TradingDaysType.NONE && lptype == LengthOfPeriodType.None) {
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

    private TradingDaysType tdOf(String str) {
        return switch (str) {
            case "TradingDays" ->
                TradingDaysType.TD7;
            case "WorkingDays" ->
                TradingDaysType.TD2;
            default ->
                TradingDaysType.NONE;
        };
    }

    private String tdToString(TradingDaysType type) {
        return switch (type) {
            case TD7 ->
                "TradingDays";
            case TD2 ->
                "WorkingDays";
            case NONE ->
                "None";
            default ->
                throw new X13Exception("Illegal conversion");
        };
    }

}
