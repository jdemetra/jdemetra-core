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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
@Development(status=Development.Status.Release)
public class TrigonometricVariables implements ITsVariable{

    public static TrigonometricVariables regular(int periodicity) {
        int n = periodicity / 2;
        double[] freq = new double[n];
        double f = 2.0 / periodicity;
        for (int i = 1; i <= n; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricVariables(freq, TsPeriod.DEFAULT_EPOCH
        );
    }

    public static TrigonometricVariables regular(int periodicity, int[] seasfreq) {
        double[] freq = new double[seasfreq.length];
        double f = 2.0 / periodicity;
        for (int i = 0; i < seasfreq.length; ++i) {
            freq[i] = f * seasfreq[i];
        }
        return new TrigonometricVariables(freq, TsPeriod.DEFAULT_EPOCH);
    }

    /**
     * Creates trigonometric series for "non regular" series Example: For weekly
     * series, periodicity is 365.25/7 = 52.1786 We compute the trigonometric
     * variables for w= (k*2*pi)/52.1786, k=1,..., nfreq
     *
     * @param periodicity Annual periodicity
     * @param nfreq Number of "seasonal" frequencies of interest
     * @return
     *
     */
    public static TrigonometricVariables all(double periodicity, int nfreq) {
        double[] freq = new double[nfreq];
        double f = 2.0 / periodicity;
        for (int i = 1; i <= nfreq; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricVariables(freq, TsPeriod.DEFAULT_EPOCH);
    }

    /**
     * to be multiplied by pi
     */
    private double[] frequencies;
    private LocalDateTime reference;

    @Override
    public int dim() {
        int n = frequencies.length;
        return frequencies[n - 1] == 1 ? 2 * n - 1 : 2 * n;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "trigonometric variable";
    }
}
