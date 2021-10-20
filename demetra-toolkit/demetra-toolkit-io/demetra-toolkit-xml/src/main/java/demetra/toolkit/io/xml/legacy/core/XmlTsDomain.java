/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.toolkit.io.xml.legacy.core;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Represents a time domain, identified by the starting period and by a number
 * of periods (length). It corresponds to the domain of a time series
 *
 *
 * <p>
 * Java class for TsDomainType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TsDomainType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TimeDomainType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}StartPeriod"/&gt;
 *         &lt;element name="Length" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TsDomainType", propOrder = {
    "frequency",
    "firstYear",
    "firstPeriod",
    "length"
})
public class XmlTsDomain
        extends XmlTimeDomain {

    @XmlElement(name = "Frequency")
    @XmlSchemaType(name = "unsignedShort")
    protected int frequency;
    @XmlElement(name = "FirstYear", required = true)
    @XmlSchemaType(name = "gYear")
    protected int firstYear;
    @XmlElement(name = "FirstPeriod")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer firstPeriod;
    @XmlElement(name = "Length")
    @XmlSchemaType(name = "unsignedInt")
    protected int length;

    /**
     * Gets the value of the frequency property.
     *
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Sets the value of the frequency property.
     *
     */
    public void setFrequency(int value) {
        this.frequency = value;
    }

    /**
     * Gets the value of the firstYear property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public int getFirstYear() {
        return firstYear;
    }

    /**
     * Sets the value of the firstYear property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setFirstYear(int value) {
        this.firstYear = value;
    }

    /**
     * Gets the value of the firstPeriod property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getFirstPeriod() {
        return firstPeriod;
    }

    /**
     * Sets the value of the firstPeriod property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setFirstPeriod(Integer value) {
        this.firstPeriod = value;
    }

    /**
     * Gets the value of the length property.
     *
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     *
     */
    public void setLength(int value) {
        this.length = value;
    }

    public static class Adapter extends XmlAdapter<XmlTsDomain, TsDomain> {

        @Override
        public TsDomain unmarshal(XmlTsDomain x) {
            TsPeriod p = XmlTsPeriod.of(x.frequency, x.firstYear, x.firstPeriod);
            return TsDomain.of(p, x.length);
        }

        @Override
        public XmlTsDomain marshal(TsDomain v) {
            XmlTsDomain x = new XmlTsDomain();
            TsPeriod start = v.getStartPeriod();
            x.frequency = start.getUnit().getAnnualFrequency();
            if (x.frequency != 1) {
                x.firstPeriod = start.annualPosition() + 1;
            } else {
                x.firstPeriod = null;
            }
            x.firstYear = start.year();
            x.length = v.getLength();
            return x;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
