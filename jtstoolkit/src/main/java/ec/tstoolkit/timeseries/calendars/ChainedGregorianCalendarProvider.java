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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ChainedGregorianCalendarProvider implements IGregorianCalendarProvider {

    public ChainedGregorianCalendarProvider(String first, Day breakDay, String second) {
        this.first = first;
        this.breakDay = breakDay;
        this.second = second;
        this.calendarManager = new WeakReference(ProcessingContext.getActiveContext().getGregorianCalendars());
    }
    
    public ChainedGregorianCalendarProvider(GregorianCalendarManager calendarManager, String first, Day breakDay, String second) {
        this.first = first;
        this.breakDay = breakDay;
        this.second = second;
        this.calendarManager = new WeakReference(calendarManager);
    }

    public final String first, second;
    /**
     * The break is defined as follows: the period that contains the day is the
     * first period where the second calendar is applied. There is no weighted
     * correction fo "incomplete" period. So, the day of the break should be
     * used with caution. Usually, it should be chosen as the first day of a
     * month, quarter... to avoid any confusion.
     */
    public final Day breakDay;
    private final WeakReference<GregorianCalendarManager> calendarManager;

    @Override
    @Deprecated
    public void calendarData(TradingDaysType dtype, TsDomain domain, List<DataBlock> buffer, int start) {
        calendarData(dtype, domain, buffer.subList(start, start+count(dtype)));
    }
    
    @Override
    public void calendarData(TradingDaysType dtype, TsDomain domain, List<DataBlock> buffer) {
        int nvars = count(dtype);
        GregorianCalendarManager mgr=calendarManager.get();
        if (mgr == null)
            return;
        if (nvars == 0 || !mgr.contains(first) || !mgr.contains(second)) {
            return;
        }
        TsPeriod br = new TsPeriod(domain.getFrequency());
        br.set(breakDay);
        // trivial cases:
        if (domain.getStart().isNotBefore(br)) {
            mgr.get(second).calendarData(dtype, domain, buffer);
        } else if (domain.getLast().isBefore(br)) {
            mgr.get(first).calendarData(dtype, domain, buffer);
        } else {
            int pos = domain.search(br);
            if (pos <= 0) {
                throw new TsException("Unexpected error in chained calendars");
            }
            // should not happen. Just to be sure...
            TsDomain bdom = new TsDomain(domain.getStart(), pos);
            TsDomain edom = domain.drop(pos, 0);
            DataBlock[] tmp = new DataBlock[nvars];
            // begin
            for (int i = 0; i < nvars; ++i) {
                tmp[i] = buffer.get(i).range(0, pos);
            }
            mgr.get(first).calendarData(dtype, bdom, Arrays.asList(tmp));
            // end
            for (int i = 0; i < nvars; ++i) {
                tmp[i] = buffer.get(i).drop(pos, 0);
            }
            mgr.get(second).calendarData(dtype, edom, Arrays.asList(tmp));
        }
    }

    @Override
    public List<DataBlock> holidays(TradingDaysType dtype, TsDomain domain) {
         GregorianCalendarManager mgr=calendarManager.get();
        if (mgr == null)
            return null;
       int nvars = count(dtype);
        if (nvars == 0 || !mgr.contains(first) || !mgr.contains(second)) {
            return null;
        }
        TsPeriod br = new TsPeriod(domain.getFrequency());
        br.set(breakDay);
        // trivial cases:
        if (domain.getStart().isNotBefore(br)) {
            return mgr.get(second).holidays(dtype, domain);
        } else if (domain.getLast().isBefore(br)) {
            return mgr.get(first).holidays(dtype, domain);
        } else {
            int pos = domain.search(br);
            if (pos <= 0) {
                throw new TsException("Unexpected error in chained calendars");
            }
            TsDomain bdom = new TsDomain(domain.getStart(), pos);
            TsDomain edom = domain.drop(pos, 0);
            DataBlock[] tmp = new DataBlock[nvars];
            // begin
            List<DataBlock> bhol = mgr.get(first).holidays(dtype, bdom);
            List<DataBlock> ehol = mgr.get(second).holidays(dtype, edom);
            for (int i = 0; i < nvars; ++i) {
                tmp[i] = new DataBlock(domain.getLength());
                tmp[i].range(0, pos).copy(bhol.get(i));
                tmp[i].drop(pos, 0).copy(ehol.get(i));
            }
            return Arrays.asList(tmp);
        }
    }

    @Override
    public int count(TradingDaysType type) {
        return type.getVariablesCount();
    }

    @Override
    public String getDescription(TradingDaysType dtype, int idx) {
        return DefaultGregorianCalendarProvider.description(dtype, idx);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ChainedGregorianCalendarProvider && equals((ChainedGregorianCalendarProvider) obj));
    }

    private boolean equals(ChainedGregorianCalendarProvider other) {
        return other.first.equals(this.first) && other.second.equals(this.second)
                && other.breakDay.equals(this.breakDay);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.first);
        hash = 97 * hash + Objects.hashCode(this.second);
        hash = 97 * hash + Objects.hashCode(this.breakDay);
        return hash;
    }
}
