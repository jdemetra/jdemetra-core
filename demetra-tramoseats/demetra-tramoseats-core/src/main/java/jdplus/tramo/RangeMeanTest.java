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
package jdplus.tramo;

import demetra.design.Development;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import demetra.modelling.TransformationType;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;
import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RangeMeanTest {

    private boolean log;
    private double tlog = 2, t;
    private int isj, itrim;

    public boolean process(RegArimaModel<SarimaModel> model) {

        DoubleSeq data = model.getY();

        if (data.anyMatch(x -> x <= 0)) {
            return false;
        }

        int ifreq = model.arima().getFrequency();
        log = useLogs(ifreq, data);
        return true;
    }

    /**
     *
     */
    public RangeMeanTest() {
    }

    // / <summary>
    // / Compute isj_ the group length which is a multiple of freq. Also compute
    // itrim_
    // / </summary>
    // / <param name="freq"></param>
    // / <param name="n"></param>
    private void computeisj(int freq, int n) {
        itrim = 1;
        switch (freq) {
            case 12:
                isj = 12;
                break;
            case 6:
                isj = 12;
                if (n <= 165) {
                    itrim = 2;
                }   break;
            case 4:
                if (n > 165) {
                    isj = 8;
                } else {
                    isj = 12;
                    itrim = 2;
                }   break;
            case 3:
                if (n > 165) {
                    isj = 6;
                } else {
                    isj = 12;
                    itrim = 2;
                }   break;
            case 2:
                if (n > 165) {
                    isj = 6;
                } else {
                    isj = 12;
                    itrim = 2;
                }   break;
            case 1:
                if (n > 165) {
                    isj = 5;
                } else {
                    isj = 9;
                    itrim = 2;
                }   break;
            default:
                isj = freq;
                break;
        }
    }

    /**
     *
     * @return
     */
    public double getTStat() {
        return t;
    }

    /**
     *
     * @return
     */
    public double getTLog() {
        return tlog;
    }

    /**
     *
     * @param value
     */
    public void setTLog(double value) {
        tlog = value;
    }

    /**
     *
     * @param freq
     * @param data
     * @return
     */
    public boolean useLogs(int freq, DoubleSeq data) {
        int n = data.length();
        isj = 0;
        itrim = 0;
        computeisj(freq, n);
        int npoints = n / isj;
        if (npoints <= 3) {
            return false;
        }
        double[] range = new double[npoints], smean = new double[npoints], srt = new double[isj];

        for (int i = 0; i < npoints; ++i) {
            // fill srt;
            for (int j = 0; j < isj; ++j) {
                srt[j] = data.get(j + i * isj);
            }
            Arrays.sort(srt);
            range[i] = srt[isj - itrim - 1] - srt[itrim];
            double s = srt[itrim];
            for (int j = itrim + 1; j < isj - itrim; ++j) {
                s += srt[j];
            }
            s /= (isj - 2 * itrim);
            smean[i] = s;
        }
        try {
            LinearModel model = LinearModel.builder()
                    .y(DoubleSeq.of(range))
                    .addX(DoubleSeq.of(smean))
                    .meanCorrection(true)
                    .build();
            LeastSquaresResults rslt = Ols.compute(model);
            t = rslt.getLikelihood().tstat(1, 0, true);
            return t > tlog;
        } catch (Exception err) {
            t = 0;
            return false;
        }
    }

    public TransformationType getTransformation() {
        return log ? TransformationType.Log : TransformationType.None;
    }

}
