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
    
    private final String name_;
    private final ITsVariable var_;

    public DecoratedTsVariable(final ITsVariable var, final String name) {
        var_ = var;
        name_ = name;
    }
    
    @Override
    public ITsVariable getVariable() {
        return var_;
    }
    
    @Override
    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        var_.data(domain, data, start);
    }
    
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        var_.data(domain, data);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return var_.getDefinitionDomain();
    }
    
    @Override
    public TsFrequency getDefinitionFrequency() {
        return var_.getDefinitionFrequency();
    }
    
    @Override
    public String getDescription() {
        return name_;
    }
    
    @Override
    public int getDim() {
        return var_.getDim();
    }
    
    @Override
    public String getItemDescription(int idx) {
        if (var_.getDim() == 1) {
            return name_;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name_).append('[').append(idx + 1).append(']');
        return builder.toString();
    }
    
    @Override
    public boolean isSignificant(TsDomain domain) {
        return var_.isSignificant(domain);
    }
}
