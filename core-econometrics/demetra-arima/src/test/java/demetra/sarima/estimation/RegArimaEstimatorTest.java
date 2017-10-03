/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sarima.estimation;

import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.Data;
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
        RegArimaEstimator monitor = RegArimaEstimator.builder()
                .startingPoint(RegArimaEstimator.StartingPoint.Multiple).build();
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        spec.setP(3);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regs = RegArimaModel.builder(DoubleSequence.of(Data.PROD), arima)
                .meanCorrection(true)
                .missing(new int[]{3, 23, 34, 65, 123, 168})
                .build();
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regs);
//        System.out.println("New");
//        System.out.println(rslt.statistics(2, 0));
//        System.out.println(rslt.getModel().arima());
    }
    
}
