/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import jdplus.maths.linearfilters.FiniteFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FSTFilterFactoryTest {
    
    public FSTFilterFactoryTest() {
    }

    @Test
    public void testSomeMethod() {
    }
    
    public static void main(String[] args){
        FSTFilterFactory factory=new FSTFilterFactory(15, 7);
        for (int i=0; i<500; ++i){
            FiniteFilter filter = factory.make(.2, .2, .6*i);
            System.out.println(factory.getT());
        }
    }
}
