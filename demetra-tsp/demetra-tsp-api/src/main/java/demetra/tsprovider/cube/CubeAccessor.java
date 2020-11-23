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
package demetra.tsprovider.cube;

import nbbrd.design.ThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface CubeAccessor extends Closeable {

    @Nullable
    IOException testConnection();

    @NonNull
    CubeId getRoot() throws IOException;

    @NonNull
    Stream<CubeSeries> getAllSeries(@NonNull CubeId id) throws IOException;

    @NonNull
    Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) throws IOException;

    @Nullable
    CubeSeries getSeries(@NonNull CubeId id) throws IOException;

    @Nullable
    CubeSeriesWithData getSeriesWithData(@NonNull CubeId id) throws IOException;

    @NonNull
    Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException;

    @NonNull
    String getDisplayName() throws IOException;

    @NonNull
    String getDisplayName(@NonNull CubeId id) throws IOException;

    @NonNull
    String getDisplayNodeName(@NonNull CubeId id) throws IOException;
}
