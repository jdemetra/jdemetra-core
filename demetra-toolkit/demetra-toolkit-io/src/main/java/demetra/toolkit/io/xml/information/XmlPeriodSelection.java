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
package demetra.toolkit.io.xml.information;

import demetra.timeseries.TimeSelector;
import demetra.timeseries.TimeSelector.SelectionType;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import demetra.toolkit.io.xml.legacy.XmlEmptyElement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlPeriodSelection.NAME)
public class XmlPeriodSelection implements IXmlConverter<TimeSelector> {

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
    public TimeSelector create() {
        if (excluding != null) {
            return TimeSelector.excluding(excluding.nfirst, excluding.nlast);
        } else if (between != null) {
            return TimeSelector.between(LocalDate.parse(between.start, DateTimeFormatter.ISO_DATE).atStartOfDay(), LocalDate.parse(between.end, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } else if (from != null) {
            return TimeSelector.from(LocalDate.parse(from, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } else if (to != null) {
            return TimeSelector.to(LocalDate.parse(to, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } else if (none != null) {
            return TimeSelector.none();
        } else if (first != null) {
            return TimeSelector.first(Integer.parseInt(first));
        } else if (last != null) {
            return TimeSelector.last(Integer.parseInt(last));
        } else {
            return TimeSelector.all();
        }
    }

    @Override
    public void copy(TimeSelector t) {
        if (t.getType() == SelectionType.All) {
            all = new XmlEmptyElement();
        } else if (t.getType() == SelectionType.Between) {
            between = new XmlPeriodSelectionBetween();
            between.start = t.getD0().toLocalDate().format(DateTimeFormatter.ISO_DATE);
            between.end = t.getD1().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        } else if (t.getType() == SelectionType.From) {
            from = t.getD0().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        } else if (t.getType() == SelectionType.To) {
            to = t.getD1().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        } else if (t.getType() == SelectionType.First) {
            first = Integer.toString(t.getN0());
        } else if (t.getType() == SelectionType.Last) {
            last = Integer.toString(t.getN1());
        } else if (t.getType() == SelectionType.Excluding) {
            excluding = new XmlPeriodSelectionExcluding();
            excluding.nfirst = t.getN0();
            excluding.nlast = t.getN1();
        } else {
            none = new XmlEmptyElement();
        }
    }
}
