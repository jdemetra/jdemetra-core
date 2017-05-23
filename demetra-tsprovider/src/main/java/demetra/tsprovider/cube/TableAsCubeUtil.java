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
package demetra.tsprovider.cube;

import demetra.timeseries.simplets.TsFrequency;
import demetra.tsprovider.util.ObsGathering;
import java.util.stream.Collector;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class TableAsCubeUtil {

    @Nonnull
    public String getDisplayName(@Nonnull String db, @Nonnull String table, @Nonnull String value, @Nonnull ObsGathering obsGathering) {
        return String.format("%s ~ %s \u00BB %s %s", db, table, value, toString(obsGathering));
    }

    @Nonnull
    public String getDisplayName(@Nonnull CubeId id, @Nonnull Collector<? super String, ?, String> joiner) {
        return id.isVoid() ? "All" : id.getDimensionValueStream().collect(joiner);
    }

    @Nonnull
    public String getDisplayNodeName(@Nonnull CubeId id) {
        return id.isVoid() ? "All" : id.getDimensionValue(id.getLevel() - 1);
    }

    private String toString(ObsGathering gathering) {
        return TsFrequency.Undefined == gathering.getFrequency() ? "" : ("(" + gathering.getFrequency() + "/" + gathering.getAggregationType() + ")");
    }
}
