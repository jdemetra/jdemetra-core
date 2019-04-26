/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.data.analysis.TukeyHanningTaper;
import demetra.design.Demo;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TukeyHanningTaperTest {
    
    public TukeyHanningTaperTest() {
    }

    @Demo
    public void testRandom() {
        double[] x=new double[60];
        DataBlock X=DataBlock.of(x);
        Random rnd=new Random();
        X.set(rnd::nextGaussian);
        TukeyHanningTaper taper=new TukeyHanningTaper(.5);
        System.out.println(X);
        taper.process(x);
        System.out.println(X);
    }
    
}
