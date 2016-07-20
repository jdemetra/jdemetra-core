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
import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlInterventionVariable.RNAME)
@XmlType(name = XmlInterventionVariable.NAME)
public class XmlInterventionVariable extends XmlVariable implements IXmlConverter<InterventionVariable> {
    static final String RNAME = "intervention", NAME=RNAME+"Type";

    @XmlElement(name="Sequence")
    public XmlSeq[] Sequence;
    @XmlElement
    public Double DeltaFilter;
    public boolean isDeltaFilterSpecified() {
        return DeltaFilter != null;
    }
    @XmlElement
    public Double DeltaSeasonalFilter;
    public boolean isDeltaSFilterSpecified() {
        return DeltaSeasonalFilter != null;
    }

    public XmlInterventionVariable() { }

    @Override
    public void copy(InterventionVariable t) {
        DeltaFilter = t.getDelta();
        DeltaSeasonalFilter = t.getDeltaS();

        int n = t.getCount();
        Sequence = new XmlSeq[n];
        for (int i = 0; i < n; ++i) {
            Sequence[i].Start = StringFormatter.yearMonth(t.getSequence(i).start);
            Sequence[i].End = StringFormatter.yearMonth(t.getSequence(i).end);
        }
    }

    @Override
    public InterventionVariable create() {
        if (Sequence == null)
            return null;
        InterventionVariable ivar = new InterventionVariable();
        if (isDeltaFilterSpecified())
            ivar.setDelta(DeltaFilter);
        if (isDeltaSFilterSpecified())
            ivar.setDeltaS(DeltaSeasonalFilter);
        for (int i = 0; i < Sequence.length; ++i)
            ivar.add(StringFormatter.yearMonth(Sequence[i].Start),
                StringFormatter.yearMonth(Sequence[i].End));
        return ivar;
    }
    
        @ServiceProvider(service = ITsVariableAdapter.class)
    public static class Adapter implements ITsVariableAdapter<XmlInterventionVariable, InterventionVariable> {

        @Override
        public Class<InterventionVariable> getValueType() {
            return InterventionVariable.class;
        }

        @Override
        public Class<XmlInterventionVariable> getXmlType() {
            return XmlInterventionVariable.class;
        }

        @Override
        public InterventionVariable decode(XmlInterventionVariable v) throws Exception {
            return v.create();
        }

        @Override
        public XmlInterventionVariable encode(InterventionVariable v) throws Exception {
            XmlInterventionVariable xml = new XmlInterventionVariable();
            xml.copy(v);
            return xml;
        }
    }

}
