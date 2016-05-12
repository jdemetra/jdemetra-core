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
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class TsVariableGroup implements IUserTsVariable {
    
    private final ITsVariable[] vars_;
    private final String desc_;
    
    public TsVariableGroup(String desc, ITsVariable[] vars ){
        desc_=desc;
        vars_=vars;
    }
    
    public TsVariableGroup(String desc, TsData[] vars ){
        desc_=desc;
        vars_=new ITsVariable[vars.length];
        for (int i=0; i<vars.length; ++i){
            vars_[i]=new TsVariable(vars[i]);
        }
    }
 
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        for (int i=0, n0=0; i<vars_.length; ++i){
            int n1=n0+vars_[i].getDim();
            vars_[i].data(domain, data.subList(n0, n1));
            n0=n1;
        }
    }
    
    @Override
    public TsDomain getDefinitionDomain() {
        TsDomain domain=null;
        for (int i=0; i<vars_.length; ++i){
            TsDomain d=vars_[i].getDefinitionDomain();
            if (d != null){
                if (domain == null)
                    domain=d;
                else
                    domain=d.intersection(domain);
            }
        }
        return domain;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        TsFrequency freq=TsFrequency.Undefined;
        for (int i=0; i<vars_.length; ++i){
            TsFrequency f=vars_[i].getDefinitionFrequency();
            if (f != TsFrequency.Undefined){
                if (freq == TsFrequency.Undefined)
                    freq=f;
                else if (freq != f)
                    throw new TsException(TsException.INCOMPATIBLE_FREQ);
            }
        }
        return freq;
    }

    @Override
    public String getDescription() {
        return desc_ == null ? "" : desc_; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDim() {
        int n=0;
        for (int i=0; i<vars_.length; ++i){
            n+=vars_[i].getDim();
        }
        return n;
    }

    @Override
    public String getItemDescription(int idx) {
        int cur=idx;
        for (int i=0; i<vars_.length; ++i){
            int dim=vars_[i].getDim();
            if (cur < dim)
                return vars_[i].getItemDescription(cur);
            cur-=dim;
        }
        return "";
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        for (int i=0; i<vars_.length; ++i){
            if (! vars_[i].isSignificant(domain))
                return false;
        } 
        return true;
    }
}
