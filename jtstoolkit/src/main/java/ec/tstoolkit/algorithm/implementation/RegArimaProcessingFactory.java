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
package ec.tstoolkit.algorithm.implementation;

import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Singleton
@Development(status = Development.Status.Alpha)
public class RegArimaProcessingFactory implements IProcessingFactory<RegArimaSpecification, TsData, PreprocessingModel> {

    static {
        RegArimaProcessingFactory factory = new RegArimaProcessingFactory();
        instance = factory;
    }
    public static final String METHOD = "regarima";
    public static final String FAMILY = "Modelling";
    public static final String VERSION = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    public static final RegArimaProcessingFactory instance;

    protected RegArimaProcessingFactory() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public IProcessing<TsData, PreprocessingModel> generateProcessing(final RegArimaSpecification specification, ProcessingContext context) {
        return new RegArimaProcessing(specification, context);
    }

    public IProcessing<TsData, PreprocessingModel> generateProcessing(final RegArimaSpecification specification) {
        return new RegArimaProcessing(specification, null);
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof RegArimaSpecification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<RegArimaSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        RegArimaSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        return PreprocessingModel.dictionary();
    }

    private static class RegArimaProcessing implements IProcessing<TsData, PreprocessingModel> {

        private final IPreprocessor preprocessor;

        public RegArimaProcessing(RegArimaSpecification specification, ProcessingContext context) {
            this.preprocessor = specification.build(context);
        }

        @Override
        public PreprocessingModel process(TsData input) {
            ModellingContext context = new ModellingContext();
            return preprocessor.process(input, context);
        }
    }
}
