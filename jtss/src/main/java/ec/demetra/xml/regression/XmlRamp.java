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
package ec.demetra.xml.regression;

import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.Ramp;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *                 Ramp variable.
 *                 A ramp is defined by two days: the first day is the starting point of the ramp and the last day is the end point of the ramp.
 *                 The variable is -1 before the starting day (included), 0 after the ending day (included) and increases linearly between the two days.
 *             
 * 
 * <p>Java class for RampType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RampType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}RegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}TimeSpan"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RampType", propOrder = {
    "from",
    "to"
})
public class XmlRamp
    extends XmlRegressionVariable
{

    @XmlElement(name = "From")
    @XmlSchemaType(name = "date")
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    protected Day from;
    @XmlElement(name = "To")
    @XmlSchemaType(name = "date")
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    protected Day to;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Day getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFrom(Day value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Day getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTo(Day value) {
        this.to = value;
    }



    public XmlRamp() {
    }

    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlRamp, Ramp> {

        @Override
        public Class<Ramp> getImplementationType() {
            return Ramp.class;
        }

        @Override
        public Class<XmlRamp> getXmlType() {
            return XmlRamp.class;
        }

        @Override
        public Ramp unmarshal(XmlRamp v) {
            Ramp o = new Ramp(v.from, v.to);
            return o;
        }

        @Override
        public XmlRamp marshal(Ramp v) {
            XmlRamp xml = new XmlRamp();
            xml.from = v.getStart();
            xml.to = v.getEnd();
            return xml;
        }
    }
    
  
    private static final Adapter ADAPTER=new Adapter();
    public static final Adapter getAdapter(){
        return ADAPTER;
    }

}
