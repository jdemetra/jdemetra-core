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


package ec.tss.xml.sa;

import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.SaItem.Status;
import ec.tss.xml.XmlTs;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tss.xml.x12.XmlX12Specification;
import ec.tss.xml.x13.XmlX13Specification;
import ec.tstoolkit.algorithm.ProcQuality;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlSaItem.NAME)
public class XmlSaItem {
    static final String NAME = "saItemType";

    @XmlElement
    public XmlTs series;
    @XmlElements ({
        @XmlElement(name="trsSpec", type = XmlTramoSeatsSpecification.class),
        @XmlElement(name="x13Spec", type = XmlX13Specification.class),
        @XmlElement(name="x12Spec", type = XmlX12Specification.class)
    })
    public AbstractXmlSaSpecification spec;
    @XmlElements ({
        @XmlElement(name="trsEstimationSpec", type = XmlTramoSeatsSpecification.class),
        @XmlElement(name="x12EstimationSpec", type = XmlX12Specification.class),
        @XmlElement(name="x13EstimationSpec", type = XmlX13Specification.class)
    })
    public AbstractXmlSaSpecification espec;
    @XmlAttribute
    public Integer priority = -1;
    public boolean isPrioritySpecified() {
        return priority != null;
    }
    @XmlAttribute
    public String defaultMethod;
    @XmlAttribute
    public String id;
    @XmlAttribute
    public EstimationPolicyType policy = EstimationPolicyType.None;
    public boolean isPolicySpecified() {
        return policy != null;
    }
    @XmlAttribute
    public ProcQuality quality = ProcQuality.Undefined;
    public boolean isQualitySpecified() {
        return quality != null;
    }
    @XmlAttribute
    public Status status = Status.Unprocessed;
    public boolean isStatusSpecified() {
        return status != null;
    }
}
