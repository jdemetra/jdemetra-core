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
import com.google.common.collect.Maps;
import ec.tss.tsproviders.sdmx.model.SdmxItem;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static ec.tss.tsproviders.sdmx.engine.FluentDom.asStream;
import ec.tss.tsproviders.utils.IParser;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class GuessingCompactFactory extends AbstractDocumentFactory {

    @Override
    public String getName() {
        return "Guessing compact";
    }

    @Override
    public boolean isValid(Document doc) {
        Optional<Node> dataSetNode = lookupDataSetNode(doc);
        return dataSetNode.isPresent() && !hasKeyFamilyRef(dataSetNode.get());
    }

    @Override
    public SdmxSource create(Document doc) {
        Optional<Node> dataSetNode = lookupDataSetNode(doc);
        return new SdmxSource(SdmxSource.Type.COMPACT, getSdmxItems(dataSetNode.get(), new GregorianCalendar()));
    }

    private static boolean hasKeyFamilyRef(Node dataSetNode) {
        return asStream(dataSetNode.getChildNodes())
                .anyMatch(o -> "KeyFamilyRef".equals(o.getLocalName()));
    }

    private static List<SdmxItem> getSdmxItems(Node dataSetNode, Calendar cal) {
        return asStream(dataSetNode.getChildNodes())
                .filter(o -> "Series".equals(o.getLocalName()))
                .map(o -> getSdmxSeries(o, cal))
                .collect(toImmutableList());
    }

    private static SdmxSeries getSdmxSeries(Node seriesNode, Calendar cal) {
        ImmutableList<Map.Entry<String, String>> key = getKey(seriesNode);
        TimeFormat timeFormat = getTimeFormat(seriesNode);
        OptionalTsData data = getData(seriesNode, timeFormat, cal);
        return new SdmxSeries(key, ImmutableList.of(), timeFormat, data);
    }

    private static OptionalTsData getData(Node seriesNode, TimeFormat timeFormat, Calendar cal) {
        IParser<Date> toPeriod = timeFormat.getParser();
        IParser<Number> toValue = DEFAULT_DATA_FORMAT.numberParser();
        ObsGathering gathering = ObsGathering.includingMissingValues(timeFormat.getFrequency(), timeFormat.getAggregationType());
        return OptionalTsData.builderByDate(cal, gathering)
                .addAll(lookupObservations(seriesNode), o -> getPeriod(o, toPeriod), o -> getValue(o, toValue))
                .build();
    }

    private static Date getPeriod(NamedNodeMap obs, IParser<Date> toPeriod) {
        Node tmp = obs.getNamedItem(TIME_PERIOD_ATTRIBUTE);
        return tmp != null ? toPeriod.parse(tmp.getNodeValue()) : null;
    }

    private static Number getValue(NamedNodeMap obs, IParser<Number> toValue) {
        Node tmp = obs.getNamedItem(OBS_VALUE_ATTRIBUTE);
        return tmp != null ? toValue.parse(tmp.getNodeValue()) : null;
    }

    private static ImmutableList<Map.Entry<String, String>> getKey(Node seriesNode) {
        return asStream(seriesNode.getAttributes())
                .filter(o -> !TIME_FORMAT_ATTRIBUTE.equals(o.getNodeName()))
                .map(o -> Maps.immutableEntry(o.getNodeName(), o.getNodeValue()))
                .collect(toImmutableList());
    }

    private static TimeFormat getTimeFormat(Node seriesNode) {
        NamedNodeMap attributes = seriesNode.getAttributes();
        Node node;

        node = attributes.getNamedItem(TIME_FORMAT_ATTRIBUTE);
        if (node != null) {
            return TimeFormat.parseByTimeFormat(node.getNodeValue());
        }

        node = attributes.getNamedItem(FREQ_ATTRIBUTE);
        if (node != null) {
            return TimeFormat.parseByFrequencyCodeId(node.getNodeValue());
        }

        return TimeFormat.UNDEFINED;
    }

    private static Optional<Node> lookupDataSetNode(Document doc) {
        return asStream(doc.getDocumentElement().getChildNodes())
                .filter(o -> "DataSet".equals(o.getLocalName()))
                .findFirst();
    }

    private static Stream<NamedNodeMap> lookupObservations(Node seriesNode) {
        return asStream(seriesNode.getChildNodes())
                .filter(o -> "Obs".equals(o.getLocalName()))
                .map(Node::getAttributes);
    }

    //<editor-fold defaultstate="collapsed" desc="Resources">    
    private static final String TIME_FORMAT_ATTRIBUTE = "TIME_FORMAT";
    private static final String FREQ_ATTRIBUTE = "FREQ";
    private static final String TIME_PERIOD_ATTRIBUTE = "TIME_PERIOD";
    private static final String OBS_VALUE_ATTRIBUTE = "OBS_VALUE";

    private static final DataFormat DEFAULT_DATA_FORMAT = DataFormat.ROOT;
    //</editor-fold>
}
