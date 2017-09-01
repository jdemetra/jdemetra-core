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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.calendar.GenericTradingDays;
import java.time.Period;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysVariables implements ITradingDaysVariable {

    private final GenericTradingDays td;
    private final String name;

    public GenericTradingDaysVariables(GenericTradingDays td) {
        this.td = td;
        this.name = ITradingDaysVariable.name(td.getCount());
    }

    public GenericTradingDaysVariables(GenericTradingDays td, String name) {
        this.td = td;
        this.name = name;
    }

    @Override
    public void data(RegularDomain domain, List<DataBlock> data) {
        td.data(domain, data);
    }

    @Override
    public String getDescription(RegularDomain context) {
        return td.toString();
    }

    @Override
    public int getDim() {
        return td.getCount();
    }

    @Override
    public String getItemDescription(int idx, RegularDomain context) {
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
    public ITsVariable<RegularDomain> rename(String nname) {
        return new GenericTradingDaysVariables(td, nname);
    }

}
