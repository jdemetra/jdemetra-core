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

import com.google.common.base.Strings;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
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

    public static final InPlaceXmlUnmarshaller<XmlSingleOutlierSpec, SingleOutlierSpec> UNMARSHALLER = (XmlSingleOutlierSpec xml, SingleOutlierSpec v) -> {
        if (xml.criticalValue != null) {
            v.setCriticalValue(xml.criticalValue);
        }
        if (!Strings.isNullOrEmpty(xml.type)) {
            v.setType(OutlierType.valueOf(xml.type));
        }
        return true;
    };

    public static final IXmlMarshaller<XmlSingleOutlierSpec, SingleOutlierSpec> MARSHALLER = (SingleOutlierSpec v) -> {
        XmlSingleOutlierSpec xml=new XmlSingleOutlierSpec();
        xml.setCriticalValue(v.getCriticalValue());
        OutlierType otype = v.getType();
        xml.setType(otype.name());
        return xml;
    };
}
