/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11plus;

import jdplus.x11plus.SeasonalFilterOption;
import jdplus.x11plus.X11Kernel;
import jdplus.x11plus.X11Context;
import demetra.data.Data;
import demetra.data.WeeklyData;
import demetra.sa.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import org.junit.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11KernelTest {
    
    public X11KernelTest() {
    }
    
    public static void main(String[] cmds){
//        try {
            new X11KernelTest().testWeekly();
//        } catch (URISyntaxException ex) {
//            Logger.getLogger(X11KernelTest.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(X11KernelTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Test
    public void testWeekly() {
        X11Kernel kernel=new X11Kernel();
//        System.out.println("Exact");
        X11Context context1=X11Context.builder()
                .period(365.25/7)
                .trendFilterLength(53)
                .initialSeasonalFilter(SeasonalFilterOption.S3X1)
                .finalSeasonalFilter(SeasonalFilterOption.S3X9)
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context1);
//        System.out.println(kernel.getDstep().getD11());
//        System.out.println("Rounded");
        X11Context context2=X11Context.builder()
                .period(52)
                .trendFilterLength(0)
                .initialSeasonalFilter(SeasonalFilterOption.S3X1)
                .finalSeasonalFilter(SeasonalFilterOption.S3X9)
                .build();
        kernel.process(DoubleSeq.of(WeeklyData.US_CLAIMS2), context2);
//        System.out.println(kernel.getDstep().getD11());
    }
    
    @Test
    public void testMonthly() {
        X11Kernel kernel=new X11Kernel();
        X11Context context1=X11Context.builder()
                .mode(DecompositionMode.Multiplicative)
                .period(12)
                .trendFilterLength(13)
                .build();
        kernel.process(DoubleSeq.of(Data.PROD), context1);
//        System.out.println(kernel.getDstep().getD13());
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
        ec.tstoolkit.timeseries.simplets.TsData b=x11.getData("d-tables.d13", ec.tstoolkit.timeseries.simplets.TsData.class);
//        System.out.println(new ec.tstoolkit.data.DataBlock(b));
    }
    
    // 31/12/2002
//    @Test
//    public void testDaily() throws URISyntaxException, IOException {
//        URI uri = X11Kernel.class.getResource("/cic.txt").toURI();
//        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");
//        X11Kernel kernel=new X11Kernel();
//        X11Context context1=X11Context.builder()
//                .mode(DecompositionMode.Multiplicative)
//                .period(7)
//                .trendFilterLength(13)
//                .build();
//        kernel.process(data.column(0), context1);
//    }
}
