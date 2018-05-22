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


package demetra.xml.regression;

import demetra.xml.IXmlConverter;
import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlInterventionVariable.NAME)
public class XmlInterventionVariable implements IXmlConverter<InterventionVariable> {
    static final String NAME = "interventionType";

    @XmlElement(name="sequence")
    public XmlSeq[] sequence;
    @XmlElement
    public Double deltaFilter;
    public boolean isDeltaFilterSpecified() {
        return deltaFilter != null;
    }
    @XmlElement
    public Double deltaSFilter;
    public boolean isDeltaSFilterSpecified() {
        return deltaSFilter != null;
    }

    public XmlInterventionVariable() { }

    @Override
    public void copy(InterventionVariable t) {
        deltaFilter = t.getDelta();
        deltaSFilter = t.getDeltaS();

        int n = t.getCount();
        sequence = new XmlSeq[n];
        for (int i = 0; i < n; ++i) {
            sequence[i].start = StringFormatter.yearMonth(t.getSequence(i).start);
            sequence[i].end = StringFormatter.yearMonth(t.getSequence(i).end);
        }
    }

    @Override
    public InterventionVariable create() {
        if (sequence == null)
            return null;
        InterventionVariable ivar = new InterventionVariable();
        if (isDeltaFilterSpecified())
            ivar.setDelta(deltaFilter);
        if (isDeltaSFilterSpecified())
            ivar.setDeltaS(deltaSFilter);
        for (int i = 0; i < sequence.length; ++i)
            ivar.add(StringFormatter.yearMonth(sequence[i].start),
                StringFormatter.yearMonth(sequence[i].end));
        return ivar;
    }
}
