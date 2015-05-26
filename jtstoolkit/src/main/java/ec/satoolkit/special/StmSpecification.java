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

import ec.satoolkit.GenericSaProcessingFactory;
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
import java.util.Objects;

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

    public boolean equals(StmSpecification other) {
        return Objects.equals(preprocessingSpec, other.preprocessingSpec)
                && Objects.equals(decompositionSpec, other.decompositionSpec)
                && Objects.equals(benchmarkingSpec, other.benchmarkingSpec);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof StmSpecification && equals((StmSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.preprocessingSpec);
        hash = 97 * hash + Objects.hashCode(this.decompositionSpec);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (preprocessingSpec != null) {
            InformationSet p = preprocessingSpec.write(verbose);
            if (p != null) {
                info.set(GenericSaProcessingFactory.PREPROCESSING, p);
            }
        }
        if (decompositionSpec != null) {
            InformationSet p = decompositionSpec.write(verbose);
            if (p != null) {
                info.set(GenericSaProcessingFactory.DECOMPOSITION, p);
            }
        }
        if (benchmarkingSpec != null) {
            InformationSet p = benchmarkingSpec.write(verbose);
            if (p != null) {
                info.set(GenericSaProcessingFactory.BENCHMARKING, p);
            }
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet p=info.getSubSet(GenericSaProcessingFactory.PREPROCESSING);
        if  (p != null){
            if (! preprocessingSpec.read(p))
                return false;
        }
        InformationSet d=info.getSubSet(GenericSaProcessingFactory.DECOMPOSITION);
        if  (d != null){
            if (! decompositionSpec.read(d))
                return false;
        }
        InformationSet b=info.getSubSet(GenericSaProcessingFactory.BENCHMARKING);
        if  (b != null){
            if (! benchmarkingSpec.read(b))
                return false;
        }
        return true;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        PreprocessingSpecification.fillDictionary(InformationSet.item(prefix, GenericSaProcessingFactory.PREPROCESSING), dic);
        BsmSpecification.fillDictionary(InformationSet.item(prefix, GenericSaProcessingFactory.DECOMPOSITION), dic);
        SaBenchmarkingSpec.fillDictionary(InformationSet.item(prefix, GenericSaProcessingFactory.BENCHMARKING), dic);
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
