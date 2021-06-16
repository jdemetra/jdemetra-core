/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.data.Range;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesInterval;
import java.time.LocalDateTime;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.RationalFunction;

/**
 *
 * @author palatej
 */
class IVFactory implements RegressionVariableFactory<InterventionVariable> {

    static IVFactory FACTORY = new IVFactory();

    private IVFactory() {
    }

    @Override
    public boolean fill(InterventionVariable var, TsPeriod start, Matrix buffer) {
        int dcount = buffer.getRowsCount();
        Range<LocalDateTime>[] seqs = var.getSequences();
        if (seqs.length == 0) {
            return true;
        }
        // first, generates the 0/1
        LocalDateTime t0 = seqs[0].start(), t1 = seqs[0].end();
        // search the Start / End of the sequences

        for (int i = 1; i < seqs.length; ++i) {
            if (t0.isAfter(seqs[i].start())) {
                t0 = seqs[i].start();
            }
            if (t1.isBefore(seqs[i].end())) {
                t1 = seqs[i].end();
            }
        }

        // period of estimation : Start->domain[last]
        TsPeriod pstart = start.withDate(t0), pend = start.withDate(t1);
        int n = dcount - start.until(pstart);
        if (n < 0) {
            return true;
        }

        double[] tmp = new double[n];
        for (int i = 0; i < seqs.length; ++i) {
            TsPeriod curstart = start.withDate(seqs[i].start()),
                    curend = start.withDate(seqs[i].end());

            int istart = pstart.until(curstart);
            int iend = 1 + pstart.until(curend);
            if (iend > n) {
                iend = n;
            }
            for (int j = istart; j < iend; ++j) {
                tmp[j] += 1;
            }
        }

        double delta=var.getDelta(), deltas=var.getDeltaSeasonal();
        int freq=start.getUnit().getAnnualFrequency();
        if (delta != 0 || deltas != 0) {
            // construct the filter
            Polynomial num = Polynomial.ONE;
            Polynomial d = delta != 0 ? Polynomial.valueOf(1, -delta): Polynomial.ONE;
            if (freq != 1 && deltas != 0) {
                double[] ds = new double[freq+1];
                ds[0] = 1;
                ds[freq] = -deltas;
                d = d.times(Polynomial.of(ds));
            }
            RationalFunction rf = RationalFunction.of(num, d);
            double[] w = rf.coefficients(n);

            // apply the filter
            double[] ftmp = new double[n];
            for (int i = 0; i < ftmp.length; ++i) {
                if (tmp[i] != 0) {
                    for (int j = 0; j < ftmp.length - i; ++j) {
                        ftmp[i + j] += tmp[i] * w[j];
                    }
                }
            }
            tmp = ftmp;
        }
        // copy in rslt
        int di = start.until(pstart);
        if (di > 0) {
            buffer.column(0).drop(di, 0).copyFrom(tmp, 0);
        } else {
            buffer.column(0).copyFrom(tmp, -di);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(InterventionVariable var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
