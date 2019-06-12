/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.data.DoubleSequence;
import demetra.x11.SeasonalFilterOption;
import ec.satoolkit.x11.DefaultSeasonalFilteringStrategy;
import ec.satoolkit.x11.FilterFactory;
import ec.satoolkit.x11.FilteredMeanEndPoints;
import ec.satoolkit.x11.MsrTable;
import ec.satoolkit.x11.SeasonalFilterFactory;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author s4504gn
 */
public class MsrFilterSelectionTest {

    private static final double DELTA = 10E-8;

    private static final double[] WU5636 = {1.1608, 1.1208, 1.0883, 1.0704, 1.0628, 1.0378, 1.0353, 1.0604, 1.0501, 1.0706, 1.0338, 1.011, 1.0137, 0.9834, 0.9643, 0.947, 0.906, 0.9492, 0.9397, 0.9041, 0.8721, 0.8552, 0.8564, 0.8973, 0.9383, 0.9217, 0.9095, 0.892, 0.8742, 0.8532, 0.8607, 0.9005, 0.9111, 0.9059, 0.8883, 0.8924, 0.8833, 0.87, 0.8758, 0.8858, 0.917, 0.9554, 0.9922, 0.9778, 0.9808, 0.9811, 1.0014, 1.0183, 1.0622, 1.0773, 1.0807, 1.0848, 1.1582, 1.1663, 1.1372, 1.1139, 1.1222, 1.1692, 1.1702, 1.2286, 1.2613, 1.2646, 1.2262, 1.1985, 1.2007, 1.2138, 1.2266, 1.2176, 1.2218, 1.249, 1.2991, 1.3408, 1.3119, 1.3014, 1.3201, 1.2938, 1.2694, 1.2165, 1.2037, 1.2292, 1.2256, 1.2015, 1.1786, 1.1856, 1.2103, 1.1938, 1.202, 1.2271, 1.277, 1.265, 1.2684, 1.2811, 1.2727, 1.2611, 1.2881, 1.3213, 1.2999, 1.3074, 1.3242, 1.3516, 1.3511, 1.3419, 1.3716, 1.3622, 1.3896, 1.4227, 1.4684, 1.457, 1.4718, 1.4748, 1.5527, 1.5751, 1.5557, 1.5553, 1.577, 1.4975, 1.437, 1.3322, 1.2732, 1.3449, 1.3239, 1.2785, 1.305, 1.319, 1.365, 1.4016, 1.4088, 1.4268, 1.4562, 1.4816, 1.4914, 1.4614, 1.4272, 1.3686, 1.3569, 1.3406, 1.2565, 1.2209, 1.277, 1.2894, 1.3067, 1.3898, 1.3661, 1.322, 1.336, 1.3649, 1.3999, 1.4442, 1.4349, 1.4388, 1.4264, 1.4343, 1.377, 1.3706, 1.3556, 1.3179, 1.2905, 1.3224, 1.3201, 1.3162, 1.2789, 1.2526, 1.2288, 1.24, 1.2856, 1.2974, 1.2828, 1.3119, 1.3288, 1.3359, 1.2964, 1.3026, 1.2982, 1.3189, 1.308, 1.331, 1.3348, 1.3635, 1.3493, 1.3704};
    private static final double[] WU5637 = {1.9462, 1.9468, 1.9482, 1.9468, 1.952, 1.9553, 1.9516, 1.9496, 1.9474, 1.9468, 1.947, 1.9465, 1.9472, 1.9467, 1.9469, 1.9466, 1.9465, 1.9527, 1.9485, 1.9494, 1.9499, 1.9522, 1.9488, 1.9476, 1.9468, 1.9469, 1.9463, 1.9498, 1.9521, 1.9555, 1.954, 1.951, 1.9473, 1.9464, 1.9463, 1.9465, 1.9463, 1.9469, 1.9473, 1.9476, 1.9533, 1.9557, 1.9535, 1.9465, 1.9465, 1.9464, 1.9547, 1.9558, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9553, 1.9561, 1.9558, 1.9558, 1.9557, 1.9558, 1.9559, 1.9557, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558};
    private static final double[] A = ThreadLocalRandom.current().doubles(250, 0.5, 1.5).toArray();

    private final int[] frequencies = new int[]{4, 12};
    private final String[] decomposition_modes = new String[]{"Additive", "Multiplicative"};

    public MsrFilterSelectionTest() {
    }

    @Test
    public void testGlobalMSR() {

        for (int freq : frequencies) {
            for (String mode : decomposition_modes) {
                processMSR(mode, freq, A);
                processMSR(mode, freq, WU5636);
            }
        }
    }

    @Test
    @Ignore
    public void test_pseudoadd() {
        for (int freq : frequencies) {
            processMSR("PseudoAdditive", freq, WU5636);
            processMSR("PseudoAdditive", freq, WU5637);
        }
    }

