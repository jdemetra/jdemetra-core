/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.regarima.ami;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DifferencingModuleTest {

    public DifferencingModuleTest() {
    }

    @Test
    public void testProd() {
        FastDifferencingModule test = FastDifferencingModule.builder().build();
        int[] diff = test.process(DoubleSeq.copyOf(Data.PROD), new int[]{1, 12}, null);
        assertTrue(diff[0] == 1 && diff[1] == 1);
//        System.out.println(diff[0]);
//        System.out.println(diff[1]);
//        System.out.println(test.isMeanCorrection());
    }

    @Test
    public void testProd2() {
        FastDifferencingModule test = FastDifferencingModule.builder().build();
        int[] diff = test.process(DoubleSeq.copyOf(Data.PROD), new int[]{12, 1}, null);
//        System.out.println(diff[0]);
//        System.out.println(diff[1]);
//        System.out.println(test.isMeanCorrection());
    }

    @Test
    public void testExports() {
        FastDifferencingModule test = FastDifferencingModule.builder()
                .k(0.9).build();
        int[] diff = test.process(DoubleSeq.copyOf(Data.EXPORTS), new int[]{1, 12}, null);
//        System.out.println(diff[0]);
//        System.out.println(diff[1]);
//        System.out.println(test.isMeanCorrection());
    }

    @Test
    public void testExports2() {
        FastDifferencingModule test = FastDifferencingModule.builder().k(0.9).build();
        int[] diff = test.process(DoubleSeq.copyOf(Data.EXPORTS), new int[]{12, 1}, null);
//        System.out.println(diff[0]);
//        System.out.println(diff[1]);
//        System.out.println(test.isMeanCorrection());
        assertTrue(diff[0] == 1 && diff[1] == 0);
    }

}
