/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.data.WeeklyData;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11KernelTest {
    
    public X11KernelTest() {
    }

    @Test
    public void testWeekly() {
        X11Kernel kernel=new X11Kernel();
        System.out.println("Exact");
        X11Context context1=X11Context.builder()
                .mode(DecompositionMode.Multiplicative)
                .period(365.25/7)
                .hendersonFilterLength(53)
                .build();
        kernel.process(DoubleSequence.ofInternal(WeeklyData.US_CLAIMS), context1);
        System.out.println(kernel.getBstep().getB10());
        System.out.println("Rounded");
        X11Context context2=X11Context.builder()
                .mode(DecompositionMode.Multiplicative)
                .period(52)
                .hendersonFilterLength(53)
                .build();
        kernel.process(DoubleSequence.ofInternal(WeeklyData.US_CLAIMS), context2);
        System.out.println(kernel.getBstep().getB10());
    }
    
    @Test
    public void testMonthly() {
        X11Kernel kernel=new X11Kernel();
        X11Context context1=X11Context.builder()
                .mode(DecompositionMode.Multiplicative)
                .period(12)
                .hendersonFilterLength(13)
                .build();
        kernel.process(DoubleSequence.ofInternal(Data.PROD), context1);
        System.out.println(kernel.getBstep().getB13());
        ec.satoolkit.x11.X11Specification spec=new ec.satoolkit.x11.X11Specification();
        spec.setMode(ec.satoolkit.DecompositionMode.Multiplicative);
        spec.setForecastHorizon(0);
        spec.setHendersonFilterLength(13);
        ec.satoolkit.x11.X11Toolkit toolkit=ec.satoolkit.x11.X11Toolkit.create(spec);
        ec.satoolkit.x11.X11Kernel okernel=new ec.satoolkit.x11.X11Kernel();
        okernel.setToolkit(toolkit);
        ec.tstoolkit.timeseries.simplets.TsData s=
                new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        X11Results x11 = okernel.process(s);
        ec.tstoolkit.timeseries.simplets.TsData b=x11.getData("b-tables.b13", ec.tstoolkit.timeseries.simplets.TsData.class);
        System.out.println(new ec.tstoolkit.data.DataBlock(b));
    }
}
