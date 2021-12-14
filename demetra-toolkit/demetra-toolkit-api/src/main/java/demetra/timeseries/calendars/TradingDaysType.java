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
package demetra.timeseries.calendars;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public enum TradingDaysType {
    /**
     * No regression variable
     */
    NONE(0),
    /**
     * 7 variables (6 in contrasts)
     */
    TD7(7),
    /**
     * Mon-Thu, Fri, Sat, Sun
     */
    TD4(4),
    /**
     * Mon-Thu, Fri-Sat, Sun
     */
    TD3c(3),
    /**
     * Mon-Fri, Sat, Sun
     */
    TD3(3),
    /**
     * Mon-Fri, Sat-Sun
     */
    TD2(2);
    //
    final int variablesCount;

    private TradingDaysType(int variablesCount) {
        this.variablesCount = variablesCount;
    }

    public int getVariablesCount() {
        return variablesCount;
    }
}
