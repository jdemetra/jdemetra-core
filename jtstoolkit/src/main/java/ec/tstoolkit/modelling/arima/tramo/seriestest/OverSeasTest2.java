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

/**
 *
 * @author gianluca
 */
public class OverSeasTest2 {

    private TsData serie;
    private int checkQs;
    private int checkSNP;
    private int checkPeaks;
    private PeaksEnum[] peaks;
    private int diffSize;
    private int checkOverSeasTest;
    private SerType type;
    protected double crFseas;

    /**
     * Get the value of crFseas
     *
     * @return the value of crFseas
     */
    public double getCrFseas() {
        return crFseas;
    }

    /**
     * Set the value of crFseas
     *
     * @param crFseas new value of crFseas
     */
    public void setCrFseas(double crFseas) {
        this.crFseas = crFseas;
    }


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

    /**
     *
     * @param targetS
     * @param Peaks
     * @param DiffSize D + BD
     * @param Type
     */
    public OverSeasTest2(TsData targetS, PeaksEnum[] Peaks, int DiffSize, SerType Type) 
    {
        serie = targetS;
        checkQs = 0;
        checkSNP = 0;
        checkPeaks = 0;
        peaks = Peaks;
        diffSize = DiffSize;
        type = Type;
        checkOverSeasTest = ComputeOverSeasTest2();
    }

    public OverSeasTest2(TsData targetS, PeaksEnum[] Peaks, int DiffSize, SerType Type,double Pseas) 
    {
        serie = targetS;
        checkQs = 0;
        checkSNP = 0;
        checkPeaks = 0;
        peaks = Peaks;
        diffSize = DiffSize;
        type = Type;
        checkOverSeasTest = ComputeOverSeasTest2();
        if (Pseas > 0.995d) {
            checkOverSeasTest++;
            crFseas = 2;
        } else {
            if (Pseas > 0.99d) {
                checkOverSeasTest++;
                crFseas = 1;
            } else {
                crFseas = 0;
            }
        }
    }

    private int ComputeOverSeasTest2() {
        int retVal = 0;
        double SNPVal = 0.0;
        TsData diffS = serie.delta(1);
        if (type == SerType.Xlin) {
            DescriptiveStatistics bs = new DescriptiveStatistics(diffS.getValues());
            TsData targetS = diffS.minus(bs.getAverage());
            SNPVal = OverSeasTest.Kendalls(targetS);
        } else {
            DescriptiveStatistics bs = new DescriptiveStatistics(serie.getValues());
            TsData targetS = serie.minus(bs.getAverage());
            SNPVal = OverSeasTest.Kendalls(targetS);
        }
        if (((SNPVal > 24.73) && (serie.getFrequency() == TsFrequency.Monthly))
                || ((SNPVal > 11.35) && serie.getFrequency() == TsFrequency.Quarterly)) {
            retVal++;
            checkSNP = 1;
        }
        int nLag = Math.max(Math.min(2, diffSize), 1);
        for (int i = 0; i < nLag; i++) {
            diffS = diffS.delta(1);
        }
        DescriptiveStatistics bs = new DescriptiveStatistics(diffS.getValues());
        TsData targetS = diffS.minus(bs.getAverage());
        double QS = OverSeasTest.CalcQs(targetS);
        if (QS > 9.21) {
            retVal++;
            checkQs = 1;
        }
        if (Spect.SeasSpectCrit(peaks, serie.getFrequency())) {
            retVal++;
            checkPeaks = 1;
        }
        return retVal;
    }
}
