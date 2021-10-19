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
package demetra.x13.io.xml.legacy;

import demetra.regarima.SingleOutlierSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Mats Maggi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleOutlierSpecType", propOrder = {
    "type",
    "criticalValue"
})
public class XmlSingleOutlierSpec {

    @XmlElement(name = "Type", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected String type;
    @XmlElement(name = "CriticalValue")
    protected Double criticalValue;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !type.isEmpty()) {
            this.type = type;
        }
    }

    public Double getCriticalValue() {
        return criticalValue;
    }

    public void setCriticalValue(Double criticalValue) {
        if (criticalValue != null && criticalValue == 0) {
            this.criticalValue = null;
        } else {
            this.criticalValue = criticalValue;
        }
    }

    public static final SingleOutlierSpec unmarshal(XmlSingleOutlierSpec xml){
        SingleOutlierSpec spec=new SingleOutlierSpec(xml.getType(), xml.getCriticalValue());
        if (xml.criticalValue != null) {
            return new SingleOutlierSpec(xml.type, xml.criticalValue);
        }else{
             return new SingleOutlierSpec(xml.type, 0);
        }
    }

    public static final XmlSingleOutlierSpec marshal(SingleOutlierSpec v){
        XmlSingleOutlierSpec xml=new XmlSingleOutlierSpec();
        xml.setCriticalValue(v.getCriticalValue());
        xml.setType(v.getType());
        return xml;
    };
}
