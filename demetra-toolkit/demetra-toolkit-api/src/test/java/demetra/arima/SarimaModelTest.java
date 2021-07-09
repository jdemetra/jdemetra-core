/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.data.DoubleSeq;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaModelTest {
    
    public SarimaModelTest() {
    }

    @Test
    public void testBuilder() {
        SarimaModel arima = SarimaModel.builder()
                .period(12)
                .d(1)
                .bd(1)
                .theta(DoubleSeq.of(-.8))
                .btheta(DoubleSeq.of(-.9))
                .build();
        System.out.println(arima);
    }
    
}
