/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data.analysis;

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
