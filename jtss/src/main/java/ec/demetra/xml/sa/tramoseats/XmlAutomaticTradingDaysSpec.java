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

import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AutomaticTradingDaysSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="AutomaticTradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Calendar" type="{http://www.w3.org/2001/XMLSchema}IDREF" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="WaldTest" type="{ec/eurostat/jdemetra/sa/tramoseats}WaldTradingDaysSelectionType"/&gt;
 *           &lt;element name="FTest" type="{ec/eurostat/jdemetra/sa/tramoseats}FTradingDaysSelectionType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutomaticTradingDaysSpecType", propOrder = {
    "calendar",
    "waldTest",
    "fTest"
})
public class XmlAutomaticTradingDaysSpec {
    
    @XmlElement(name = "Calendar")
    @XmlSchemaType(name = "NMTOKEN")
    protected String calendar;
    @XmlElement(name = "WaldTest")
    protected WaldSelection waldTest;
    @XmlElement(name = "FTest")
    protected FSelection fTest;

    /**
     * Gets the value of the calendar property.
     *
     * @return possible object is {@link Object }
     *
     */
    public String getCalendar() {
        return calendar;
    }

    /**
     * Sets the value of the calendar property.
     *
     * @param value allowed object is {@link Object }
     *
     */
    public void setCalendar(String value) {
        this.calendar = value;
    }

    /**
     * Gets the value of the waldTest property.
     *
     * @return possible object is {@link WaldTradingDaysSelectionType }
     *
     */
    public WaldSelection getWaldTest() {
        return waldTest;
    }

    /**
     * Sets the value of the waldTest property.
     *
     * @param value allowed object is {@link WaldTradingDaysSelectionType }
     *
     */
    public void setWaldTest(WaldSelection value) {
        this.waldTest = value;
    }

    /**
     * Gets the value of the fTest property.
     *
     * @return possible object is {@link FTradingDaysSelectionType }
     *
     */
    public FSelection getFTest() {
        return fTest;
    }

    /**
     * Sets the value of the fTest property.
     *
     * @param value allowed object is {@link FTradingDaysSelectionType }
     *
     */
    public void setFTest(FSelection value) {
        this.fTest = value;
    }

    /**
     * <p>
     * Java class for WaldTradingDaysSelectionType complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType name="WaldTradingDaysSelectionType"&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="ftest" type="{ec/eurostat/jdemetra/core}Probability" default="0.01" /&gt;
     *       &lt;attribute name="dftest" type="{http://www.w3.org/2001/XMLSchema}double" default="1.96" /&gt;
     *       &lt;attribute name="lptest" type="{http://www.w3.org/2001/XMLSchema}double" default="1.96" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "WaldTradingDaysSelectionType")
    public static class WaldSelection {
        
        @XmlAttribute(name = "ftest")
        protected Double ftest;
        @XmlAttribute(name = "dftest")
        protected Double dftest;
        @XmlAttribute(name = "lptest")
        protected Double lptest;

        /**
         * Gets the value of the ftest property.
         *
         * @return possible object is {@link Double }
         *
         */
        public double getFtest() {
            if (ftest == null) {
                return 0.01D;
            } else {
                return ftest;
            }
        }

        /**
         * Sets the value of the ftest property.
         *
         * @param value allowed object is {@link Double }
         *
         */
        public void setFtest(Double value) {
            this.ftest = value;
        }

        /**
         * Gets the value of the dftest property.
         *
         * @return possible object is {@link Double }
         *
         */
        public double getDftest() {
            if (dftest == null) {
                return 1.96D;
            } else {
                return dftest;
            }
        }

        /**
         * Sets the value of the dftest property.
         *
         * @param value allowed object is {@link Double }
         *
         */
        public void setDftest(Double value) {
            this.dftest = value;
        }

        /**
         * Gets the value of the lptest property.
         *
         * @return possible object is {@link Double }
         *
         */
        public double getLptest() {
            if (lptest == null) {
                return 1.96D;
            } else {
                return lptest;
            }
        }

        /**
         * Sets the value of the lptest property.
         *
         * @param value allowed object is {@link Double }
         *
         */
        public void setLptest(Double value) {
            this.lptest = value;
        }
        
    }

    /**
     * <p>
     * Java class for FTradingDaysSelectionType complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType name="FTradingDaysSelectionType"&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="ftest" type="{ec/eurostat/jdemetra/core}Probability" default="0.01" /&gt;
     *       &lt;attribute name="lptest" type="{http://www.w3.org/2001/XMLSchema}double" default="1.96" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "FTradingDaysSelectionType")
    public static class FSelection {
        
        @XmlAttribute(name = "ftest")
        protected Double ftest;
        @XmlAttribute(name = "lptest")
        protected Double lptest;

        /**
         * Gets the value of the ftest property.
         *
         * @return possible object is {@link Double }
         *
         */
        public double getFtest() {
            if (ftest == null) {
                return 0.01D;
            } else {
                return ftest;
            }
        }

        /**
         * Sets the value of the ftest property.
         *
         * @param value allowed object is {@link Double }
         *
         */
        public void setFtest(Double value) {
            this.ftest = value;
        }

        /**
         * Gets the value of the lptest property.
         *
         * @return possible object is {@link Double }
         *
         */
        public double getLptest() {
            if (lptest == null) {
                return 1.96D;
            } else {
                return lptest;
            }
        }

        /**
         * Sets the value of the lptest property.
         *
         * @param value allowed object is {@link Double }
         *
         */
        public void setLptest(Double value) {
            this.lptest = value;
        }
        
    }
    
    public static final InPlaceXmlMarshaller<XmlAutomaticTradingDaysSpec, TradingDaysSpec> MARSHALLER = (TradingDaysSpec v, XmlAutomaticTradingDaysSpec xml) -> {
        if (!v.isAutomatic()) {
            return false;
        }
        if (v.getAutomaticMethod() == TradingDaysSpec.AutoMethod.FTest) {
            FSelection f = new FSelection();
            f.setFtest(v.getProbabibilityForFTest());
            xml.setFTest(f);
            // options not available. For future use
        } else {
            WaldSelection wald = new WaldSelection();
            // options not available. For future use
            xml.setWaldTest(wald);
        }
        xml.setCalendar(v.getHolidays());
        return true;
    };
    
    public static final InPlaceXmlUnmarshaller<XmlAutomaticTradingDaysSpec, TradingDaysSpec> UNMARSHALLER = (XmlAutomaticTradingDaysSpec xml, TradingDaysSpec v) -> {
        if (xml.calendar != null) {
            v.setHolidays(xml.calendar);
        }
        if (xml.fTest != null) {
            v.setAutomaticMethod(TradingDaysSpec.AutoMethod.FTest);
            if (xml.fTest.ftest != null) {
                v.setProbabibilityForFTest(xml.fTest.ftest);
            }
            // options not available. For future use
        } else {
            v.setAutomaticMethod(TradingDaysSpec.AutoMethod.WaldTest);
            // options not available. For future use
        }
        return true;
    };
    
}
