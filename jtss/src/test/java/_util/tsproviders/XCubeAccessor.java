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

import ec.tss.tsproviders.cube.CubeAccessor;
import ec.tss.tsproviders.cube.CubeId;
import ec.tss.tsproviders.cursor.TsCursor;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public final class XCubeAccessor implements CubeAccessor {

    private final CubeId root;
    private final ResourceWatcher<?> resourceWatcher;

    public XCubeAccessor(CubeId root, ResourceWatcher<?> resourceWatcher) {
        this.root = Objects.requireNonNull(root);
        this.resourceWatcher = Objects.requireNonNull(resourceWatcher);
    }

    @Override
    public IOException testConnection() {
        return null;
    }

    @Override
    public CubeId getRoot() {
        return root;
    }

    @Override
    public TsCursor<CubeId> getAllSeries(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return TsCursor.<CubeId>empty().onClose(resourceWatcher.watchAsCloseable("getAllSeries"));
    }

    @Override
    public TsCursor<CubeId> getAllSeriesWithData(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return TsCursor.<CubeId>empty().onClose(resourceWatcher.watchAsCloseable("getAllSeriesWithData"));
    }

    @Override
    public TsCursor<CubeId> getSeriesWithData(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return TsCursor.<CubeId>empty().onClose(resourceWatcher.watchAsCloseable("getSeriesWithData"));
    }

    @Override
    public TsCursor<CubeId> getChildren(CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return TsCursor.<CubeId>empty().onClose(resourceWatcher.watchAsCloseable("getChildren"));
    }

    @Override
    public String getDisplayName() throws IOException {
        return root.toString();
    }
}
