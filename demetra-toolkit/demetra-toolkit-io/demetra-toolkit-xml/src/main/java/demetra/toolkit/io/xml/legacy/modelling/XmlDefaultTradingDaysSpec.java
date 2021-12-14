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
package demetra.toolkit.io.xml.legacy.modelling;

import demetra.DemetraException;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for DefaultTradingDaysSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="DefaultTradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Calendar" type="{http://www.w3.org/2001/XMLSchema}IDREF" minOccurs="0"/&gt;
 *         &lt;element name="TdOption" type="{ec/eurostat/jdemetra/core}TradingDaysEnum"/&gt;
 *         &lt;element name="LpOption" type="{ec/eurostat/jdemetra/core}LengthOfPeriodEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefaultTradingDaysSpecType", propOrder = {
    "calendar",
    "tdOption",
    "lpOption"
})
@XmlSeeAlso({})
public class XmlDefaultTradingDaysSpec {

    @XmlElement(name = "Calendar")
    @XmlSchemaType(name = "NMTOKEN")
    protected String calendar;
    @XmlElement(name = "TdOption", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected String tdOption;
    @XmlElement(name = "LpOption")
    @XmlSchemaType(name = "NMTOKEN")
    protected LengthOfPeriodType lpOption;

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
     * Gets the value of the tdOption property.
     *
     * @return possible object is {@link TradingDaysEnum }
     *
     */
    public TradingDaysType getTdOption() {
        return tdOf(tdOption);
    }

    /**
     * Sets the value of the tdOption property.
     *
     * @param value allowed object is {@link TradingDaysEnum }
     *
     */
    public void setTdOption(TradingDaysType value) {
        this.tdOption = tdToString(value);
    }

    /**
     * Gets the value of the lpOption property.
     *
     * @return possible object is {@link LengthOfPeriodEnum }
     *
     */
    public LengthOfPeriodType getLpOption() {
        return lpOption;
    }

    /**
     * Sets the value of the lpOption property.
     *
     * @param value allowed object is {@link LengthOfPeriodEnum }
     *
     */
    public void setLpOption(LengthOfPeriodType value) {
        if (value != null && value == LengthOfPeriodType.None) {
            this.lpOption = null;
        } else {
            this.lpOption = value;
        }
    }

    private TradingDaysType tdOf(String str){
        if (str == null)
            return TradingDaysType.NONE;
        switch (str){
            case "TradingDays":
                return TradingDaysType.TD7;
            case "WorkingDays":
                return TradingDaysType.TD2;
            default: 
                return TradingDaysType.NONE;
        }
    }

    private String tdToString(TradingDaysType type){
        if (type == null) return "None";
        switch (type){
            case TD7: return "TradingDays";
             case TD2 : return "WorkingDays";
             case NONE: return "None";
             default:
                 throw new DemetraException("Illegal conversion");
        }
    }

}
