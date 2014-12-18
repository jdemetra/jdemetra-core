/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.data;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DataBlockTest {

    public DataBlockTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testExtract() {
        DataBlock A = new DataBlock(100);
        DataBlock B = A.extract(0, -1, 3);
        B.set(1);
        DataBlock C = B.extract(4, -1, 7);
        C.set(-1);
        DataBlock AM = new DataBlock(100).reverse();
        DataBlock BM = AM.extract(0, -1, 3);
        BM.set(1);
        DataBlock CM = BM.extract(4, -1, 7);
        CM.set(-1);
        assertTrue(ReadDataBlock.equals(A, AM, 0));
    }

    @Test
    public void testStats() {
        // creates a block of 1000 random numbers 
        DataBlock O = new DataBlock(1000);
        O.randomize();
        // extracts a sub-datablock of 105 items (at position 1, 9, 17...) 
        DataBlock A=O.extract(1, 105, 8);
        DescriptiveStatistics all = new DescriptiveStatistics(A);
        double max = all.getMax();
        double min = all.getMin();
        // creates extracts of 10 data (0,10,20...), (1, 11, 21, ...) (indexes in A)
        // or (1, 81, 161...), (2, 82, 162...) (indexes in O)
        // computes statistics on them

        double sum1 = 0, ssq1 = 0;
        for (int i = 0; i < 10; ++i) {
            DataBlock E = A.extract(i, -1, 10);
            DescriptiveStatistics estats = new DescriptiveStatistics(E);
            assertTrue(estats.getMax() <= max);
            assertTrue(estats.getMin() >= min);
            sum1 += estats.getSum();
            ssq1 += estats.getSumSquare();
        }
        assertTrue(Math.abs(A.sum()-sum1)<1e-12);
        assertTrue(Math.abs(A.ssq()-ssq1)<1e-12);

        // same problem in reverse order
        double sum2 = 0, ssq2 = 0;
        int n=0;
        for (int i = 0; i < 10; ++i) {
            DataBlock E = A.extract(A.getLastIndex()-i, -1, -10);
            DescriptiveStatistics estats = new DescriptiveStatistics(E);
            assertTrue(estats.getMax() <= max);
            assertTrue(estats.getMin() >= min);
            sum2 += estats.getSum();
            ssq2 += estats.getSumSquare();
            n+=E.getLength();
        }
        assertTrue(Math.abs(A.sum()-sum2)<1e-12);
        assertTrue(Math.abs(A.ssq()-ssq2)<1e-12);

        sum1 = 0; ssq1 = 0;
        for (int i = 0; i < 10; ++i) {
            DataBlock E = A.reverse().extract(i, -1, 10);
            DescriptiveStatistics estats = new DescriptiveStatistics(E);
            assertTrue(estats.getMax() <= max);
            assertTrue(estats.getMin() >= min);
            sum1 += estats.getSum();
            ssq1 += estats.getSumSquare();
        }
        assertTrue(Math.abs(A.sum()-sum1)<1e-12);
        assertTrue(Math.abs(A.ssq()-ssq1)<1e-12);

        // same problem in reverse order
        sum2 = 0; ssq2 = 0;
        n=0;
        for (int i = 0; i < 10; ++i) {
            DataBlock E = A.reverse().extract(A.getLastIndex()-i, -1, -10);
            DescriptiveStatistics estats = new DescriptiveStatistics(E);
            assertTrue(estats.getMax() <= max);
            assertTrue(estats.getMin() >= min);
            sum2 += estats.getSum();
            ssq2 += estats.getSumSquare();
            n+=E.getLength();
        }
        assertTrue(Math.abs(A.sum()-sum2)<1e-12);
        assertTrue(Math.abs(A.ssq()-ssq2)<1e-12);

    }
}
