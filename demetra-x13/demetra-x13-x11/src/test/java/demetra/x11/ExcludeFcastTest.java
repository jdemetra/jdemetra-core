/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSequence;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x11.X11Toolkit;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class ExcludeFcastTest {

    double[] B1 = {2.6461, 2.5308, 2.4537, 2.379, 2.3565, 2.1487, 2.055, 2.0372, 2.0012, 1.9937, 1.9771, 1.9962, 1.9746, 1.9446, 1.9378, 1.917, 1.9045, 1.8899, 1.9104, 1.9103, 1.8961, 1.9079, 1.9039, 1.94, 1.9378, 1.9226, 1.9822, 1.9806, 1.985, 1.9798, 1.9811, 1.9711, 1.9719, 1.9785, 2.0053, 2.1, 2.1336, 2.1725, 2.2888, 2.3375, 2.3868, 2.4799, 2.5884, 2.6936, 2.7429, 2.8583, 2.9227, 3.0703, 3.1626, 3.2103, 3.3261, 3.3972, 3.4644, 3.5494, 3.6592, 3.7668, 3.8584, 3.9112, 3.9474, 4.0623, 4.035, 4.0059, 4.0334, 4.108, 4.172, 4.2327, 4.3251, 4.403, 4.4584, 4.512, 4.4207, 4.2558, 3.9833, 3.8197, 3.7109, 3.5593, 3.4212, 3.3163, 3.1038, 2.9132, 2.7541, 2.587, 2.3942, 2.1768, 1.9159, 1.8032, 1.7712, 1.7364, 1.6951, 1.5772, 1.5232, 1.4986, 1.4694, 1.4676, 1.4692, 1.4618, 1.4647, 1.4611, 1.4671, 1.5019, 1.5462, 1.5838, 1.6408, 1.6872, 1.7135, 1.746, 1.7641, 1.7677, 1.7584, 1.7457, 1.733, 1.718, 1.701, 1.6858, 1.6522, 1.6165, 1.5776, 1.526, 1.4788, 1.4305, 1.3779, 1.3334, 1.2923, 1.2435, 1.2045, 1.1589, 1.0913, 1.044, 1.0107, 0.9751, 0.9595, 0.9389, 0.9234, 0.9015, 0.8855, 0.8692, 0.8506, 0.8348, 0.8021, 0.776, 0.7522, 0.734, 0.7108, 0.6724, 0.6565, 0.6418, 0.6152, 0.5941, 0.5714, 0.5436, 0.5223, 0.5062, 0.4999, 0.4926, 0.4765, 0.464, 0.451, 0.4422, 0.4441, 0.4394, 0.433, 0.4192, 0.4068, 0.4087, 0.4005, 0.4015, 0.3937, 0.3772, 0.3677, 0.3507, 0.3362, 0.3342, 0.329, 0.3261, 0.3223, 0.3119, 0.3059, 0.301, 0.299, 0.2935, 0.2852, 0.2812, 0.2713, 0.2666, 0.2659, 0.2602, 0.255, 0.2486, 0.2438, 0.2384, 0.2376, 0.2307, 0.20375339911111273, 0.18324531122329432, 0.17194084678338567, 0.16135837689581206, 0.15197163048263918, 0.13725628553865504, 0.12256970403408764, 0.11178177453754014, 0.10084363454139757, 0.0925250062803331, 0.07965662061437864, 0.06352959314738488};

    SigmavecOption[] sigmavecOptions = new SigmavecOption[12];

    @Test
    public void excludeFcastFalseSelectTest() {
        for (int i = 0; i < 12; i++) {
            sigmavecOptions[i] = SigmavecOption.Group2;
        }
        sigmavecOptions[0] = SigmavecOption.Group1;
        sigmavecOptions[1] = SigmavecOption.Group1;
        sigmavecOptions[6] = SigmavecOption.Group1;
        excludeFcastTest(CalendarSigmaOption.Select, true, sigmavecOptions);
    }

    @Test
    public void excludeFcastFalseAllTest() {
        excludeFcastTest(CalendarSigmaOption.All, false, sigmavecOptions);
    }

    @Test
    public void excludeFcastFalseSignifTest() {
        excludeFcastTest(CalendarSigmaOption.Signif, false, sigmavecOptions);
    }

    @Test
    public void excludeFcastTrueSignifTest() {
        excludeFcastTest(CalendarSigmaOption.Signif, true, sigmavecOptions);
    }

    @Test
    public void excludeFcastFalseNoneTest() {
        excludeFcastTest(CalendarSigmaOption.None, false, sigmavecOptions);
    }

    @Test
    public void excludeFcastTrueAllTest() {
        excludeFcastTest(CalendarSigmaOption.All, true, sigmavecOptions);
    }

    @Test
    public void excludeFcastTrueNoneTest() {
        excludeFcastTest(CalendarSigmaOption.None, true, sigmavecOptions);
    }

    private void excludeFcastTest(CalendarSigmaOption option, boolean excludeFcast, SigmavecOption[] sigmavecOptions) {
        SeasonalFilterOption[] filters_new = new SeasonalFilterOption[12];
        ec.satoolkit.x11.SeasonalFilterOption[] filters_old = new ec.satoolkit.x11.SeasonalFilterOption[12];
        for (int i = 0; i < 12; i++) {
            filters_new[i] = SeasonalFilterOption.S3X3;
            filters_old[i] = ec.satoolkit.x11.SeasonalFilterOption.S3X3;
        }

        DoubleSequence b1 = DoubleSequence.of(B1);
        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(DecompositionMode.Additive)
                .finalSeasonalFilter(filters_new)
                .initialSeasonalFilter(filters_new)
                .trendFilterLength(5)
                .period(12)
                .calendarSigma(option)
                .sigmavecOptions(sigmavecOptions)
                .excludefcast(excludeFcast)
                .forecastHorizon(12)
                .build();

        X11BStep bStep = new X11BStep();
        X11CStep cStep = new X11CStep();
        bStep.process(b1, context);
        cStep.process(b1, context.remove(b1, bStep.getB20()), context);
        X11DStep instance = new X11DStep();
        instance.process(b1, context.remove(b1, cStep.getC20()), context);

//D-Step
        //alte Berechnung
        X11Specification oldSpec = new X11Specification();
        oldSpec.setMode(ec.satoolkit.DecompositionMode.Additive);
        oldSpec.setSeasonalFilter(ec.satoolkit.x11.SeasonalFilterOption.S3X3);
        oldSpec.setCalendarSigma(CalendarSigma.valueOf(option.name()));

        if (option.name().equals(CalendarSigma.Select.name())) {
            ec.satoolkit.x11.SigmavecOption[] sigmavecOptionOld = new ec.satoolkit.x11.SigmavecOption[sigmavecOptions.length];
            for (int i = 0; i < sigmavecOptionOld.length; i++) {
                sigmavecOptionOld[i] = ec.satoolkit.x11.SigmavecOption.valueOf(sigmavecOptions[i].name());
            }
            oldSpec.setSigmavec(sigmavecOptionOld);
        }

        oldSpec.setHendersonFilterLength(5);
        oldSpec.setForecastHorizon(-1);
        oldSpec.setExcludefcst(excludeFcast);
        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        X11Toolkit toolkit = ec.satoolkit.x11.X11Toolkit.create(oldSpec);
        toolkit.setPreprocessor(null);
        old.setToolkit(toolkit);
        ec.satoolkit.x11.X11Results old_Results = old.process(new TsData(TsFrequency.Monthly, 1999, 0, B1, true));

        double[] B1_old = old_Results.getData("b-tables.b1", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B1", B1_old, B1, DELTA);

        double[] actual_B4 = bStep.getB4().toArray();
        double[] B4_old = old_Results.getData("b-tables.b4", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B4", B4_old, actual_B4, DELTA);

        double[] actual_B20 = bStep.getB20().toArray();
        double[] B20_old = old_Results.getData("b-tables.b20", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B20", B20_old, actual_B20, DELTA);

        double[] actual_C1 = cStep.getC1().toArray();
        double[] C1_old = old_Results.getData("c-tables.c1", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in C1", C1_old, actual_C1, DELTA);

        double[] expected_D1 = old_Results.getData("d-tables.d1", TsData.class).internalStorage();
        double[] actual_D1 = instance.getD1().toArray();
        Assert.assertArrayEquals("Error in D1", expected_D1, actual_D1, DELTA);
        double[] expected_D2 = old_Results.getData("d-tables.d2", TsData.class).cleanExtremities().internalStorage();
        double[] actual_D2 = instance.getD2().toArray();
        Assert.assertArrayEquals("Error in D2", expected_D2, actual_D2, DELTA);
        double[] expected_D4 = old_Results.getData("d-tables.d4", TsData.class).cleanExtremities().internalStorage();
        double[] actual_D4 = instance.getD4().toArray();
        Assert.assertArrayEquals("Error in D4", expected_D4, actual_D4, DELTA);
        double[] expected_D5 = old_Results.getData("d-tables.d5", TsData.class).internalStorage();
        double[] actual_D5 = instance.getD5().toArray();
        Assert.assertArrayEquals("Error in D5", expected_D5, actual_D5, DELTA);
        double[] expected_D6 = old_Results.getData("d-tables.d6", TsData.class).internalStorage();
        double[] actual_D6 = instance.getD6().toArray();
        Assert.assertArrayEquals("Error in D6", expected_D6, actual_D6, DELTA);
        double[] expected_D7 = old_Results.getData("d-tables.d7", TsData.class).internalStorage();
        double[] actual_D7 = instance.getD7().toArray();
        Assert.assertArrayEquals("Error in D7", expected_D7, actual_D7, DELTA);

        double[] actual_D9 = instance.getD9().toArray();
        double[] D9_old = old_Results.getData("d-tables.d9", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in D9", D9_old, actual_D9, DELTA);

        double[] actual_D10 = instance.getD10().toArray();
        double[] D10_old = old_Results.getData("d-tables.d10", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in D10", D10_old, actual_D10, DELTA);

        double[] actual_D11 = instance.getD11().toArray();
        double[] D11_old = old_Results.getData("d-tables.d11", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in D11", D11_old, actual_D11, DELTA);

        double[] actual_D12 = instance.getD12().toArray();
        double[] D12_old = old_Results.getData("d-tables.d12", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in D12", D12_old, actual_D12, DELTA);

        double[] actual_D13 = instance.getD13().toArray();
        double[] D13_old = old_Results.getData("d-tables.d13", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in D13", D13_old, actual_D13, DELTA);

    }
    private static final double DELTA = 0.00000000051;
}
