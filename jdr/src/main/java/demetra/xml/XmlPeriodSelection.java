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


package demetra.xml;

import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlPeriodSelection.NAME)
public class XmlPeriodSelection implements IXmlConverter<TsPeriodSelector> {
    static final String NAME = "periodSelectionType";

    @XmlElement
    public XmlEmptyElement all;
    @XmlElement
    public XmlEmptyElement none;
    @XmlElement
    public String from;
    @XmlElement
    public String to;
    @XmlElement
    public String first;
    @XmlElement
    public String last;
    @XmlElement
    public XmlPeriodSelectionExcluding excluding;
    @XmlElement
    public XmlPeriodSelectionBetween between;

    @Override
    public TsPeriodSelector create() {
        TsPeriodSelector tssel = new TsPeriodSelector();
        if (excluding != null)
            tssel.excluding(excluding.nfirst, excluding.nlast);
        else if (between != null)
            tssel.between(StringFormatter.convertDay(between.start, Day.BEG), StringFormatter.convertDay(between.end, Day.END));
        else if (from != null)
            tssel.from(StringFormatter.convertDay(from, Day.BEG));
        else if (to != null)
            tssel.to(StringFormatter.convertDay(to, Day.BEG));
        else if (all != null)
            tssel.all();
        else if (none != null)
            tssel.none();
        else if (first != null)
            tssel.first(Integer.parseInt(first));
        else if (last != null)
            tssel.last(Integer.parseInt(last));
        return tssel;
    }

    @Override
    public void copy(TsPeriodSelector t) {
        if (t.getType() == PeriodSelectorType.All)
            all = new XmlEmptyElement();
        else if (t.getType() == PeriodSelectorType.Between) {
            between = new XmlPeriodSelectionBetween();
            between.start = StringFormatter.convert(t.getD0(), Day.BEG);
            between.end = StringFormatter.convert(t.getD1(), Day.END);
        }
        else if (t.getType() == PeriodSelectorType.From) {
            from=StringFormatter.convert(t.getD0(), Day.BEG);
        }
        else if (t.getType() == PeriodSelectorType.To) {
            to = StringFormatter.convert(t.getD1(), Day.END);
        }
        else if (t.getType() == PeriodSelectorType.First) {
            first = Integer.toString(t.getN0());
        }
        else if (t.getType() == PeriodSelectorType.Last) {
            last = Integer.toString(t.getN1());
        }
        else if (t.getType() == PeriodSelectorType.Excluding) {
            excluding = new XmlPeriodSelectionExcluding();
            excluding.nfirst = t.getN0();
            excluding.nlast = t.getN1();
        }
        else
            none = new XmlEmptyElement();
    }
}
