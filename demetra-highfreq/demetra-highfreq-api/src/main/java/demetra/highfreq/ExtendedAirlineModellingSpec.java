/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.highfreq;

import demetra.modelling.highfreq.RegressionSpec;
import demetra.modelling.highfreq.OutlierSpec;
import demetra.modelling.highfreq.EstimateSpec;
import demetra.modelling.highfreq.SeriesSpec;
import demetra.modelling.highfreq.TransformSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.timeseries.TsUnit;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ExtendedAirlineModellingSpec implements ProcSpecification {
    
    @NonNull
    private TsUnit period;

    @NonNull
    private SeriesSpec series;

    private boolean enabled;

    @NonNull
    private EstimateSpec estimate;
    @NonNull
    private TransformSpec transform;
    @NonNull
    private RegressionSpec regression;
    // Will use a default if null
    private ExtendedAirlineSpec stochastic;
    @NonNull
    private OutlierSpec outlier;

    public static Builder builder() {
        return new Builder()
                .period(TsUnit.UNDEFINED)
                .series(SeriesSpec.DEFAULT)
                .enabled(true)
                .transform(TransformSpec.DEFAULT)
                .stochastic(null)
                .regression(RegressionSpec.DEFAULT)
                .outlier(OutlierSpec.DEFAULT_DISABLED)
                .estimate(EstimateSpec.DEFAULT);
    }
    
    public ExtendedAirlineModellingSpec withPeriod(@NonNull TsUnit unit){
        if (unit.equals(period))
            return this;
        Builder builder = toBuilder();
        ExtendedAirlineSpec nspec;
        if (unit.equals(TsUnit.UNDEFINED))
            nspec=null;
        else
            nspec=ExtendedAirlineSpec.createDefault(unit);
        return builder.period(unit)
                .stochastic(nspec)
                .build();
                    
    }

 
    public static final ExtendedAirlineModellingSpec DEFAULT_ENABLED = builder().build();
    public static final ExtendedAirlineModellingSpec DEFAULT_DISABLED = builder().enabled(false).build();

    public static final String METHOD = "extendedairline";
    public static final String FAMILY = "Modelling";
    public static final String VERSION = "0.1.0.0";

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    }

    @Override
    public String display() {
        return "Extended airline";
    }

}
