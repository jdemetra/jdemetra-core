/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

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
    public void testSomeMethod() {
        SarimaType arima = new SarimaType();
        arima.period=12;
        arima.setBphi(PolynomialType.of(1, -.8));
        System.out.println(arima);
        arima.setBtheta(PolynomialType.of(1, -.9));
    }
    
}
