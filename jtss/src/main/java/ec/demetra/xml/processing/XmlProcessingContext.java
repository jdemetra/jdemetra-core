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
package ec.demetra.xml.processing;

import ec.demetra.xml.calendars.CalendarAdapters;
import ec.demetra.xml.calendars.XmlCalendar;
import ec.demetra.xml.calendars.XmlCalendars;
import ec.demetra.xml.regression.XmlTsVariables;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ProcessingContextType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ProcessingContextType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variables" type="{ec/eurostat/jdemetra/core}TsVariablesType"/&gt;
 *         &lt;element name="Calendars" type="{ec/eurostat/jdemetra/core}CalendarsType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ProcessingContext")
@XmlType(name = "ProcessingContextType", propOrder = {
    "variables",
    "calendars"
})
public class XmlProcessingContext {

    @XmlElement(name = "Variables", required = true)
    protected XmlProcessingContext.Variables variables;
    @XmlElement(name = "Calendars", required = true)
    protected XmlCalendars calendars;

    /**
     * Gets the value of the variables property.
     *
     * @return possible object is {@link TsVariablesType }
     *
     */
    public XmlProcessingContext.Variables getVariables() {
        return variables;
    }

    /**
     * Sets the value of the variables property.
     *
     * @param value allowed object is {@link TsVariablesType }
     *
     */
    public void setVariables(XmlProcessingContext.Variables value) {
        this.variables = value;
    }

    /**
     * Gets the value of the calendars property.
     *
     * @return possible object is {@link CalendarsType }
     *
     */
    public XmlCalendars getCalendars() {
        return calendars;
    }

    /**
     * Sets the value of the calendars property.
     *
     * @param value allowed object is {@link CalendarsType }
     *
     */
    public void setCalendars(XmlCalendars value) {
        this.calendars = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "group"
    })
    public static class Variables {

        @XmlElement(name = "Group")
        protected List<XmlTsVariables> group = new ArrayList<>();

    }

    public static final InPlaceXmlMarshaller<XmlProcessingContext, ProcessingContext> MARSHALLER = (ProcessingContext v, XmlProcessingContext xml) -> {
        if (!v.getGregorianCalendars().isEmpty()) {
            xml.calendars = new XmlCalendars();
            String[] names = v.getGregorianCalendars().getNames();
            for (int i = 0; i < names.length; ++i) {
                IGregorianCalendarProvider cur = v.getGregorianCalendars().get(names[i]);
                XmlCalendar xcur = CalendarAdapters.getDefault().marshal(cur);
                if (xcur != null) {
                    xcur.setName(names[i]);
                    xml.calendars.getCalendars().add(xcur);
                }
            }
        }
        int count = v.getTsVariableManagers().getCount();
        if (count > 0) {
            xml.variables = new Variables();
            for (String n : v.getTsVariableManagers().getNames()) {
                TsVariables cur = v.getTsVariables(n);
                if (!cur.isEmpty()) {
                    XmlTsVariables xcur = new XmlTsVariables();
                    xcur.setName(n);
                    XmlTsVariables.MARSHALLER.marshal(cur, xcur);
                    xml.variables.group.add(xcur);
                }
            }
        }
        return true;
    };

    public static final InPlaceXmlUnmarshaller<XmlProcessingContext, ProcessingContext> UNMARSHALLER = (XmlProcessingContext xml, ProcessingContext v) -> {
        if (xml.calendars != null) {
            for (XmlCalendar xcal : xml.calendars.getCalendars()) {
                IGregorianCalendarProvider cal = CalendarAdapters.getDefault().unmarshal(xcal, v.getGregorianCalendars());
                v.getGregorianCalendars().set(xcal.getName(), cal);
            }
        }
        if (xml.variables != null) {
            for (XmlTsVariables xcur : xml.variables.group) {
                TsVariables vars = new TsVariables();
                XmlTsVariables.UNMARSHALLER.unmarshal(xcur, vars);
                v.getTsVariableManagers().set(xcur.getName(), vars);
            }
        }
        return true;
    };
}
