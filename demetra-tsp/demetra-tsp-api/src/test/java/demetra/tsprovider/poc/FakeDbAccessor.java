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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import static demetra.timeseries.TsUnit.MONTH;
import demetra.tsprovider.cube.CubeSeries;
import demetra.tsprovider.cube.CubeSeriesWithData;
import java.util.stream.Stream;

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
    public Stream<CubeSeries> getAllSeries(CubeId id) throws IOException {
        return toSeriesStream(data).filter(ts -> id.isAncestorOf(ts.getId()));
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId id) throws IOException {
        return toSeriesWithDataStream(data).filter(ts -> id.isAncestorOf(ts.getId()));
    }

    @Override
    public CubeSeries getSeries(CubeId id) throws IOException {
        return toSeriesStream(data).filter(ts -> id.isAncestorOf(ts.getId())).findFirst().orElse(null);
    }

    @Override
    public CubeSeriesWithData getSeriesWithData(CubeId id) throws IOException {
        return toSeriesWithDataStream(data).filter(ts -> id.isAncestorOf(ts.getId())).findFirst().orElse(null);
    }

    @Override
    public Stream<CubeId> getChildren(CubeId id) throws IOException {
        return data.keySet().stream()
                .filter(id::isAncestorOf)
                .map(o -> o.getDimensionValue(id.getLevel()))
                .distinct().sorted()
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

    private static Stream<CubeSeries> toSeriesStream(Map<CubeId, TsData> data) {
        return data.entrySet().stream()
                .map(o -> new CubeSeries(o.getKey(), getLabel(o.getKey()), Collections.emptyMap()));
    }

    private static Stream<CubeSeriesWithData> toSeriesWithDataStream(Map<CubeId, TsData> data) {
        return data.entrySet().stream()
                .map(o -> new CubeSeriesWithData(o.getKey(), getLabel(o.getKey()), Collections.emptyMap(), o.getValue()));
    }

    private static String getLabel(CubeId id) {
        return id.getDimensionValueStream().collect(Collectors.joining("/"));
    }

    @Override
    public void close() throws IOException {
    }
}
