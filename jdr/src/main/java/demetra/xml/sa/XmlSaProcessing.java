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
package demetra.xml.sa;

import demetra.datatypes.Ts;
import demetra.datatypes.sa.SaItemType;
import demetra.datatypes.sa.SaProcessingType;
import demetra.xml.IXmlConverter;
import demetra.xml.XmlMetaData;
import demetra.xml.XmlTs;
import demetra.xml.sa.tramoseats.XmlTramoSeatsSpecification;
import demetra.xml.sa.x13.XmlX13Specification;
import ec.satoolkit.ISaSpecification;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlSaProcessing.RNAME)
@XmlType(name = XmlSaProcessing.NAME)
public class XmlSaProcessing implements IXmlConverter<SaProcessingType> {

    static final String NAME = "saProcessingType";
    static final String RNAME = "saProcessing";

    @XmlAttribute
    public Date timeStamp;
    @XmlElement
    public XmlMetaData metadata;
    @XmlElements({
        @XmlElement(name = "trsSpec", type = XmlTramoSeatsSpecification.class)
        ,
        @XmlElement(name = "x13Spec", type = XmlX13Specification.class),})
    public AbstractXmlSaSpecification[] defaultMethods;
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "saItem")
    public XmlSaItem[] items;

    @Override
    public SaProcessingType create() {
        // read the context and put it in m_context.
        SaProcessingType processing = new SaProcessingType();
        if (metadata != null) {
            processing.getMetaData().putAll(metadata.create());
        }
        // load default specs...
        Map<String, ISaSpecification> dic = new HashMap<>();
        if (defaultMethods != null) {
            for (AbstractXmlSaSpecification xspec : defaultMethods) {
                dic.put(xspec.name, xspec.convert(true));
            }
        }
        // read items
        if (items != null) {
            for (XmlSaItem xitem : items) {
                // create the current item...
                Ts s = xitem.series.create();
                ISaSpecification spec = null;
                if (xitem.spec != null) {
                    spec = xitem.spec.convert(false);
                }
                ISaSpecification espec = null;
                if (xitem.espec != null) {
                    espec = xitem.espec.convert(false);
                }
                ISaSpecification defspec;
                defspec = dic.get(xitem.defaultMethod);
                SaItemType item = SaItemType.builder()
                        .ts(s)
                        .domainSpec(defspec)
                        .estimationSpec(espec)
                        .pointSpec(spec)
                        .status(xitem.status)
                        .quality(xitem.quality)
                        .estimationPolicy(xitem.policy)
                        .name(xitem.id)
                        .priority(xitem.priority)
                        .build();
                processing.getItems().add(item);
            }
        }
        if (timeStamp != null) {
            processing.getMetaData().put(SaProcessingType.TIMESTAMP, timeStamp.toString());
        }
        return processing;
    }

    @Override
    public void copy(SaProcessingType t) {
        timeStamp = new Date();
        List<SaItemType> pitems = t.getItems();
        int n = pitems.size();
        if (n > 0) {
            Map<ISaSpecification, String> dic = new HashMap<>();
            XmlSaItem[] xitems = new XmlSaItem[n];
            List<AbstractXmlSaSpecification> xdspecs = new ArrayList<>();
            int i = 0;
            int ispec = 1;
            for (SaItemType item : pitems) {
                XmlSaItem xitem = new XmlSaItem();
                xitem.series = new XmlTs();
                xitem.series.copy(item.getTs());
                xitem.priority = item.getPriority();
                xitem.quality = item.getQuality();
                xitem.policy = item.getEstimationPolicy();
                xitem.status = item.getStatus();
                ISaSpecification rspec = item.getPointSpec(), dspec = item.getDomainSpec();
                if (rspec != null) {
                    xitem.spec = AbstractXmlSaSpecification.create(rspec);
                }
                if (item.getEstimationSpec() != item.getDomainSpec()) {
                    xitem.espec = AbstractXmlSaSpecification.create(item.getEstimationSpec());
                }
                String name;
                if (!dic.containsKey(dspec)) {
                    name = "spec" + Integer.toString(ispec++);
                    dic.put(dspec, name);
                    AbstractXmlSaSpecification xdspec = AbstractXmlSaSpecification.create(dspec);
                    xdspec.name = name;
                    xdspecs.add(xdspec);
                    xitem.defaultMethod = name;
                }
                xitems[i++] = xitem;
            }
            items = xitems;
            defaultMethods = xdspecs.stream().toArray(AbstractXmlSaSpecification[]::new);
        }
    }
}
