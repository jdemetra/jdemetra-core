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
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.utilities.WeightedItem;
import ec.tstoolkit.utilities.WeightedItems;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CompositeGregorianCalendarProvider extends WeightedItems<String> implements IGregorianCalendarProvider {

    private final WeakReference<GregorianCalendarManager> calendarManager;

    public CompositeGregorianCalendarProvider(GregorianCalendarManager calendarManager) {
        this.calendarManager = new WeakReference<>(calendarManager);
    }

    public CompositeGregorianCalendarProvider() {
        this.calendarManager = new WeakReference<>(ProcessingContext.getActiveContext().getGregorianCalendars());
    }

    @Override
    @Deprecated
    public void calendarData(TradingDaysType dtype, TsDomain domain, List<DataBlock> buffer, int start) {
        calendarData(dtype, domain, buffer.subList(start, start+count(dtype)));
    }
    
    @Override
    public void calendarData(TradingDaysType dtype, TsDomain domain, List<DataBlock> buffer) {
        GregorianCalendarManager mgr = calendarManager.get();
        if (mgr == null) {
            return;
        }
        int nvars = count(dtype);
        if (nvars == 0) {
            return;
        }
        int n = domain.getLength();
        Matrix S = new Matrix(n, nvars);
        Matrix M = new Matrix(n, nvars);
        List<DataBlock> cols = new ArrayList<>();
        for (int i = 0; i < nvars; ++i) {
            cols.add(M.column(i));
        }
        for (WeightedItem<String> item : items()) {
            if (item.weight != 0) {
                mgr.get(item.item).calendarData(dtype, domain, cols);
                S.add(M.times(item.weight));
                //M.Clear(); should not be necessary. To be checked
            }
        }
        //copy the results
        DataBlockIterator scols = S.columns();
        DataBlock scur = scols.getData();
        do {
            buffer.get(scols.getPosition()).copy(scur);
        } while (scols.next());

    }

    @Override
    public List<DataBlock> holidays(TradingDaysType dtype, TsDomain domain) {
        GregorianCalendarManager mgr = calendarManager.get();
        if (mgr == null) {
            return null;
        }
        int nvars = count(dtype);
        if (nvars == 0) {
            return null;
        }
        int n = domain.getLength();
        Matrix S = new Matrix(n, nvars);
        List<DataBlock> cols = new ArrayList<>();
        for (int i = 0; i < nvars; ++i) {
            cols.add(S.column(i));
        }
        for (WeightedItem<String> item : items()) {
            if (item.weight != 0) {
                List<DataBlock> hcur = mgr.get(item.item).holidays(dtype, domain);
                for (int j = 0; j < nvars; ++j) {
                    cols.get(j).addAY(item.weight, hcur.get(j));
                }
            }
        }
        return cols;
    }

    @Override
    public int count(TradingDaysType dtype) {
        return dtype.getVariablesCount();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CompositeGregorianCalendarProvider && equals((CompositeGregorianCalendarProvider) obj));
    }

    private boolean equals(CompositeGregorianCalendarProvider other) {
        return WeightedItems.equals(this, other);
    }

    @Override
    public String getDescription(TradingDaysType dtype, int idx) {
        return DefaultGregorianCalendarProvider.description(dtype, idx);
    }
}
