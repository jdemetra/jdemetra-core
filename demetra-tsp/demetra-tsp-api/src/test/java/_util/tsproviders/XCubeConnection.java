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
package _util.tsproviders;

import demetra.io.ResourceWatcher;
import demetra.tsprovider.cube.CubeConnection;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSeries;
import demetra.tsprovider.cube.CubeSeriesWithData;
import nbbrd.io.function.IORunnable;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public final class XCubeConnection implements CubeConnection {

    private final CubeId root;
    private final ResourceWatcher resourceWatcher;

    public XCubeConnection(CubeId root, ResourceWatcher resourceWatcher) {
        this.root = Objects.requireNonNull(root);
        this.resourceWatcher = Objects.requireNonNull(resourceWatcher);
    }

    @Override
    public Optional<IOException> testConnection() {
        return Optional.empty();
    }

    @Override
    public CubeId getRoot() {
        return root;
    }

    @Override
    public Stream<CubeSeries> getAllSeries(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeSeries>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getAllSeries")::close));
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeSeriesWithData>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getAllSeriesWithData")::close));
    }

    @Override
    public Optional<CubeSeries> getSeries(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        resourceWatcher.watchAsCloseable("getSeries").close();
        return Optional.empty();
    }

    @Override
    public Optional<CubeSeriesWithData> getSeriesWithData(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        resourceWatcher.watchAsCloseable("getSeriesWithData").close();
        return Optional.empty();
    }

    @Override
    public Stream<CubeId> getChildren(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeId>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getChildren")::close));
    }

    @Override
    public String getDisplayName() throws IOException {
        return root.toString();
    }

    @Override
    public String getDisplayName(CubeId id) throws IOException {
        return id.toString();
    }

    @Override
    public String getDisplayNodeName(CubeId id) throws IOException {
        return id.toString();
    }

    @Override
    public void close() throws IOException {
    }
}
