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
package demetra.toolkit.io.xml.legacy.processing;

import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.toolkit.io.xml.legacy.calendars.CalendarAdapters;
import demetra.toolkit.io.xml.legacy.calendars.XmlCalendar;
import demetra.toolkit.io.xml.legacy.calendars.XmlCalendars;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
        private List<XmlTsVariables> group = new ArrayList<>();

        /**
         * @return the group
         */
        public List<XmlTsVariables> getGroup() {
            return group;
        }

    }

    public static XmlProcessingContext marshal(ModellingContext v) {
        XmlProcessingContext xml = new XmlProcessingContext();
        if (!v.getCalendars().isEmpty()) {
            xml.calendars = new XmlCalendars();
            String[] names = v.getCalendars().getNames();
            for (int i = 0; i < names.length; ++i) {
                CalendarDefinition cur = v.getCalendars().get(names[i]);
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
                TsDataSuppliers cur = v.getTsVariables(n);
                if (!cur.isEmpty()) {
                    XmlTsVariables xcur = XmlTsVariables.marshal(cur);
                    xcur.setName(n);
                    xml.variables.getGroup().add(xcur);
                }
            }
        }
        return xml;
    }

    public static boolean unmarshal(XmlProcessingContext xml, ModellingContext v) {
        if (xml.calendars != null) {
            for (XmlCalendar xcal : xml.calendars.getCalendars()) {
                CalendarDefinition cal = CalendarAdapters.getDefault().unmarshal(xcal);
                v.getCalendars().set(xcal.getName(), cal);
            }
        }
        if (xml.variables != null) {
            for (XmlTsVariables xcur : xml.variables.getGroup()) {
                TsDataSuppliers vars = XmlTsVariables.unmarshal(xcur);
                v.getTsVariableManagers().set(xcur.getName(), vars);
            }
        }
        return true;
    }
}
