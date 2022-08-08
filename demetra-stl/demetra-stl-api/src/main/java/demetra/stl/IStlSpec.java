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
package demetra.stl;

import demetra.data.WeightFunction;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import java.util.List;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class IStlSpec implements ProcSpecification {

    @lombok.Value
    public static class PeriodSpec {
        
        public static PeriodSpec createDefault(int period){
            return new PeriodSpec(LoessSpec.defaultTrend(period), SeasonalSpec.createDefault(period));
        }

        private LoessSpec trendSpec;
        private SeasonalSpec seasonalSpec;

    }

    private boolean multiplicative;
    @lombok.Singular
    private List<PeriodSpec> periodSpecs;
    private int innerLoopsCount, outerLoopsCount;
    private double robustWeightThreshold;

    private WeightFunction robustWeightFunction;

    public static final double RWTHRESHOLD = 0.001;
    public static final WeightFunction RWFUNCTION = WeightFunction.BIWEIGHT;

    public static Builder robustBuilder() {
        return new Builder()
                .innerLoopsCount(1)
                .outerLoopsCount(15)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    public static Builder builder() {
        return new Builder()
                .innerLoopsCount(2)
                .outerLoopsCount(0)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    public static final IStlSpec DEFAULT = createDefault(true, 7);

    /**
     * Creates a default specification for a series that has a given periodicity
     *
     * @param robust True for robust filtering, false otherwise.
     * @param periods
     * @return
     */
    public static IStlSpec createDefault(boolean robust, int... periods) {
        Builder builder;
        if (robust) {
            builder = robustBuilder();
        } else {
            builder = builder();
        }
        for (int i=0; i<periods.length; ++i)
            builder.periodSpec(PeriodSpec.createDefault(periods[i]));
        return builder.build();
    }

    public static final String METHOD = "stlplus";
    public static final String FAMILY = "Seasonal adjustment";
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
