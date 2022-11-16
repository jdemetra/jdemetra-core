/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.Ramp;
import jdplus.data.DataBlock;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import jdplus.math.matrices.FastMatrix;
import demetra.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 */
class RampFactory implements RegressionVariableFactory<Ramp> {

    static RampFactory FACTORY = new RampFactory();

    private RampFactory() {
    }

    @Override
    public boolean fill(Ramp var, TsPeriod start, FastMatrix buffer) {
        data(var, start, buffer.column(0));
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(Ramp var, D domain, FastMatrix buffer) {
        data(var, domain, buffer.column(0));
        return true;
    }

    private void data(Ramp var, TimeSeriesDomain domain, DataBlock cur) {
        // start corresponds to the first period with data > -1
        // end corresponds to the first period with data = 0
        LocalDateTime start = var.getStart(), end = var.getEnd();
        int t1 = domain.indexOf(end);
        int len = cur.length(); // =domain.length()
        if (t1 == -1) { // ramp before the domain: nothing to do
            return;
        }
        int t0 = domain.indexOf(start);
        if (t0 == -len) { // Ramp after the domain
            cur.set(-1);
            return;
        }
        if (t1 < 0) {
            t1 = -t1;
        }

        // set -1 until t0 excluded
        if (t0 > 0) {
            cur.range(0, t0).set(-1);
        }
        if (t1 == t0) {
            return;
        }
        int k0 = Math.max(t0 + 1, 0);
        int k1 = Math.min(t1, len);
        double denom = t1 - t0;
        for (int k = k0; k < k1; ++k) {
            cur.set(k, (k - t0) / denom - 1);
        }
    }

    public void data(Ramp var, TsPeriod pstart, DataBlock data) {
        // start corresponds to the last period with data = -1
        // end corresponds to the first period with data = 0
        TsPeriod start = TsPeriod.of(pstart.getUnit(), var.getStart()),
                end = TsPeriod.of(pstart.getUnit(), var.getEnd());
        int t0 = pstart.until(start);
        int t1 = pstart.until(end);
        int len = data.length();
        if (t1 <= t0) {
            data.set(0);
            return;
        }

        // set -1 until t0 (included)
        if (t0 >= 0) {
            if (t0 >= len - 1) {
                data.set(-1);
                return;
            } else {
                data.range(0, t0 + 1).set(-1);
            }
        }
        // set 0 from t1
        if (t1 < len) {
            if (t1 <= 0) {
                data.set(0);
                return;
            } else {
                data.range(t1, len).set(0);
            }
        }
        int k0 = Math.max(t0 + 1, 0);
        int k1 = Math.min(t1, len);
        double denom = t1 - t0;
        for (int k = k0; k < k1; ++k) {
            data.set(k, (k - t0) / denom - 1);
        }
    }

}
