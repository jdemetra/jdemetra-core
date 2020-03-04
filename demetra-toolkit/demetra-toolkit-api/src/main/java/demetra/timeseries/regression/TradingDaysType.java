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
package demetra.timeseries.regression;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public enum TradingDaysType {
    // / <summary>
    // / No regression variable
    // / </summary>

    /**
     *
     */
    None(0),
    // / <summary>
    // / X13: tdnolpyear
    // / Tramo: TD6
    // / </summary>
    /**
     *
     */
    TradingDays(6),
    // / <summary>
    // / X13: td1nolpyear
    // / Tramo: TD1
    // / </summary>
    /**
     *
     */
    WorkingDays(1);
    //
    final int variablesCount;

    private TradingDaysType(int variablesCount) {
        this.variablesCount = variablesCount;
    }

    public int getVariablesCount() {
        return variablesCount;
    }
}
