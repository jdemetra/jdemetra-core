/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.modelling.regression.Ramp;
import jdplus.data.DataBlock;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
class RampFactory implements RegressionVariableFactory<Ramp> {

    static RampFactory FACTORY=new RampFactory();

    private RampFactory(){}

    @Override
    public boolean fill(Ramp var, TsPeriod start, FastMatrix buffer) {
        data(var, TsDomain.of(start, buffer.getRowsCount()), buffer.column(0));
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(Ramp var, D domain, FastMatrix buffer) {
        data(var, domain, buffer.column(0));
        return true;
    }

    private void data(Ramp var, TimeSeriesDomain domain, DataBlock cur) {
        LocalDateTime start=var.getStart(), end = var.getEnd();
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

        // set -1 until t0 included
        if (t0 >= 0) {
            cur.range(0, t0 + 1).set(-1);
        }
        if (t1 == t0 + 1) {
            return;
        }
        int k0 = Math.max(t0 + 1, 0);
        int k1 = Math.min(t1, len);
        double denom = t1 - t0;
        for (int k = k0; k < k1; ++k) {
            cur.set(k, (k - t0) / denom - 1);
        }
    }

}
