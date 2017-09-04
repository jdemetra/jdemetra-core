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
package internal.tsprovider.util;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.timeseries.simplets.TsData;
import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

/**
 * A TSDataCollecor collects time observations (identified by pairs of
 * date-double) to create simple time series. Time series can be created
 * following different aggregation mode or in an automatic way. See the "make"
 * method for further information
 *
 * @author Jean Palate. National Bank of Belgium
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
class TsDataCollector {

    public TsData makeWithAggregation(ObsList obs, TsUnit unit, AggregationType convMode) {
        int n = obs.size();
        if (n == 0) {
            return null; // NO_DATA
        }
        obs.sortByPeriod();

        IntUnaryOperator toPeriodId = obs.getPeriodIdFunc(unit);

        double[] vals = new double[n];
        int[] ids = new int[n];
        int ncur = -1;

        int avn = 0;
        for (int i = 0; i < n; ++i) {
            int curid = toPeriodId.applyAsInt(i);
            double value = obs.getValue(i);
            switch (convMode) {
                case Average: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        if (ncur >= 0) {
                            vals[ncur] /= avn;
                        }
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                        avn = 1;
                    } else {
                        vals[ncur] += value;
                        ++avn;
                    }
                    break;
                }
                case Sum: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        vals[ncur] += value;
                    }
                    break;
                }
                case First: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    }
                    break;
                }
                case Last: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        ids[++ncur] = curid;
                    }
                    vals[ncur] = value;
                    break;
                }
                case Max: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = value;
                        if (dcur > vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
                case Min: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = value;
                        if (dcur < vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
            }
        }

        // correction pour le dernier cas
        if (convMode == AggregationType.Average && ncur >= 0) {
            vals[ncur] /= avn;
        }

        int firstId = ids[0];
        int lastId = ids[ncur];

        TsPeriod start = TsPeriod.of(unit, firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == ncur + 1) {
            return TsData.of(start, DoubleSequence.ofInternal(ncur + 1 == n ? vals : Arrays.copyOf(vals, ncur + 1)));
        } else {
            return TsData.of(start, expand(ncur + 1, l, ids, o -> vals[o]));
        }
    }

    private boolean isNewPeriod(int ncur, int curid, int[] ids) {
        return ncur < 0 || curid != ids[ncur];
    }

    public TsData makeFromUnknownUnit(ObsList obs) {
        int n = obs.size();
        if (n < 2) {
            return null; // NO_DATA or GUESS_SINGLE
        }
        obs.sortByPeriod();

        int s = 0;

        int[] ids = new int[n];
        TsUnit[] units = TsDataBuilderUtil.GUESSING_UNITS;
        for (; s < units.length; ++s) {
            if (makeIdsFromUnit(obs, units[s], ids)) {
                break;
            }
        }
        if (s == units.length) {
            return null; // GUESS_DUPLICATION
        }
        int firstId = ids[0];
        int lastId = ids[n - 1];

        TsPeriod start = TsPeriod.of(units[s], firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == n) {
            return TsData.of(start, obs.getValues());
        } else {
            return TsData.of(start, expand(n, l, ids, obs::getValue));
        }
    }

    private boolean makeIdsFromUnit(ObsList obs, TsUnit unit, int[] ids) {
        IntUnaryOperator toPeriodId = obs.getPeriodIdFunc(unit);
        ids[0] = toPeriodId.applyAsInt(0);
        for (int i = 1; i < ids.length; ++i) {
            ids[i] = toPeriodId.applyAsInt(i);
            if (ids[i] == ids[i - 1]) {
                return false;
            }
        }
        return true;
    }

    public TsData makeWithoutAggregation(ObsList obs, TsUnit unit) {
        int n = obs.size();
        if (n == 0) {
            return null; // NO_DATA
        }

        obs.sortByPeriod();

        IntUnaryOperator toPeriodId = obs.getPeriodIdFunc(unit);

        int[] ids = new int[n];
        ids[0] = toPeriodId.applyAsInt(0);
        for (int i = 1; i < n; i++) {
            ids[i] = toPeriodId.applyAsInt(i);
            if (ids[i] == ids[i - 1]) {
                return null; // DUPLICATION_WITHOUT_AGGREGATION
            }
        }

        int firstId = ids[0];
        int lastId = ids[n - 1];

        TsPeriod start = TsPeriod.of(unit, firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == n) {
            return TsData.of(start, obs.getValues());
        } else {
            return TsData.of(start, expand(n, l, ids, obs::getValue));
        }
    }

    private DoubleSequence expand(int currentSize, int expectedSize, int[] ids, IntToDoubleFunction valueFunc) {
        double[] result = new double[expectedSize];
        Arrays.fill(result, Double.NaN);
        result[0] = valueFunc.applyAsDouble(0);
        for (int j = 1; j < currentSize; ++j) {
            result[ids[j] - ids[0]] = valueFunc.applyAsDouble(j);
        }
        return DoubleSequence.ofInternal(result);
    }
}
