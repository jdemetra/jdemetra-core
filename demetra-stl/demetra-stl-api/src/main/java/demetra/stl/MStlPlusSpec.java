/*
 * Copyright 2023 National Bank of Belgium
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
package demetra.stl;

import demetra.highfreq.ExtendedAirlineModellingSpec;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.SaSpecification;
import static demetra.sa.SaSpecification.FAMILY;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.timeseries.TsUnit;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class MStlPlusSpec implements SaSpecification {

    public static final String METHOD = "mstlplus";
    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private ExtendedAirlineModellingSpec preprocessing;

    // We will use a default if null !
    private MStlSpec stl;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .preprocessing(ExtendedAirlineModellingSpec.DEFAULT_ENABLED)
                .stl(null);
    }

    @Override
    public String display() {
        return SMETHOD;
    }

    private static final String SMETHOD = "MSTL+";

    public static final MStlPlusSpec DEFAULT = MStlPlusSpec.builder().build();

    public MStlPlusSpec withPeriod(TsUnit unit) {
        TsUnit period = preprocessing.getPeriod();
        if (unit.equals(period)) {
            return this;
        }
        Builder builder = toBuilder();
        ExtendedAirlineModellingSpec nspec;
        MStlSpec dspec;
        if (unit.equals(TsUnit.UNDEFINED)) {
            nspec = preprocessing.toBuilder()
                    .period(unit)
                    .stochastic(null)
                    .build();
            dspec = null;
        } else {
            nspec = preprocessing.toBuilder()
                    .period(unit)
                    .stochastic(ExtendedAirlineSpec.createDefault(unit))
                    .build();
            dspec = MStlSpec.createDefault(unit, true);
        }
        return builder
                .preprocessing(nspec)
                .stl(dspec)
                .build();
    }

}
