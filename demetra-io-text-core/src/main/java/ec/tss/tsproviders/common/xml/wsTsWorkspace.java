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
package ec.tss.tsproviders.common.xml;

/**
 *
 * @author Jean Palate
 */
import demetra.data.DoubleList;
import internal.util.Strings;
import ioutil.Stax;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class wsTsWorkspace {

    wsTsCollection[] tsclist;

    public static wsTsWorkspace parse(File file) throws IOException {
        return Stax.StreamParser.valueOf(wsTsWorkspace::parseWorkspace).parseFile(file);
    }

    static wsTsWorkspace parseWorkspace(XMLStreamReader reader) throws XMLStreamException {
        wsTsWorkspace result = new wsTsWorkspace();
        result.tsclist = parseCollections(reader).toArray(new wsTsCollection[0]);
        return result;
    }

    static List<wsTsCollection> parseCollections(XMLStreamReader reader) throws XMLStreamException {
        List<wsTsCollection> result = new ArrayList<>();
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

    static wsTsCollection parseCollection(XMLStreamReader reader) throws XMLStreamException {
        wsTsCollection result = new wsTsCollection();
        result.name = reader.getAttributeValue(null, "name");
        result.tslist = parseTss(reader).toArray(new wsTs[0]);
        return result;
    }

    static List<wsTs> parseTss(XMLStreamReader reader) throws XMLStreamException {
        List<wsTs> result = new ArrayList<>();
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

    static wsTs parseTs(XMLStreamReader reader) throws XMLStreamException {
        wsTs result = new wsTs();
        result.name = reader.getAttributeValue(null, "name");
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("tsdata")) {
                        result.tsdata = parseTsData(reader);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (reader.getLocalName().equals("ts")) {
                        return result;
                    }
                    break;
            }
        }
        return result;
    }

    static wsTsData parseTsData(XMLStreamReader reader) throws XMLStreamException {
        wsTsData result = new wsTsData();
        result.frequency = parseInt(reader.getAttributeValue(null, "freq"));
        result.firstyear = parseInt(reader.getAttributeValue(null, "ystart"));
        result.firstperiod = parseInt(reader.getAttributeValue(null, "pstart"));
        result.data = parseData(reader);
        return result;
    }

    static double[] parseData(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("data")) {
                        DoubleList result = new DoubleList();
                        for (Iterator<String> iter = Strings.splitToIterator(' ', reader.getElementText()); iter.hasNext();) {
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
