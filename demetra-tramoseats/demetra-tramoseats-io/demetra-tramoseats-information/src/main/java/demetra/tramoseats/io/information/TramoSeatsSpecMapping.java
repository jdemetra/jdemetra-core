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
package demetra.tramoseats.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.SaBenchmarkingSpecMapping;
import demetra.sa.io.information.SaSpecificationMapping;
import demetra.tramoseats.TramoSeatsSpec;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsSpecMapping {

    public static final InformationSetSerializer<TramoSeatsSpec> SERIALIZER_V3 = new InformationSetSerializer<TramoSeatsSpec>() {
        @Override
        public InformationSet write(TramoSeatsSpec object, boolean verbose) {
            return TramoSeatsSpecMapping.write(object, verbose);
        }

        @Override
        public TramoSeatsSpec read(InformationSet info) {
            return TramoSeatsSpecMapping.read(info);
        }
        
        @Override
        public boolean match(DemetraVersion version){
            return version == DemetraVersion.JD3;
        }
    };

    public static final InformationSetSerializer<TramoSeatsSpec> SERIALIZER_LEGACY = new InformationSetSerializer<TramoSeatsSpec>() {
        @Override
        public InformationSet write(TramoSeatsSpec object, boolean verbose) {
            return TramoSeatsSpecMapping.writeLegacy(object, verbose);
        }

        @Override
        public TramoSeatsSpec read(InformationSet info) {
            return TramoSeatsSpecMapping.readLegacy(info);
        }

        @Override
        public boolean match(DemetraVersion version){
            return version == DemetraVersion.JD2;
        }
    };

    public static final String TRAMO = "tramo", SEATS = "seats", BENCH = "benchmarking", RSA = "method";

    public InformationSet write(TramoSeatsSpec spec, boolean verbose, DemetraVersion version) {
        return switch (version) {
            case JD2 -> writeLegacy(spec, verbose);
            default -> write(spec, verbose);
        };
        }

    public InformationSet write(TramoSeatsSpec spec, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ProcSpecification.ALGORITHM, TramoSeatsSpec.DESCRIPTOR_V3);
        InformationSet tinfo = TramoSpecMapping.write(spec.getTramo(), verbose);
        if (tinfo != null) {
            specInfo.add(TRAMO, tinfo);
        }
        InformationSet sinfo = DecompositionSpecMapping.write(spec.getSeats(), verbose);
        if (sinfo != null) {
            specInfo.add(SEATS, sinfo);
        }
        InformationSet binfo = SaBenchmarkingSpecMapping.write(spec.getBenchmarking(), verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    public InformationSet writeLegacy(TramoSeatsSpec spec, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ProcSpecification.ALGORITHM, TramoSeatsSpec.DESCRIPTOR_LEGACY);
        InformationSet tinfo = TramoSpecMapping.writeLegacy(spec.getTramo(), verbose);
        if (tinfo != null) {
            specInfo.add(TRAMO, tinfo);
        }
        InformationSet sinfo = DecompositionSpecMapping.write(spec.getSeats(), verbose);
        if (sinfo != null) {
            specInfo.add(SEATS, sinfo);
        }
        InformationSet binfo = SaBenchmarkingSpecMapping.write(spec.getBenchmarking(), verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    public TramoSeatsSpec read(InformationSet info) {
        if (info == null) {
            return null;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc == null) {
            return null;
        }
        if (desc.equals(TramoSeatsSpec.DESCRIPTOR_LEGACY)) {
            return readLegacy(info);
        } else if (desc.equals(TramoSeatsSpec.DESCRIPTOR_V3)) {
            return readV3(info);
        } else {
            return null;
        }
    }

    public TramoSeatsSpec readV3(InformationSet info) {
        if (info == null) {
            return TramoSeatsSpec.DEFAULT;
        }
        return TramoSeatsSpec.builder()
                .tramo(TramoSpecMapping.readV3(info.getSubSet(TRAMO)))
                .seats(DecompositionSpecMapping.read(info.getSubSet(SEATS)))
                .benchmarking(SaBenchmarkingSpecMapping.read(info.getSubSet(BENCH)))
                .build();
    }

    public TramoSeatsSpec readLegacy(InformationSet info) {
        if (info == null) {
            return TramoSeatsSpec.DEFAULT;
        }
        return TramoSeatsSpec.builder()
                .tramo(TramoSpecMapping.readLegacy(info.getSubSet(TRAMO)))
                .seats(DecompositionSpecMapping.read(info.getSubSet(SEATS)))
                .benchmarking(SaBenchmarkingSpecMapping.read(info.getSubSet(BENCH)))
                .build();
    }

    @ServiceProvider(SaSpecificationMapping.class)
    public static class Serializer implements SaSpecificationMapping {

        @Override
        public SaSpecification read(InformationSet info) {
            return TramoSeatsSpecMapping.read(info);
        }

        @Override
        public InformationSet write(SaSpecification spec, boolean verbose, DemetraVersion version) {
            if (spec instanceof TramoSeatsSpec tramoSeatsSpec) {
                return TramoSeatsSpecMapping.write(tramoSeatsSpec, verbose, version);
            } else {
                return null;
            }
        }

    }

}
