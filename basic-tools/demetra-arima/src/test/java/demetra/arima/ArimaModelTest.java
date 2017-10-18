/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArimaModelTest {

    public ArimaModelTest() {
    }

    @Test
    public void testConstants() {
        assertTrue(ArimaModel.ONE.isWhiteNoise());
        assertFalse(ArimaModel.ONE.isNull());
        assertTrue(ArimaModel.NULL.isNull());
    }

    @Test
    public void testSumDiff() {
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();

        ArimaModel wn = ArimaModel.whiteNoise();
        ArimaModel sum = ArimaModel.add(sarima, wn);
//        System.out.println(sum);
        ArimaModel m = ArimaModel.subtract(sum, ArimaModel.copyOf(sarima));
        m=m.simplifyUr();
//        System.out.println();
        assertTrue(m.isWhiteNoise());
    }
}
