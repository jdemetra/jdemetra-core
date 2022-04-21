/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl;

import demetra.stl.SeasonalSpecification;
import demetra.stl.LoessSpecification;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class StlPlusSpecification implements ProcSpecification {

    private boolean multiplicative;
    private LoessSpecification trendSpec;
    @lombok.Singular
    private List<SeasonalSpecification> seasonalSpecs;
    private int innerLoopsCount, outerLoopsCount;
    private double robustWeightThreshold;

    private DoubleUnaryOperator robustWeightFunction, kernel;

    public static final double RWTHRESHOLD = 0.001;
    public static final DoubleUnaryOperator RWFUNCTION = x -> {
        double t = 1 - x * x;
        return t * t;
    };

    protected static final DoubleUnaryOperator KERNEL = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    public static Builder robustBuilder() {
        return new Builder()
                .innerLoopsCount(1)
                .outerLoopsCount(15)
                .kernel(KERNEL)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    public static Builder builder() {
        return new Builder()
                .innerLoopsCount(2)
                .outerLoopsCount(0)
                .kernel(KERNEL)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    public static final StlPlusSpecification DEFAULT = createDefault(7, true);

    /**
     * Creates a default specification for a series that has a given periodicity
     *
     * @param period The periodicity of the series
     * @param robust True for robust filtering, false otherwise.
     * @return
     */
    public static StlPlusSpecification createDefault(int period, boolean robust) {
        return createDefault(period, 7, robust);
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
    public static StlPlusSpecification createDefault(int period, int swindow, boolean robust) {

        if (robust) {
            return robustBuilder()
                    .trendSpec(LoessSpecification.defaultTrend(period, swindow))
                    .seasonalSpec(new SeasonalSpecification(period, swindow))
                    .build();
        } else {
            return builder()
                    .trendSpec(LoessSpecification.defaultTrend(period, swindow))
                    .seasonalSpec(new SeasonalSpecification(period, swindow))
                    .build();
        }
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
