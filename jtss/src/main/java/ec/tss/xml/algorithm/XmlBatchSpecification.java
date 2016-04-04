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
import ec.tstoolkit.algorithm.BatchProcessingFactory.Node;
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
@XmlRootElement(name = XmlBatchSpecification.RNAME)
@XmlType(name = XmlBatchSpecification.NAME)
public class XmlBatchSpecification implements IXmlConverter<BatchProcessingFactory> {

    static final String NAME = "batchSpecificationType";
    static final String RNAME = "batchSpecification";
    @XmlElement
    public XmlBatchStep[] steps;

    @Override
    public BatchProcessingFactory create() {
        BatchProcessingFactory fac = new BatchProcessingFactory();
        if (steps != null) {
            for (int i = 0; i < steps.length; ++i) {
                InformationSet spec = steps[i].specification.create();
                IProcSpecification pspec = ProcessingManager.getInstance().createSpecification(spec);
                if (pspec == null) {
                    return null;
                }
                fac.add(new BatchProcessingFactory.Node<>(
                        steps[i].name,
                        pspec,
                        steps[i].link.linktype,
                        steps[i].link.linkid));
            }
        }
        return fac;
    }

    @Override
    public void copy(BatchProcessingFactory t) {
        List<Node<? extends IProcSpecification>> nodes = t.nodes();
        steps = new XmlBatchStep[nodes.size()];
        int cur = 0;
        for (Node<? extends IProcSpecification> node : nodes) {
            XmlBatchStep step = new XmlBatchStep();
            step.name = node.name;
            step.specification = new XmlInformationSet();
            step.specification.copy(node.specification.write(false));
            step.link = new XmlBatchLink();
            step.link.linktype = node.linkType;
            step.link.linkid = node.linkId;
            steps[cur++] = step;
        }
    }
}