    @Test
    public void test_cutYears() {
        int freq = 12;
        int numberOfYears = 6;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5636, 0, freq * numberOfYears + 3));
    }
    /*
     * Range 1:     msr < 2.5
     * Range 2:     2.5 <= msr < 3.5 
     * Range 3:     3.5 <= msr < 5.5
     * Range 4:     5.5 <= msr < 6.5
     * Range 5:     6.5 <= msr
     */
    @Test
    public void test_Range1() {
        // Msr = 1.088
        int freq = 4;
        int numberOfYears = 7;
        int startOfTimeseriesIndex = 30;
        processMSR("Multiplicative", freq, Arrays.copyOfRange(WU5636, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));

    }

    @Test
    public void test_Range1_upperLimit() {
        // Msr = 2.49
        int freq = 4;
        int numberOfYears = 41;
        int startOfTimeseriesIndex = 13;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5636, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range2_lowerLimit() {
        // Msr = 2.56
        int freq = 12;
        int numberOfYears = 11;
        int startOfTimeseriesIndex = 28;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5637, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range2_upperLimit() {
        // Msr = 3.32
        int freq = 4;
        int numberOfYears = 30;
        int startOfTimeseriesIndex = 34;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5637, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range3_lowerLimit() {
        // Msr = 3.62
        int freq = 12;
        int numberOfYears = 6;
        int startOfTimeseriesIndex = 0;
        processMSR("Additive", freq, Arrays.copyOf(WU5636, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range3_upperLimit() {
        // Msr = 5.48
        int freq = 4;
        int numberOfYears = 28;
        int startOfTimeseriesIndex = 47;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5637, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range4_lowerLimit() {
        // Msr = 5.73
        int freq = 12;
        int numberOfYears = 6;
        int startOfTimeseriesIndex = 1;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5636, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range4_upperLimit() {
        // Msr = 6.13
        int freq = 4;
        int numberOfYears = 6;
        int startOfTimeseriesIndex = 10;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5637, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range5_lowerLimit() {
        // Msr = 6.53
        int freq = 4;
        int numberOfYears = 12;
        int startOfTimeseriesIndex = 3;
        processMSR("Additive", freq, Arrays.copyOfRange(WU5637, startOfTimeseriesIndex, startOfTimeseriesIndex + freq * numberOfYears));
    }

    @Test
    public void test_Range5() {
        // Msr = 8.49
        int freq = 4;
        int numberOfYears = 7;
        int startOfTimeseriesIndex = 0;
        processMSR("Multiplicative", freq, Arrays.copyOf(WU5637, startOfTimeseriesIndex + freq * numberOfYears));
    }

    private void processMSR(String modeName, int frequency, double[] data) {

        SeasonalFilterOption[] filter_new = new SeasonalFilterOption[frequency];

        for (int i = 0; i < frequency; i++) {
            filter_new[i] = SeasonalFilterOption.Msr;
        }

        // neu
        DoubleSequence series = DoubleSequence.ofInternal(data);
        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(demetra.sa.DecompositionMode.valueOf(modeName))
                .period(frequency)
                .firstPeriod(0)
                .initialSeasonalFilter(filter_new)
                .finalSeasonalFilter(filter_new)
                .build();

        MsrFilterSelection table = new MsrFilterSelection();
        table.doMSR(series, context);

        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(TsFrequency.valueOf(frequency), 1999, 0, data, true);
        MsrTable oldMsrTable = doOldMSR(s, ec.satoolkit.DecompositionMode.valueOf(modeName));

        double newMSR = table.getGlobalMsr();
        double oldMSR = oldMsrTable.getGlobalMsr();
        Assert.assertEquals("Error in MSR", oldMSR, newMSR, DELTA);
    }

    private MsrTable doOldMSR(ec.tstoolkit.timeseries.simplets.TsData s, ec.satoolkit.DecompositionMode mode) {
        // remove incomplete year
        ec.tstoolkit.timeseries.simplets.TsDomain rdomain = s.getDomain();//.drop(context.getBackcastHorizon(), context.getForecastHorizon());
        ec.tstoolkit.maths.linearfilters.SymmetricFilter f7 = FilterFactory.makeSymmetricFilter(7);
        ec.satoolkit.x11.DefaultSeasonalFilteringStrategy fseas = new DefaultSeasonalFilteringStrategy(
                f7, new FilteredMeanEndPoints(f7));
        MsrTable rms = calculateMsr(fseas, rdomain, s, mode);
        double grms = rms.getGlobalMsr();

        int ndrop = rdomain.getEnd().getPosition();
        if (ndrop != 0) {
            rdomain = rdomain.drop(0, ndrop);
            rms = calculateMsr(fseas, rdomain, s, mode);
            grms = rms.getGlobalMsr();
        }
        int freq = s.getFrequency().intValue();
        ec.satoolkit.x11.IFiltering finalSeasonalFilter = null;
        if (!Double.isInfinite(grms)) {
            finalSeasonalFilter = ec.satoolkit.x11.SeasonalFilterFactory.getFilteringStrategyForGlobalRMS(grms);
        }

        while (finalSeasonalFilter == null && rdomain.getLength() / freq >= 6) {
            rdomain = rdomain.drop(0, freq);
            rms = calculateMsr(fseas, rdomain, s, mode);
            grms = rms.getGlobalMsr();
            finalSeasonalFilter = ec.satoolkit.x11.SeasonalFilterFactory.getFilteringStrategyForGlobalRMS(grms);
        }

        return rms;
    }

    private MsrTable calculateMsr(DefaultSeasonalFilteringStrategy fseas,
            ec.tstoolkit.timeseries.simplets.TsDomain rdomain,
            ec.tstoolkit.timeseries.simplets.TsData s,
            ec.satoolkit.DecompositionMode mode) {

        ec.tstoolkit.timeseries.simplets.TsData s1 = fseas.process(s, rdomain);
        ec.tstoolkit.timeseries.simplets.TsData s2;
        if (ec.satoolkit.DecompositionMode.PseudoAdditive.equals(mode)) {
            s2 = s.minus(s1).plus(1);
        } else {
            if (mode != ec.satoolkit.DecompositionMode.Multiplicative && mode != ec.satoolkit.DecompositionMode.PseudoAdditive) {
                s2 = ec.tstoolkit.timeseries.simplets.TsData.subtract(s, s1);
            } else {
                s2 = ec.tstoolkit.timeseries.simplets.TsData.divide(s, s1);
            }
        }
        return MsrTable.create(s1, s2, ec.satoolkit.DecompositionMode.Multiplicative.equals(mode));
    }
}
