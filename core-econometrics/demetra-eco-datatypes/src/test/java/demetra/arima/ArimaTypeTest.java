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
public class ArimaTypeTest {
    
    public ArimaTypeTest() {
    }

    @Test
    public void testSomeMethod() {
        ArimaType arima = ArimaType.builder()
                .delta(PolynomialType.of(1, -1))
                .build();
        
    }
    
}
