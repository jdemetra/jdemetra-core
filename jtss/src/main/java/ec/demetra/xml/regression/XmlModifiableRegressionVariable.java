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
package ec.demetra.xml.regression;

import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.regression.ITsModifier;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Jean Palate
 */
public abstract class XmlModifiableRegressionVariable extends XmlRegressionVariable{
    @XmlElement(name="Modifier")
    public List<XmlRegressionVariableModifier> modifiers=new ArrayList<>();
    
    public static XmlModifiableRegressionVariable toXml(ITsModifier var){
        ITsVariable cur=var;
        List<XmlRegressionVariableModifier> mod=new ArrayList<>();
        while (cur instanceof ITsModifier){
            ITsModifier m=(ITsModifier) cur;
            mod.add(TsModifierAdapters.getDefault().encode(m));
            cur=m.getVariable();
        }
        XmlRegressionVariable xml=TsVariableAdapters.getDefault().encode(cur);
        if (! (xml instanceof XmlModifiableRegressionVariable))
            return null;
        XmlModifiableRegressionVariable mxml=(XmlModifiableRegressionVariable) xml;
        mxml.modifiers.addAll(mod);
        return mxml;
    }

    @Override
    public ITsVariable toTsVariable(){
        ITsVariable var=TsVariableAdapters.getDefault().decode(this);
        for (int i=modifiers.size()-1; i>=0; --i){
            XmlRegressionVariableModifier m =modifiers.get(i);
            ITsModifier tsm = TsModifierAdapters.getDefault().decode(m);
            tsm.setVariable(var);
            var=tsm;
        }
        return var;
    }
}
