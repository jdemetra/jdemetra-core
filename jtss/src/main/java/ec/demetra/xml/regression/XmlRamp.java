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

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.Ramp;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlRamp.RNAME)
@XmlType(name = XmlRamp.NAME)
public class XmlRamp extends XmlVariable implements IXmlConverter<Ramp>{

    static final String RNAME = "ramp", NAME = RNAME + "Type";

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day start;

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day end;

    public XmlRamp() {
    }

    @ServiceProvider(service = ITsVariableAdapter.class)
    public static class Adapter implements ITsVariableAdapter<XmlRamp, Ramp> {

        @Override
        public Class<Ramp> getValueType() {
            return Ramp.class;
        }

        @Override
        public Class<XmlRamp> getXmlType() {
            return XmlRamp.class;
        }

        @Override
        public Ramp decode(XmlRamp v) throws Exception {
            Ramp o = new Ramp(v.start, v.end);
            return o;
        }

        @Override
        public XmlRamp encode(Ramp v) throws Exception {
            XmlRamp xml = new XmlRamp();
            xml.start = v.getStart();
            xml.end = v.getEnd();
            return xml;
        }
    }
    
    @Override
    public Ramp create() {
        return new Ramp(start, end);
    }

    @Override
    public void copy(Ramp t) {
        start =t.getStart();
        end = t.getEnd();
    }
}
