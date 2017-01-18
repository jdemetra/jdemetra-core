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
package ec.tss.tsproviders.cursor;

import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import ec.tstoolkit.utilities.TreeOfIds;
import ec.tstoolkit.utilities.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
final class IdTsSupport implements HasTsCursor, HasDataDisplayName {

    private final TreeOfIds tree;
    private final Function<Id, OptionalTsData> toData;
    private final Function<Id, Map<String, String>> toMeta;
    private final Function<Id, Map<String, String>> nodeToMeta;

    private IdTsSupport(TreeOfIds tree, Function<Id, OptionalTsData> toData, Function<Id, Map<String, String>> toMeta, Function<Id, Map<String, String>> nodeToMeta) {
        this.tree = tree;
        this.toData = toData;
        this.toMeta = toMeta;
        this.nodeToMeta = nodeToMeta;
    }

    @Override
    public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IllegalArgumentException, IOException {
        return TsCursor.from(leafs(tree).iterator(), getDataFunc(type), getMetaFunc(type))
                .transform(getSeriesDataSetFunc(dataSource))
                .withMetaData(getNodeMeta(""));
    }

    @Override
    public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
        return getData(dataSet.getDataSource(), type)
                .withMetaData(getNodeMeta(dataSet))
                .filter(isChildOf(dataSet));
    }

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        return "root";
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        return dataSet.get(ID_PARAM);
    }

    private Function<Id, OptionalTsData> getDataFunc(TsInformationType type) {
        return type.encompass(TsInformationType.Data) ? toData : o -> NOT_REQUESTED_DATA;
    }

    private Function<Id, Map<String, String>> getMetaFunc(TsInformationType type) {
        return type.encompass(TsInformationType.MetaData) ? toMeta : o -> NOT_REQUESTED_META;
    }

    private Map<String, String> getNodeMeta(DataSet parent) {
        String parentId = parent.getParam(ID_PARAM).orElseThrow(IllegalArgumentException::new);
        return getNodeMeta(parentId);
    }

    private Map<String, String> getNodeMeta(String parentId) {
        return Optional.ofNullable(nodeToMeta.apply(new LinearId(parentId))).orElseGet(Collections::emptyMap);
    }

    private static final String ID_PARAM = "id";
    private static final OptionalTsData NOT_REQUESTED_DATA = OptionalTsData.absent("Not requested");
    private static final Map<String, String> NOT_REQUESTED_META = null;

    private static Stream<Id> leafs(TreeOfIds tree) {
        return Stream.of(tree.roots())
                .flatMap(o -> Trees.breadthFirstStream(o, x -> Stream.of(tree.children(x))))
                .filter(o -> tree.children(o).length == 0);
    }

    private static Function<Id, DataSet> getSeriesDataSetFunc(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        return o -> builder.put(ID_PARAM, o.toString()).build();
    }

    private static Predicate<DataSet> isChildOf(DataSet parent) {
        String parentId = parent.getParam(ID_PARAM).orElseThrow(IllegalArgumentException::new);
        return o -> o.getParam(ID_PARAM).filter(x -> x.startsWith(parentId)).isPresent();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TsData dataOf(TsFrequency freq, int firstyear, int firstperiod, double... values) {
        return new TsData(freq, firstyear, firstperiod, values, false);
    }

    public static MetaData metaOf(String key, String value) {
        return new MetaData(Collections.singletonMap(key, value));
    }

    public static final class Builder {

        private Function<Id, Map<String, String>> nodeToMeta = o -> Collections.emptyMap();
        private final List<Item> items = new ArrayList<>();

        public Builder add(Id id) {
            items.add(new Item(id, OptionalTsData.absent("No data available"), Collections.emptyMap()));
            return this;
        }

        public Builder add(Id id, TsData data) {
            items.add(new Item(id, OptionalTsData.present(data), Collections.emptyMap()));
            return this;
        }

        public Builder add(Id id, TsData data, Map<String, String> meta) {
            items.add(new Item(id, OptionalTsData.present(data), meta));
            return this;
        }

        public Builder add(Id id, Map<String, String> meta) {
            items.add(new Item(id, OptionalTsData.absent("No data available"), meta));
            return this;
        }

        public Builder nodeMeta(Function<Id, Map<String, String>> nodeToMeta) {
            this.nodeToMeta = nodeToMeta;
            return this;
        }

        public IdTsSupport build() {
            Map<Id, Item> target = items.stream().collect(Collectors.toMap(o -> o.id, o -> o));
            return new IdTsSupport(new TreeOfIds(new ArrayList(target.keySet())), o -> target.get(o).data, o -> target.get(o).meta, nodeToMeta);
        }
    }

    private static final class Item {

        final Id id;
        final OptionalTsData data;
        final Map<String, String> meta;

        private Item(Id id, OptionalTsData data, Map<String, String> meta) {
            this.id = id;
            this.data = data;
            this.meta = meta;
        }
    }
}
