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
import ec.tstoolkit.timeseries.Day;

import ec.tstoolkit.timeseries.TsPeriodSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlPeriodSelection.NAME)
public class XmlPeriodSelection implements IXmlConverter<TsPeriodSelector> {

    static final String NAME = "PeriodSelectionType";

    @XmlElement
    public XmlEmptyElement All;
    @XmlElement
    public XmlEmptyElement None;
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day From;
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day To;
    @XmlElement
    public Integer First;
    @XmlElement
    public Integer Last;
    @XmlElement
    public Integer ExcludeFirst;
    @XmlElement
    public Integer ExcludeLast;

    @Override
    public TsPeriodSelector create() {
        TsPeriodSelector tssel = new TsPeriodSelector();
        if (All != null) {
            tssel.all();
        } else if (None != null) {
            tssel.none();
        } else if (ExcludeFirst != null || ExcludeLast != null) {
            int first = ExcludeFirst == null ? 0 : ExcludeFirst;
            int last = ExcludeLast == null ? 0 : ExcludeLast;
            tssel.excluding(first, last);
        } else if (First != null) {
            tssel.first(First);
        } else if (Last != null) {
            tssel.last(Last);
        } else if (From != null || To != null) {
            if (From != null && To != null) {
                tssel.between(From, To);
            } else if (From != null) {
                tssel.from(From);
            } else {
                tssel.to(To);
            }
        }
        return tssel;
    }

    @Override
    public void copy(TsPeriodSelector t
    ) {
        switch (t.getType()) {
            case All:
                All = new XmlEmptyElement();
                return;
            case None:
                All = new XmlEmptyElement();
                return;
            case From:
                From = t.getD0();
                return;
            case To:
                To = t.getD1();
                return;
            case Between:
                From = t.getD0();
                To = t.getD1();
                return;
            case First:
                First = t.getN0();
                return;
            case Last:
                Last = t.getN1();
                return;
            case Excluding:
                ExcludeFirst = t.getN0();
                ExcludeLast = t.getN1();
        }
    }
}
