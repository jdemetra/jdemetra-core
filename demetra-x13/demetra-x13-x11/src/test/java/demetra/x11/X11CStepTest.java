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
public class X11CStepTest {

    private static final double[] WU5636 = {1.1608, 1.1208, 1.0883, 1.0704, 1.0628, 1.0378, 1.0353, 1.0604, 1.0501, 1.0706, 1.0338, 1.011, 1.0137, 0.9834, 0.9643, 0.947, 0.906, 0.9492, 0.9397, 0.9041, 0.8721, 0.8552, 0.8564, 0.8973, 0.9383, 0.9217, 0.9095, 0.892, 0.8742, 0.8532, 0.8607, 0.9005, 0.9111, 0.9059, 0.8883, 0.8924, 0.8833, 0.87, 0.8758, 0.8858, 0.917, 0.9554, 0.9922, 0.9778, 0.9808, 0.9811, 1.0014, 1.0183, 1.0622, 1.0773, 1.0807, 1.0848, 1.1582, 1.1663, 1.1372, 1.1139, 1.1222, 1.1692, 1.1702, 1.2286, 1.2613, 1.2646, 1.2262, 1.1985, 1.2007, 1.2138, 1.2266, 1.2176, 1.2218, 1.249, 1.2991, 1.3408, 1.3119, 1.3014, 1.3201, 1.2938, 1.2694, 1.2165, 1.2037, 1.2292, 1.2256, 1.2015, 1.1786, 1.1856, 1.2103, 1.1938, 1.202, 1.2271, 1.277, 1.265, 1.2684, 1.2811, 1.2727, 1.2611, 1.2881, 1.3213, 1.2999, 1.3074, 1.3242, 1.3516, 1.3511, 1.3419, 1.3716, 1.3622, 1.3896, 1.4227, 1.4684, 1.457, 1.4718, 1.4748, 1.5527, 1.5751, 1.5557, 1.5553, 1.577, 1.4975, 1.437, 1.3322, 1.2732, 1.3449, 1.3239, 1.2785, 1.305, 1.319, 1.365, 1.4016, 1.4088, 1.4268, 1.4562, 1.4816, 1.4914, 1.4614, 1.4272, 1.3686, 1.3569, 1.3406, 1.2565, 1.2209, 1.277, 1.2894, 1.3067, 1.3898, 1.3661, 1.322, 1.336, 1.3649, 1.3999, 1.4442, 1.4349, 1.4388, 1.4264, 1.4343, 1.377, 1.3706, 1.3556, 1.3179, 1.2905, 1.3224, 1.3201, 1.3162, 1.2789, 1.2526, 1.2288, 1.24, 1.2856, 1.2974, 1.2828, 1.3119, 1.3288, 1.3359, 1.2964, 1.3026, 1.2982, 1.3189, 1.308, 1.331, 1.3348, 1.3635, 1.3493, 1.3704};
    private static final double DELTA = 10E-13;

    private static final double[] A = ThreadLocalRandom.current().doubles(250, 0.5, 1.5).toArray();

    public X11CStepTest() {
    }

