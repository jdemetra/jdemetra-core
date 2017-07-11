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

import ec.tstoolkit.timeseries.TsException;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractTsModifier implements ITsModifier{
    
    protected ITsVariable var;
    
    protected AbstractTsModifier(ITsVariable var){
        this.var=var;
    }
    
    @Override
    public void setVariable(ITsVariable var){
        if (this.dependsOn(var))
            throw new TsException("Cycle in modifiers");
        this.var=var;
    }
    
    @Override
    public ITsVariable getVariable(){
        return var;
    }
}
