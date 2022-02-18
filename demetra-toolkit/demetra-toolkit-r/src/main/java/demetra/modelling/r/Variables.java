/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.r;

import demetra.data.Range;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.JulianEasterVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.PeriodicContrasts;
import demetra.timeseries.regression.PeriodicDummies;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.StockTradingDays;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TrigonometricVariables;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.modelling.regression.HolidaysCorrectionFactory;
import jdplus.modelling.regression.Regression;
import jdplus.modelling.regression.TrigonometricVariablesFactory;
import demetra.math.matrices.Matrix;
import java.time.DayOfWeek;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Variables {

    public Matrix td(TsDomain domain, int[] groups, boolean contrasts) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            GenericTradingDaysFactory.FACTORY.fill(gtd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount());
            GenericTradingDaysFactory.FACTORY.fill(gtd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public Matrix htd(Calendar calendar, TsDomain domain, int[] groups, int dw, boolean contrasts) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, DayOfWeek.of(dw), true);
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, DayOfWeek.of(dw), false);
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount());
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }
    
    public Matrix htd(Calendar calendar, TsDomain domain, int[] groups, int dw, boolean contrasts, boolean meanCorrection) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, DayOfWeek.of(dw), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, DayOfWeek.of(dw), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount());
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public Matrix htd(ModellingContext ctxt, String name, TsDomain domain, int[] groups, int dw, boolean contrasts, boolean meanCorrection) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(name, ctxt.getCalendars(), DayOfWeek.of(dw), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(name, ctxt.getCalendars(), DayOfWeek.of(dw), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            FastMatrix m = FastMatrix.make(domain.getLength(), dc.getGroupsCount());
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public double[] easter(TsDomain domain, int duration, int endpos, String corr) {
        EasterVariable.Correction correction = EasterVariable.Correction.valueOf(corr);
        EasterVariable easter = EasterVariable.builder()
                .duration(duration)
                .endPosition(endpos)
                .meanCorrection(correction)
                .build();

        DataBlock x = Regression.x(domain, easter);
        return x.getStorage();
    }

    public double[] julianEaster(TsDomain domain, int duration) {
        JulianEasterVariable easter = new JulianEasterVariable(duration, true);
        DataBlock x = Regression.x(domain, easter);
        return x.getStorage();
    }

    public double[] leapYear(TsDomain domain, boolean lp) {
        LengthOfPeriod lpvar = new LengthOfPeriod(lp ? LengthOfPeriodType.LeapYear : LengthOfPeriodType.LengthOfPeriod);

        DataBlock x = Regression.x(domain, lpvar);
        return x.toArray();
    }

    private double[] ao(TsDomain domain, LocalDateTime pos) {
        AdditiveOutlier ao = new AdditiveOutlier(pos);
        DataBlock x = Regression.x(domain, ao);
        return x.getStorage();
    }

    public double[] ao(TsDomain domain, String pos) {
        LocalDate dt = LocalDate.parse(pos, DateTimeFormatter.ISO_DATE);
        return ao(domain, dt.atStartOfDay());
    }

    public double[] ao(TsDomain domain, int pos) {
        LocalDateTime dt = domain.get(pos).start();
        return ao(domain, dt);
    }

    private double[] ls(TsDomain domain, LocalDateTime pos, boolean zeroended) {
        LevelShift ls = new LevelShift(pos, zeroended);
        DataBlock x = Regression.x(domain, ls);
        return x.getStorage();
    }

    public double[] ls(TsDomain domain, String pos, boolean zeroended) {
        LocalDate dt = LocalDate.parse(pos, DateTimeFormatter.ISO_DATE);
        return ls(domain, dt.atStartOfDay(), zeroended);
    }

    public double[] ls(TsDomain domain, int pos, boolean zeroended) {
        LocalDateTime dt = domain.get(pos).start();
        return ls(domain, dt, zeroended);
    }

    private double[] so(TsDomain domain, LocalDateTime pos, boolean zeroended) {
        PeriodicOutlier so = new PeriodicOutlier(pos, domain.getAnnualFrequency(), zeroended);
        DataBlock x = Regression.x(domain, so);
        return x.getStorage();
    }

    public double[] so(TsDomain domain, String pos, boolean zeroended) {
        LocalDate dt = LocalDate.parse(pos, DateTimeFormatter.ISO_DATE);
        return so(domain, dt.atStartOfDay(), zeroended);
    }

    public double[] so(TsDomain domain, int pos, boolean zeroended) {
        LocalDateTime dt = domain.get(pos).start();
        return so(domain, dt, zeroended);
    }

    private double[] tc(TsDomain domain, LocalDateTime pos, double delta) {
        TransitoryChange tc = new TransitoryChange(pos, delta);
        DataBlock x = Regression.x(domain, tc);
        return x.getStorage();
    }

    public double[] tc(TsDomain domain, String pos, double delta) {
        LocalDate dt = LocalDate.parse(pos, DateTimeFormatter.ISO_DATE);
        return tc(domain, dt.atStartOfDay(), delta);
    }

    public double[] tc(TsDomain domain, int pos, double delta) {
        LocalDateTime dt = domain.get(pos).start();
        return tc(domain, dt, delta);
    }

    private double[] ramp(TsDomain domain, LocalDateTime start, LocalDateTime end) {
        Ramp ramp = new Ramp(start, end);
        DataBlock x = Regression.x(domain, ramp);
        return x.getStorage();
    }

    public double[] ramp(TsDomain domain, String start, String end) {
        LocalDate d0 = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate d1 = LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
        return ramp(domain, d0.atStartOfDay(), d1.atStartOfDay());
    }

    public double[] ramp(TsDomain domain, int start, int end) {
        return ramp(domain, domain.get(start).start(), domain.get(end).start());
    }

    public FastMatrix stockTradingDays(TsDomain domain, int w) {
        StockTradingDays std = new StockTradingDays(w);
        return Regression.matrix(domain, std);
    }

    public FastMatrix periodicDummies(TsDomain domain) {
        PeriodicDummies var = new PeriodicDummies(domain.getAnnualFrequency());
        return Regression.matrix(domain, var);
    }

    public FastMatrix periodicContrasts(TsDomain domain) {
        PeriodicContrasts var = new PeriodicContrasts(domain.getAnnualFrequency());
        return Regression.matrix(domain, var);
    }

    public FastMatrix trigonometricVariables(TsDomain domain, int[] seasonal) {
        TrigonometricVariables var;
        if (seasonal == null) {
            var = TrigonometricVariables.regular(domain.getAnnualFrequency());
        } else {
            var = TrigonometricVariables.regular(domain.getAnnualFrequency(), seasonal);
        }
        return Regression.matrix(domain, var);
    }
    
    public FastMatrix trigonometricVariables(TsDomain domain, int[] seasonal, String reference) {
        LocalDate ref = LocalDate.parse(reference, DateTimeFormatter.ISO_DATE);
        TrigonometricVariables var;
        if (seasonal == null) {
            var = TrigonometricVariables.regular(domain.getAnnualFrequency(), ref.atStartOfDay());
        } else {
            var = TrigonometricVariables.regular(domain.getAnnualFrequency(), seasonal, ref.atStartOfDay());
        }
        return Regression.matrix(domain, var);
    }

    public FastMatrix trigonometricVariables(double[] freq, int length, int start) {
        TrigonometricVariables var = new TrigonometricVariables(freq, TsPeriod.DEFAULT_EPOCH);
        return TrigonometricVariablesFactory.matrix(var, length, start);
    }

    public double[] interventionVariable(TsDomain domain, double delta, double sdelta, String[] starts, String[] ends) {

        InterventionVariable.Builder builder = InterventionVariable.builder()
                .delta(delta)
                .deltaSeasonal(sdelta);
        for (int i = 0; i < starts.length; ++i) {
            LocalDate start = LocalDate.parse(starts[i], DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(ends[i], DateTimeFormatter.ISO_DATE);
            builder.sequence(Range.of(start.atStartOfDay(), end.atStartOfDay()));
        }
        InterventionVariable var = builder.build();
        return Regression.matrix(domain, var).getStorage();
    }

    public double[] interventionVariable(TsDomain domain, double delta, double sdelta, int[] starts, int[] ends) {

        InterventionVariable.Builder builder = InterventionVariable.builder()
                .delta(delta)
                .deltaSeasonal(sdelta);
        for (int i = 0; i < starts.length; ++i) {
            LocalDateTime start = domain.get(starts[i]).start();
            LocalDateTime end = domain.get(ends[i]).start();
            builder.sequence(Range.of(start, end));
        }
        InterventionVariable var = builder.build();
        return Regression.matrix(domain, var).getStorage();
    }
}
