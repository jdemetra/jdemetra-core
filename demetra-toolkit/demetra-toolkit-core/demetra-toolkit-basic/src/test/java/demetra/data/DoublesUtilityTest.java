/*
 * Copyright 2016 National Bank ofInternal Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofInternal the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class DoublesUtilityTest {

    public DoublesUtilityTest() {
    }

    @Test
    @Ignore
    public void testSomeMethod() {

        int N = 100, K = 1000;
        DataBlock a = DataBlock.make(N);
        DataBlock b = DataBlock.make(N).reverse();
        double[] pc=new double[N*3];
        DataBlock c=DataBlock.of(pc, 0, pc.length, 3);
        a.set(i->i);
        b.set(i->i);
        c.set(i->i*i+1);
        for (int k = 0; k < K; ++k) {
            double s = 0;
            for (int i = 0; i < N; i++) {
                s += a.get(i) * b.get(i);
            }
            for (int i = 0; i < N; i++) {
                s += c.get(i) * b.get(i);
            }
        }
        for (int k = 0; k < K; ++k) {
            double s = 0;
            DoubleSeqCursor cur = a.cursor();
            DoubleSeqCursor xcur = b.cursor();
            for (int i = 0; i < N; i++) {
                s += cur.getAndNext() * xcur.getAndNext();
            }
        }
        K=50000000;
  
        long t0=System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            double s = 0;
            DoubleSeqCursor cur = a.cursor();
            DoubleSeqCursor xcur = b.cursor();
            for (int i = 0; i < N; i++) {
                s += cur.getAndNext() * xcur.getAndNext();
            }
            cur = c.cursor();
            xcur = b.cursor();
           for (int i = 0; i < N; i++) {
                s += cur.getAndNext() * xcur.getAndNext();
            }
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        t0=System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            double s = 0;
            for (int i = 0; i < N; i++) {
                s += a.get(i) * b.get(i);
            }
            for (int i = 0; i < N; i++) {
                s += c.get(i) * b.get(i);
            }
        }
        t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        t0=System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            double s = a.dot(b)+c.dot(b);
        }
        t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }

}
