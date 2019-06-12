/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sarima;

import demetra.arima.SarimaSpecification;
import demetra.data.Data;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
public class RegSarimaProcessorTest {
    
    public RegSarimaProcessorTest() {
    }

    @Test
    public void testProd() {
        assertTrue(prodAirline() != null);
    }
    
    public static RegArimaModel<SarimaModel> prodAirline(){
        SarimaSpecification spec=new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel model = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSeq.of(Data.PROD))
                .arima(arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = RegSarimaProcessor.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-9)
                .build()
                .process(model);
        return rslt.getModel();
    }
    
}
