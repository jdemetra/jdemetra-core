/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.saexperimental.r;

import demetra.data.DoubleSeq;
import jdplus.math.linearfilters.FiniteFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class FiltersToolkitTest {
    
    public FiltersToolkitTest() {
    }

    @Test
    public void testFST() {
        FiltersToolkit.FSTResult rslt = FiltersToolkit.fstfilter(12, 0, 2, 0.001, 3, 0.999, Math.PI/6, true);
        FiniteFilter filter = rslt.getFilter();
        FiltersToolkit.FSTResult fst=FiltersToolkit.fst(filter.weightsToArray(), -12, Math.PI/6);
        
        assertEquals(fst.getCriterions()[0], rslt.getCriterions()[0], 1e-6);
        assertEquals(fst.getCriterions()[1], rslt.getCriterions()[1], 1e-6);
        assertEquals(fst.getCriterions()[2], rslt.getCriterions()[2], 1e-6);
//        System.out.println(DoubleSeq.of(rslt.getRslt().getFilter().weightsToArray()));
    }
    
}
