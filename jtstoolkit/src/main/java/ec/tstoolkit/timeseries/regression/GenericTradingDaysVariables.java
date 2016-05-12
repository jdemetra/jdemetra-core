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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysVariables implements ITradingDaysVariable {

    private final GenericTradingDays td;

    public GenericTradingDaysVariables(GenericTradingDays td) {
        this.td = td;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        td.data(domain, data);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Undefined;
    }

    @Override
    public String getDescription() {
        return td.toString();
    }

    @Override
    public int getDim() {
        return td.getCount();
    }

    @Override
    public String getItemDescription(int idx) {
        return td.getDescription(idx);
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return true;
    }
    
    public GenericTradingDays getCore(){
        return td;
    }

}
