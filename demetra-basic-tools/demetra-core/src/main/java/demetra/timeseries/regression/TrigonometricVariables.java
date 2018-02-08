/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Computes trigonometric variables: sin(wt), cos(wt) at given frequencies if w
 * = pi, sin(wt) is omitted t = 0 for 1/1/70
 *
 * @author Jean Palate
 */
public class TrigonometricVariables implements ITsVariable<TsDomain> {

    /**
     * to be multiplied by pi
     */
    private final double[] freq;
    private final LocalDateTime ref;
    private final String name;

    public static TrigonometricVariables regular(int periodicity) {
        int n = periodicity / 2;
        double[] freq = new double[n];
        double f = 2.0 / periodicity;
        for (int i = 1; i <= n; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricVariables(freq);
    }

    public static TrigonometricVariables regular(int periodicity, int[] seasfreq) {
        double[] freq = new double[seasfreq.length];
        double f = 2.0 / periodicity;
        for (int i = 0; i < seasfreq.length; ++i) {
            freq[i] = f * seasfreq[i];
        }
        return new TrigonometricVariables(freq);
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
        return new TrigonometricVariables(freq);
    }

    public TrigonometricVariables(double[] freq) {
        this.freq = freq;
        this.name = "trig#" + freq.length;
        this.ref = EPOCH;
    }

    public TrigonometricVariables(double[] freq, LocalDateTime ref, String name) {
        this.freq = freq;
        this.ref = ref;
        this.name = name;
    }

    public Matrix matrix(int length, int start) {
        Matrix m = Matrix.make(length, getDim());
        int nlast = freq.length - 1;
        if (freq[nlast] != 1) {
            ++nlast;
        }
        for (int i = 0; i < nlast; ++i) {
            double w = freq[i] * Math.PI;
            DataBlock c = m.column(2 * i);
            c.set(k -> Math.cos(w * (k + start)));
            DataBlock s = m.column(2 * i + 1);
            s.set(k -> Math.sin(w * (k + start)));
        }
        if (nlast < freq.length) { // PI
            DataBlock c = m.column(2 * nlast);
            c.set(k -> (k + start) % 2 == 0 ? 1 : -1);
        }
        return m;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriod refPeriod = domain.getStartPeriod().withDate(ref);
        long start = domain.getStartPeriod().getId() - refPeriod.getId();
        int nlast = freq.length - 1;
        if (freq[nlast] != 1) {
            ++nlast;
        }
        for (int i = 0; i < nlast; ++i) {
            double w = freq[i] * Math.PI;
            DataBlock c = data.get(2 * i);
            c.set(k -> Math.cos(w * (k + start)));
            DataBlock s = data.get(2 * i + 1);
            s.set(k -> Math.sin(w * (k + start)));
        }
        if (nlast < freq.length) { // PI
            DataBlock c = data.get(2 * nlast);
            c.set(k -> (k + start) % 2 == 0 ? 1 : -1);
        }
    }

    @Override
    public String getDescription(TsDomain context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDim() {
        int n = freq.length;
        return freq[n - 1] == 1 ? 2 * n - 1 : 2 * n;
    }

    @Override
    public String getItemDescription(int idx, TsDomain context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TrigonometricVariables rename(String nname) {
        return new TrigonometricVariables(freq, ref, nname);
    }

}
