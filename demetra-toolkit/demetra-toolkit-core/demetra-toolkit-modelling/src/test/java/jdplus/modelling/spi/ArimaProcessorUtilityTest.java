/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.spi;

import jdplus.modelling.spi.ArimaProcessorUtility;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArimaProcessorUtilityTest {
    
    public ArimaProcessorUtilityTest() {
    }

    @Test
    public void testConversion() {
        SarimaSpecification spec=new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel sarima=SarimaModel.builder(spec)
                .setDefault(.3, -.1)
                .build();
        demetra.arima.SarimaModel carima = ArimaProcessorUtility.convert(sarima);
        System.out.println(carima);
        SarimaModel sarima2 = ArimaProcessorUtility.convert(carima);
        assertTrue(sarima.equals(sarima2));
        demetra.arima.SarimaModel carima2 = ArimaProcessorUtility.convert(sarima2);
        assertTrue(carima.equals(carima2));
        spec.setP(3);
        spec.setBp(1);
        sarima=SarimaModel.builder(spec)
                .setDefault(.3, -.1)
                .build();
        carima = ArimaProcessorUtility.convert(sarima);
        System.out.println(carima);
        sarima2 = ArimaProcessorUtility.convert(carima);
        assertTrue(sarima.equals(sarima2));
        carima2 = ArimaProcessorUtility.convert(sarima2);
        assertTrue(carima.equals(carima2));
    }
    
}
