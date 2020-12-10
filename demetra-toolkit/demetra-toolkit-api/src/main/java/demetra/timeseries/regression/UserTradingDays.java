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

import nbbrd.design.Development;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class UserTradingDays extends TsVariables implements ITradingDaysVariable {

    public static UserTradingDays of(String[] id, ModellingContext context, String[] desc) {
        TsData[] data = data(id, context);
        if (data == null) {
            return null;
        } else {
            return new UserTradingDays(id, data, desc);
        }
    }

    public static UserTradingDays of(String[] id, ModellingContext context) {
        return of(id, context, null);
    }

    public UserTradingDays(String[] id, TsData[] data) {
        super("td", id, data, null);
    }

    public UserTradingDays(String[] id, TsData[] data, String[] desc) {
        super("td", id, data, desc);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserTradingDays) {
            return equals((TsVariables) obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash();
    }
}
