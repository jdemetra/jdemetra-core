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

import ec.benchmarking.simplets.TsMultiBenchmarking;
import ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor;
import ec.tss.disaggregation.documents.MultiBenchmarkingResults;
import ec.tss.disaggregation.documents.MultiCholetteSpecification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class MultiCholetteProcessor implements IProcessingFactory<MultiCholetteSpecification, TsVariables, MultiBenchmarkingResults> {

    public static final String FAMILY = "Benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Multi-variate Cholette", null);

    public static final MultiCholetteProcessor instance=new MultiCholetteProcessor();
    
    protected MultiCholetteProcessor() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof MultiCholetteSpecification;
    }

    @Override
    public IProcessing<TsVariables, MultiBenchmarkingResults> generateProcessing(final MultiCholetteSpecification spec, ProcessingContext context) {
        return new DefaultProcessing(spec);
    }

    public IProcessing<TsVariables, MultiBenchmarkingResults> generateProcessing(final MultiCholetteSpecification spec) {
        return new DefaultProcessing(spec);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<MultiCholetteSpecification> specClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class DefaultProcessing implements IProcessing<TsVariables, MultiBenchmarkingResults> {

        private final MultiCholetteSpecification spec;

        public DefaultProcessing(MultiCholetteSpecification spec) {
            this.spec = spec.clone();
        }

        private String var(int pos) {
            return "s" + (pos + 1);
        }

        @Override
        public MultiBenchmarkingResults process(TsVariables input) {
            TsMultiBenchmarking bench = new TsMultiBenchmarking();
            bench.setRho(spec.getParameters().getRho());
            bench.setLambda(spec.getParameters().getLambda());
            // input
            TsFrequency[] freqs=input.frequencies();
            HashMap<TsFrequency, TsDomain> domains=new HashMap<>();
            for (int i=0; i<freqs.length; ++i)
                domains.put(freqs[i], input.common(freqs[i]));
            for (ITsVariable var : input.variables()) {
                if (var.getDefinitionFrequency() != TsFrequency.Undefined && var.getDim() == 1){
                    TsDomain dom=domains.get(var.getDefinitionFrequency());
                    TsData data=new TsData(dom);
                    List<DataBlock> singletonList = Collections.singletonList(new DataBlock(data.getValues().internalStorage()));
                    var.data(dom, singletonList);
                    bench.addInput(input.get(var), data);
                }
            }
            // temporal constraints
            for (String c : spec.getConstraints()) {
                TsMultiBenchmarking.TemporalConstraintDescriptor temp = TsMultiBenchmarking.TemporalConstraintDescriptor.parse(c);
                if (temp != null)
                    bench.addTemporalConstraint(temp);
                else{
                    ContemporaneousConstraintDescriptor cont = TsMultiBenchmarking.ContemporaneousConstraintDescriptor.parse(c);
                    if (cont != null)
                        bench.addContemporaneousConstraint(cont);
                }
            }
            
            bench.process();
            // fill the results
            MultiBenchmarkingResults results=new MultiBenchmarkingResults();
            for (String n : bench.endogenous()){
                results.addBenchmarked(n, bench.getResult(n));
            }
            for (String n : bench.input()){
                results.addInput(n, bench.getInput(n));
            }

            return results;
        }
    }
}
