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
package ec.demetra.xml.sa.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
    protected Boolean test;
    @XmlAttribute(name = "option")
    protected EasterSpec.Type option;

    /**
     * Gets the value of the test property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public boolean isTest() {
        return test == null ? false : test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setTest(Boolean value) {
        if (value != null && !value) {
            this.test = null;
        } else {
            this.test = value;
        }
    }

    /**
     * Gets the value of the option property.
     *
     * @return possible object is {@link EasterHolidayEnum }
     *
     */
    public EasterSpec.Type getOption() {
        if (option == null) {
            return EasterSpec.Type.IncludeEaster;
        } else {
            return option;
        }
    }

    /**
     * Sets the value of the option property.
     *
     * @param value allowed object is {@link EasterHolidayEnum }
     *
     */
    public void setOption(EasterSpec.Type value) {
        this.option = value;
    }

    
}
