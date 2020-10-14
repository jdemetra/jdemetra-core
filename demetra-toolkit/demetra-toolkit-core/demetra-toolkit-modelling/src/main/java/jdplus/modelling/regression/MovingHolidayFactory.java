/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.MovingHolidayVariable;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesPeriod;

/**
 *
 * @author palatej
 */
public class MovingHolidayFactory implements RegressionVariableFactory<MovingHolidayVariable> {

    private static final List<MovingHolidayProvider> PROVIDERS;

    static {
        PROVIDERS = MovingHolidayProviderLoader.load();
    }
    static MovingHolidayFactory FACTORY=new MovingHolidayFactory();

    private static MovingHolidayProvider find(String id) {
        Optional<MovingHolidayProvider> provider = PROVIDERS.stream().filter(p -> p.identifier().equals(id)).findFirst();
        return provider.orElse(null);
    }

    /**
     *
     * @param var
     * @param start
     * @param buffer
     * @return
     */
    @Override
    public boolean fill(MovingHolidayVariable var, TsPeriod start, Matrix buffer) {
        MovingHolidayProvider provider = find(var.getEvent());
        if (provider == null) {
            return false;
        }
        DataBlock data = buffer.column(0);

        TsDomain domain = TsDomain.of(start, buffer.getRowsCount());
        TsUnit unit = domain.getTsUnit();
        if (unit.getChronoUnit().isTimeBased()) {
            return false;
        }
        double[] w = var.getPattern().getWeights();
        int hstart = var.getPattern().getStart();
        // period from hstart to hstart+w.length
        // hstart usually negative
        LocalDate sd = domain.start().toLocalDate(), ed = domain.end().toLocalDate();
        // correct the domain for limit cases
        LocalDate[] holidays = provider.holidays(sd.plusDays(-hstart-w.length), ed.plusDays(-hstart));
        double all = var.getPattern().sum();
        for (int i = 0; i < holidays.length; ++i) {
            // current moving holiday
            LocalDate dhstart = holidays[i].plusDays(hstart), dhend = holidays[i].plusDays(w.length + hstart);
            // current moving holidays is beofre the domain. next
            if (! sd.isBefore(dhend))
                continue;
            // current moving holidays is after the domain. stop
            if (! dhstart.isBefore(ed))
                break;
            // periods which contains the start/end
            TsPeriod pstart = TsPeriod.of(unit, dhstart), pend = TsPeriod.of(unit, dhend);
            int spos = domain.indexOf(pstart), epos = domain.indexOf(pend);
            if (spos == epos && spos>=0) { // simple case. all effect in 1 period 
                data.set(spos, all);
            } else {
                int ibeg = 0, iend = 0;
                LocalDate cur = dhstart;
                if (spos < 0) {
                    ibeg = (int) cur.until(sd, ChronoUnit.DAYS);
                    iend=ibeg;                    
                    cur = sd;
                    spos = 0;
                }
                if (epos < 0) {
                    epos = domain.getLength();
                }
                for (int k = spos; k < epos; ++k) {
                    LocalDate next = domain.get(k + 1).start().toLocalDate();
                    iend = (int) cur.until(next, ChronoUnit.DAYS);
                    if (iend > w.length) {
                        data.set(k, sum(w, ibeg, w.length));
                        iend=w.length;
                        break;
                    } else {
                        data.set(k, sum(w, ibeg, iend));
                        ibeg = iend;
                        cur = next;
                    }
                }
                if (iend < w.length && epos<domain.getLength()) {
                    data.set(epos, sum(w, iend, w.length));
                }
            }
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesPeriod, D extends TimeSeriesDomain<P>> boolean fill(MovingHolidayVariable var, D domain, Matrix buffer) {
//        MovingHolidayProvider provider=find(var.getEvent());
//        if (provider == null)
//            return false;
//        P start = domain.get(0), end=domain.get(domain.length()-1);
//        LocalDate[] holidays = provider.holidays(start.start().toLocalDate(), end.end().toLocalDate());
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static double sum(double[] w, int start, int end) {
        double s = 0;
        for (int i = start; i < end; ++i) {
            s += w[i];
        }
        return s;
    }

}
