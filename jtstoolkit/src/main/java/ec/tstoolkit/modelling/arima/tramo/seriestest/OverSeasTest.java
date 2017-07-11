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

package ec.tstoolkit.modelling.arima.tramo.seriestest;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.tramo.spectrum.PeaksEnum;
import ec.tstoolkit.modelling.arima.tramo.spectrum.Spect;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;

/**
 *
 * @author gianluca
 */
public class OverSeasTest {

    private TsData serie;
    private int checkQs;
    private int checkSNP;
    private int checkPeaks;
    private PeaksEnum[] peaks;
    private boolean diff;
    private int checkOverSeasTest;

    public int getCheckOverSeasTest() {
        return checkOverSeasTest;
    }

    public int getCheckPeaks() {
        return checkPeaks;
    }

    public int getCheckQs() {
        return checkQs;
    }

    public int getCheckSNP() {
        return checkSNP;
    }
    public OverSeasTest(TsData targetS, PeaksEnum [] Peaks,boolean Diff) {
        serie = targetS;
        checkQs = 0;
        checkSNP = 0;
        checkPeaks = 0;
        peaks = Peaks;
        diff = Diff;
        checkOverSeasTest = ComputeOverSeasTest();
    }

    public static double Kendalls(TsData targetS) {
        double retVal = 0.0;
        double data[];
        double obs[];
        double r[][];
        if (targetS.getFrequency().intValue() <= 1) {
            return retVal;
        }
        int Nz = targetS.getLength();
        data = new double[Nz];
        for (int i = 0; i < Nz; i++) {
            data[i] = targetS.get(i);
        }
        int Mq = targetS.getFrequency().intValue();
        int Ny = Nz / Mq;
        int res = Nz - Ny * Mq ;
        r = new double[Ny][Mq];
        for (int i = 0; i < Ny; i++) {
            Arrays.fill(r[i], 0.0);
        }
        obs = new double[Mq];
        for (int i = 0; i < Ny; i++) {
            for (int j = 0; j < Mq; j++) {
                obs[j] = data[res + i * Mq + j];
            }
            int ind = 1;
            int MaxLoop = 0;
            int LoopMax = 1000;
            while ((ind <= Mq) && (MaxLoop < LoopMax)) {
                MaxLoop++;
                DescriptiveStatistics bs = new DescriptiveStatistics(obs);
                double min_val = bs.getMin();
                int k = 0;
                int[] found = new int[Mq];
                for (int j = 0; j < Mq; j++) {
                    found[j] = 0;
                    if (Math.abs(obs[j] - min_val) < 1.0e-20) {
                        k++;
                        found[j] = 1;
                    }
                }
                double value = ind + (k - 1) / 2.0;
                for (int j = 0; j < Mq; j++) {
                    if (found[j] == 1) {
                        obs[j] = java.lang.Double.MAX_VALUE;
                        r[i][j] = value;
                    }
                }
                ind = ind + k;
            }
            if (MaxLoop > LoopMax) {
                return retVal;
            }
        }
        double[] m = new double[Mq];
        for (int i = 0; i < Mq; i++) {
            double sum = 0.0;
            for (int j = 0; j < Ny; j++) {
                sum += r[j][i];
            }
            m[i] = sum;
        }
        for (int i = 0; i < Mq; i++) {
            retVal += (m[i] - Ny * (Mq + 1) / 2.0) * (m[i] - Ny * (Mq + 1) / 2.0);
        }
        return 12.0*retVal /  ((Mq+1)*Mq*Ny);

    }
    
    public static double CalcQs(TsData targetS) {
        double retVal = 0.0;
        if (targetS.getFrequency().intValue() == 1)
            return retVal;
        DescriptiveStatistics bs = new DescriptiveStatistics(targetS);
        double c0 = bs.getSumSquare();
        c0 = c0 / targetS.getLength();
        int Mq = targetS.getFrequency().intValue();
        int Mq2=Mq*2;
        double [] C = new double[Mq2];
        double [] R = new double[Mq2];
        Arrays.fill(C, 0.0);
        for (int k=0;k<Mq2;k++)
        {
            for (int i=k+1;i<targetS.getLength();i++)
            {
                C[k] += targetS.get(i)*targetS.get(i-1-k);
            }
            C[k] /= targetS.getLength();
            R[k] = C[k]/c0;    
        }
        if (R[Mq-1] > 0.0)
        {
            retVal += R[Mq - 1] * R[Mq - 1] / (targetS.getLength() - Mq);
            if (R[Mq2 - 1] > 0.0) {
                retVal += R[Mq2 - 1] * R[Mq2 - 1] / (targetS.getLength() - Mq2);
            }
            retVal *= targetS.getLength() * (targetS.getLength() + 2);
        }
        return retVal;
    }

    private int ComputeOverSeasTest() {
        int retVal = 0;
        TsData diffS = serie.delta(1);
        DescriptiveStatistics bs = new DescriptiveStatistics(diffS);
        TsData targetS = diffS.minus(bs.getAverage());
        double SNPVal = Kendalls(targetS);
        if (diff) {
            diffS = serie.delta(1);
            bs = new DescriptiveStatistics(diffS);
            targetS = diffS.minus(bs.getAverage());
        } else {
            targetS = serie.clone();
        }
        double Qs = CalcQs(targetS);
        if (Qs > 9.21) {
            retVal++;
            checkQs = 1;
        }
        if (((SNPVal > 24.73) && (serie.getFrequency() == TsFrequency.Monthly))
                || ((SNPVal > 11.35) && (serie.getFrequency() == TsFrequency.Quarterly))) {
            retVal++;
            checkSNP = 1;
        } else {
            checkSNP = 0;
        }
        if (Spect.SeasSpectCrit(peaks, serie.getFrequency())) {
            retVal++;
            checkPeaks = 1;
        } else {
            checkPeaks = 0;
        }
        return retVal;
    }
}
