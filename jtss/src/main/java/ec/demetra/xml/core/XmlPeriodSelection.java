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

import ec.tss.xml.*;

import ec.tstoolkit.timeseries.TsPeriodSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlPeriodSelection.NAME)
public class XmlPeriodSelection implements IXmlConverter<TsPeriodSelector> {

    static final String NAME = "PeriodSelectionType";

    @XmlElement
    public XmlEmptyElement All;
    @XmlElement(name = "None")
    public XmlEmptyElement None;
    @XmlElement
    public XmlRange Range;
    @XmlElement
    public XmlRange Excluding;
    @XmlElement
    public XmlTimeSpan Span;

    @Override
    public TsPeriodSelector create() {
        TsPeriodSelector tssel = new TsPeriodSelector();
        if (All != null) {
            tssel.all();
        } else if (None != null) {
            tssel.none();
        } else if (Excluding != null) {
            int first = Excluding.First == null ? 0 : Excluding.First;
            int last = Excluding.Last == null ? 0 : Excluding.Last;
            tssel.excluding(first, last);
        } else if (Range != null) {
            int first = Range.First == null ? -1 : Range.First;
            int last = Range.Last == null ? -1 : Range.Last;
            if (first >= 0) {
                tssel.first(first);
            } else if (last >= 0) {
                tssel.last(last);
            }
            // else not supported
        } else if (Span != null) {
            if (Span.Start != null && Span.End != null) {
                tssel.between(Span.Start, Span.End);
            } else if (Span.Start != null) {
                tssel.from(Span.Start);
            } else if (Span.End != null) {
                tssel.to(Span.End);
            }
        }
        return tssel;
    }

    @Override
    public void copy(TsPeriodSelector t
    ) {
        switch (t.getType()){
            case All:
                All=new XmlEmptyElement();
                return;
           case None:
                All=new XmlEmptyElement();
                return;
           case From:
               Span=new XmlTimeSpan();
               Span.Start=t.getD0();
               return;
           case To:
               Span=new XmlTimeSpan();
               Span.End=t.getD1();
               return;
           case Between:
               Span=new XmlTimeSpan();
               Span.Start=t.getD0();
               Span.End=t.getD1();
               return;
           case First:
               Range=new XmlRange();
               Range.First=t.getN0();
               return;
           case Last:
               Range=new XmlRange();
               Range.Last=t.getN1();
               return;
           case Excluding:
               Excluding=new XmlRange();
               Excluding.First=t.getN0();
               Excluding.Last=t.getN1();
        }
    }
}
