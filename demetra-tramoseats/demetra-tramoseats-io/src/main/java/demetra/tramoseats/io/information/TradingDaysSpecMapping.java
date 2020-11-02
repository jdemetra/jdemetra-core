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
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.tramo.RegressionTestType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.tramo.TradingDaysSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysSpecMapping {

    public final String AUTO = "auto", MAUTO = "mauto", PFTD = "pftd", TDOPTION = "option", LPOPTION = "leapyear", HOLIDAYS = "holidays", USER = "user", TEST = "test", TESTTYPE = "testtype", W = "stocktd";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, AUTO), Boolean.class);
        dic.put(InformationSet.item(prefix, MAUTO), String.class);
        dic.put(InformationSet.item(prefix, PFTD), Double.class);
        dic.put(InformationSet.item(prefix, TDOPTION), String.class);
        dic.put(InformationSet.item(prefix, LPOPTION), String.class);
        dic.put(InformationSet.item(prefix, USER), String[].class);
        dic.put(InformationSet.item(prefix, HOLIDAYS), String.class);
        dic.put(InformationSet.item(prefix, W), Integer.class);
        dic.put(InformationSet.item(prefix, TESTTYPE), String.class);
    }

    public InformationSet write(TradingDaysSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.isAutomatic()) {
            info.add(MAUTO, spec.getAutomaticMethod().name());
        }
        if (verbose || spec.getProbabilityForFTest() != TradingDaysSpec.DEF_PFTD) {
            info.add(PFTD, spec.getProbabilityForFTest());
        }
        if (verbose || spec.getTradingDaysType() != TradingDaysType.None) {
            info.add(TDOPTION, spec.getTradingDaysType().name());
        }
        if (verbose || spec.getLengthOfPeriodType() != LengthOfPeriodType.None) {
            info.add(LPOPTION, spec.getLengthOfPeriodType().name());
        }
        if (spec.isHolidays()) {
            info.add(HOLIDAYS, spec.getHolidays());
        }
        if (spec.isUserDefined()) {
            info.add(USER, spec.getUserVariables());
        }
        if (verbose || spec.isStockTradingDays()) {
            info.add(W, spec.getStockTradingDays());
        }
        if (verbose || spec.isTest()) {
            info.add(TESTTYPE, spec.getRegressionTestType().name());
        }
        return info;
    }

    public TradingDaysSpec read(InformationSet info) {
        if (info == null) {
            return TradingDaysSpec.none();
        }
        Boolean auto = info.get(AUTO, Boolean.class);
        String mauto = info.get(MAUTO, String.class);
        Double pftd = info.get(PFTD, Double.class);
        String td = info.get(TDOPTION, String.class);
        Boolean lp = info.get(LPOPTION, Boolean.class);
        String lpt = info.get(LPOPTION, String.class);
        String holidays = info.get(HOLIDAYS, String.class);
        String[] user = info.get(USER, String[].class);
        Integer w = info.get(W, Integer.class);
        Boolean test = info.get(TEST, Boolean.class);
        String testtype = info.get(TESTTYPE, String.class);

        TradingDaysType tdo = td == null ? TradingDaysType.None : TradingDaysType.valueOf(td);
        LengthOfPeriodType lpo = lp == null ? LengthOfPeriodType.None : LengthOfPeriodType.LeapYear;
        if (lpt != null) {
            lpo = LengthOfPeriodType.valueOf(lpt);
        }
        if ((auto != null && auto) || mauto != null) {
            TradingDaysSpec.AutoMethod method = mauto == null ? TradingDaysSpec.AutoMethod.FTest : TradingDaysSpec.AutoMethod.valueOf(mauto);
            if (holidays != null) {
                return TradingDaysSpec.automaticHolidays(holidays, method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
            } else {
                return TradingDaysSpec.automatic(method, pftd == null ? TradingDaysSpec.DEF_PFTD : pftd);
            }
        }
        RegressionTestType reg = test != null ? RegressionTestType.Separate_T : RegressionTestType.None;
        if (testtype != null) {
            reg = RegressionTestType.valueOf(testtype);
        }
        if (user != null) {
            return TradingDaysSpec.userDefined(user, reg);
        } else if (w != null) {
            return TradingDaysSpec.stockTradingDays(w, reg);
        } else if (tdo == TradingDaysType.None && lpo == LengthOfPeriodType.None) {
            return TradingDaysSpec.none();
        } else {
            return TradingDaysSpec.td(tdo, lpo, reg);
        }
    }

}
