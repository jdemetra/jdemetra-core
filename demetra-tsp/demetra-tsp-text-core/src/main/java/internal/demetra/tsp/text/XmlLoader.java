package internal.demetra.tsp.text;

import demetra.data.DoubleList;
import demetra.timeseries.*;
import demetra.tsp.text.XmlBean;
import demetra.tsprovider.HasFilePaths;
import internal.util.Strings;
import nbbrd.io.xml.Stax;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@lombok.AllArgsConstructor
public class XmlLoader {

    @lombok.NonNull
    private final HasFilePaths filePathSupport;

    public @NonNull List<TsCollection> load(@NonNull XmlBean bean) throws IOException {
        File file = filePathSupport.resolveFilePath(bean.getFile());
        return parse(file);
    }

    public static List<TsCollection> parse(File file) throws IOException {
        return Stax.StreamParser.valueOf(XmlLoader::parseWorkspace).parseFile(file);
    }

    static List<TsCollection> parseWorkspace(XMLStreamReader reader) throws XMLStreamException {
        List<TsCollection> result = new ArrayList<>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("tscollection")) {
                        result.add(parseCollection(reader));
                    }
                    break;
            }
        }
        return result;
    }

    static TsCollection parseCollection(XMLStreamReader reader) throws XMLStreamException {
        return TsCollection
                .builder()
                .name(Strings.nullToEmpty(reader.getAttributeValue(null, "name")))
                .items(parseTss(reader))
                .build();
    }

    static List<Ts> parseTss(XMLStreamReader reader) throws XMLStreamException {
        List<Ts> result = new ArrayList<>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("ts")) {
                        result.add(parseTs(reader));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (reader.getLocalName().equals("tscollection")) {
                        return result;
                    }
                    break;
            }
        }
        return result;
    }

    static Ts parseTs(XMLStreamReader reader) throws XMLStreamException {
        Ts.Builder result = Ts.builder();
        result.name(reader.getAttributeValue(null, "name"));
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("tsdata")) {
                        result.data(parseTsData(reader));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (reader.getLocalName().equals("ts")) {
                        return result.build();
                    }
                    break;
            }
        }
        return result.build();
    }

    static TsData parseTsData(XMLStreamReader reader) throws XMLStreamException {
        int frequency = parseInt(reader.getAttributeValue(null, "freq"));
        int firstYear = parseInt(reader.getAttributeValue(null, "ystart"));
        int firstPeriod = parseInt(reader.getAttributeValue(null, "pstart"));
        double[] data = parseData(reader);

        TsPeriod start = TsPeriod.of(TsUnit.ofAnnualFrequency(frequency), Year.of(firstYear).atDay(1)).plus(firstPeriod - 1);
        return TsData.ofInternal(start, data);
    }

    static double[] parseData(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("data")) {
                        DoubleList result = new DoubleList();
                        for (Iterator<String> iter = Strings.splitToIterator(' ', reader.getElementText()); iter.hasNext(); ) {
                            result.add(parseDouble(iter.next()));
                        }
                        return result.toArray();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (reader.getLocalName().equals("tsdata")) {
                        return null;
                    }
                    break;
            }
        }
        return null;
    }

    static int parseInt(String value) throws XMLStreamException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new XMLStreamException("Failed to parse integer", ex);
        }
    }

    static double parseDouble(String value) throws XMLStreamException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new XMLStreamException("Failed to parse double", ex);
        }
    }
}
