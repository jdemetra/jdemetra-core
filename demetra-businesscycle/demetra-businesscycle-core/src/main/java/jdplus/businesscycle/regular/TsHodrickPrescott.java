/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package jdplus.businesscycle.regular;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import jdplus.businesscycle.HodrickPrescottFilter;


/**
 *
 * @author Jean Palate
 */
public class TsHodrickPrescott {

    private TsData cycle, trend;
    private final double lambda;
    private final double cycleLength;

    /**
     * 
     * @param cycleLength Length of a cycle in years
     * @return 
     */
    public static TsHodrickPrescott forCycleLength(double cycleLength) {
        return new TsHodrickPrescott(-1, cycleLength);
    }
    
    /**
     * 
     * @param lambda Signal to noise ratio
     * @return 
     */
    public static TsHodrickPrescott forSignalToNoiseRatio(double lambda) {
        return new TsHodrickPrescott(lambda, 0);
    }

    private TsHodrickPrescott(double lambda, double cycleLength){
        this.lambda=lambda;
        this.cycleLength=cycleLength;
    }

    public boolean process(TsData s) {
        clear();
        double lb = calcLambda(s.getAnnualFrequency());
        HodrickPrescottFilter hp = new HodrickPrescottFilter(lb);
        DoubleSeq[] tc  = hp.process(s.getValues());
        
            trend = TsData.ofInternal(s.getStart(), tc[0]);
            cycle = TsData.ofInternal(s.getStart(), tc[1]);
            return true;
        
    }

    public TsData getCycle() {
        return cycle;
    }

    public TsData getTrend() {
        return trend;
    }

    private double calcLambda(int frequency) {
        if (lambda > 0) {
            return lambda;
        } else {
            return HodrickPrescottFilter.lambda(cycleLength*frequency);
        }
    }

    private void clear() {
        trend = null;
        cycle = null;
    }

}
