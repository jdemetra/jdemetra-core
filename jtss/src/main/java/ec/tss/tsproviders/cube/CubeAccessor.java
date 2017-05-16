/*
 * Copyright 2015 National Bank of Belgium
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
package ec.tss.tsproviders.cube;

import ec.tss.tsproviders.cursor.TsCursor;
import ec.tss.tsproviders.utils.IteratorWithIO;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface CubeAccessor {

    @Nullable
    IOException testConnection();

    @Nonnull
    CubeId getRoot();

    @Nonnull
    TsCursor<CubeId> getAllSeries(@Nonnull CubeId id) throws IOException;

    @Nonnull
    TsCursor<CubeId> getAllSeriesWithData(@Nonnull CubeId id) throws IOException;

    @Nonnull
    TsCursor<CubeId> getSeriesWithData(@Nonnull CubeId id) throws IOException;

    @Nonnull
    IteratorWithIO<CubeId> getChildren(@Nonnull CubeId id) throws IOException;

    @Nonnull
    String getDisplayName() throws IOException;

    @Nonnull
    String getDisplayName(@Nonnull CubeId id) throws IOException;

    @Nonnull
    String getDisplayNodeName(@Nonnull CubeId id) throws IOException;

    @Nonnull
    default CubeAccessor bulk(@Nonnegative int depth, @Nonnull ConcurrentMap<CubeId, Object> cache) {
        return new CubeAccessors.BulkCubeAccessor(this, depth, Objects.requireNonNull(cache));
    }
}