    @Test
    public void testProcess_Multiplicative() throws Exception {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Multiplicative2() throws Exception {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 4;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    @Ignore
    public void testProcess_LogAdd() throws Exception {
        String modeName = DecompositionMode.LogAdditive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X15_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X15.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X9_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X9.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X5_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X3_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X3.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_S3X1_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X1.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    @Ignore
    public void testProcess_Msr_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.Msr.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_X11Default_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.X11Default.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Stable_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.Stable.name();
        int filterLength = 13;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_Henderson9_S3X5_Add() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 9;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, A);
    }

    @Test
    public void testProcess_AutoHenderson() throws Exception {
        String modeName = DecompositionMode.Multiplicative.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_AutoHenderson2() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 12;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_AutoHenderson_Quarterly() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 4;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    @Test
    @Ignore
    public void testProcess_AutoHenderson_Halfyearly() throws Exception {
        String modeName = DecompositionMode.Additive.name();
        String seasonalFilterOptionName = SeasonalFilterOption.S3X5.name();
        int filterLength = 0;
        int frequency = 2;
        testC(modeName, seasonalFilterOptionName, filterLength, frequency, WU5636);
    }

    private void process(X11CStep instance, X11Context context, double[] input) throws Exception {
        DoubleSequence b1 = DoubleSequence.of(input);
        X11BStep bStep = new X11BStep();
        bStep.process(b1, context);
        instance.process(b1, context.remove(b1, bStep.getB20()), context);
    }

    private void testC(String modeName, String seasonalFilterOptionName, int filterLength, int frequency, double[] values) throws Exception {
        X11CStep instance = new X11CStep();
        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(DecompositionMode.valueOf(modeName))
                .initialSeasonalFilter(SeasonalFilterOption.valueOf(seasonalFilterOptionName))
                .finalSeasonalFilter(SeasonalFilterOption.valueOf(seasonalFilterOptionName))
                .trendFilterLength(filterLength)
                .period(frequency)
                .build();
        process(instance, context, values);

        X11Specification oldSpec = new X11Specification();
        oldSpec.setMode(ec.satoolkit.DecompositionMode.valueOf(modeName));
        oldSpec.setSeasonalFilter(ec.satoolkit.x11.SeasonalFilterOption.valueOf(seasonalFilterOptionName));
        oldSpec.setHendersonFilterLength(filterLength);
        oldSpec.setForecastHorizon(0);

        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        old.setToolkit(ec.satoolkit.x11.X11Toolkit.create(oldSpec));
        X11Results old_Results = old.process(new TsData(TsFrequency.valueOf(frequency), 1999, 0, values, true));

        double[] expected_C1 = old_Results.getData("c-tables.c1", TsData.class).internalStorage();
        double[] actual_C1 = instance.getC1().toArray();
        Assert.assertArrayEquals("Error in C1", expected_C1, actual_C1, DELTA);

        double[] expected_C2 = old_Results.getData("c-tables.c2", TsData.class).internalStorage();
        double[] actual_C2 = instance.getC2().toArray();
        Assert.assertArrayEquals("Error in C2", expected_C2, actual_C2, DELTA);

        double[] expected_C4 = old_Results.getData("c-tables.c4", TsData.class).internalStorage();
        double[] actual_C4 = instance.getC4().toArray();
        Assert.assertArrayEquals("Error in C4", expected_C4, actual_C4, DELTA);

        double[] expected_C5 = old_Results.getData("c-tables.c5", TsData.class).internalStorage();
        double[] actual_C5 = instance.getC5().toArray();
        Assert.assertArrayEquals("Error in C5", expected_C5, actual_C5, DELTA);

        double[] expected_C6 = old_Results.getData("c-tables.c6", TsData.class).internalStorage();
        double[] actual_C6 = instance.getC6().toArray();
        Assert.assertArrayEquals("Error in C6", expected_C6, actual_C6, DELTA);

        double[] expected_C7 = old_Results.getData("c-tables.c7", TsData.class).internalStorage();
        double[] actual_C7 = instance.getC7().toArray();
        Assert.assertArrayEquals("Error in C7", expected_C7, actual_C7, DELTA);

        double[] expected_C9 = old_Results.getData("c-tables.c9", TsData.class).internalStorage();
        double[] actual_C9 = instance.getC9().toArray();
        Assert.assertArrayEquals("Error in C9", expected_C9, actual_C9, DELTA);

        double[] expected_C10 = old_Results.getData("c-tables.c10", TsData.class).internalStorage();
        double[] actual_C10 = instance.getC10().toArray();
        Assert.assertArrayEquals("Error in C10", expected_C10, actual_C10, DELTA);

        double[] expected_C11 = old_Results.getData("c-tables.c11", TsData.class).internalStorage();
        double[] actual_C11 = instance.getC11().toArray();
        Assert.assertArrayEquals("Error in C11", expected_C11, actual_C11, DELTA);

        double[] expected_C13 = old_Results.getData("c-tables.c13", TsData.class).internalStorage();
        double[] actual_C13 = instance.getC13().toArray();
        Assert.assertArrayEquals("Error in C13", expected_C13, actual_C13, DELTA);

        double[] expected_C17 = old_Results.getData("c-tables.c17", TsData.class).internalStorage();
        double[] actual_C17 = instance.getC17().toArray();
        Assert.assertArrayEquals("Error in C17", expected_C17, actual_C17, DELTA);

        double[] expected_C20 = old_Results.getData("c-tables.c20", TsData.class).internalStorage();
        double[] actual_C20 = instance.getC20().toArray();
        Assert.assertArrayEquals("Error in C20", expected_C20, actual_C20, DELTA);
    }

}
