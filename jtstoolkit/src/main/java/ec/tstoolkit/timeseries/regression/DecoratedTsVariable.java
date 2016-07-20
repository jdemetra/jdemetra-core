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
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class DecoratedTsVariable implements ITsModifier {
    
    private final String name;
    private final ITsVariable variable;

    public DecoratedTsVariable(final ITsVariable var, final String name) {
        variable = var;
        this.name = name;
    }
    
    @Override
    public ITsVariable getVariable() {
        return variable;
    }
    
    @Override
    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        variable.data(domain, data, start);
    }
    
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        variable.data(domain, data);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return variable.getDefinitionDomain();
    }
    
    @Override
    public TsFrequency getDefinitionFrequency() {
        return variable.getDefinitionFrequency();
    }
    
    @Override
    public String getDescription(TsFrequency context) {
        return name;
    }
    
    @Override
    public int getDim() {
        return variable.getDim();
    }
    
    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        if (variable.getDim() == 1) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name).append('[').append(idx + 1).append(']');
        return builder.toString();
    }
    
    @Override
    public boolean isSignificant(TsDomain domain) {
        return variable.isSignificant(domain);
    }
}
