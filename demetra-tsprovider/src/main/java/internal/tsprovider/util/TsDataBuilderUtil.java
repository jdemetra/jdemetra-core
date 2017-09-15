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
package internal.tsprovider.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import static demetra.timeseries.TsUnit.*;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.tsprovider.OptionalTsData;
import static demetra.tsprovider.OptionalTsData.present;
import demetra.tsprovider.util.ObsCharacteristics;
import demetra.tsprovider.util.ObsGathering;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsDataBuilderUtil {

    public final OptionalTsData NO_DATA = OptionalTsData.absent("No data available");
    public final OptionalTsData INVALID_AGGREGATION = OptionalTsData.absent("Invalid aggregation mode");
    public final OptionalTsData GUESS_SINGLE = OptionalTsData.absent("Cannot guess frequency with a single observation");
    public final OptionalTsData GUESS_DUPLICATION = OptionalTsData.absent("Cannot guess frequency with duplicated periods");
    public final OptionalTsData DUPLICATION_WITHOUT_AGGREGATION = OptionalTsData.absent("Duplicated observations without aggregation");
    public final OptionalTsData UNKNOWN = OptionalTsData.absent("Unexpected error");

    public final List<TsUnit> GUESSING_UNITS = Arrays.asList(TsDataCollector.GUESSING_UNITS);
    public final List<TsUnit> DEFINED_UNITS = Arrays.asList(YEARLY, HALF_YEARLY, QUADRI_MONTHLY, QUARTERLY, BI_MONTHLY, MONTHLY, DAILY, HOURLY, MINUTELY);
    public final List<TsUnit> ALL_UNITS = Stream.concat(Stream.of(UNDEFINED), GUESSING_UNITS.stream()).collect(Collectors.toList());

    boolean isOrdered(ObsCharacteristics[] characteristics) {
        return Arrays.binarySearch(characteristics, ObsCharacteristics.ORDERED) != -1;
    }

    boolean isValid(ObsGathering gathering) {
        return !(gathering.getUnit().equals(TsUnit.UNDEFINED) && gathering.getAggregationType() != AggregationType.None);
    }

    Function<ObsList, OptionalTsData> getMaker(ObsGathering gathering) {
        if (gathering.getUnit().equals(TsUnit.UNDEFINED)) {
            return o -> makeFromUnknownFrequency(o);
        }
        if (gathering.getAggregationType() != AggregationType.None) {
            return o -> makeWithAggregation(o, gathering.getUnit(), gathering.getAggregationType(), gathering.isComplete());
        }
        return o -> makeWithoutAggregation(o, gathering.getUnit());
    }

    private OptionalTsData makeFromUnknownFrequency(ObsList obs) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            case 1:
                return GUESS_SINGLE;
            default:
                TsData result = TsDataCollector.makeFromUnknownUnit(obs);
                return result != null ? present(result) : GUESS_DUPLICATION;
        }
    }

    private OptionalTsData makeWithoutAggregation(ObsList obs, TsUnit unit) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeWithoutAggregation(obs, unit);
                return result != null ? present(result) : DUPLICATION_WITHOUT_AGGREGATION;
        }
    }

    private OptionalTsData makeWithAggregation(ObsList obs, TsUnit unit, AggregationType convMode, boolean complete) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeFromUnknownUnit(obs);
                if (result != null && result.getUnit().contains(unit)) {
                    // should succeed
                    result = TsDataConverter.changeTsUnit(result, unit, convMode, complete);
                } else {
                    result = TsDataCollector.makeWithAggregation(obs, unit, convMode);
                }
                return result != null ? present(result) : UNKNOWN;
        }
    }
}
