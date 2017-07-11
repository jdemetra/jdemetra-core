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
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EasterSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EasterSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}EasterSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Test" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="option" type="{ec/eurostat/jdemetra/sa/tramoseats}EasterHolidayEnum" default="IncludeEaster" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EasterSpecType", propOrder = {
    "test"
})
public class XmlEasterSpec
        extends ec.demetra.xml.modelling.XmlEasterSpec {

    @XmlElement(name = "Test")
    protected RegressionTestSpec test;

    /**
     * Gets the value of the test property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public RegressionTestSpec getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setTest(RegressionTestSpec value) {
        if (value == RegressionTestSpec.None) {
            this.test = null;
        } else {
            this.test = value;
        }
    }

    public static final InPlaceXmlMarshaller<XmlEasterSpec, MovingHolidaySpec> MARSHALLER = (MovingHolidaySpec v, XmlEasterSpec xml) -> {
        if (v.getW() != MovingHolidaySpec.DEF_EASTERDUR) {
            xml.setDuration(v.getW());
        }
        xml.setJulian(v.getType() == MovingHolidaySpec.Type.JulianEaster);
        xml.setTest(v.getTest());
        return true;
    };

    public static final InPlaceXmlUnmarshaller<XmlEasterSpec, MovingHolidaySpec> UNMARSHALLER = (XmlEasterSpec xml, MovingHolidaySpec v) -> {
        if (xml.julian != null) {
            v.setType(xml.julian ? MovingHolidaySpec.Type.JulianEaster: MovingHolidaySpec.Type.Easter);
        }else
            v.setType(MovingHolidaySpec.Type.Easter);
        if (xml.test != null) {
            v.setTest(xml.test);
        }
        if (xml.duration != null) {
            v.setW(xml.duration);
        }
        return true;
    };

}
