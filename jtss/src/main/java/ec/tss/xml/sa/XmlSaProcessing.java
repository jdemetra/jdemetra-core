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

import com.google.common.collect.Iterables;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlMetaData;
import ec.tss.xml.XmlTs;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tss.xml.x12.XmlX12Specification;
import ec.tss.xml.x13.XmlX13Specification;
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
public class XmlSaProcessing implements IXmlConverter<SaProcessing> {
    static final String NAME = "saProcessingType";
    static final String RNAME = "saProcessing";

    @XmlAttribute
    public Date timeStamp;
    @XmlElement
    public XmlMetaData metadata;
    @XmlElements ({
        @XmlElement(name="trsSpec", type = XmlTramoSeatsSpecification.class),
        @XmlElement(name="x13Spec", type = XmlX13Specification.class),
        @XmlElement(name="x12Spec", type = XmlX12Specification.class)
    })
    public AbstractXmlSaSpecification[] defaultMethods;
    @XmlElementWrapper(name="items")
    @XmlElement(name="saItem")
    public XmlSaItem[] items;

    @Override
    public SaProcessing create() {
        // read the context and put it in m_context.
        SaProcessing processing = new SaProcessing();
        // load default specs...
        Map<String, ISaSpecification> dic = new HashMap<>();
        if (defaultMethods != null)
            for (AbstractXmlSaSpecification xspec : defaultMethods)
                dic.put(xspec.name, xspec.convert(true));
        // read items
        if (items != null) {
            for (XmlSaItem xitem : items) {
                // create the current item...
                TsInformation info = xitem.series.create();
                Ts s = TsFactory.instance.createTs(info.name, info.moniker, info.metaData, info.data);
                ISaSpecification spec = null;
                if (xitem.spec != null)
                    spec = xitem.spec.convert(false);
                ISaSpecification espec = null;
                if (xitem.espec != null)
                    espec = xitem.espec.convert(false);
                ISaSpecification defspec;
                defspec = dic.get(xitem.defaultMethod);
                SaItem item = null;
                if (spec != null)
                {
                    item = new SaItem(defspec, xitem.policy, espec, s);
                    item.setPointSpecification(spec);
                }
                else
                    item = new SaItem(defspec, s);
                item.setPriority(xitem.priority);
                item.setQuality(xitem.quality);
                item.setStatus(xitem.status);
                processing.add(item);
            }
        }
        processing.getMetaData().put(SaProcessing.TIMESTAMP, timeStamp.toString());
        return processing;
    }

    @Override
    public void copy(SaProcessing t) {
        timeStamp = new Date();
        int n = t.size();
        if (n > 0) {
            Map<ISaSpecification, String> dic = new HashMap<>();
            XmlSaItem[] xitems = new XmlSaItem[n];
            List<AbstractXmlSaSpecification> xdspecs = new ArrayList<>();
            int i = 0;
            int ispec = 1;
            for (SaItem item : t) {
                XmlSaItem xitem = new XmlSaItem();
                xitem.series = new XmlTs();
                if (item.getMoniker().isAnonymous())
                    xitem.series.copy(new TsInformation(item.getTs(), TsInformationType.All));
                else if (item.getStatus() == SaItem.Status.Unprocessed)
                    xitem.series.copy(new TsInformation(item.getTs(), TsInformationType.Definition));
                else
                    xitem.series.copy(new TsInformation(item.getTs().freeze(), TsInformationType.All));
                xitem.priority = item.getPriority();
                xitem.quality = item.getQuality();
                xitem.policy = item.getEstimationPolicy();
                xitem.status = item.getStatus();
                ISaSpecification rspec = item.getPointSpecification(), dspec = item.getDomainSpecification();
                if (rspec != null)
                    xitem.spec = AbstractXmlSaSpecification.create(rspec);
                if (item.getEstimationSpecification() != item.getDomainSpecification())
                    xitem.espec = AbstractXmlSaSpecification.create(item.getEstimationSpecification());
                if (dspec != null) {
                    String name;
                    if (!dic.containsKey(dspec)) {
                        name = "spec" + Integer.toString(ispec++);
                        dic.put(dspec, name);
                        AbstractXmlSaSpecification xdspec = AbstractXmlSaSpecification.create(dspec);
                        xdspec.name = name;
                        xdspecs.add(xdspec);
                    }
                    else
                        name = dic.get(dspec);
                    xitem.defaultMethod = name;
                }
                xitems[i++] = xitem;
            }
            items = xitems;
            defaultMethods = Iterables.toArray(xdspecs, AbstractXmlSaSpecification.class);
        }
    }
}
