/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DFTTest {

    public DFTTest() {
    }

    @Test
    public void test1() {
        double[] r = new double[20];
        double[] im = new double[20];
        for (int i = 0; i < r.length; ++i) {
            r[i] = i * i;
            im[i] = i;
        }
        double[] rr = new double[r.length], ri = new double[r.length];

        DFT.transform(r, im, rr, ri);

        double[] r1 = new double[20];
        double[] im1 = new double[20];

        DFT.backTransform(rr, ri, r1, im1);
        
        for (int i = 0; i < r.length; ++i) {
            assertEquals(r[i], r1[i], 1e-9);
            assertEquals(im[i], im1[i], 1e-9);
        }
    }

    @Test
    public void testSym() {
        double[] r = new double[21];
        r[0]=10;
        for (int i = 1; i < r.length/2; ++i) {
            r[i] = 10-i;
            r[r.length-i]=10-i;
        }
        double[] rr = new double[r.length], ri = new double[r.length];

        DFT.transform2(r, new double[r.length], rr, ri);
    }
    
    @Test
    public void testSym2() {
        double[] r = new double[11];
        for (int i = 0; i < r.length; ++i) {
            r[i] = 10-i;
        }

        double[] tr = DFT.transformSymmetric(r);
    }

    @Test
    public void test2() {
        double[] r = new double[20];
        double[] im = new double[20];
        for (int i = 0; i < r.length; ++i) {
            r[i] = i * i;
            im[i] = i;
        }
        double[] rr = new double[r.length], ri = new double[r.length];

        DFT.transform2(r, im, rr, ri);

        double[] r1 = new double[20];
        double[] im1 = new double[20];

        DFT.backTransform2(rr, ri, r1, im1);
        for (int i = 0; i < r.length; ++i) {
            assertEquals(r[i], r1[i], 1e-9);
            assertEquals(im[i], im1[i], 1e-9);
        }
    }

    @Test
    @Ignore
    public void stressTest() {
        double[] r = new double[10000];
        double[] im = new double[r.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = i * i;
            im[i] = i;
        }
        double[] rr = new double[r.length], ri = new double[r.length];

        long t0=System.currentTimeMillis();
        for (int i = 0; i < 10; ++i) {
            DFT.transform(r, im, rr, ri);
            DFT.backTransform(rr, ri, r, im);
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        t0=System.currentTimeMillis();
        for (int i = 0; i < 10; ++i) {
            DFT.transform2(r, im, rr, ri);
            DFT.backTransform2(rr, ri, r, im);
        }
        t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }
}
