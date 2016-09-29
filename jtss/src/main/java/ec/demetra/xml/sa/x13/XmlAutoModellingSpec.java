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
package ec.demetra.xml.sa.x13;

import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoModellingSpecType", propOrder = {
    "lbLimit",
    "armaLimit",
    "ub1",
    "ub2",
    "urFinal",
    "cancel",
    "reducedCV",
    "acceptDefault",
    "mixed",
    "balanced"
})
public class XmlAutoModellingSpec {
    
    @XmlElement(name = "LjungBoxLimit")
    protected Double lbLimit;
    @XmlElement(name = "ArmaLimit")
    protected Double armaLimit;
    @XmlElement(name = "UB1")
    protected Double ub1;
    @XmlElement(name = "UB2")
    protected Double ub2;
    @XmlElement(name = "URFinal", defaultValue = "1.05")
    protected Double urFinal;
    @XmlElement(name = "Cancel", defaultValue = "0.1")
    protected Double cancel;
    @XmlElement(name = "ReducedCV", defaultValue = "0.14286")
    protected Double reducedCV;
    @XmlElement(name = "AcceptDefault")
    protected Boolean acceptDefault;
    @XmlElement(name = "Mixed", defaultValue = "true")
    protected Boolean mixed;
    @XmlElement(name = "Balanced")
    protected Boolean balanced;
    
    public Double getUB1() {
        return ub1;
    }
    
    public void setUB1(Double value) {
        if (value != null && value == AutoModelSpec.DEF_UB1) {
            ub1 = null;
        } else {
            ub1 = value;
        }
    }
    
    public Double getUB2() {
        return ub2;
    }
    
    public void setUB2(Double value) {
        if (value != null && value == AutoModelSpec.DEF_UB2) {
            ub2 = null;
        } else {
            ub2 = value;
        }
    }
    
    public Double getCancel() {
        return cancel;
    }
    
    public void setCancel(Double value) {
        if (value != null && value == AutoModelSpec.DEF_CANCEL) {
            cancel = null;
        } else {
            cancel = value;
        }
    }
    
    public Double getLbLimit() {
        return lbLimit;
    }
    
    public void setLbLimit(Double value) {
        if (value != null && value == AutoModelSpec.DEF_LJUNGBOX)
            this.lbLimit=null;
        this.lbLimit = value;
    }
    
    public Double getArmaLimit() {
        // Not supported
        return armaLimit;
    }
    
    public void setArmaLimit(Double value) {
        // Not supported
        this.armaLimit = value;
    }
    
    public Double getUrFinal() {
        return urFinal;
    }
    
    public void setUrFinal(Double value) {
        if (value != null && value == AutoModelSpec.DEF_UBFINAL) {
            this.urFinal = null;
        } else {
            this.urFinal = value;
        }
    }
    
    public Double getReducedCV() {
        return reducedCV;
    }
    
    public void setReducedCV(Double value) {
        if (value != null && value == AutoModelSpec.DEF_PREDCV) {
            this.reducedCV = null;
        } else {
            this.reducedCV = value;
        }
    }
    
    public Boolean getMixed() {
        return mixed;
    }
    
    public void setMixed(Boolean value) {
        if (value != null && value == AutoModelSpec.DEF_MIXED) {
            this.mixed = null;
        } else {
            this.mixed = value;
        }
    }
    
    public Boolean getBalanced() {
        return balanced;
    }
    
    public void setBalanced(Boolean value) {
        if (value != null && value == AutoModelSpec.DEF_BALANCED) {
            this.balanced = null;
        } else {
            this.balanced = value;
        }
    }

    /**
     * Gets the value of the acceptDefault property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isAcceptDefault() {
        return acceptDefault;
    }

    /**
     * Sets the value of the acceptDefault property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setAcceptDefault(Boolean value) {
        if (value != null && value == AutoModelSpec.DEF_ACCEPTDEF) {
            acceptDefault = null;
        } else {
            this.acceptDefault = value;
        }
    }

    // TODO : Check the filling of objects
    public static final InPlaceXmlMarshaller<XmlAutoModellingSpec, AutoModelSpec> MARSHALLER = (AutoModelSpec v, XmlAutoModellingSpec xml) -> {
        if (v.isDefault()) {
            return true;
        }
        xml.setAcceptDefault(v.isAcceptDefault());
        xml.setCancel(v.getCancelationLimit());
        xml.setLbLimit(v.getLjungBoxLimit());
        xml.setUB1(v.getInitialUnitRootLimit());
        xml.setUB2(v.getFinalUnitRootLimit());
        xml.setUrFinal(v.getUnitRootLimit());
        xml.setMixed(v.isMixed());
        xml.setBalanced(v.isBalanced());
        xml.setReducedCV(v.getPercentReductionCV());
        return true;
    };
    
    public static final InPlaceXmlUnmarshaller<XmlAutoModellingSpec, AutoModelSpec> UNMARSHALLER = (XmlAutoModellingSpec xml, AutoModelSpec v) -> {
        v.setEnabled(true);
        if (xml.acceptDefault != null) {
            v.setAcceptDefault(xml.acceptDefault);
        }
        if (xml.lbLimit != null) {
            v.setLjungBoxLimit(xml.lbLimit);
        }
        if (xml.cancel != null) {
            v.setCancelationLimit(xml.cancel);
        }
        if (xml.ub1 != null) {
            v.setInitialUnitRootLimit(xml.ub1);
        }
        if (xml.ub2 != null) {
            v.setFinalUnitRootLimit(xml.ub2);
        }
        if (xml.mixed != null) {
            v.setMixed(xml.mixed);
        }
        if (xml.balanced != null) {
            v.setBalanced(xml.balanced);
        }
        if (xml.reducedCV != null) {
            v.setPercentReductionCV(xml.reducedCV);
        }
        if (xml.urFinal != null) {
            v.setUnitRootLimit(xml.urFinal);
        }
        
        return true;
    };
}
