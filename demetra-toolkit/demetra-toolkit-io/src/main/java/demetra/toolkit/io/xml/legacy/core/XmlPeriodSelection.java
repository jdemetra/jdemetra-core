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

import demetra.timeseries.TimeSelector;
import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import demetra.toolkit.io.xml.legacy.XmlEmptyElement;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * A period selection is a flexible way for selecting a part in a time domain.
 *
 * The selection may be defined through start/end dates (FROM, TO, BETWEEN). In
 * that case only the periods that are completely after and/or before the given
 * dates are selected; only the date part is considered. For instance, if
 * start=2 January, the first monthly period is February; if end = 31 March, the
 * last monthly period is March.
 *
 * The period selection may also refer to the numbers of periods that should be
 * selected (FIRST / LAST) or excluded (EXCLUDING) in the selection.
 *
 *
 * <p>
 * Java class for PeriodSelectionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="PeriodSelectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="All" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *         &lt;element name="None" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}TimeSpan"/&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}Range"/&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}Exclusion"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeriodSelectionType", propOrder = {
    "all",
    "none",
    "from",
    "to",
    "first",
    "last",
    "excludeFirst",
    "excludeLast"
})
public class XmlPeriodSelection {

    @XmlElement(name = "All")
    protected XmlEmptyElement all;
    @XmlElement(name = "None")
    protected XmlEmptyElement none;
    @XmlElement(name = "From")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate from;
    @XmlElement(name = "To")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate to;
    @XmlElement(name = "First")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer first;
    @XmlElement(name = "Last")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer last;
    @XmlElement(name = "ExcludeFirst")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer excludeFirst;
    @XmlElement(name = "ExcludeLast")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer excludeLast;

    /**
     * Gets the value of the all property.
     *
     * @return possible object is {@link Object }
     *
     */
    public XmlEmptyElement getAll() {
        return all;
    }

    /**
     * Sets the value of the all property.
     *
     * @param value allowed object is {@link Object }
     *
     */
    public void setAll(XmlEmptyElement value) {
        this.all = value;
    }

    /**
     * Gets the value of the none property.
     *
     * @return possible object is {@link Object }
     *
     */
    public XmlEmptyElement getNone() {
        return none;
    }

    /**
     * Sets the value of the none property.
     *
     * @param value allowed object is {@link Object }
     *
     */
    public void setNone(XmlEmptyElement value) {
        this.none = value;
    }

    /**
     * Gets the value of the from property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setFrom(LocalDate value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public LocalDate getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setTo(LocalDate value) {
        this.to = value;
    }

    /**
     * Gets the value of the first property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getFirst() {
        return first;
    }

    /**
     * Sets the value of the first property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setFirst(Integer value) {
        this.first = value;
    }

    /**
     * Gets the value of the last property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getLast() {
        return last;
    }

    /**
     * Sets the value of the last property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setLast(Integer value) {
        this.last = value;
    }

    /**
     * Gets the value of the excludeFirst property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getExcludeFirst() {
        return excludeFirst;
    }

    /**
     * Sets the value of the excludeFirst property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setExcludeFirst(Integer value) {
        this.excludeFirst = value;
    }

    /**
     * Gets the value of the excludeLast property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getExcludeLast() {
        return excludeLast;
    }

    /**
     * Sets the value of the excludeLast property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setExcludeLast(Integer value) {
        this.excludeLast = value;
    }

    private void clear() {
        all = null;
        none = null;
        from = null;
        to = null;
        first = null;
        last = null;
        excludeFirst = null;
        excludeLast = null;
    }

    public static class Adapter extends XmlAdapter<XmlPeriodSelection, TimeSelector> {

        @Override
        public TimeSelector unmarshal(XmlPeriodSelection xml) {
            return XmlPeriodSelection.unmarshal(xml);
        }

        @Override
        public XmlPeriodSelection marshal(TimeSelector v) {
            XmlPeriodSelection xml = new XmlPeriodSelection();
            XmlPeriodSelection.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }

    public static XmlPeriodSelection marshal(TimeSelector v) {
        if (v == null || v.getType() == TimeSelector.SelectionType.All) {
            return null;
        }
        XmlPeriodSelection xml = new XmlPeriodSelection();
        marshal(v, xml);
        return xml;
    }

    public static boolean marshal(TimeSelector v, XmlPeriodSelection xml) {
        switch (v.getType()) {
            case All:
                xml.all = new XmlEmptyElement();
                break;
            case None:
                xml.none = new XmlEmptyElement();
                break;
            case Between:
                xml.from = v.getD0().toLocalDate();
                xml.to = v.getD1().toLocalDate();
                break;
            case From:
                xml.from = v.getD0().toLocalDate();
                break;
            case To:
                xml.to = v.getD0().toLocalDate();
                break;
            case First:
                xml.first = v.getN0();
                break;
            case Last:
                xml.last = v.getN1();
                break;
            case Excluding:
                xml.excludeFirst = v.getN0();
                xml.excludeLast = v.getN1();
                break;
        }
        return true;
    }

    public static TimeSelector unmarshal(XmlPeriodSelection xml) {
        if (xml.all != null) {
            return TimeSelector.all();
        } else if (xml.none != null) {
            return TimeSelector.none();
        } else if (xml.from != null && xml.to != null) {
            return TimeSelector.between(xml.from.atStartOfDay(), xml.to.atStartOfDay());
        } else if (xml.from != null) {
            return TimeSelector.from(xml.from.atStartOfDay());
        } else if (xml.to != null) {
            return TimeSelector.from(xml.to.atStartOfDay());
        } else if (xml.first != null) {
            return TimeSelector.first(xml.first);
        } else if (xml.last != null) {
            return TimeSelector.last(xml.last);
        } else {
            return TimeSelector.excluding(xml.excludeFirst == null ? 0 : xml.excludeFirst, xml.excludeLast == null ? 0 : xml.excludeLast);
        }
    }

}
