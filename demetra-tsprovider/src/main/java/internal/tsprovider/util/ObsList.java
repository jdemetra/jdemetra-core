/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.tsprovider.util;

import demetra.data.DoubleValues;
import demetra.design.Internal;
import demetra.design.NewObject;
import demetra.timeseries.simplets.TsFrequency;
import demetra.utilities.functions.ObjLongToIntFunction;

/**
 * INTERNAL USE ONLY
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Internal
public interface ObsList {

    int size();

    void sortByPeriod();

    int getPeriodId(TsFrequency frequency, int index) throws IndexOutOfBoundsException;

    double getValue(int index) throws IndexOutOfBoundsException;

    @NewObject
    default DoubleValues getValues() {
        double[] result = new double[size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getValue(i);
        }
        return DoubleValues.ofInternal(result);
    }

    @Internal
    public interface LongObsList extends ObsList {

        void clear();

        void add(long period, double value);
    }

    @NewObject
    static LongObsList newLongObsList(boolean preSorted, ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc) {
        return preSorted
                ? new ObsLists.PreSortedLongObsList(tsPeriodIdFunc, 32)
                : new ObsLists.SortableLongObsList(tsPeriodIdFunc);
    }
}
