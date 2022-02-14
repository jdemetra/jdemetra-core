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
package demetra.x13.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.SaBenchmarkingSpecMapping;
import demetra.sa.io.information.SaSpecificationMapping;
import demetra.x13.X13Spec;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13SpecMapping {

    public static final InformationSetSerializer<X13Spec> SERIALIZER_V3 = new InformationSetSerializer<X13Spec>() {
        @Override
        public InformationSet write(X13Spec object, boolean verbose) {
            return X13SpecMapping.write(object, verbose);
        }

        @Override
        public X13Spec read(InformationSet info) {
            return X13SpecMapping.read(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }

    };

    public static final InformationSetSerializer<X13Spec> SERIALIZER_LEGACY = new InformationSetSerializer<X13Spec>() {
        @Override
        public InformationSet write(X13Spec object, boolean verbose) {
            return X13SpecMapping.writeLegacy(object, verbose);
        }

        @Override
        public X13Spec read(InformationSet info) {
            return X13SpecMapping.readLegacy(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD2;
        }

    };

    public static final String REGARIMA = "regarima", X11 = "x11", BENCH = "benchmarking";

    public InformationSet write(X13Spec spec, boolean verbose, DemetraVersion version) {
        switch (version) {
            case JD2:
                return writeLegacy(spec, verbose);
            default:
                return write(spec, verbose);
        }
    }

    public InformationSet write(X13Spec spec, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ProcSpecification.ALGORITHM, X13Spec.DESCRIPTOR_V3);
        InformationSet tinfo = RegArimaSpecMapping.write(spec.getRegArima(), verbose);
        if (tinfo != null) {
            specInfo.add(REGARIMA, tinfo);
        }
        InformationSet sinfo = X11SpecMapping.write(spec.getX11(), verbose);
        if (sinfo != null) {
            specInfo.add(X11, sinfo);
        }
        InformationSet binfo = SaBenchmarkingSpecMapping.write(spec.getBenchmarking(), verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    public InformationSet writeLegacy(X13Spec spec, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ProcSpecification.ALGORITHM, X13Spec.DESCRIPTOR_LEGACY);
        InformationSet tinfo = RegArimaSpecMapping.writeLegacy(spec.getRegArima(), verbose);
        if (tinfo != null) {
            specInfo.add(REGARIMA, tinfo);
        }
        InformationSet sinfo = X11SpecMapping.write(spec.getX11(), verbose);
        if (sinfo != null) {
            specInfo.add(X11, sinfo);
        }
        InformationSet binfo = SaBenchmarkingSpecMapping.write(spec.getBenchmarking(), verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    public X13Spec read(InformationSet info) {
        if (info == null) {
            return null;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc == null) {
            return null;
        }
        if (desc.equals(X13Spec.DESCRIPTOR_LEGACY)) {
            return readLegacy(info);
        } else if (desc.equals(X13Spec.DESCRIPTOR_V3)) {
            return readV3(info);
        } else {
            return null;
        }
    }

    public X13Spec readV3(InformationSet info) {
        if (info == null) {
            return X13Spec.RSA0;
        }
        return X13Spec.builder()
                .regArima(RegArimaSpecMapping.readV3(info.getSubSet(REGARIMA)))
                .x11(X11SpecMapping.read(info.getSubSet(X11)))
                .benchmarking(SaBenchmarkingSpecMapping.read(info.getSubSet(BENCH)))
                .build();
    }

    public X13Spec readLegacy(InformationSet info) {
        if (info == null) {
            return X13Spec.RSA0;
        }
        return X13Spec.builder()
                .regArima(RegArimaSpecMapping.readLegacy(info.getSubSet(REGARIMA)))
                // .x11(X11SpecMapping.read(info.getSubSet(X11)))
                .x11(X11SpecMapping.readlegacy(info.getSubSet(X11), -1))
                .benchmarking(SaBenchmarkingSpecMapping.read(info.getSubSet(BENCH)))
                .build();
    }

    @ServiceProvider(SaSpecificationMapping.class)
    public static class Serializer implements SaSpecificationMapping {

        @Override
        public SaSpecification read(InformationSet info) {
            return X13SpecMapping.read(info);
        }

        @Override
        public InformationSet write(SaSpecification spec, boolean verbose, DemetraVersion version) {
            if (spec instanceof X13Spec) {
                return X13SpecMapping.write((X13Spec) spec, verbose, version);
            } else {
                return null;
            }
        }
    }
}
