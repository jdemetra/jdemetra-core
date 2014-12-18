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

package ec.tss.sa.documents;

import ec.satoolkit.ISaSpecification;
import ec.tss.documents.TsDocument;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaItem;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.timeseries.analysis.ITsProcessing;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class SaDocumentProcessing<S extends ISaSpecification> implements ITsProcessing<CompositeResults> {
    private final IProcessing<TsData, CompositeResults> processing_;
    private final TsData data_;

    public SaDocumentProcessing(SaDocument<S> doc, EstimationPolicyType policy) {
        ISaProcessingFactory<S> factory=(ISaProcessingFactory<S>) doc.getProcessor();
        SaItem item=new SaItem(doc.getSpecification(), doc.getInput());
        item.unsafeFill(doc.getResults());
        factory.updatePointSpecification(item);
        S spec = (S)factory.createSpecification(item, null, policy, true);
        processing_=factory.generateProcessing(spec, null);
        data_ = doc.getInput().getTsData();
    }

    @Override
    public CompositeResults process(TsDomain domain) {
        if (data_ == null) {
            return null;
        }
        return processing_.process(data_.fittoDomain(domain));
    }
   
}
