/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DoubleSequence;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class ModeTest {

    @Test
    @Ignore
    public void Mode_PseudoAddTest() {
        double[] B1 = {2.6461, 2.5308, 2.4537, 2.379, 2.3565, 2.1487, 2.055, 2.0372, 2.0012, 1.9937, 1.9771, 1.9962, 1.9746, 1.9446, 1.9378, 1.917, 1.9045, 1.8899, 1.9104, 1.9103, 1.8961, 1.9079, 1.9039, 1.94, 1.9378, 1.9226, 1.9822, 1.9806, 1.985, 1.9798, 1.9811, 1.9711, 1.9719, 1.9785, 2.0053, 2.1, 2.1336, 2.1725, 2.2888, 2.3375, 2.3868, 2.4799, 2.5884, 2.6936, 2.7429, 2.8583, 2.9227, 3.0703, 3.1626, 3.2103, 3.3261, 3.3972, 3.4644, 3.5494, 3.6592, 3.7668, 3.8584, 3.9112, 3.9474, 4.0623, 4.035, 4.0059, 4.0334, 4.108, 4.172, 4.2327, 4.3251, 4.403, 4.4584, 4.512, 4.4207, 4.2558, 3.9833, 3.8197, 3.7109, 3.5593, 3.4212, 3.3163, 3.1038, 2.9132, 2.7541, 2.587, 2.3942, 2.1768, 1.9159, 1.8032, 1.7712, 1.7364, 1.6951, 1.5772, 1.5232, 1.4986, 1.4694, 1.4676, 1.4692, 1.4618, 1.4647, 1.4611, 1.4671, 1.5019, 1.5462, 1.5838, 1.6408, 1.6872, 1.7135, 1.746, 1.7641, 1.7677, 1.7584, 1.7457, 1.733, 1.718, 1.701, 1.6858, 1.6522, 1.6165, 1.5776, 1.526, 1.4788, 1.4305, 1.3779, 1.3334, 1.2923, 1.2435, 1.2045, 1.1589, 1.0913, 1.044, 1.0107, 0.9751, 0.9595, 0.9389, 0.9234, 0.9015, 0.8855, 0.8692, 0.8506, 0.8348, 0.8021, 0.776, 0.7522, 0.734, 0.7108, 0.6724, 0.6565, 0.6418, 0.6152, 0.5941, 0.5714, 0.5436, 0.5223, 0.5062, 0.4999, 0.4926, 0.4765, 0.464, 0.451, 0.4422, 0.4441, 0.4394, 0.433, 0.4192, 0.4068, 0.4087, 0.4005, 0.4015, 0.3937, 0.3772, 0.3677, 0.3507, 0.3362, 0.3342, 0.329, 0.3261, 0.3223, 0.3119, 0.3059, 0.301, 0.299, 0.2935, 0.2852, 0.2812, 0.2713, 0.2666, 0.2659, 0.2602, 0.255, 0.2486, 0.2438, 0.2384, 0.2376, 0.2307, 0.20375339911111273, 0.18324531122329432, 0.17194084678338567, 0.16135837689581206, 0.15197163048263918, 0.13725628553865504, 0.12256970403408764, 0.11178177453754014, 0.10084363454139757, 0.0925250062803331, 0.07965662061437864, 0.06352959314738488};
        DoubleSequence b1 = DoubleSequence.of(B1);
        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(DecompositionMode.PseudoAdditive)
                //                .finalSeasonalFilter(SeasonalFilterOption.S3X3)
                //                .initialSeasonalFilter(SeasonalFilterOption.S3X3)
                .trendFilterLength(5)
                .forecastHorizon(0)
                .period(12)
                .build();

        X11BStep bStep = new X11BStep();
        X11CStep cStep = new X11CStep();
        bStep.process(b1, context);
        cStep.process(b1, context.remove(b1, bStep.getB20()), context);
        X11DStep instance = new X11DStep();
        instance.process(b1, context.remove(b1, cStep.getC20()), context);

//D-Step
        //alte Berechnung
        X11Specification oldX11Spec = new X11Specification();
        oldX11Spec.setMode(ec.satoolkit.DecompositionMode.PseudoAdditive);
        oldX11Spec.setSeasonalFilter(ec.satoolkit.x11.SeasonalFilterOption.S3X3);
        oldX11Spec.setHendersonFilterLength(5);
        oldX11Spec.setForecastHorizon(0);
        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        old.setToolkit(ec.satoolkit.x11.X11Toolkit.create(oldX11Spec));
        X11Results old_Results = old.process(new TsData(TsFrequency.Monthly, 1999, 0, B1, true));
        double[] B1_old = old_Results.getData("b-tables.b1", TsData.class).internalStorage();
        double[] B2_old = old_Results.getData("b-tables.b2", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B1", B1_old, bStep.getB1().toArray(), 0.00000000051);
        Assert.assertArrayEquals("Error in B2", B2_old, bStep.getB2().toArray(), 0.00000000051);
        double[] B3_old = old_Results.getData("b-tables.b3", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B3", B3_old, bStep.getB3().toArray(), 0.00000000051);
        double[] B4_old = old_Results.getData("b-tables.b4", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B4", B4_old, bStep.getB4().toArray(), 0.00000000051);

        double[] B5_old = old_Results.getData("b-tables.b5", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B5", B5_old, bStep.getB5().toArray(), 0.00000000051);

        double[] B6_old = old_Results.getData("b-tables.b6", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B6", B6_old, bStep.getB6().toArray(), 0.00000000051);

        double[] B7_old = old_Results.getData("b-tables.b7", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B7", B7_old, bStep.getB7().toArray(), 0.00000000051);

        double[] B8_old = old_Results.getData("b-tables.b8", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B8", B8_old, bStep.getB8().toArray(), 0.00000000051);

        double[] B20_old = old_Results.getData("b-tables.b20", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B20", B20_old, bStep.getB20().toArray(), 0.00000000051);

        double[] C1_old = old_Results.getData("c-tables.c1", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in C1", C1_old, cStep.getC1().toArray(), 0.00000000051);

    }

    @Test
    public void Mode_LogAddTest() {
        double[] B1 = {2.6461, 2.5308, 2.4537, 2.379, 2.3565, 2.1487, 2.055, 2.0372, 2.0012, 1.9937, 1.9771, 1.9962, 1.9746, 1.9446, 1.9378, 1.917, 1.9045, 1.8899, 1.9104, 1.9103, 1.8961, 1.9079, 1.9039, 1.94, 1.9378, 1.9226, 1.9822, 1.9806, 1.985, 1.9798, 1.9811, 1.9711, 1.9719, 1.9785, 2.0053, 2.1, 2.1336, 2.1725, 2.2888, 2.3375, 2.3868, 2.4799, 2.5884, 2.6936, 2.7429, 2.8583, 2.9227, 3.0703, 3.1626, 3.2103, 3.3261, 3.3972, 3.4644, 3.5494, 3.6592, 3.7668, 3.8584, 3.9112, 3.9474, 4.0623, 4.035, 4.0059, 4.0334, 4.108, 4.172, 4.2327, 4.3251, 4.403, 4.4584, 4.512, 4.4207, 4.2558, 3.9833, 3.8197, 3.7109, 3.5593, 3.4212, 3.3163, 3.1038, 2.9132, 2.7541, 2.587, 2.3942, 2.1768, 1.9159, 1.8032, 1.7712, 1.7364, 1.6951, 1.5772, 1.5232, 1.4986, 1.4694, 1.4676, 1.4692, 1.4618, 1.4647, 1.4611, 1.4671, 1.5019, 1.5462, 1.5838, 1.6408, 1.6872, 1.7135, 1.746, 1.7641, 1.7677, 1.7584, 1.7457, 1.733, 1.718, 1.701, 1.6858, 1.6522, 1.6165, 1.5776, 1.526, 1.4788, 1.4305, 1.3779, 1.3334, 1.2923, 1.2435, 1.2045, 1.1589, 1.0913, 1.044, 1.0107, 0.9751, 0.9595, 0.9389, 0.9234, 0.9015, 0.8855, 0.8692, 0.8506, 0.8348, 0.8021, 0.776, 0.7522, 0.734, 0.7108, 0.6724, 0.6565, 0.6418, 0.6152, 0.5941, 0.5714, 0.5436, 0.5223, 0.5062, 0.4999, 0.4926, 0.4765, 0.464, 0.451, 0.4422, 0.4441, 0.4394, 0.433, 0.4192, 0.4068, 0.4087, 0.4005, 0.4015, 0.3937, 0.3772, 0.3677, 0.3507, 0.3362, 0.3342, 0.329, 0.3261, 0.3223, 0.3119, 0.3059, 0.301, 0.299, 0.2935, 0.2852, 0.2812, 0.2713, 0.2666, 0.2659, 0.2602, 0.255, 0.2486, 0.2438, 0.2384, 0.2376, 0.2307, 0.20375339911111273, 0.18324531122329432, 0.17194084678338567, 0.16135837689581206, 0.15197163048263918, 0.13725628553865504, 0.12256970403408764, 0.11178177453754014, 0.10084363454139757, 0.0925250062803331, 0.07965662061437864, 0.06352959314738488};
        DoubleSequence b1 = DoubleSequence.of(B1);
        demetra.x11.X11Context context = demetra.x11.X11Context.builder()
                .mode(DecompositionMode.LogAdditive)
                //                .finalSeasonalFilter(SeasonalFilterOption.S3X3)
                //                .initialSeasonalFilter(SeasonalFilterOption.S3X3)
                .calendarSigma(CalendarSigmaOption.All)
                .trendFilterLength(5)
                .forecastHorizon(0)
                .period(12)
                .build();

        if (context.isLogAdd()) {
            b1 = b1.log();
        }

        X11BStep bStep = new X11BStep();
        X11CStep cStep = new X11CStep();
        bStep.process(b1, context);
        cStep.process(b1, context.remove(b1, bStep.getB20()), context);
        X11DStep instance = new X11DStep();
        instance.process(b1, context.remove(b1, cStep.getC20()), context);

//D-Step
        //alte Berechnung
        X11Specification oldX11Spec = new X11Specification();
        oldX11Spec.setMode(ec.satoolkit.DecompositionMode.LogAdditive);
        oldX11Spec.setSeasonalFilter(ec.satoolkit.x11.SeasonalFilterOption.S3X3);
        oldX11Spec.setHendersonFilterLength(5);
        oldX11Spec.setForecastHorizon(0);
        oldX11Spec.setCalendarSigma(CalendarSigma.All);
        oldX11Spec.setBiasCorrection(ec.satoolkit.x11.BiasCorrection.None);
        ec.satoolkit.x11.X11Kernel old = new ec.satoolkit.x11.X11Kernel();
        old.setToolkit(ec.satoolkit.x11.X11Toolkit.create(oldX11Spec));
        X11Results old_Results = old.process(new TsData(TsFrequency.Monthly, 1999, 0, B1, true));
        double[] B1_old = old_Results.getData("b-tables.b1", TsData.class).internalStorage();
        double[] B2_old = old_Results.getData("b-tables.b2", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B1", B1_old, bStep.getB1().exp().toArray(), 0.00000000051);
        Assert.assertArrayEquals("Error in B2", B2_old, bStep.getB2().exp().toArray(), 0.00000000051);
        double[] B3_old = old_Results.getData("b-tables.b3", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B3", B3_old, bStep.getB3().exp().toArray(), 0.00000000051);
        double[] B4_old = old_Results.getData("b-tables.b4", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B4", B4_old, bStep.getB4().exp().toArray(), 0.00000000051);

        double[] B5_old = old_Results.getData("b-tables.b5", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B5", B5_old, bStep.getB5().exp().toArray(), 0.00000000051);

        double[] B6_old = old_Results.getData("b-tables.b6", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B6", B6_old, bStep.getB6().exp().toArray(), 0.00000000051);

        double[] B7_old = old_Results.getData("b-tables.b7", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B7", B7_old, bStep.getB7().exp().toArray(), 0.00000000051);

        double[] B8_old = old_Results.getData("b-tables.b8", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B8", B8_old, bStep.getB8().exp().toArray(), 0.00000000051);

        double[] B20_old = old_Results.getData("b-tables.b20", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in B20", B20_old, bStep.getB20().exp().toArray(), 0.00000000051);

        double[] C1_old = old_Results.getData("c-tables.c1", TsData.class).internalStorage();
        Assert.assertArrayEquals("Error in C1", C1_old, cStep.getC1().exp().toArray(), 0.00000000051);

        double[] expected_D13 = old_Results.getData("d-tables.d13", TsData.class).internalStorage();
        double[] actual_D13 = instance.getD13().exp().toArray();
        Assert.assertArrayEquals("Error in D13", expected_D13, actual_D13, .00000000051);
    }
}
