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

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.tramo.spectrum.PeaksEnum.AR;
import ec.tstoolkit.modelling.arima.tramo.spectrum.PeaksEnum.Tukey;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author gianluca
 */
public class Spect {

    public static PeaksEnum[] SpectrumComputation(TsData serie) {
        PeaksEnum[] RetVal;
        RetVal = new PeaksEnum[7];
        AR ar;
        Tukey tu;
        double prob1 = 0.99;
        double prob2 = 0.95;
        if (serie.getLength() >= 80 || (serie.getFrequency() != TsFrequency.Monthly && serie.getLength() >= 60)) {
            Peaks pkar = new Peaks(serie, 120, false);
            DescriptiveStatistics bs = new DescriptiveStatistics(serie);
            TsData targetS = serie.minus(bs.getAverage());
            TPeaks pkt = new TPeaks(targetS);
            if (pkar.getTDPeaks()[0] >= prob1) {
                ar = AR.A;
            } else if (pkar.getTDPeaks()[0] >= prob2) {
                ar = AR.a;
            } else {
                ar = AR.none;
            }
            if (pkt.getTDPeaks() >= prob1) {
                tu = Tukey.T;
            } else if (pkt.getTDPeaks() >= prob2) {
                tu = Tukey.t;
            } else {
                tu = Tukey.none;
            }
            RetVal[6] = new PeaksEnum(ar, tu);
            for (int i = 0; i < RetVal.length - 1; i++) {
                if (pkar.getSPeaks()[i] >= prob1) {
                    ar = AR.A;
                } else if (pkar.getSPeaks()[i] >= prob2) {
                    ar = AR.a;
                } else {
                    ar = AR.none;
                }
                if (pkt.getSPeaks()[i] >= prob1) {
                    tu = Tukey.T;
                } else if (pkt.getSPeaks()[i] >= prob2) {
                    tu = Tukey.t;
                } else {
                    tu = Tukey.none;
                }
                RetVal[i] = new PeaksEnum(ar, tu);
            }
        } else {
            for (int i = 0; i < RetVal.length; i++) {
                RetVal[i] = new PeaksEnum(AR.undef, Tukey.undef);
            }
        }
        return RetVal;
    }

    public static boolean SeasSpectCrit(PeaksEnum[] peaks, TsFrequency freq) {
        boolean retVal = false;
        int ip = 0;
        int id = 0;
        if (freq == TsFrequency.Quarterly) {
            if (peaks.length < 3) {
                return retVal;
            }
            for (int i = 0; i < 2; i++) {
                if (!peaks[i].equals(PeaksEnum.NONE) && !peaks[i].equals(PeaksEnum.UNDEF)) {
                    ip++;
                }
            }
            if (!peaks[2].equals(PeaksEnum.NONE) && !peaks[2].equals(PeaksEnum.UNDEF)) {
                retVal = true;
            } else if (ip == 2) {
                retVal = true;
            } else {
                retVal = false;
            }
        } else {
            if (peaks.length < 6) {
                return retVal;
            }
            for (int i = 0; i < 6; i++) {
                if (peaks[i].equals(PeaksEnum.ALL)) {
                    ip++;
                    id++;
                } else if (!peaks[i].equals(PeaksEnum.NONE)) {
                    ip++;
                }
            }
            switch (ip) {
                case 3:
                case 4:
                case 5:
                case 6:
                    retVal = true;
                    break;
                case 2:
                    if ((peaks[5].ar.isPresent())
                            && (peaks[5].tu.isPresent())) {
                        if (id == 2) {
                            retVal = true;
                        }
                    } else {
                        if (peaks[5].equals(PeaksEnum.NONE)) {
                            if (id == 1) {
                                retVal = true;
                            }
                        }
                    }
                    break;
                default:
                    retVal = false;
            }
        }
        return retVal;
    }

    public static boolean SeasSpectCrit2(PeaksEnum[] peaks, TsFrequency freq) {
        boolean retVal = false;
        int ip = 0;
        int id = 0;
        if (freq == TsFrequency.Quarterly) {
            if (peaks.length < 3) {
                return retVal;
            }
            for (int i = 0; i < 2; i++) {
                if (peaks[i].equals(PeaksEnum.ALL)) {
                    ip++;
                }
            }
            if (peaks[0].equals(PeaksEnum.ALL)) {
                retVal = true;
            } else if (ip == 2) {
                retVal = true;
            } else {
                retVal = false;
            }

        } else {
            if (peaks.length < 6) {
                return retVal;
            }
            for (int i = 0; i < 6; i++) {
                if (peaks[i].equals(PeaksEnum.ALL)) {
                    ip++;
                    id++;
                } else if (peaks[i].ar == AR.A || peaks[i].tu == Tukey.T) {
                    ip++;
                }
            }
            switch (ip) {
                case 3:
                case 4:
                case 5:
                case 6:
                    retVal = true;
                    break;
                case 2:
                    if (peaks[5].equals(PeaksEnum.ALL)) {
                        if (id == 2) {
                            retVal = true;
                        }
                    } else //if (!peaks[5].isEqual(AR.A) || !peaks[5].isEqual(Tukey.T))
                    {
                        if (id == 1) {
                            retVal = true;
                        }
                    }
                    break;
                default:
                    retVal = false;
            }

        }
        return retVal;
    }
}
