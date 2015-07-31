/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.sdmx.engine;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import static ec.tss.tsproviders.sdmx.engine.FluentDom.*;
import ec.tss.tsproviders.sdmx.model.SdmxGroup;
import ec.tss.tsproviders.sdmx.model.SdmxItem;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Parsers.Parser;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Kristof Bayens
 */
public class GenericDocFactory extends AbstractDocumentFactory {

    @Override
    public String getName() {
        return "Generic doc";
    }

    @Override
    public boolean isValid(Document doc) {
        Optional<Node> dataSetNode = lookupDataSetNode(doc);
        return dataSetNode.isPresent() && hasKeyFamilyRef(dataSetNode.get());
    }

    @Override
    public SdmxSource create(Document doc) {
        Optional<Node> dataSetNode = lookupDataSetNode(doc);
        return new SdmxSource(SdmxSource.Type.GENERIC, getSdmxItems(dataSetNode.get()));
    }

    private static boolean hasKeyFamilyRef(Node dataSetNode) {
        return childNodes(dataSetNode).anyMatch(IS_KEY_FAMILY_REF);
    }

    private static ImmutableList<SdmxItem> getSdmxItems(Node dataSetNode) {
        ImmutableList.Builder<SdmxItem> result = ImmutableList.builder();
        for (Node node : childNodes(dataSetNode)) {
            if (IS_GROUP.apply(node)) {
                result.add(getSdmxGroup(node));
            } else if (IS_SERIES.apply(node)) {
                result.add(getSdmxSeries(node, ALL_CONCEPTS));
            }
        }
        return result.build();
    }

    private static SdmxGroup getSdmxGroup(Node groupNode) {
        FluentIterable<Node> children = childNodes(groupNode);
        ImmutableList<Concept> key = lookupConcepts(children.firstMatch(IS_GROUP_KEY).get()).toList();
        ImmutableList<Concept> attributes = lookupConcepts(children.firstMatch(IS_ATTRIBUTES).get()).toList();
        Predicate<Concept> keyFilter = Predicates.not(Predicates.in(key));
        ImmutableList.Builder<SdmxSeries> tss = ImmutableList.builder();
        for (Node series : children.filter(IS_SERIES)) {
            tss.add(getSdmxSeries(series, keyFilter));
        }
        return new SdmxGroup(key, attributes, tss.build());
    }

    private static SdmxSeries getSdmxSeries(Node seriesNode, Predicate<Concept> keyFilter) {
        FluentIterable<Node> children = childNodes(seriesNode);
        ImmutableList<Concept> key = lookupConcepts(children.firstMatch(IS_SERIES_KEY).get()).filter(keyFilter).toList();
        ImmutableList<Concept> attributes = lookupConcepts(children.firstMatch(IS_ATTRIBUTES).get()).filter(keyFilter).toList();
        TimeFormat timeFormat = getTimeFormat(seriesNode);
        OptionalTsData data = getData(seriesNode, timeFormat);
        return new SdmxSeries(key, attributes, timeFormat, data);
    }

    private static OptionalTsData getData(Node seriesNode, TimeFormat timeFormat) {
        Parser<Date> toPeriod = timeFormat.getParser();
        Parser<Number> toValue = DEFAULT_DATA_FORMAT.numberParser();
        OptionalTsData.Builder result = new OptionalTsData.Builder(timeFormat.getFrequency(), timeFormat.getAggregationType());
        for (Node obs : lookupObservations(seriesNode)) {
            Date period = toPeriod.parse(lookupPeriod(obs));
            Number value = period != null ? toValue.parse(lookupValue(obs)) : null;
            result.add(period, value);
        }
        return result.build();
    }

    private static String lookupPeriod(Node obs) {
        return childNodes(obs).firstMatch(IS_TIME).get().getTextContent();
    }

    private static String lookupValue(Node obs) {
        return childNodes(obs).firstMatch(IS_OBS_VALUE).get().getAttributes().getNamedItem(VALUE_ATTRIBUTE).getNodeValue();
    }

    private static TimeFormat getTimeFormat(Node series) {
        Map<String, String> concepts = new HashMap<>();
        for (Node o : childNodes(series).filter(Predicates.or(IS_SERIES_KEY, IS_ATTRIBUTES))) {
            for (Concept concept : lookupConcepts(o)) {
                concepts.put(concept.getKey(), concept.getValue());
            }
        }

        String value;

        value = concepts.get("TIME_FORMAT");
        if (value != null) {
            return TimeFormat.parseByTimeFormat(value);
        }

        value = concepts.get("FREQ");
        if (value != null) {
            return TimeFormat.parseByFrequencyCodeId(value);
        }

        return TimeFormat.UNDEFINED;
    }

    private static Optional<Node> lookupDataSetNode(Document doc) {
        return childNodes(doc.getDocumentElement()).firstMatch(IS_DATA_SET);
    }

    private static Iterable<Node> lookupObservations(Node seriesNode) {
        return childNodes(seriesNode).filter(IS_OBS);
    }

    private static FluentIterable<Concept> lookupConcepts(Node node) {
        return childNodes(node).filter(IS_VALUE).transform(TO_CONCEPT);
    }

    //<editor-fold defaultstate="collapsed" desc="Resources">
    private static final String CONCEPT_ATTRIBUTE = "concept";
    private static final String VALUE_ATTRIBUTE = "value";

    private static final Predicate<Node> IS_DATA_SET = localNameEqualTo("DataSet");
    private static final Predicate<Node> IS_KEY_FAMILY_REF = localNameEqualTo("KeyFamilyRef");
    private static final Predicate<Node> IS_GROUP = localNameEqualTo("Group");
    private static final Predicate<Node> IS_SERIES = localNameEqualTo("Series");
    private static final Predicate<Node> IS_GROUP_KEY = localNameEqualTo("GroupKey");
    private static final Predicate<Node> IS_SERIES_KEY = localNameEqualTo("SeriesKey");
    private static final Predicate<Node> IS_VALUE = localNameEqualTo("Value");
    private static final Predicate<Node> IS_OBS = localNameEqualTo("Obs");
    private static final Predicate<Node> IS_TIME = localNameEqualTo("Time");
    private static final Predicate<Node> IS_OBS_VALUE = localNameEqualTo("ObsValue");
    private static final Predicate<Node> IS_ATTRIBUTES = localNameEqualTo("Attributes");
    private static final Predicate<Concept> ALL_CONCEPTS = Predicates.alwaysTrue();

    private static final Function<Node, Concept> TO_CONCEPT = new Function<Node, Concept>() {
        @Override
        public Concept apply(Node input) {
            NamedNodeMap attr = input.getAttributes();
            return new Concept(attr.getNamedItem(CONCEPT_ATTRIBUTE).getNodeValue(), attr.getNamedItem(VALUE_ATTRIBUTE).getNodeValue());
        }
    };

    private static final DataFormat DEFAULT_DATA_FORMAT = new DataFormat(Locale.ROOT, null, null);
    //</editor-fold>

    private static final class Concept implements Map.Entry<String, String> {

        private final String concept;
        private final String value;

        public Concept(String concept, String value) {
            this.concept = concept;
            this.value = value;
        }

        @Override
        public String getKey() {
            return concept;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof Concept && equals((Concept) obj));
        }

        private boolean equals(Concept other) {
            return concept.equals(other.concept) && value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * 1 + concept.hashCode()) + value.hashCode();
        }
    }
}
