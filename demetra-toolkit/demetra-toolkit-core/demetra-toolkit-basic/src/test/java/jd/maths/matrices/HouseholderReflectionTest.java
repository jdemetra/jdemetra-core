/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jd.maths.matrices;

import jd.maths.matrices.decomposition.HouseholderReflection;
import jd.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class HouseholderReflectionTest {
    
    public HouseholderReflectionTest() {
    }

    @Test
    public void testHouseholder() {
        DataBlock x=DataBlock.make(10);
        Random rnd=new Random();
        x.set(rnd::nextDouble);
        double nx=x.norm2();
        // Creates the Householder reflection
        HouseholderReflection hr = HouseholderReflection.of(x);
        // x is now (|| x || 0 ... 0)
        assertTrue(x.drop(1, 0).allMatch(z->z==0));
        assertEquals(nx, x.get(0), 1e-9);
        // apply the transformation on another vector
        DataBlock y=DataBlock.make(10);
        y.set(rnd::nextDouble);
        hr.transform(y);
    }
    
}
