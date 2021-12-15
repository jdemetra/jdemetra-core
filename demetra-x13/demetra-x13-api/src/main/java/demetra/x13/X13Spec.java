/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.x13;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.processing.AlgorithmDescriptor;
import demetra.regarima.RegArimaSpec;
import demetra.sa.DecompositionMode;
import demetra.sa.SaSpecification;
import static demetra.sa.SaSpecification.FAMILY;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.util.Validatable;
import demetra.x11.X11Spec;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public class X13Spec implements Validatable<X13Spec>, SaSpecification {

    public static final String METHOD = "x13";
    public static final String VERSION = "0.1.0.0";
    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR_V3;
    }
    private static final X13Spec DEFAULT = X13Spec.builder().build();

    @lombok.NonNull
    private RegArimaSpec regArima;
    @lombok.NonNull
    private X11Spec x11;
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .regArima(RegArimaSpec.DEFAULT_ENABLED)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public X13Spec validate() throws IllegalArgumentException {
        regArima.validate();
        x11.validate();
        benchmarking.validate();
        return this;
    }

    public static class Builder implements Validatable.Builder<X13Spec> {
    }

    //<editor-fold defaultstate="collapsed" desc="Default specifications">
    public static final X13Spec RSAX11, RSA0, RSA1, RSA2, RSA3, RSA4, RSA5;

    public static final X13Spec[] allSpecifications() {
        return new X13Spec[]{RSAX11, RSA0, RSA1, RSA2, RSA3, RSA4, RSA5};
    }

    static {
        RSAX11 = X13Spec.builder()
                .regArima(RegArimaSpec.RGDISABLED)
                .x11(X11Spec.builder()
                        .mode(DecompositionMode.Multiplicative)
                        .forecastHorizon(0)
                        .build())
                .build();
        RSA0 = X13Spec.builder()
                .regArima(RegArimaSpec.RG0)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
        RSA1 = X13Spec.builder()
                .regArima(RegArimaSpec.RG1)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
        RSA2 = X13Spec.builder()
                .regArima(RegArimaSpec.RG2)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
        RSA3 = X13Spec.builder()
                .regArima(RegArimaSpec.RG3)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
        RSA4 = X13Spec.builder()
                .regArima(RegArimaSpec.RG4)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
        RSA5 = X13Spec.builder()
                .regArima(RegArimaSpec.RG5)
                .x11(X11Spec.DEFAULT_UNDEFINED)
                .build();
    }

    public static X13Spec fromString(String name) {
        if (name.equalsIgnoreCase("X11") || name.equalsIgnoreCase("X11")) {
            return RSAX11;
        }
        if (name.equalsIgnoreCase("RSA0") || name.equalsIgnoreCase("RSA0")) {
            return RSA0;
        }
        if (name.equalsIgnoreCase("RSA1") || name.equalsIgnoreCase("RSA1")) {
            return RSA1;
        }
        if (name.equalsIgnoreCase("RSA2c") || name.equalsIgnoreCase("RSA2")) {
            return RSA2;
        }
        if (name.equalsIgnoreCase("RSA3") || name.equalsIgnoreCase("RSA3")) {
            return RSA3;
        }
        if (name.equalsIgnoreCase("RSA4c") || name.equalsIgnoreCase("RSA4")) {
            return RSA4;
        }
        if (name.equalsIgnoreCase("RSA5c") || name.equalsIgnoreCase("RSA5")) {
            return RSA5;
        }
        throw new X13Exception();
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
        return SMETHOD;
    }

    private static final String SMETHOD = "TS";

}
