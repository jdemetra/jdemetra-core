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
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.tramo.RegressionTestType;
import demetra.tramo.TradingDaysSpec;
import demetra.tramoseats.TramoSeatsException;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TradingDaysSpecMapping {

    final String AUTO = "auto", MAUTO = "mauto", PFTD = "pftd", TDOPTION = "option", LPOPTION = "leapyear", ADJUST = "autoadjust", 
            HOLIDAYS = "holidays", USER = "user", TEST = "test", TESTTYPE = "testtype", W = "stocktd",
            LPCOEF = "lpcoef", TDCOEF = "tdcoef";

//    void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, AUTO), Boolean.class);
//        dic.put(InformationSet.item(prefix, MAUTO), String.class);
//        dic.put(InformationSet.item(prefix, PFTD), Double.class);
//        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
//        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
//        dic.put(InformationSet.item(prefix, USER), String[].class);
//        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
//        dic.put(InformationSet.item(prefix, W), Integer.class);
//        dic.put(InformationSet.item(prefix, TESTTYPE), String.class);
//    }
//
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
        InformationSet cinfo = regInfo.subSet(RegressionSpecMapping.CALENDAR);
        InformationSet tdInfo = cinfo.subSet(CalendarSpecMapping.TD);

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
            } else {
                if (tcoef.length == 1) {
                    RegressionSpecMapping.set(regInfo, LEGACY_WD, tcoef[0]);
                } else {
                    RegressionSpecMapping.set(regInfo, LEGACY_TD, tcoef);
                }
            }
        }
    }
    
    TradingDaysSpec.AutoMethod methodOf(String method){
        method=method.toUpperCase();
        try{
            return TradingDaysSpec.AutoMethod.valueOf(method);
        }
        catch(Exception ex){
            return TradingDaysSpec.AutoMethod.FTEST;
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

        if (verbose || spec.isAutomatic()) {
            tdInfo.set(MAUTO, spec.getAutomaticMethod().name());
        }
        if (verbose || spec.getProbabilityForFTest() != TradingDaysSpec.DEF_PFTD) {
            tdInfo.set(PFTD, spec.getProbabilityForFTest());
        }
        if (verbose || spec.getTradingDaysType() != TradingDaysType.NONE) {
            if (v3) {
                tdInfo.set(TDOPTION, spec.getTradingDaysType().name());
            } else {
                tdInfo.set(TDOPTION, tdToString(spec.getTradingDaysType()));
            }
        }
        if (verbose || spec.getLengthOfPeriodType() != LengthOfPeriodType.None) {
            tdInfo.set(LPOPTION, spec.getLengthOfPeriodType().name());
        }
        if (spec.isHolidays()) {
            tdInfo.set(HOLIDAYS, spec.getHolidays());
        }
        if (spec.isUserDefined()) {
            tdInfo.set(USER, spec.getUserVariables());
        }
        if (verbose || spec.isStockTradingDays()) {
            tdInfo.set(W, spec.getStockTradingDays());
        }
        if (verbose || spec.isTest()) {
            tdInfo.set(TESTTYPE, spec.getRegressionTestType().name());
        }
        if (verbose || spec.isAutoAdjust() != TradingDaysSpec.DEF_ADJUST) {
            tdInfo.add(ADJUST, spec.isAutoAdjust());
        }
    }

    TradingDaysSpec readLegacy(InformationSet regInfo) {
        InformationSet cinfo = regInfo.getSubSet(RegressionSpecMapping.CALENDAR);
        if (cinfo == null) {
            return TradingDaysSpec.none();
        }
        InformationSet tdInfo = cinfo.getSubSet(CalendarSpecMapping.TD);
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Boolean auto = tdInfo.get(AUTO, Boolean.class);
        String mauto = tdInfo.get(MAUTO, String.class);
        Double pftd = tdInfo.get(PFTD, Double.class);
        String td = tdInfo.get(TDOPTION, String.class);
        Boolean lp = tdInfo.get(LPOPTION, Boolean.class);
        String lpt = tdInfo.get(LPOPTION, String.class);
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] users = tdInfo.get(USER, String[].class);
        Integer w = tdInfo.get(W, Integer.class);
        Boolean test = tdInfo.get(TEST, Boolean.class);
        String testtype = tdInfo.get(TESTTYPE, String.class);

        Parameter lcoef;
        Parameter[] tdcoef;
        TradingDaysType tdo = td == null ? TradingDaysType.NONE : tdOf(td);
        LengthOfPeriodType lpo = lp == null ? LengthOfPeriodType.None : LengthOfPeriodType.LeapYear;
        if (lpt != null) {
            lpo = LengthOfPeriodType.valueOf(lpt);
        }
        if (lpo != LengthOfPeriodType.None) {
            lcoef = RegressionSpecMapping.coefficientOf(regInfo, LEGACY_LP);
        } else {
            lcoef = null;
        }
        TradingDaysSpec.AutoMethod method = TradingDaysSpec.AutoMethod.UNUSED;
        if ((auto != null && auto) || mauto != null) {
            method = mauto == null ? TradingDaysSpec.AutoMethod.FTEST : methodOf(mauto);
        }
        if (method != TradingDaysSpec.AutoMethod.UNUSED) {
            if (holidays != null) {
                return TradingDaysSpec.automaticHolidays(holidays, lpo, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd, false);
            } else {
                return TradingDaysSpec.automatic(lpo, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd, false);
            }
        }
        RegressionTestType reg = test != null ? RegressionTestType.Separate_T : RegressionTestType.None;
        if (testtype != null) {
            reg = RegressionTestType.valueOf(testtype);
        }
        if (users != null) {
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
                return TradingDaysSpec.userDefined(users, reg);
            }
        } else if (w != null && w != 0) {
            tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, LEGACY_TD);
            if (tdcoef != null) {
                return TradingDaysSpec.stockTradingDays(w, tdcoef);
            } else {
                return TradingDaysSpec.stockTradingDays(w, reg);
            }
        } else if (tdo == TradingDaysType.NONE){// && lpo == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        }
        if (tdo == TradingDaysType.TD2) {
            Parameter wdcoef = RegressionSpecMapping.coefficientOf(regInfo, LEGACY_WD);
            tdcoef = wdcoef == null ? null : new Parameter[]{wdcoef};
        } else {
            tdcoef = RegressionSpecMapping.coefficientsOf(regInfo, LEGACY_TD);
        }
        if (holidays != null) {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, reg, false);
            }

        } else {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.td(tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.td(tdo, lpo, reg, false);
            }
        }
    }

    TradingDaysSpec read(InformationSet tdInfo) {
        if (tdInfo == null) {
            return TradingDaysSpec.none();
        }
        Parameter lcoef = tdInfo.get(LPCOEF, Parameter.class);
        Parameter[] tdcoef = tdInfo.get(TDCOEF, Parameter[].class);

        Boolean auto = tdInfo.get(AUTO, Boolean.class);
        String mauto = tdInfo.get(MAUTO, String.class);
        Double pftd = tdInfo.get(PFTD, Double.class);
        String td = tdInfo.get(TDOPTION, String.class);
        Boolean lp = tdInfo.get(LPOPTION, Boolean.class);
        String lpt = tdInfo.get(LPOPTION, String.class);
        String holidays = tdInfo.get(HOLIDAYS, String.class);
        String[] user = tdInfo.get(USER, String[].class);
        Integer w = tdInfo.get(W, Integer.class);
        Boolean test = tdInfo.get(TEST, Boolean.class);
        String testtype = tdInfo.get(TESTTYPE, String.class);
        boolean adjust = TradingDaysSpec.DEF_ADJUST;
        Boolean adj = tdInfo.get(ADJUST, Boolean.class);
        if (adj != null) {
            adjust = adj;
        }

        TradingDaysType tdo = td == null ? TradingDaysType.NONE : TradingDaysType.valueOf(td);
        LengthOfPeriodType lpo = lp == null ? LengthOfPeriodType.None : LengthOfPeriodType.LeapYear;
        if (lpt != null) {
            lpo = LengthOfPeriodType.valueOf(lpt);
        }
        TradingDaysSpec.AutoMethod method = TradingDaysSpec.AutoMethod.UNUSED;
        if ((auto != null && auto) || mauto != null) {
            method = mauto == null ? TradingDaysSpec.AutoMethod.FTEST : methodOf(mauto);
        }
        if (method != TradingDaysSpec.AutoMethod.UNUSED) {
            if (holidays != null) {
                return TradingDaysSpec.automaticHolidays(holidays, lpo, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd, adjust);
            } else {
                return TradingDaysSpec.automatic(lpo, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd, adjust);
            }
        }
        RegressionTestType reg = test != null ? RegressionTestType.Separate_T : RegressionTestType.None;
        if (testtype != null) {
            reg = RegressionTestType.valueOf(testtype);
        }
        if (user != null) {
            if (tdcoef != null) {
                return TradingDaysSpec.userDefined(user, tdcoef);
            } else {
                return TradingDaysSpec.userDefined(user, reg);
            }
        } else if (w != null && w != 0) {
            if (tdcoef != null) {
                return TradingDaysSpec.stockTradingDays(w, tdcoef);
            } else {
                return TradingDaysSpec.stockTradingDays(w, reg);
            }
        } else if (tdo == TradingDaysType.NONE && lpo == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        } else if (holidays != null) {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.holidays(holidays, tdo, lpo, reg, adjust);
            }

        } else {
            if (tdcoef != null || lcoef != null) {
                return TradingDaysSpec.td(tdo, lpo, tdcoef, lcoef);
            } else {
                return TradingDaysSpec.td(tdo, lpo, reg, adjust);
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
        switch (type) {
            case TD7 -> {
                return "TradingDays";
            }
            case TD2 -> {
                return "WorkingDays";
            }
            case NONE -> {
                return "None";
            }
            default ->
                throw new TramoSeatsException("Illegal conversion");
        }
    }

//    TradingDaysSpec readProperties(InformationSet tdInfo, Parameter lcoef, Parameter[] tdcoef, boolean v3) {
//        Boolean auto = tdInfo.get(AUTO, Boolean.class);
//        String mauto = tdInfo.get(MAUTO, String.class);
//        Double pftd = tdInfo.get(PFTD, Double.class);
//        String td = tdInfo.get(TDOPTION, String.class);
//        Boolean lp = tdInfo.get(LPOPTION, Boolean.class);
//        String lpt = tdInfo.get(LPOPTION, String.class);
//        String holidays = tdInfo.get(HOLIDAYS, String.class);
//        String[] user = tdInfo.get(USER, String[].class);
//        Integer w = tdInfo.get(W, Integer.class);
//        Boolean test = tdInfo.get(TEST, Boolean.class);
//        String testtype = tdInfo.get(TESTTYPE, String.class);
//
//        TradingDaysType tdo = td == null ? TradingDaysType.NONE : (v3 ? TradingDaysType.valueOf(td) : tdOf(td));
//        LengthOfPeriodType lpo = lp == null ? LengthOfPeriodType.None : LengthOfPeriodType.LeapYear;
//        if (lpt != null) {
//            lpo = LengthOfPeriodType.valueOf(lpt);
//        }
//        TradingDaysSpec.AutoMethod method = TradingDaysSpec.AutoMethod.Unused;
//        if ((auto != null && auto) || mauto != null) {
//            method = mauto == null ? TradingDaysSpec.AutoMethod.FTest : TradingDaysSpec.AutoMethod.valueOf(mauto);
//        }
//        if (method != TradingDaysSpec.AutoMethod.Unused) {
//            if (holidays != null) {
//                return TradingDaysSpec.automaticHolidays(holidays, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
//            } else {
//                return TradingDaysSpec.automatic(method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
//            }
//        }
//        RegressionTestType reg = test != null ? RegressionTestType.Separate_T : RegressionTestType.None;
//        if (testtype != null) {
//            reg = RegressionTestType.valueOf(testtype);
//        }
//        if (user != null) {
//            if (tdcoef != null) {
//                return TradingDaysSpec.userDefined(user, tdcoef);
//            } else {
//                return TradingDaysSpec.userDefined(user, reg);
//            }
//        } else if (w != null && w != 0) {
//            if (tdcoef != null) {
//                return TradingDaysSpec.stockTradingDays(w, tdcoef);
//            } else {
//                return TradingDaysSpec.stockTradingDays(w, reg);
//            }
//        } else if (tdo == TradingDaysType.NONE && lpo == LengthOfPeriodType.None) {
//            return TradingDaysSpec.none();
//        } else if (holidays != null) {
//            if (tdcoef != null || lcoef != null) {
//                return TradingDaysSpec.holidays(holidays, tdo, lpo, tdcoef, lcoef);
//            } else {
//                return TradingDaysSpec.holidays(holidays, tdo, lpo, reg);
//            }
//
//        } else {
//            if (tdcoef != null || lcoef != null) {
//                return TradingDaysSpec.td(tdo, lpo, tdcoef, lcoef);
//            } else {
//                return TradingDaysSpec.td(tdo, lpo, reg);
//            }
//        }
//    }
}
