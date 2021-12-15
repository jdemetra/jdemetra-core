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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.data.Range;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

/**
 *
 * Intervention variable.
 *
 *
 * <p>
 * Java class for InterventionVariableType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="InterventionVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}RegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}TimeSpan" maxOccurs="unbounded"/&gt;
 *         &lt;element name="DeltaFilter" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="DeltaSeasonalFilter" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name="InterventionVariable")
@XmlType(name = "InterventionVariableType", propOrder = {
    "sequence",
    "deltaFilter",
    "deltaSeasonalFilter"
})
public class XmlInterventionVariable
        extends XmlRegressionVariable {

    @XmlElement(name = "Sequence")
    protected List<XmlSpan> sequence;

    @XmlElement(name = "DeltaFilter")
    protected Double deltaFilter;
    @XmlElement(name = "DeltaSeasonalFilter")
    protected Double deltaSeasonalFilter;

    public List<XmlSpan> getSequences() {
        if (sequence == null) {
            sequence = new ArrayList<>();
        }
        return this.sequence;
    }

    /**
     * Gets the value of the deltaFilter property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getDeltaFilter() {
        return deltaFilter;
    }

    /**
     * Sets the value of the deltaFilter property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setDeltaFilter(Double value) {
        if (value != null && value == 0) {
            this.deltaFilter = null;
        } else {
            this.deltaFilter = value;
        }
    }

    /**
     * Gets the value of the deltaSeasonalFilter property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getDeltaSeasonalFilter() {
        return deltaSeasonalFilter;
    }

    /**
     * Sets the value of the deltaSeasonalFilter property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setDeltaSeasonalFilter(Double value) {
        if (value != null && value == 0) {
            this.deltaSeasonalFilter = null;
        } else {
            this.deltaSeasonalFilter = value;
        }
    }

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

 
        @Override
        public InterventionVariable unmarshal(XmlRegressionVariable var) {
            if (! (var instanceof XmlInterventionVariable))
                return null;
            XmlInterventionVariable v=(XmlInterventionVariable) var;
            if (v.sequence == null || v.sequence.isEmpty()) {
                return null;
            }
            InterventionVariable.Builder builder = InterventionVariable.builder();
            if (v.deltaFilter != null) {
                builder.delta(v.deltaFilter);
            }
            if (v.deltaSeasonalFilter != null) {
                builder.deltaSeasonal(v.deltaSeasonalFilter);
            }

            for (XmlSpan seq : v.sequence) {
                builder.sequence(Range.of(seq.getFrom().atStartOfDay(), seq.getTo().atStartOfDay()));
            }
            return builder.build();
        }

        @Override
        public XmlInterventionVariable marshal(ITsVariable v) {
            if (! (v instanceof InterventionVariable))
                return null;
            InterventionVariable t=(InterventionVariable) v;
            XmlInterventionVariable xml = new XmlInterventionVariable();
            xml.setDeltaFilter(t.getDelta());
            xml.setDeltaSeasonalFilter(t.getDeltaSeasonal());
            List<XmlSpan> sequences = xml.getSequences();
            List<Range<LocalDateTime>> seqs = t.getSequences();
            for (Range<LocalDateTime> seq : seqs) {
                XmlSpan span = new XmlSpan();
                span.setFrom(seq.start().toLocalDate());
                span.setTo(seq.end().toLocalDate());
                sequences.add(span);
            }
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlInterventionVariable.class);
         }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static final Adapter getAdapter() {
        return ADAPTER;
    }

}
