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
import demetra.timeseries.TsUnit;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class MStlSpec implements ProcSpecification {

    private boolean multiplicative;
    private LoessSpec trendSpec;
    @lombok.Singular
    private List<SeasonalSpec> seasonalSpecs;
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

    public static final MStlSpec DEF_W = createDefault(7, true);

    /**
     * Creates a default specification for a series that has a given periodicity
     *
     * @param period The periodicity of the series
     * @param robust True for robust filtering, false otherwise.
     * @return
     */
    public static MStlSpec createDefault(int period, boolean robust) {
        return createDefault(period, StlSpec.DEF_SWINDOW, robust);
    }

    public static MStlSpec createDefault(int[] periods, boolean robust) {
        Builder builder = robust ? robustBuilder() : builder();
        for (int i = 0; i < periods.length; ++i) {
            builder.seasonalSpec(SeasonalSpec.createDefault(periods[i], true));
        }
        int p = periods[periods.length - 1];
        if (p % 2 == 0) {
            ++p;
        }
        return builder.trendSpec(LoessSpec.defaultTrend(p, true)).build();
    }

    public static MStlSpec createDefault(TsUnit unit, boolean robust) {
        int freq = unit.getAnnualFrequency();
        if (freq > 0) {
            return MStlSpec.createDefault(freq, true);
        } else if (unit.equals(TsUnit.WEEK)) {
            return MStlSpec.createDefault(52, true);
        } else if (unit.equals(TsUnit.DAY)) {
            return MStlSpec.createDefault(new int[]{7, 365}, true);
        } else {
            throw new UnsupportedOperationException("Not supported yet."); 
        }
    }

    /**
     * Given the length of the seasonal window, creates a default specification
     * for a series that has a given periodicity
     *
     * @param period
     * @param swindow
     * @param robust
     * @return
     */
    public static MStlSpec createDefault(int period, int swindow, boolean robust) {

        if (robust) {
            return robustBuilder()
                    .trendSpec(LoessSpec.defaultTrend(period, swindow, true))
                    .seasonalSpec(new SeasonalSpec(period, swindow, true))
                    .build();
        } else {
            return builder()
                    .trendSpec(LoessSpec.defaultTrend(period, swindow, true))
                    .seasonalSpec(new SeasonalSpec(period, swindow, true))
                    .build();
        }
    }

    public static final String METHOD = "mstlplus";
    public static final String FAMILY = "Seasonal adjustment";
    public static final String VERSION = "0.1.0.0";

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    }

    @Override
    public String display() {
        return "MSTL";
    }
}
