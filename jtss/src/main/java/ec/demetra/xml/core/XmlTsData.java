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
package ec.demetra.xml.core;

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.IXmlUnmarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Representation of a time series (data only). It is identified by the starting
 * period (frequency, first year, first period) and by its values. Missing
 * values are identified by NaN. A name (or description) may also be provided.
 *
 *
 * <p>
 * Java class for TsDataType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TsDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TimeSeriesType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}StartPeriod"/&gt;
 *         &lt;element name="Values" type="{ec/eurostat/jdemetra/core}Doubles"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TsDataType", propOrder = {
    "frequency",
    "firstYear",
    "firstPeriod",
    "values"
})
@XmlSeeAlso({})
public class XmlTsData
        extends XmlTimeSeries {

    @XmlElement(name = "Frequency")
    @XmlSchemaType(name = "unsignedShort")
    protected int frequency;
    @XmlElement(name = "FirstYear", required = true)
    @XmlSchemaType(name = "gYear")
    protected int firstYear;
    @XmlElement(name = "FirstPeriod")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer firstPeriod;
    @XmlElement(name = "Values")
    @XmlList
    protected double[] values;
    @XmlAttribute(name = "name")
    protected String name;

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

    public double[] getValues() {
        return this.values;
    }

   /**
     * @param values the values to set
     */
    public void setValues(double[] values) {
        this.values = values;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    public static final IXmlUnmarshaller<XmlTsData, TsData> UNMARSHALLER = (XmlTsData xml) -> {
        int firstperiod = xml.firstPeriod != null ? xml.firstPeriod - 1 : 0;

        return new TsData(TsFrequency.valueOf(xml.frequency), xml.firstYear, firstperiod,
                xml.values, false);
    };

    public static final InPlaceXmlMarshaller<XmlTsData, TsData> MARSHALLER = (TsData v, XmlTsData xml) -> {
        TsPeriod start = v.getStart();
        xml.frequency = start.getFrequency().intValue();
        xml.firstYear = start.getYear();
        if (xml.frequency != 1) {
            xml.firstPeriod = start.getPosition() + 1;
        } else {
            xml.firstPeriod = null;
        }
        xml.values = v.internalStorage();
        return true;
    };

     public static class Adapter extends XmlAdapter<XmlTsData, TsData> {

        @Override
        public TsData unmarshal(XmlTsData v) throws Exception {
            return UNMARSHALLER.unmarshal(v);
        }

        @Override
        public XmlTsData marshal(TsData v) throws Exception {
            XmlTsData xml = new XmlTsData();
            MARSHALLER.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlTsData, TsData> getAdapter() {
        return ADAPTER;
    }
}
