/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.CalendarUtility;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
class LPFactory implements RegressionVariableFactory<LengthOfPeriod> {

    static LPFactory FACTORY=new LPFactory();

    private LPFactory(){}
    
    @Override
    public boolean fill(LengthOfPeriod var, TsPeriod start, Matrix buffer) {
        switch (var.getType()) {
            case LeapYear:
                lp(TsDomain.of(start, buffer.getRowsCount()), buffer.column(0));
                return true;
            case LengthOfPeriod:
                length(TsDomain.of(start, buffer.getRowsCount()), buffer.column(0));
                return true;
            default:
                return false;
        }
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(LengthOfPeriod var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private void lp(TsDomain domain, DataBlock buffer) {
        int freq = domain.getAnnualFrequency();
        if (freq < 2) {
            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int n = domain.getLength();
        int period = 0;
        if (freq == 12) {
            period = 1;
        }
        // position of the starting period in the year
        int pos = (start.start().getMonthValue() - 1) % freq;
        int idx = period - pos;
        if (idx < 0) {
            idx += freq;
        }
        // position of the first period containing 29/2
        int lppos = idx;
        int year = domain.get(idx).year();
        while (!CalendarUtility.isLeap(year)) {
            lppos += freq;
            ++year;
        }

        buffer.extract(idx, -1, freq).set(-.25);
        buffer.extract(lppos, -1, 4 * freq).set(.75);
    }

    private void length(TsDomain domain, DataBlock buffer) {
        int freq = domain.getAnnualFrequency();
        if (freq < 2) {
            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int[] ndays = CalendarUtility.daysCount(domain);
        final double m = 365.25 / freq;
        buffer.set(i -> ndays[i] - m);
    }
}
