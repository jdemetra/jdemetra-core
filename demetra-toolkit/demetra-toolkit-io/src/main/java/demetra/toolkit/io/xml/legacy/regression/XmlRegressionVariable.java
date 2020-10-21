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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.regression.ITsModifier;
import demetra.timeseries.regression.ITsVariable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *                 Base type for any regression variable. 
 *             
 * 
 * <p>Java class for RegressionVariableType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegressionVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegressionVariableType")
@XmlSeeAlso({
//    XmlModifiableRegressionVariable.class,
    XmlOutlier.class,
    XmlRamp.class,
    XmlInterventionVariable.class
})
public abstract class XmlRegressionVariable {

    public static XmlRegressionVariable toXml(ITsVariable var){
//        if (var instanceof ITsModifier){
//            return XmlModifiableRegressionVariable.toXml((ITsModifier)var);
//        }else{
            return TsVariableAdapters.getDefault().marshal(var);
//        }
    }
    
    public ITsVariable toTsVariable(){
        return TsVariableAdapters.getDefault().unmarshal(this);
    }
}
