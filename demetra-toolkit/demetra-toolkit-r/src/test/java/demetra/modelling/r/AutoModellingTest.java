/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.r;

import demetra.data.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class AutoModellingTest {

    public AutoModellingTest() {
    }

    @Test
    public void testRangeMean() {
        double t = AutoModelling.rangeMean(Data.EXPORTS, 12, 0, 0);
        assertTrue(t > 0);
        t = AutoModelling.rangeMean(Data.EXPORTS, 0, 24, 1);
        assertTrue(t > 0);
    }

}
