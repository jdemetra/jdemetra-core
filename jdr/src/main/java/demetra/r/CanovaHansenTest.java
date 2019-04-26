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
package demetra.r;

import demetra.data.analysis.WindowFunction;
import demetra.stats.tests.seasonal.CanovaHansen;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class CanovaHansenTest {
    
    private final CanovaHansen ch;
    
    private CanovaHansenTest(CanovaHansen ch){
        this.ch=ch;
    }
    
    public static CanovaHansenTest ofSeasonalDummies(double[] data, int period, boolean lag1, String window, int truncation){
        WindowFunction fn=WindowFunction.valueOf(window);
        return new CanovaHansenTest(CanovaHansen.test(DoubleSeq.of(data))
                .lag1(lag1)
                .dummies(period)
                .windowFunction(fn)
                .truncationLag(truncation)
                .build());
    }
    
    public static CanovaHansenTest ofTrigs(double[] data, int period, boolean lag1, String window, int truncation){
        WindowFunction fn=WindowFunction.valueOf(window);
        return new CanovaHansenTest(CanovaHansen.test(DoubleSeq.of(data))
                .lag1(lag1)
                .trigonometric(period)
                .windowFunction(fn)
                .truncationLag(truncation)
                .build());
    }

    public static CanovaHansenTest ofSpecificFrequency(double[] data, double period, int nh, boolean lag1, String window, int truncation){
        WindowFunction fn=WindowFunction.valueOf(window);
        return new CanovaHansenTest(CanovaHansen.test(DoubleSeq.of(data))
                .lag1(lag1)
                .specific(period, nh)
                .windowFunction(fn)
                .truncationLag(truncation)
                .build());
    }
    
    public double testAll(){
        return ch.testAll();
    }
    
    public double test(int var, int nvars){
        return ch.test(var, nvars);
    }
}
