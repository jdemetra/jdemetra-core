package demetra.saexperimental.r;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/**
*
* @author Alain QLT
*/

public class RKHSFilterTest {
	public RKHSFilterTest() {
    }

    @Test
    public void testOptimalCriteria() {
    	DoubleUnaryOperator ocsym = RKHSFilters.optimalCriteria(6, 6, 2, "BiWeight", "Timeliness", true, Math.PI/8);
  
        
        assertEquals(ocsym.applyAsDouble(6), 0, 1e-6);
        assertEquals(ocsym.applyAsDouble(7), 0, 1e-6);
        assertEquals(ocsym.applyAsDouble(6*3), 0, 1e-6);
    }
}
