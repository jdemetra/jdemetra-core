/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.extremevaluecorrector;

import demetra.data.DataBlock;
import demetra.design.Development;
import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 * This extremvalueCorrector uses period specific Standarddeviation for the detection of extremevalues, used for Calendarsigma.All or Calendarsigma.Signif if Cochran false
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class PeriodSpecificExtremeValuesCorrector extends DefaultExtremeValuesCorrector {

    public PeriodSpecificExtremeValuesCorrector() {
        super();
    }

    /**
     * Calculates the Standarddeviation for each period
     *
     * @param s
     * @return Standarddeviation for each period
     */
    @Override
    protected double[] calcStdev(DoubleSeq s) {

        double[] stdev;

        if (excludeFcast) {
            s = s.drop(0, forecastHorizon);
        }
        DataBlock db = DataBlock.of(s);
//      one value for each period
        stdev = new double[period];
        for (int i = 0; i < period; i++) {
            int j = i + start > period - 1 ? i + start - period : i + start;
            DataBlock dbPeriod = db.extract(j, -1, period);
            stdev[i] = calcSingleStdev(dbPeriod);
            // WAs ist der  Unterschied zu
//               double e =mul ? 1: 0;
//                Doubles.ssqcWithMissing(dsPeriod, e);
        }
        return stdev;
    }

    @Override
    protected DoubleSeq outliersDetection(DoubleSeq cur, double[] stdev) {
        int n = cur.length();

        double[] w = new double[n];
        Arrays.fill(w, 1);
        double xbar = mul ? 1 : 0;
        for (int iPeriod = 0; iPeriod < period; iPeriod++) {
            double lv, uv;
            lv = stdev[iPeriod] * lsigma;
            uv = stdev[iPeriod] * usigma;

            int j = iPeriod + start > period - 1 ? iPeriod + start - period : iPeriod + start;
            DataBlock dCur = DataBlock.of(cur);
            DataBlock dsPeriod = dCur.extract(j, -1, period);

            for (int i = 0; i < dsPeriod.length(); i++) {
                double tt = Math.abs(dsPeriod.get(i) - xbar);
                if (tt > uv) {
                    w[i * period + j] = 0;
                } else if (tt > lv) {
                    w[i * period + j] = (uv - tt) / (uv - lv);
                }
            }

        }
        return DoubleSeq.copyOf(w);
    }
}
