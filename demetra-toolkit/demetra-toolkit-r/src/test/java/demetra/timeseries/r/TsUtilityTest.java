/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.r;

import demetra.data.Data;
import demetra.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TsUtilityTest {
    
    public TsUtilityTest() {
    }

    @Test
    public void testAggregate() {
        TsData aggregate = TsUtility.aggregate(Data.TS_PROD, 1, "Sum", true);
        assertTrue(aggregate != null);
    }
    
    @Test
    public void testTsData() {
        TsData prod = TsUtility.of(12, 1967, 2, Data.PROD);
        assertTrue(prod != null);
        TsData aggregate = TsUtility.aggregate(prod, 6, "Sum", true);
        assertTrue(aggregate != null);
   }
}
