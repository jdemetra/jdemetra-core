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
package ec.tss.xml.algorithm;

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.information.XmlInformationSet;
import ec.tstoolkit.algorithm.BatchProcessingFactory;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.ProcessingManager;
import ec.tstoolkit.information.InformationSet;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlBatch.RNAME)
@XmlType(name = XmlBatch.NAME)
public class XmlBatch implements IXmlConverter<BatchProcessingFactory> {

    static final String NAME = "batchType";
    static final String RNAME = "batch";
    @XmlElement
    public XmlProcessingContext context;
    @XmlElement
    public XmlInformationSet input;
    @XmlElement
    public XmlBatchStep[] step;
    @XmlElement
    public String[] filter;

    @Override
    public BatchProcessingFactory create() {
        BatchProcessingFactory fac = new BatchProcessingFactory();
        if (step != null) {
            for (int i = 0; i < step.length; ++i) {
                InformationSet spec = step[i].specification.create();
                IProcSpecification pspec = ProcessingManager.getInstance().createSpecification(spec);
                if (pspec == null) {
                    return null;
                }
                fac.add(new BatchProcessingFactory.Node<>(
                        step[i].name,
                        pspec,
                        step[i].link.linktype,
                        step[i].link.linkid));
            }
        }
        return fac;
    }

    @Override
    public void copy(BatchProcessingFactory t) {
        List<BatchProcessingFactory.Node<? extends IProcSpecification>> nodes = t.nodes();
        step = new XmlBatchStep[nodes.size()];
        int cur = 0;
        for (BatchProcessingFactory.Node<? extends IProcSpecification> node : nodes) {
            XmlBatchStep bstep = new XmlBatchStep();
            bstep.name = node.name;
            bstep.specification = new XmlInformationSet();
            bstep.specification.copy(node.specification.write(false));
            bstep.link = new XmlBatchLink();
            bstep.link.linktype = node.linkType;
            bstep.link.linkid = node.linkId;
            this.step[cur++] = bstep;
        }
    }

}
