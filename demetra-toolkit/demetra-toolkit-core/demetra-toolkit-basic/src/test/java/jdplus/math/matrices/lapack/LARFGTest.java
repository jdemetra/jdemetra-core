/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import java.util.Random;
import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LARFGTest {
    
    public LARFGTest() {
    }

    @Test
    public void testRnd() {
                DataBlock x=DataBlock.make(10);
        Random rnd=new Random(0);
        x.set(rnd::nextDouble);
        LARFG.Reflector reflector=new LARFG.Reflector(10, DataPointer.of(x));
        LARFG.apply(reflector);
    }
    
}
