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

package ec.tstoolkit.modelling.arima.tramo.spectrum;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;

/**
 *
 * @author gianluca
 */
public class TuckeyPeaks {
    private TsData serie;
    private double[] Spect;
    private double[] SPeaks;
    private int nSPeaks;
    private double TDPeaks;

    public double[] getSPeaks() {
        return SPeaks;
    }

    public double[] getSpect() {
        return Spect;
    }

    public double getTDPeaks() {
        return TDPeaks;
    }

    public int getnSPeaks() {
        return nSPeaks;
    }

    public TuckeyPeaks(TsData targetS) {
        serie = targetS;
        nSPeaks = 0;
        TDPeaks = -1.0;
        ComputeTuckeyPeaks();
    }
    private void Tpeaks(int Win_Size)
    {
        double incHpi =0.0, incHmid =0.0, incHtd = 0.0, incH = 0.0;
        int indTD =0, indPI = 0, indSmidLen = 0;
        int[] indSmid;
        int prob = 2;
        if (prob == 1) //Test 99%
        {
             switch (Win_Size) {
                 case 112:
                     incHpi = 5.51;
                     incHmid = 3.86;
                     incHtd = incHmid;
                     break;
                 case 79:
                     incHpi = 9.1;
                     incHmid = 3.86;
                     incHtd = incHmid;
                     break;
                 default :
                     incHpi = 4.08;
                     incHmid = 8.82;
                     incHtd = 3.86;
                     break;
             }
        }else //Test 95%
        {
             switch (Win_Size) {
                 case 112:
                     incHpi = 3.67;
                     incHmid = 2.7;
                     incHtd = incHmid;
                     break;
                 case 79:
                     incHpi = 4.45;
                     incHmid = 2.7;
                     incHtd = incHmid;
                     break;
                 default :
                     incHpi = 4.36;
                     incHmid = 2.7;
                     incHtd = incHmid;
                     break;
             }
            
        }
        indSmid = new int[5];
        switch(Win_Size){
            case 112:
                indSmid[0] = 10;
                indSmid[1] = 20;
                indSmid[2] = 29;
                indSmid[3] = 38;
                indSmid[4] = 48;
                indSmidLen = 5;
                indTD = 40;
                indPI = 57;
                break;
            case 79:
                indSmid[0] = 8;
                indSmid[1] = 14;
                indSmid[2] = 21;
                indSmid[3] = 27;
                indSmid[4] = 34;
                indSmidLen = 5;
                indTD = 29;
                indPI = 40;
                break;
            default :
                indTD = -1;
                indPI = 22;
                indSmidLen = 0;
                switch (serie.getFrequency()){
                    case BiMonthly :
                        indSmid[0] = 8;
                        indSmid[1] = 15;
                        indSmidLen = 2;
                        break;
                    case Quarterly :
                        indTD = 14;
                        indSmid[0] = 12;
                        indSmidLen = 1;
                        break;
                    case QuadriMonthly :
                        indPI = -1;
                        indSmid[1] = 15;
                        indSmidLen = 1;
                        break;
                    case Yearly :
                        indPI = -1;
                        break;
                }
        }
        nSPeaks = -1;
        TDPeaks = -1.0;
        if (indTD > 0)
        {
            incH = 2.0 * Spect[indTD-1]/(Spect[indTD]+Spect[indTD - 2]);
            if (incH > incHtd)
                TDPeaks = incH;
        }
        for (int i=0;i<indSmidLen;i++)
        {
            incH = (2.0 * Spect[indSmid[i]-1]) / (Spect[indSmid[i]]+Spect[indSmid[i]-2]);
            if (incH > incHmid)
            {
                nSPeaks++;
                SPeaks[nSPeaks] = indSmid[i]-1;
            }
        }
        if (indPI > 0)
        {
            incH = Spect[indPI-1]/Spect[indPI-2];
            if (incH > incHpi)
            {
                nSPeaks++;
                SPeaks[nSPeaks] = indPI;
            }
        }
    }
    private void ComputeTuckeyPeaks()
    {

        int Win_Size = 0;
        if ((serie.getFrequency() != TsFrequency.Monthly) && (serie.getLength() >= 60)) {
            Win_Size = 44;
        } else if ((serie.getFrequency() == TsFrequency.Monthly) && (serie.getLength() >= 120)) {
            Win_Size = 112;
        } else if ((serie.getFrequency() == TsFrequency.Monthly) && (serie.getLength() >= 80)) {
            Win_Size = 79;
        } else {
            Win_Size = -1;
            Spect = null;
            SPeaks = null;
            nSPeaks = 0;
            TDPeaks = -1;
            return;
        }
        Spect = new double[60];
        double[] window = TPeaks.getWindow(WinType.Tukey, Win_Size);
        TPeaks.covWind(serie, Spect, window);
        SPeaks = new double[6];
        Arrays.fill(SPeaks, 0.0);
        Tpeaks(Win_Size);
    }
}
