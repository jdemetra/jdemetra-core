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
package ec.businesscycle.simplets;

import ec.businesscycle.impl.HodrickPrescott;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class TsHodrickPrescott {

    private TsData cycle, trend;
    private double lambda;
    private double cyclelen;

    public TsHodrickPrescott() {
        lambda = -1;
        cyclelen = 8;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        cyclelen = -1;
    }

    public void setCycleLength(double len) {
        cyclelen = len;
        lambda = -1;
    }

    public double getLambda() {
        return lambda;
    }

    public double getCycleLength() {
        return cyclelen;
    }

    public boolean process(TsData s) {
        clear();
        HodrickPrescott hp = new HodrickPrescott();
        double lb = calcLambda(s.getFrequency());
        hp.setLambda(lb);
        if (!hp.process(s)) {
            return false;
        } else {
            trend = new TsData(s.getStart(), hp.getSignal(), false);
            cycle = TsData.subtract(s, trend);
            return true;
        }
    }

    public TsData getCycle() {
        return cycle;
    }

    public TsData getTrend() {
        return trend;
    }

    public static double defaultLambda(double ylen, int freq) {
        double w = 2 * Math.PI / (freq * ylen);
        double x = 1 - Math.cos(w);
        return .75 / (x * x);
    }

    private double calcLambda(TsFrequency frequency) {
        if (lambda > 0) {
            return lambda;
        } else {
            return defaultLambda(cyclelen, frequency.intValue());
        }
    }

    private void clear() {
        trend = null;
        cycle = null;
    }

}
