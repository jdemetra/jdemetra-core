/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSequence;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class X11KernelTest_MixedFilters {

    private static final double[] WU5636 = {1.1608, 1.1208, 1.0883, 1.0704, 1.0628, 1.0378, 1.0353, 1.0604, 1.0501, 1.0706, 1.0338, 1.011, 1.0137, 0.9834, 0.9643, 0.947, 0.906, 0.9492, 0.9397, 0.9041, 0.8721, 0.8552, 0.8564, 0.8973, 0.9383, 0.9217, 0.9095, 0.892, 0.8742, 0.8532, 0.8607, 0.9005, 0.9111, 0.9059, 0.8883, 0.8924, 0.8833, 0.87, 0.8758, 0.8858, 0.917, 0.9554, 0.9922, 0.9778, 0.9808, 0.9811, 1.0014, 1.0183, 1.0622, 1.0773, 1.0807, 1.0848, 1.1582, 1.1663, 1.1372, 1.1139, 1.1222, 1.1692, 1.1702, 1.2286, 1.2613, 1.2646, 1.2262, 1.1985, 1.2007, 1.2138, 1.2266, 1.2176, 1.2218, 1.249, 1.2991, 1.3408, 1.3119, 1.3014, 1.3201, 1.2938, 1.2694, 1.2165, 1.2037, 1.2292, 1.2256, 1.2015, 1.1786, 1.1856, 1.2103, 1.1938, 1.202, 1.2271, 1.277, 1.265, 1.2684, 1.2811, 1.2727, 1.2611, 1.2881, 1.3213, 1.2999, 1.3074, 1.3242, 1.3516, 1.3511, 1.3419, 1.3716, 1.3622, 1.3896, 1.4227, 1.4684, 1.457, 1.4718, 1.4748, 1.5527, 1.5751, 1.5557, 1.5553, 1.577, 1.4975, 1.437, 1.3322, 1.2732, 1.3449, 1.3239, 1.2785, 1.305, 1.319, 1.365, 1.4016, 1.4088, 1.4268, 1.4562, 1.4816, 1.4914, 1.4614, 1.4272, 1.3686, 1.3569, 1.3406, 1.2565, 1.2209, 1.277, 1.2894, 1.3067, 1.3898, 1.3661, 1.322, 1.336, 1.3649, 1.3999, 1.4442, 1.4349, 1.4388, 1.4264, 1.4343, 1.377, 1.3706, 1.3556, 1.3179, 1.2905, 1.3224, 1.3201, 1.3162, 1.2789, 1.2526, 1.2288, 1.24, 1.2856, 1.2974, 1.2828, 1.3119, 1.3288, 1.3359, 1.2964, 1.3026, 1.2982, 1.3189, 1.308, 1.331, 1.3348, 1.3635, 1.3493, 1.3704};
    private static final double[] WU5637 = {1.9462, 1.9468, 1.9482, 1.9468, 1.952, 1.9553, 1.9516, 1.9496, 1.9474, 1.9468, 1.947, 1.9465, 1.9472, 1.9467, 1.9469, 1.9466, 1.9465, 1.9527, 1.9485, 1.9494, 1.9499, 1.9522, 1.9488, 1.9476, 1.9468, 1.9469, 1.9463, 1.9498, 1.9521, 1.9555, 1.954, 1.951, 1.9473, 1.9464, 1.9463, 1.9465, 1.9463, 1.9469, 1.9473, 1.9476, 1.9533, 1.9557, 1.9535, 1.9465, 1.9465, 1.9464, 1.9547, 1.9558, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9559, 1.9553, 1.9561, 1.9558, 1.9558, 1.9557, 1.9558, 1.9559, 1.9557, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558, 1.9558};
    private static final double[] A = ThreadLocalRandom.current().doubles(250, 0.5, 1.5).toArray();
    private static final double DELTA = 10E-13;

    @Test
    public void testProcess_Stable() {
        String modeName = DecompositionMode.Additive.name();
        int filterLength = 13;
        int frequency = 12;

        String[] seasonal_filter = new String[]{
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Stable.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name(),
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name(),
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Stable.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name()
        };
        testKernel(modeName, seasonal_filter, filterLength, frequency, WU5636);
    }

    @Test
    public void testProcess_Msr() {
        String modeName = DecompositionMode.Multiplicative.name();
        int filterLength = 13;
        int frequency = 12;

        String[] seasonal_filter = new String[]{
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Msr.name(), SeasonalFilterOption.Msr.name(), SeasonalFilterOption.S3X5.name(),
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Msr.name(), SeasonalFilterOption.S3X5.name(),
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Msr.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name()
        };
        testKernel(modeName, seasonal_filter, filterLength, frequency, WU5637);
    }

    @Test
    public void testProcess_Freq4() {
        String modeName = DecompositionMode.Additive.name();
        int filterLength = 5;
        int frequency = 4;

        String[] seasonal_filter = new String[]{
            SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.Stable.name(), SeasonalFilterOption.S3X5.name(), SeasonalFilterOption.S3X5.name()
        };
        testKernel(modeName, seasonal_filter, filterLength, frequency, WU5636);
    }
    
     @Test
    public void testProcess_Equal() {
        String modeName = DecompositionMode.Additive.name();
        int filterLength = 5;
        int frequency = 4;

        String[] seasonal_filter = new String[]{
            SeasonalFilterOption.S3X15.name(), SeasonalFilterOption.S3X15.name(), SeasonalFilterOption.S3X15.name(), SeasonalFilterOption.S3X15.name()};
        testKernel(modeName, seasonal_filter, filterLength, frequency, WU5636);
    }

    private void testKernel(String modeName, String[] seasonalFilterOptionName, int filterLength, int frequency, double[] values) {

        demetra.x11.X11Kernel instanceKernel = new X11Kernel();
        List<SigmavecOption> sigmavecOptions = new ArrayList<>();

        for (int i = 0; i < frequency; i++) {
            sigmavecOptions.add(SigmavecOption.Group1);//has to be removed
        }
        demetra.x11.X11Spec spec = demetra.x11.X11Spec.builder()
                .mode(DecompositionMode.valueOf(modeName))
                .hendersonFilterLength(filterLength)
                .sigmavec(sigmavecOptions)
                .calendarSigma(CalendarSigmaOption.All)
                .upperSigma(21)
                .lowerSigma(20)
                .filters(getNewSeasonalFilter(seasonalFilterOptionName))
                .buildWithoutValidation();//this has to be removed

        demetra.x11.X11Results x11Results = instanceKernel.process(
                demetra.timeseries.TsData.ofInternal(TsPeriod.of(TsUnit.ofAnnualFrequency(frequency), LocalDate.of(1900, Month.JANUARY, 1)), 
                        DoubleSequence.ofInternal(values)), spec);

        X11Specification oldSpec = new X11Specification();
        oldSpec.setMode(ec.satoolkit.DecompositionMode.valueOf(modeName));
        oldSpec.setHendersonFilterLength(filterLength);
        oldSpec.setCalendarSigma(ec.satoolkit.x11.CalendarSigma.All);
        oldSpec.setSigma(20, 21);

        oldSpec.setSeasonalFilters(getOldSeasonalFilter(seasonalFilterOptionName));
        oldSpec.setForecastHorizon(0);
        oldSpec.setBiasCorrection(ec.satoolkit.x11.BiasCorrection.Legacy);

        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        old.setToolkit(ec.satoolkit.x11.X11Toolkit.create(oldSpec));
        ec.satoolkit.x11.X11Results old_Results = old.process(new TsData(TsFrequency.valueOf(frequency), 1900, 0, values, true));

        double[] expected_B1 = old_Results.getData("b-tables.b1", TsData.class).internalStorage();
        double[] actual_B1 = x11Results.getB1().getValues().toArray();
        Assert.assertArrayEquals("Error in B1", expected_B1, actual_B1, DELTA);

        double[] expected_B2 = old_Results.getData("b-tables.b2", TsData.class).internalStorage();
        double[] actual_B2 = x11Results.getB2().getValues().toArray();
        Assert.assertArrayEquals("Error in B2", expected_B2, actual_B2, DELTA);

        double[] expected_B3 = old_Results.getData("b-tables.b3", TsData.class).internalStorage();
        double[] actual_B3 = x11Results.getB3().getValues().toArray();
        Assert.assertArrayEquals("Error in B3", expected_B3, actual_B3, DELTA);

        double[] expected_B4 = old_Results.getData("b-tables.b4", TsData.class).internalStorage();
        double[] actual_B4 = x11Results.getB4().getValues().toArray();
        Assert.assertArrayEquals("Error in B4", expected_B4, actual_B4, DELTA);

        double[] expected_B5 = old_Results.getData("b-tables.b5", TsData.class).internalStorage();
        double[] actual_B5 = x11Results.getB5().getValues().toArray();
        Assert.assertArrayEquals("Error in B5", expected_B5, actual_B5, DELTA);

        double[] expected_B6 = old_Results.getData("b-tables.b6", TsData.class).internalStorage();
        double[] actual_B6 = x11Results.getB6().getValues().toArray();
        Assert.assertArrayEquals("Error in B6", expected_B6, actual_B6, DELTA);

        double[] expected_B7 = old_Results.getData("b-tables.b7", TsData.class).internalStorage();
        double[] actual_B7 = x11Results.getB7().getValues().toArray();
        Assert.assertArrayEquals("Error in B7", expected_B7, actual_B7, DELTA);

        double[] expected_B8 = old_Results.getData("b-tables.b8", TsData.class).internalStorage();
        double[] actual_B8 = x11Results.getB8().getValues().toArray();
        Assert.assertArrayEquals("Error in B8", expected_B8, actual_B8, DELTA);

        double[] expected_B9 = old_Results.getData("b-tables.b9", TsData.class).internalStorage();
        double[] actual_B9 = x11Results.getB9().getValues().toArray();
        Assert.assertArrayEquals("Error in B9", expected_B9, actual_B9, DELTA);

        double[] expected_B10 = old_Results.getData("b-tables.b10", TsData.class).internalStorage();
        double[] actual_B10 = x11Results.getB10().getValues().toArray();
        Assert.assertArrayEquals("Error in B10", expected_B10, actual_B10, DELTA);

        double[] expected_C13 = old_Results.getData("c-tables.c13", TsData.class).internalStorage();
        double[] actual_C13 = x11Results.getC13().getValues().toArray();
        Assert.assertArrayEquals("Error in D1", expected_C13, actual_C13, DELTA);

        double[] expected_D1 = old_Results.getData("d-tables.d1", TsData.class).internalStorage();
        double[] actual_D1 = x11Results.getD1().getValues().toArray();
        Assert.assertArrayEquals("Error in D1", expected_D1, actual_D1, DELTA);
        double[] expected_D2 = old_Results.getData("d-tables.d2", TsData.class).cleanExtremities().internalStorage();
        double[] actual_D2 = x11Results.getD2().getValues().toArray();
        Assert.assertArrayEquals("Error in D2", expected_D2, actual_D2, DELTA);
        double[] expected_D4 = old_Results.getData("d-tables.d4", TsData.class).cleanExtremities().internalStorage();
        double[] actual_D4 = x11Results.getD4().getValues().toArray();
        Assert.assertArrayEquals("Error in D4", expected_D4, actual_D4, DELTA);
        double[] expected_D5 = old_Results.getData("d-tables.d5", TsData.class).internalStorage();
        double[] actual_D5 = x11Results.getD5().getValues().toArray();
        Assert.assertArrayEquals("Error in D5", expected_D5, actual_D5, DELTA);
        double[] expected_D6 = old_Results.getData("d-tables.d6", TsData.class).internalStorage();
        double[] actual_D6 = x11Results.getD6().getValues().toArray();
        Assert.assertArrayEquals("Error in D6", expected_D6, actual_D6, DELTA);
        double[] expected_D7 = old_Results.getData("d-tables.d7", TsData.class).internalStorage();
        double[] actual_D7 = x11Results.getD7().getValues().toArray();
        Assert.assertArrayEquals("Error in D7", expected_D7, actual_D7, DELTA);
        double[] expected_D9 = old_Results.getData("d-tables.d9", TsData.class).internalStorage();
        double[] actual_D9 = x11Results.getD9().getValues().toArray();
        Assert.assertArrayEquals("Error in D9", expected_D9, actual_D9, DELTA);
        double[] expected_D10 = old_Results.getData("d-tables.d10", TsData.class).internalStorage();
        double[] actual_D10 = x11Results.getD10().getValues().toArray();
        Assert.assertArrayEquals("Error in D10", expected_D10, actual_D10, DELTA);
        double[] expected_D11 = old_Results.getData("d-tables.d11", TsData.class).internalStorage();
        double[] actual_D11 = x11Results.getD11().getValues().toArray();
        Assert.assertArrayEquals("Error in D11", expected_D11, actual_D11, DELTA);
        double[] expected_D12 = old_Results.getData("d-tables.d12", TsData.class).internalStorage();
        double[] actual_D12 = x11Results.getD12().getValues().toArray();
        Assert.assertArrayEquals("Error in D12", expected_D12, actual_D12, DELTA);
        double[] expected_D13 = old_Results.getData("d-tables.d13", TsData.class).internalStorage();
        double[] actual_D13 = x11Results.getD13().getValues().toArray();
        Assert.assertArrayEquals("Error in D13", expected_D13, actual_D13, DELTA);
    }

    private List<SeasonalFilterOption> getNewSeasonalFilter(String[] filter) {
        List<SeasonalFilterOption> result = new ArrayList<>();
        for (String f : filter) {
            result.add(SeasonalFilterOption.valueOf(f));
        }
        return result;
    }

    private ec.satoolkit.x11.SeasonalFilterOption[] getOldSeasonalFilter(String[] filter) {
        List<ec.satoolkit.x11.SeasonalFilterOption> result = new ArrayList<>();
        for (String f : filter) {
            result.add(ec.satoolkit.x11.SeasonalFilterOption.valueOf(f));
        }
        return result.toArray(new ec.satoolkit.x11.SeasonalFilterOption[0]);
    }

}
