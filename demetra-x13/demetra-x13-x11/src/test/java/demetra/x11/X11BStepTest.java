/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSequence;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class X11BStepTest {

    private static final double[] WU5636 = {1.1608, 1.1208, 1.0883, 1.0704, 1.0628, 1.0378, 1.0353, 1.0604, 1.0501, 1.0706, 1.0338, 1.011, 1.0137, 0.9834, 0.9643, 0.947, 0.906, 0.9492, 0.9397, 0.9041, 0.8721, 0.8552, 0.8564, 0.8973, 0.9383, 0.9217, 0.9095, 0.892, 0.8742, 0.8532, 0.8607, 0.9005, 0.9111, 0.9059, 0.8883, 0.8924, 0.8833, 0.87, 0.8758, 0.8858, 0.917, 0.9554, 0.9922, 0.9778, 0.9808, 0.9811, 1.0014, 1.0183, 1.0622, 1.0773, 1.0807, 1.0848, 1.1582, 1.1663, 1.1372, 1.1139, 1.1222, 1.1692, 1.1702, 1.2286, 1.2613, 1.2646, 1.2262, 1.1985, 1.2007, 1.2138, 1.2266, 1.2176, 1.2218, 1.249, 1.2991, 1.3408, 1.3119, 1.3014, 1.3201, 1.2938, 1.2694, 1.2165, 1.2037, 1.2292, 1.2256, 1.2015, 1.1786, 1.1856, 1.2103, 1.1938, 1.202, 1.2271, 1.277, 1.265, 1.2684, 1.2811, 1.2727, 1.2611, 1.2881, 1.3213, 1.2999, 1.3074, 1.3242, 1.3516, 1.3511, 1.3419, 1.3716, 1.3622, 1.3896, 1.4227, 1.4684, 1.457, 1.4718, 1.4748, 1.5527, 1.5751, 1.5557, 1.5553, 1.577, 1.4975, 1.437, 1.3322, 1.2732, 1.3449, 1.3239, 1.2785, 1.305, 1.319, 1.365, 1.4016, 1.4088, 1.4268, 1.4562, 1.4816, 1.4914, 1.4614, 1.4272, 1.3686, 1.3569, 1.3406, 1.2565, 1.2209, 1.277, 1.2894, 1.3067, 1.3898, 1.3661, 1.322, 1.336, 1.3649, 1.3999, 1.4442, 1.4349, 1.4388, 1.4264, 1.4343, 1.377, 1.3706, 1.3556, 1.3179, 1.2905, 1.3224, 1.3201, 1.3162, 1.2789, 1.2526, 1.2288, 1.24, 1.2856, 1.2974, 1.2828, 1.3119, 1.3288, 1.3359, 1.2964, 1.3026, 1.2982, 1.3189, 1.308, 1.331, 1.3348, 1.3635, 1.3493, 1.3704};
    private static final double DELTA = 10E-13;

    private static final double[] A = ThreadLocalRandom.current().doubles(250, 0.5, 1.5).toArray();

    public X11BStepTest() {
    }

    @Test
    public void testProcess_Multiplicative() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    @Ignore
    public void testProcess_Multiplicative_Halfyearly() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 5;
        int frequency = 2;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_Additive_Quarterly() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 4;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Multiplicative2() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 4;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_AutoHenderson() {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_AutoHenderson2() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_AutoHenderson_Quarterly() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 4;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    @Ignore
    public void testProcess_AutoHenderson_Halfyearly() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 2;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_LogAdd() {
        String modeName = DecompositionMode.LogAdditive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    @Ignore
    public void testProcess_PseudoAdd() {
        String modeName = DecompositionMode.PseudoAdditive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X15_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X15.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X9_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X9.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X5_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X3_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X3.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X1_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X1.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Msr_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.Msr.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_X11Default_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.X11Default.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Stable_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.Stable.name();
        int filterLength = 13;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Henderson9_S3X5_Add() {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 9;
        int frequency = 12;
        testB(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    private void process(X11BStep instance, X11Context context, double[] input) {
        DoubleSequence b1 = DoubleSequence.of(input);
        if (context.isLogAdd()) {
            b1 = b1.log();
        }
        instance.process(b1, context);
    }

    private void testB(String modeName, String seasonalFilterOptionName, int filterLength, int frequency, double[] values) {
        X11BStep instance = new X11BStep();
        SeasonalFilterOption[] filters_new = new SeasonalFilterOption[frequency];
        ec.satoolkit.x11.SeasonalFilterOption[] filters_old = new ec.satoolkit.x11.SeasonalFilterOption[frequency];
        for (int i = 0; i < frequency; i++) {
            filters_new[i] = SeasonalFilterOption.valueOf(seasonalFilterOptionName);
            filters_old[i] = ec.satoolkit.x11.SeasonalFilterOption.valueOf(seasonalFilterOptionName);
        }

        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(DecompositionMode.valueOf(modeName))
                .initialSeasonalFilter(filters_new)
                .finalSeasonalFilter(filters_new)
                .trendFilterLength(filterLength)
                .period(frequency)
                .build();
        process(instance, context, values);

        X11Specification oldSpec = new X11Specification();
        oldSpec.setMode(ec.satoolkit.DecompositionMode.valueOf(modeName));
        oldSpec.setSeasonalFilters(filters_old);
        oldSpec.setHendersonFilterLength(filterLength);
        oldSpec.setForecastHorizon(0);

        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        old.setToolkit(ec.satoolkit.x11.X11Toolkit.create(oldSpec));
        X11Results old_Results = old.process(new TsData(TsFrequency.valueOf(frequency), 1999, 0, values, true));

        double[] expected_B1 = old_Results.getData("b-tables.b1", TsData.class).internalStorage();
        double[] actual_B1 = prepareForCompare(instance.getB1(), context);
        Assert.assertArrayEquals("Error in B1", expected_B1, actual_B1, DELTA);

        double[] expected_B2 = old_Results.getData("b-tables.b2", TsData.class).internalStorage();
        double[] actual_B2 = prepareForCompare(instance.getB2(), context);
        Assert.assertArrayEquals("Error in B2", expected_B2, actual_B2, DELTA);

        double[] expected_B4 = old_Results.getData("b-tables.b4", TsData.class).internalStorage();
        double[] actual_B4 = prepareForCompare(instance.getB4(), context);
        Assert.assertArrayEquals("Error in B4", expected_B4, actual_B4, DELTA);

        double[] expected_B5 = old_Results.getData("b-tables.b5", TsData.class).internalStorage();
        double[] actual_B5 = prepareForCompare(instance.getB5(), context);
        Assert.assertArrayEquals("Error in B5", expected_B5, actual_B5, DELTA);

        double[] expected_B6 = old_Results.getData("b-tables.b6", TsData.class).internalStorage();
        double[] actual_B6 = prepareForCompare(instance.getB6(), context);
        Assert.assertArrayEquals("Error in B6", expected_B6, actual_B6, DELTA);

        double[] expected_B7 = old_Results.getData("b-tables.b7", TsData.class).internalStorage();
        double[] actual_B7 = prepareForCompare(instance.getB7(), context);
        Assert.assertArrayEquals("Error in B7", expected_B7, actual_B7, DELTA);

        double[] expected_B9 = old_Results.getData("b-tables.b9", TsData.class).internalStorage();
        double[] actual_B9 = prepareForCompare(instance.getB9(), context);
        Assert.assertArrayEquals("Error in B9", expected_B9, actual_B9, DELTA);

        double[] expected_B10 = old_Results.getData("b-tables.b10", TsData.class).internalStorage();
        double[] actual_B10 = prepareForCompare(instance.getB10(), context);
        Assert.assertArrayEquals("Error in B10", expected_B10, actual_B10, DELTA);

        double[] expected_B11 = old_Results.getData("b-tables.b11", TsData.class).internalStorage();
        double[] actual_B11 = prepareForCompare(instance.getB11(), context);
        Assert.assertArrayEquals("Error in B11", expected_B11, actual_B11, DELTA);

        double[] expected_B13 = old_Results.getData("b-tables.b13", TsData.class).internalStorage();
        double[] actual_B13 = prepareForCompare(instance.getB13(), context);
        Assert.assertArrayEquals("Error in B13", expected_B13, actual_B13, DELTA);

        double[] expected_B17 = old_Results.getData("b-tables.b17", TsData.class).internalStorage();
        double[] actual_B17 = instance.getB17().toArray();
        Assert.assertArrayEquals("Error in B17", expected_B17, actual_B17, DELTA);

        double[] expected_B20 = old_Results.getData("b-tables.b20", TsData.class).internalStorage();
        double[] actual_B20 = prepareForCompare(instance.getB20(), context);
        Assert.assertArrayEquals("Error in B20", expected_B20, actual_B20, DELTA);
    }

    private double[] prepareForCompare(final DoubleSequence in, X11Context context) {
        DoubleSequence ds = in;
        if (context.isLogAdd()) {
            ds = ds.exp();
        }
        return ds.toArray();
    }

}
