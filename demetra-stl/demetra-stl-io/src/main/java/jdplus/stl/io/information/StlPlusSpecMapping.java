/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.information.InformationSetSerializerEx;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.ModellingSpecMapping;
import demetra.sa.io.information.SaBenchmarkingSpecMapping;
import demetra.sa.io.information.SaSpecificationMapping;
import demetra.stl.StlPlusSpec;
import demetra.timeseries.TsDomain;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class StlPlusSpecMapping {

    public static final InformationSetSerializerEx<StlPlusSpec, TsDomain> SERIALIZER = new InformationSetSerializerEx<StlPlusSpec, TsDomain>() {
        @Override
        public InformationSet write(StlPlusSpec object, TsDomain context, boolean verbose) {
            return StlPlusSpecMapping.write(object, context, verbose);
        }

        @Override
        public StlPlusSpec read(InformationSet info, TsDomain context) {
            return StlPlusSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public static final String PREPROCESSING = "preprocessing", STL = "stl", BENCH = "benchmarking";

    public InformationSet write(StlPlusSpec spec, TsDomain context, boolean verbose, DemetraVersion version) {
        return switch (version) {
            default ->
                write(spec, context, verbose);
        };
    }

    public InformationSet write(StlPlusSpec spec, TsDomain context, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ProcSpecification.ALGORITHM, StlPlusSpec.DESCRIPTOR);
        InformationSet tinfo = ModellingSpecMapping.write(spec.getPreprocessing(), context, verbose);
        if (tinfo != null) {
            specInfo.add(PREPROCESSING, tinfo);
        }
        InformationSet sinfo = StlSpecMapping.write(spec.getStl(), verbose);
        if (sinfo != null) {
            specInfo.add(STL, sinfo);
        }
        InformationSet binfo = SaBenchmarkingSpecMapping.write(spec.getBenchmarking(), verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    public StlPlusSpec read(InformationSet info, TsDomain context) {
        if (info == null) {
            return null;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc == null) {
            return null;
        }
        if (desc.equals(StlPlusSpec.DESCRIPTOR)) {
            return readV3(info, context);
        } else {
            return null;
        }
    }

    public StlPlusSpec readV3(InformationSet info, TsDomain context) {
        if (info == null) {
            return StlPlusSpec.DEFAULT;
        }
        return StlPlusSpec.builder()
                .preprocessing(ModellingSpecMapping.read(info.getSubSet(PREPROCESSING), context))
                .stl(StlSpecMapping.read(info.getSubSet(STL)))
                .benchmarking(SaBenchmarkingSpecMapping.read(info.getSubSet(BENCH)))
                .build();
    }

    @ServiceProvider(SaSpecificationMapping.class)
    public static class Serializer implements SaSpecificationMapping {

        @Override
        public SaSpecification read(InformationSet info, TsDomain context) {
            return StlPlusSpecMapping.read(info, context);
        }

        @Override
        public InformationSet write(SaSpecification spec, TsDomain context, boolean verbose, DemetraVersion version) {
            if (spec instanceof StlPlusSpec tramoSeatsSpec) {
                return StlPlusSpecMapping.write(tramoSeatsSpec, context, verbose, version);
            } else {
                return null;
            }
        }
    }

}
