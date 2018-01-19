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

import com.google.common.collect.ImmutableList;
import static com.google.common.collect.ImmutableList.toImmutableList;
import ec.tss.tsproviders.sdmx.model.SdmxGroup;
import ec.tss.tsproviders.sdmx.model.SdmxItem;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static ec.tss.tsproviders.sdmx.engine.FluentDom.asStream;
import ec.tss.tsproviders.utils.IParser;

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
        return new SdmxSource(SdmxSource.Type.GENERIC, getSdmxItems(dataSetNode.get(), new GregorianCalendar()));
    }

    private static boolean hasKeyFamilyRef(Node dataSetNode) {
        return asStream(dataSetNode.getChildNodes())
                .anyMatch(o -> "KeyFamilyRef".equals(o.getLocalName()));
    }

    private static ImmutableList<SdmxItem> getSdmxItems(Node dataSetNode, Calendar cal) {
        Predicate<Concept> keyFilter = o -> true;
        return asStream(dataSetNode.getChildNodes())
                .map(o -> "Group".equals(o.getLocalName()) ? getSdmxGroup(o, cal) : "Series".equals(o.getLocalName()) ? getSdmxSeries(o, keyFilter, cal) : null)
                .filter(o -> o != null)
                .collect(toImmutableList());
    }

    private static SdmxGroup getSdmxGroup(Node groupNode, Calendar cal) {
        ImmutableList<Concept> key = getGroupKeyNode(groupNode)
                .map(GenericDocFactory::lookupConcepts)
                .get()
                .collect(toImmutableList());
        ImmutableList<Concept> attributes = getAttributeNode(groupNode)
                .map(GenericDocFactory::lookupConcepts)
                .get()
                .collect(toImmutableList());
        ImmutableList<SdmxSeries> tss = asStream(groupNode.getChildNodes())
                .filter(o -> "Series".equals(o.getLocalName()))
                .map(o -> getSdmxSeries(o, x -> !key.contains(x), cal))
                .collect(toImmutableList());
        return new SdmxGroup(key, attributes, tss);
    }

    private static Optional<Node> getGroupKeyNode(Node node) {
        return asStream(node.getChildNodes())
                .filter(o -> "GroupKey".equals(o.getLocalName()))
                .findFirst();
    }

    private static Optional<Node> getAttributeNode(Node node) {
        return asStream(node.getChildNodes())
                .filter(o -> "Attributes".equals(o.getLocalName()))
                .findFirst();
    }

    private static SdmxSeries getSdmxSeries(Node seriesNode, Predicate<Concept> keyFilter, Calendar cal) {
        ImmutableList<Concept> key = getSeriesKeyNode(seriesNode)
                .map(GenericDocFactory::lookupConcepts)
                .get()
                .filter(keyFilter)
                .collect(toImmutableList());
        ImmutableList<Concept> attributes = getAttributeNode(seriesNode)
                .map(GenericDocFactory::lookupConcepts)
                .get()
                .filter(keyFilter)
                .collect(toImmutableList());
        TimeFormat timeFormat = getTimeFormat(seriesNode);
        OptionalTsData data = getData(seriesNode, timeFormat, cal);
        return new SdmxSeries(key, attributes, timeFormat, data);
    }

    private static Optional<Node> getSeriesKeyNode(Node node) {
        return asStream(node.getChildNodes())
                .filter(o -> "SeriesKey".equals(o.getLocalName()))
                .findFirst();
    }

    private static OptionalTsData getData(Node seriesNode, TimeFormat timeFormat, Calendar cal) {
        IParser<Date> toPeriod = timeFormat.getParser();
        IParser<Number> toValue = DEFAULT_DATA_FORMAT.numberParser();
        ObsGathering gathering = ObsGathering.includingMissingValues(timeFormat.getFrequency(), timeFormat.getAggregationType());
        return OptionalTsData.builderByDate(cal, gathering)
                .addAll(lookupObservations(seriesNode), o -> toPeriod.parse(getPeriod(o)), o -> toValue.parse(getValue(o)))
                .build();
    }

    private static String getPeriod(Node obs) {
        return asStream(obs.getChildNodes())
                .filter(o -> "Time".equals(o.getLocalName()))
                .map(Node::getTextContent)
                .findFirst()
                .get();
    }

    private static String getValue(Node obs) {
        return asStream(obs.getChildNodes())
                .filter(o -> "ObsValue".equals(o.getLocalName()))
                .map(o -> o.getAttributes().getNamedItem(VALUE_ATTRIBUTE).getNodeValue())
                .findFirst()
                .get();
    }

    private static TimeFormat getTimeFormat(Node series) {
        Map<String, String> concepts = asStream(series.getChildNodes())
                .filter(o -> "SeriesKey".equals(o.getLocalName()) || "Attributes".equals(o.getLocalName()))
                .flatMap(GenericDocFactory::lookupConcepts)
                .collect(Collectors.toMap(Concept::getKey, Concept::getValue));

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
        return asStream(doc.getDocumentElement().getChildNodes())
                .filter(o -> "DataSet".equals(o.getLocalName()))
                .findFirst();
    }

    private static Stream<Node> lookupObservations(Node seriesNode) {
        return asStream(seriesNode.getChildNodes())
                .filter(o -> "Obs".equals(o.getLocalName()));
    }

    private static Stream<Concept> lookupConcepts(Node node) {
        return asStream(node.getChildNodes())
                .filter(o -> "Value".equals(o.getLocalName()))
                .map(GenericDocFactory::toConcept);
    }

    private static Concept toConcept(Node node) {
        NamedNodeMap attr = node.getAttributes();
        return new Concept(attr.getNamedItem(CONCEPT_ATTRIBUTE).getNodeValue(), attr.getNamedItem(VALUE_ATTRIBUTE).getNodeValue());
    }

    //<editor-fold defaultstate="collapsed" desc="Resources">
    private static final String CONCEPT_ATTRIBUTE = "concept";
    private static final String VALUE_ATTRIBUTE = "value";

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
