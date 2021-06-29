/*
 * Copyright 2017 National Bank of Belgium
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
package internal.timeseries.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsCharacteristics;
import demetra.timeseries.util.ObsGathering;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsDataBuilderUtil {

    public final TsData NO_DATA = TsData.empty("No data available");
    public final TsData INVALID_AGGREGATION = TsData.empty("Invalid aggregation mode");
    public final TsData GUESS_SINGLE = TsData.empty("Cannot guess frequency with a single observation");
    public final TsData GUESS_DUPLICATION = TsData.empty("Cannot guess frequency with duplicated periods");
    public final TsData DUPLICATION_WITHOUT_AGGREGATION = TsData.empty("Duplicated observations without aggregation");
    public final TsData UNKNOWN = TsData.empty("Unexpected error");

    boolean isOrdered(ObsCharacteristics[] characteristics) {
        return Arrays.binarySearch(characteristics, ObsCharacteristics.ORDERED) != -1;
    }

    boolean isValid(ObsGathering gathering) {
        return !(gathering.getUnit().equals(TsUnit.UNDEFINED) && gathering.getAggregationType() != AggregationType.None);
    }

    Function<ObsList, TsData> getMaker(ObsGathering gathering) {
        if (gathering.getUnit().equals(TsUnit.UNDEFINED)) {
            return o -> makeFromUnknownFrequency(o);
        }
        if (gathering.getAggregationType() != AggregationType.None) {
            return o -> makeWithAggregation(o, gathering.getUnit(), TsPeriod.DEFAULT_EPOCH, gathering.getAggregationType(), gathering.isAllowPartialAggregation());
        }
        return o -> makeWithoutAggregation(o, gathering.getUnit(), TsPeriod.DEFAULT_EPOCH);
    }

    private TsData makeFromUnknownFrequency(ObsList obs) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            case 1:
                return GUESS_SINGLE;
            default:
                TsData result = TsDataCollector.makeFromUnknownUnit(obs);
                return result != null ? result : GUESS_DUPLICATION;
        }
    }

    private TsData makeWithoutAggregation(ObsList obs, TsUnit unit, LocalDateTime reference) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeWithoutAggregation(obs, unit, reference);
                return result != null ? result : DUPLICATION_WITHOUT_AGGREGATION;
        }
    }

    private TsData makeWithAggregation(ObsList obs, TsUnit unit, LocalDateTime reference, AggregationType aggregationType, boolean allowPartialAggregation) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeFromUnknownUnit(obs);
                if (result != null && unit.contains(result.getTsUnit())) {
                    // should succeed
                    result = result.aggregate(unit, aggregationType, !allowPartialAggregation);
                } else {
                    result = TsDataCollector.makeWithAggregation(obs, unit, reference, aggregationType);
                }
                return result != null ? result : UNKNOWN;
        }
    }
}
