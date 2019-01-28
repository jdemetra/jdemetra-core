/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.tramo;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Data
public final class CalendarSpec implements Cloneable {

    public static final String TD = "td", EASTER = "easter";

    @lombok.NonNull
    private TradingDaysSpec tradingDays;

    @lombok.NonNull
    private EasterSpec easter;

    public CalendarSpec() {
        tradingDays = new TradingDaysSpec();
        easter = new EasterSpec();
    }

    @Override
    public CalendarSpec clone() {
        try {
            CalendarSpec c = (CalendarSpec) super.clone();
            c.tradingDays = tradingDays.clone();
            c.easter = easter.clone();
            return c;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean isUsed() {
        return easter.isUsed() || tradingDays.isUsed();
    }

    public boolean isDefault() {
        return easter.isDefault()
                && tradingDays.isDefault();
    }
}
