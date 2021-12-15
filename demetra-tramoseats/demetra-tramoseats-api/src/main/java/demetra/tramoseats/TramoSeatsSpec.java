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
package demetra.tramoseats;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.SaSpecification;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.seats.DecompositionSpec;
import demetra.tramo.TramoSpec;
import demetra.util.Validatable;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class TramoSeatsSpec implements Validatable<TramoSeatsSpec>, SaSpecification{
    
    public static final String METHOD = "tramoseats";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR_V3;
    }

    public static final TramoSeatsSpec DEFAULT = TramoSeatsSpec.builder().build();

    @lombok.NonNull
    private TramoSpec tramo;
    
    @lombok.NonNull
    private DecompositionSpec seats;
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @Override
    public TramoSeatsSpec validate() throws IllegalArgumentException {
        tramo.validate();
        seats.validate();
        benchmarking.validate();
        return this;
    }

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .tramo(TramoSpec.DEFAULT)
                .seats(DecompositionSpec.DEFAULT)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }


    public static class Builder implements Validatable.Builder<TramoSeatsSpec> {
    }

    //<editor-fold defaultstate="collapsed" desc="Default specifications">
    public static final TramoSeatsSpec RSA0, RSA1, RSA2, RSA3, RSA4, RSA5, RSAfull;

    public static final TramoSeatsSpec[] allSpecifications() {
        return new TramoSeatsSpec[]{RSA0, RSA1, RSA2, RSA3, RSA4, RSA5, RSAfull};
    }

    static {
        RSA0 = TramoSeatsSpec.DEFAULT;
 
        RSA1 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR1)
                .seats(DecompositionSpec.DEFAULT)
                .build();

        RSA2 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR2)
                .seats(DecompositionSpec.DEFAULT)
                .build();

        RSA3 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR3)
                .seats(DecompositionSpec.DEFAULT)
                .build();

        RSA4 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR4)
                .seats(DecompositionSpec.DEFAULT)
                .build();

        RSA5 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR5)
                .seats(DecompositionSpec.DEFAULT)
                .build();

        RSAfull = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TRfull)
                .seats(DecompositionSpec.DEFAULT)
                .build();
    }

    public static TramoSeatsSpec fromString(String name) {
        switch (name) {
            case "RSA0":
            case "rsa0":
                return RSA0;
            case "RSA1":
            case "rsa1":
                return RSA1;
            case "RSA2":
            case "rsa2":
                return RSA2;
            case "RSA3":
            case "rsa3":
                return RSA3;
            case "RSA4":
            case "rsa4":
                return RSA4;
            case "RSA5":
            case "rsa5":
                return RSA5;
            case "RSAfull":
            case "rsafull":
                return RSAfull;
            default:
                throw new TramoSeatsException();
        }
    }
    //</editor-fold>
    
    @Override
    public String display() {
        if (this == RSA0) {
            return "RSA0";
        }
        if (this == RSA1) {
            return "RSA1";
        }
        if (this == RSA2) {
            return "RSA2";
        }
        if (this == RSA3) {
            return "RSA3";
        }
        if (this == RSA4) {
            return "RSA4";
        }
        if (this == RSA5) {
            return "RSA5";
        }
        if (this == RSAfull) {
            return "RSAfull";
        }
        if (equals(RSA0)) {
            return "RSA0";
        }
        if (equals(RSA1)) {
            return "RSA1";
        }
        if (equals(RSA2)) {
            return "RSA2";
        }
        if (equals(RSA3)) {
            return "RSA3";
        }
        if (equals(RSA4)) {
            return "RSA4";
        }
        if (equals(RSA5)) {
            return "RSA5";
        }
        if (equals(RSAfull)) {
            return "RSAfull";
        }
        return SMETHOD;
    }

    private static final String SMETHOD = "TS";
    
}
