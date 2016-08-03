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
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Jean Palate
 */
public abstract class XmlRegressionVariable {

    public static XmlRegressionVariable toXml(ITsVariable var){
        if (var instanceof ITsModifier){
            return XmlModifiableRegressionVariable.toXml((ITsModifier)var);
        }else{
            return TsVariableAdapters.getDefault().encode(var);
        }
    }
    
    public static List<Class> xmlClasses(){
        List<Class> xmlclvar = TsVariableAdapters.getDefault().getXmlClasses();
        List<Class> xmlclmod = TsModifierAdapters.getDefault().getXmlClasses();
        xmlclvar.addAll(xmlclmod);
        return xmlclvar;
        
    }
}
