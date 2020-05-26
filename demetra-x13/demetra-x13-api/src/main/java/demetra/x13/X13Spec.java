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

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.processing.AlgorithmDescriptor;
import demetra.regarima.RegArimaSpec;
import demetra.sa.DecompositionMode;
import demetra.sa.SaSpecification;
import static demetra.sa.SaSpecification.FAMILY;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.util.Validatable;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Spec;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public class X13Spec implements Validatable<X13Spec>, SaSpecification {

    public static final String METHOD = "x13";
    public static final String VERSION = "1.0.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }
    private static final X13Spec DEFAULT = X13Spec.builder().build();

    private RegArimaSpec regArima;
    private X11Spec x11;
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .regArima(RegArimaSpec.builder().build())
                .x11(X11Spec.builder().build())
                .benchmarking(SaBenchmarkingSpec.builder().build());
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
                        .filter(SeasonalFilterOption.Msr)
                        .forecastHorizon(0)
                        .build())
                .build();
        RSA0 = X13Spec.builder()
                .regArima(RegArimaSpec.RG0)
                .x11(X11Spec.DEFAULT)
                .build();
        RSA1 = X13Spec.builder()
                .regArima(RegArimaSpec.RG1)
                .x11(X11Spec.DEFAULT)
                .build();
        RSA2 = X13Spec.builder()
                .regArima(RegArimaSpec.RG2)
                .x11(X11Spec.DEFAULT)
                .build();
        RSA3 = X13Spec.builder()
                .regArima(RegArimaSpec.RG3)
                .x11(X11Spec.DEFAULT)
                .build();
        RSA4 = X13Spec.builder()
                .regArima(RegArimaSpec.RG4)
                .x11(X11Spec.DEFAULT)
                .build();
        RSA5 = X13Spec.builder()
                .regArima(RegArimaSpec.RG5)
                .x11(X11Spec.DEFAULT)
                .build();
    }

    public static X13Spec fromString(String name) {
        if (name.equals("X11")) {
            return RSAX11;
        }
        if (name.equals("RSA0")) {
            return RSA0;
        }
        if (name.equals("RSA1")) {
            return RSA1;
        }
        if (name.equals("RSA2c")) {
            return RSA2;
        }
        if (name.equals("RSA3")) {
            return RSA3;
        }
        if (name.equals("RSA4c")) {
            return RSA4;
        }
        if (name.equals("RSA5c")) {
            return RSA5;
        }
        throw new X13Exception();
    }

    //</editor-fold>
}
