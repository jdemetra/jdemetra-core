/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysVariables implements ITradingDaysVariable {

    private final GenericTradingDays td;
    private final String name;

    public GenericTradingDaysVariables(GenericTradingDays td) {
        this.td = td;
        this.name = ITradingDaysVariable.defaultName(td.getCount());
    }

    public GenericTradingDaysVariables(GenericTradingDays td, String name) {
        this.td = td;
        this.name = name;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        td.data(domain, data);
    }

    @Override
    public String getDescription(TsDomain context) {
        return td.toString();
    }

    @Override
    public int getDim() {
        return td.getCount();
    }

    @Override
    public String getItemDescription(int idx, TsDomain context) {
        return td.getDescription(idx);
    }

    public GenericTradingDays getCore() {
        return td;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ITsVariable<TsDomain> rename(String nname) {
        return new GenericTradingDaysVariables(td, nname);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof GenericTradingDaysVariables) {
            GenericTradingDaysVariables x = (GenericTradingDaysVariables) other;
            return x.td.equals(td);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.td);
        return hash;
    }

}
