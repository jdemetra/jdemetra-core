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
package ec.satoolkit.special;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.StmProcessingFactory;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.structural.BsmSpecification;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class StmSpecification implements ISaSpecification, Cloneable {

    private PreprocessingSpecification preprocessingSpec;
    private BsmSpecification decompositionSpec;
    private SaBenchmarkingSpec benchmarkingSpec;

    public StmSpecification() {
        preprocessingSpec = new PreprocessingSpecification();
        preprocessingSpec.dtype = TradingDaysType.TradingDays;
        preprocessingSpec.ltype = LengthOfPeriodType.LeapYear;
        decompositionSpec = new BsmSpecification();
        benchmarkingSpec = new SaBenchmarkingSpec();
    }

    public PreprocessingSpecification getPreprocessingSpec() {
        return preprocessingSpec;
    }

    public BsmSpecification getDecompositionSpec() {
        return decompositionSpec;
    }

    public SaBenchmarkingSpec getBenchmarkingSpec() {
        return benchmarkingSpec;
    }

    public void setPreprocessingSpec(PreprocessingSpecification spec) {
        preprocessingSpec = spec;
    }

    public void setDecompositionSpec(BsmSpecification spec) {
        decompositionSpec = spec;
    }

    public void setBenchmarkingSpec(SaBenchmarkingSpec spec) {
        benchmarkingSpec = spec;
    }

    @Override
    public String toString() {
        return StmProcessingFactory.DESCRIPTOR.name;
    }

    @Override
    public StmSpecification clone() {
        try {
            StmSpecification spec = (StmSpecification) super.clone();
            if (preprocessingSpec != null) {
                spec.preprocessingSpec = preprocessingSpec.clone();
            }
            if (decompositionSpec != null) {
                spec.decompositionSpec = decompositionSpec.clone();
            }
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean read(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        // TODO Fill the dictionary
    }

    public IPreprocessor buildPreprocessor(ProcessingContext context) {
        return preprocessingSpec == null ? null : preprocessingSpec.build(context);
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    @Override
    public String toLongString() {
        return toString();
    }
}
