/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regarima;

import demetra.arima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author palatej
 */
public class RegArimaEstimationTest {
    
    public RegArimaEstimationTest() {
    }

    @Test
    public void testBuilder() {
        SarimaModel arima = SarimaModel.builder()
                .period(12)
                .build();
        RegArimaEstimation<SarimaModel> regarima = RegArimaEstimation.<SarimaModel>builder()
                .y(new double[10])
                .arima(arima)
                .build();
        assertTrue(regarima != null);
    }
    
}
