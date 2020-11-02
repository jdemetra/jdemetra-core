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

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.ComponentType;
import demetra.sa.SaSpecification;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.seats.DecompositionSpec;
import demetra.seats.SeatsSpec;
import demetra.tramo.TramoSpec;
import demetra.util.Validatable;
import java.util.Map;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class TramoSeatsSpec implements Validatable<TramoSeatsSpec>, SaSpecification{
    
    public static final String METHOD = "tramoseats";
    public static final String VERSION = "1.0.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    private static final TramoSeatsSpec DEFAULT = TramoSeatsSpec.builder().build();

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
                .benchmarking(SaBenchmarkingSpec.DEFAULT);
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
        RSA0 = TramoSeatsSpec.builder()
                .tramo(TramoSpec.TR0)
                .seats(DecompositionSpec.DEFAULT)
                .build();

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
    //</editor-fold>
}
