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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.List;

/**
 * Basic class for a single regression variable
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public abstract class AbstractSingleTsVariable implements ITsVariable {

    @Override
    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        data(domain.getStart(), data.get(start));
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        data(domain.getStart(), data.get(0));
    }
    /**
     *
     * @param start
     * @param data
     */
    public abstract void data(TsPeriod start, DataBlock data);

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Undefined;
    }

    @Override
    public int getDim() {
        return 1;
    }

    @Override
    public String getItemDescription(int item) {
        return getDescription();
    }

    @Override
    public String toString() {
        return getDescription();
    }

}
