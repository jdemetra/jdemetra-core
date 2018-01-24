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
package demetra.tsprovider.poc;

import demetra.timeseries.TsData;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cursor.TsCursor;
import demetra.io.IteratorWithIO;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import static demetra.timeseries.TsUnit.MONTH;

/**
 *
 * @author Philippe Charles
 */
final class FakeDbAccessor implements CubeAccessor {

    private final CubeId root;
    private final Map<CubeId, TsData> data;

    public FakeDbAccessor() {
        this.root = CubeId.root("REGION", "SECTOR");
        this.data = new HashMap<>();
        data.put(root.child("BE", "INDUSTRY"), TsData.random(MONTH, 1));
        data.put(root.child("FR", "INDUSTRY"), TsData.random(MONTH, 2));
        data.put(root.child("BE", "STUFF"), TsData.random(MONTH, 3));
        data.put(root.child("FR", "STUFF"), TsData.empty("Not enough data"));
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
        return toDataCursor(data).filter(id::isAncestorOf);
    }

    @Override
    public TsCursor<CubeId> getAllSeriesWithData(CubeId id) throws IOException {
        return toDataCursor(data).filter(id::isAncestorOf);
    }

    @Override
    public TsCursor<CubeId> getSeriesWithData(CubeId id) throws IOException {
        return toDataCursor(data).filter(id::isAncestorOf);
    }

    @Override
    public IteratorWithIO<CubeId> getChildren(CubeId id) throws IOException {
        return IteratorWithIO.checked(data.keySet().stream()
                .filter(id::isAncestorOf)
                .map(o -> o.getDimensionValue(id.getLevel()))
                .distinct().sorted().iterator())
                .map(id::child);
    }

    @Override
    public String getDisplayName() throws IOException {
        return "Fake";
    }

    @Override
    public String getDisplayName(CubeId id) throws IOException {
        return id.isVoid() ? "All" : id.getDimensionValueStream().collect(Collectors.joining(", "));
    }

    @Override
    public String getDisplayNodeName(CubeId id) throws IOException {
        return id.isVoid() ? "All" : id.getDimensionValue(id.getLevel() - 1);
    }

    private static TsCursor<CubeId> toDataCursor(Map<CubeId, TsData> data) {
        return TsCursor
                .from(data.entrySet().iterator(), Map.Entry::getValue, o -> Collections.emptyMap(), o -> o.getKey().getDimensionValueStream().collect(Collectors.joining("/")))
                .map(Map.Entry::getKey);
    }
}
