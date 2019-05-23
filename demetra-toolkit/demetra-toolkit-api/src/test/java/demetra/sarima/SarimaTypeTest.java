/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sarima;

import demetra.arima.SarimaProcess;
import demetra.maths.PolynomialType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaTypeTest {
    
    public SarimaTypeTest() {
    }

    @Test
    public void testBuilder() {
        SarimaProcess arima = SarimaProcess.builder()
                .period(12)
                .d(1)
                .bd(1)
                .theta(PolynomialType.of(1, -.8))
                .btheta(PolynomialType.of(1, -.9))
                .build();
        System.out.println(arima);
    }
    
}
