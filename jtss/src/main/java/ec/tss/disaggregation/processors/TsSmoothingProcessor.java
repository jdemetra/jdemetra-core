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
package ec.tss.disaggregation.processors;

import ec.tss.disaggregation.documents.TsSmoothingSpecification;
import ec.benchmarking.simplets.TsExpander;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class TsSmoothingProcessor implements ITemporalDisaggregationProcessingFactory, IProcessingFactory<TsSmoothingSpecification, TsData, SingleTsData> {
    
    public static final String NAME = "tsexpander", INTERPOLATION = "interpolation";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, NAME, null);
    public static final TsSmoothingProcessor instance = new TsSmoothingProcessor();
    
    @Override
    public void dispose() {
    }
    
    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }
    
    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof TsSmoothingSpecification;
    }
    
    @Override
    public IProcessing<TsData, SingleTsData> generateProcessing(final TsSmoothingSpecification specification, ProcessingContext context) {
        return new IProcessing<TsData, SingleTsData>() {
            
            @Override
            public SingleTsData process(TsData input) {
                TsExpander expander = new TsExpander();
                double p = specification.getParameter() == null ? 0 : specification.getParameter().getValue();
                expander.setModel(specification.getModel());
                expander.setType(specification.getAggregationType());
                expander.useConst(specification.isConstant());
                expander.useTrend(specification.isTrend());
                if (specification.getParameter() != null
                        && specification.getParameter().isFixed()) {
                    expander.estimateParameter(false);
                    expander.setParameter(specification.getParameter().getValue());
                } else {
                    expander.estimateParameter(true);
                }
                TsDomain ndom = input.getDomain().changeFrequency(specification.getNewFrequency(), true);
                ndom = ndom.extend(specification.getBackcastsCount(), specification.getForecastsCount());
                TsData ns = expander.expand(input, ndom);
                if (ns == null) {
                    return null;
                } else {
                    return new SingleTsData(INTERPOLATION, ns);
                }
            }
        };
    }
    
    @Override
    public Map<String, Class> getSpecificationDictionary(Class<TsSmoothingSpecification> specClass) {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        TsSmoothingSpecification.fillDictionary(null, dic);
        return dic;
    }
    
    @Override
    public Map<String, Class> getOutputDictionary() {
        return Collections.singletonMap(INTERPOLATION, (Class) TsData.class);
    }
    
}
