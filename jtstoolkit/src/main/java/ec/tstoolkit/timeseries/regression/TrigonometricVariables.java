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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.List;

/**
 * Computes trigonometric variables:
 * sin(wt), cos(wt) at given frequencies
 * if w = pi, sin(wt) is omitted
 * t = 0 for 1/1/70
 * @author Jean Palate
 */
public class TrigonometricVariables implements ITsVariable {
    
    /**
     * to be multiplied by pi
     */
    private final double[] freq;
    
    public static TrigonometricVariables regular(int periodicity){
        int n=periodicity/2;
        double[] freq=new double[n];
        double f=2.0/periodicity;
        for (int i=1; i<=n; ++i){
            freq[i-1]=f*i;
        }
        return new TrigonometricVariables(freq);
    }

    public static TrigonometricVariables regular(int periodicity, int[] seasfreq){
        double[] freq=new double[seasfreq.length];
        double f=2.0/periodicity;
        for (int i=0; i<seasfreq.length; ++i){
            freq[i]=f*seasfreq[i];
        }
        return new TrigonometricVariables(freq);
    }

    /**
     * Creates trigonometric series for "non regular" series
     * Example:
     * For weekly series, periodicity is 365.25/7 = 52.1786
     * We compute the trigonometric variables for 
     * w= (k*2*pi)/52.1786, k=1,..., nfreq
     * 
     * @param periodicity Annual periodicity 
     * @param nfreq Number of "seasonal" frequencies of interest
     * @return 
     * 
     */
    public static TrigonometricVariables all(double periodicity, int nfreq){
        double[] freq=new double[nfreq];
        double f=2.0/periodicity;
        for (int i=1; i<=nfreq; ++i){
            freq[i-1]=f*i;
        }
        return new TrigonometricVariables(freq);
    }

    public TrigonometricVariables(double[] freq){
        this.freq=freq;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        int start = domain.startId();
        int nlast=freq.length-1;
        if (freq[nlast] != 1)
            ++nlast;
        for (int i=0; i<nlast; ++i){
            double w=freq[i]*Math.PI;
            DataBlock c = data.get(2*i);
            c.set(k->Math.cos(w*(k+start)));
            DataBlock s = data.get(2*i+1);
            s.set(k->Math.sin(w*(k+start)));
        }
        if (nlast<freq.length){ // PI
            DataBlock c = data.get(2*nlast);
            c.set(k->(k+start)%2 == 0 ? 1 : -1);
        }
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Undefined;
    }

    @Override
    public String getDescription(TsFrequency context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDim() {
        int n=freq.length;
        return freq[n-1] == 1 ? 2*n-1 : 2*n;
    }

    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
