/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests;

import demetra.stats.StatisticalTest;
import java.util.Random;
import jdplus.data.DataBlock;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class ArchTest {

    public ArchTest() {
    }

    @Test
    public void testRandom() {
        int N = 1000;
        DataBlock X = DataBlock.make(N);
        Random rnd = new Random();
        X.set(rnd::nextGaussian);
        for (int i = 2; i < 20; ++i) {
            StatisticalTest lm = Arch.lm(X)
                    .autoCorrelationsCount(i)
                    .build();
            StatisticalTest lb = Arch.porteManteau(X)
                    .autoCorrelationsCount(i)
                    .build();
//            System.out.print(lm.getPvalue());
//            System.out.print('\t');
//            System.out.println(lb.getPvalue());
        }
    }

    public static void main(String[] arg) {
        int K = 1000, N = 500;
        double lms=0, lbs=0;
        for (int k = 0; k < K; ++k) {
            DataBlock X = DataBlock.make(N);
            Random rnd = new Random();
            X.set(rnd::nextGaussian);
            StatisticalTest lm = Arch.lm(X)
                    .autoCorrelationsCount(5)
                    .build();
            StatisticalTest lb = Arch.porteManteau(X)
                    .autoCorrelationsCount(5)
                    .build();
            lms+=lm.getValue();
            lbs+=lb.getValue();
        }
        System.out.println(lms/K);
        System.out.println(lbs/K);
    }
}
