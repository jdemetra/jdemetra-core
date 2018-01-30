/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sarima.estimation;

import demetra.sarima.RegSarimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RegArimaEstimatorTest {
    
    public RegArimaEstimatorTest() {
    }

    @Test
    public void testNew() {
        RegSarimaProcessor monitor = RegSarimaProcessor.builder()
                 .precision(1e-12)
               .startingPoint(RegSarimaProcessor.StartingPoint.Multiple).build();
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        spec.setP(3);
        spec.setQ(0);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regs = RegArimaModel.builder(SarimaModel.class)
                        .y(DoubleSequence.of(Data.RETAIL_FUELDEALERS))
                        .arima(arima)
//                .meanCorrection(true)
//                .missing(new int[]{3, 23, 34, 65, 123, 168})
                .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regs);
//        System.out.println("New");
//        System.out.println(rslt.statistics(5, 0));
//        System.out.println(rslt.getModel().arima());
//        DataBlock diag=monitor.getParametersCovariance().diagonal();
//        diag.apply(x->Math.sqrt(x));
//        System.out.println(diag);
//        System.out.println(DataBlock.ofInternal(monitor.getScore()));
    }
    
   @Test
    public void testAirline() {
        RegSarimaProcessor monitor = RegSarimaProcessor.builder()
                .precision(1e-12)
                .startingPoint(RegSarimaProcessor.StartingPoint.Multiple).build();
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regs = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSequence.of(Data.RETAIL_FUELDEALERS))
                .arima(arima)
//                .meanCorrection(true)
//                .missing(new int[]{3, 23, 34, 65, 123, 168})
                .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regs);
//        System.out.println("New");
//        System.out.println(rslt.statistics(2, 0));
//        System.out.println(rslt.getModel().arima());
//        DataBlock diag=monitor.getParametersCovariance().diagonal();
//        diag.apply(x->Math.sqrt(x));
//        System.out.println(diag);
//        System.out.println(DataBlock.ofInternal(monitor.getScore()));
    }
}
