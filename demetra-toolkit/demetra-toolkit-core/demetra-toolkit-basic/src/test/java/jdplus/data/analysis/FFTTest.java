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
package jdplus.data.analysis;

import demetra.math.Complex;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class FFTTest {

    public FFTTest() {
    }

    @Test
    public void test1() {
        int N = 1024;
        Complex[] data = new Complex[N];
        double[] rdata = new double[N];
        double[] idata = new double[N];
        for (int i = 0; i < N; ++i) {
            rdata[i] = i;
            idata[i] = N - i;
            data[i] = Complex.cart(i, N - i);
        }
        FFT.transform(data);
        FFT.transform(rdata, idata);
        for (int i = 0; i < N; ++i) {
            assertEquals(rdata[i], data[i].getRe(), 1e-9);
            assertEquals(idata[i], data[i].getIm(), 1e-9);
        }
        FFT.backTransform(data);
        FFT.backTransform(rdata, idata);
        for (int i = 0; i < N; ++i) {
            assertEquals(rdata[i], data[i].getRe(), 1e-9);
            assertEquals(idata[i], data[i].getIm(), 1e-9);
            assertEquals(rdata[i], i, 1e-9);
            assertEquals(idata[i], N - i, 1e-9);
        }
    }

    @Test
    public void test2() {
        int N = 100;
        Complex[] data = new Complex[N];
        double[] rdata = new double[N];
        double[] idata = new double[N];
        for (int i = 0; i < N; ++i) {
            rdata[i] = i;
            idata[i] = N - i;
            data[i] = Complex.cart(i, N - i);
        }
        data=FFT.expand(data);
        rdata=FFT.expand(rdata);
        idata=FFT.expand(idata);
        FFT.transform(data);
        FFT.transform(rdata, idata);
        for (int i = 0; i < N; ++i) {
            assertEquals(rdata[i], data[i].getRe(), 1e-9);
            assertEquals(idata[i], data[i].getIm(), 1e-9);
        }
        FFT.backTransform(data);
        FFT.backTransform(rdata, idata);
        for (int i = 0; i < N; ++i) {
            assertEquals(rdata[i], data[i].getRe(), 1e-9);
            assertEquals(idata[i], data[i].getIm(), 1e-9);
            assertEquals(rdata[i], i, 1e-9);
            assertEquals(idata[i], N - i, 1e-9);
        }
        for (int i = N; i < rdata.length; ++i) {
            assertEquals(rdata[i], 0, 1e-9);
            assertEquals(idata[i], 0, 1e-9);
        }
    }
    
    static public void main(String[] v){
        stressTest();
    }
    
//    @Test
//    @Ignore
    static public void stressTest() {
        int N = 1024;
        int K = 100000;
        Complex[] data = new Complex[N];
        double[] rdata = new double[N];
        double[] idata = new double[N];
        for (int i = 0; i < N; ++i) {
            rdata[i] = i;
            idata[i] = N - i;
            data[i] = Complex.cart(i, N - i);
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FFT.transform(data);
            FFT.backTransform(data);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FFT.transform(rdata, idata);
            FFT.backTransform(rdata, idata);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
