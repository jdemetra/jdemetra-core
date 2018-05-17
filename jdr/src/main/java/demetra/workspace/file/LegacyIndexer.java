/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.workspace.file;

import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static demetra.workspace.WorkspaceFamily.SA_MULTI;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static demetra.workspace.WorkspaceFamily.UTIL_VAR;
import demetra.workspace.file.xml.XmlLegacyWorkspace;
import demetra.workspace.file.xml.XmlLegacyWorkspaceItem;
import demetra.workspace.io.JaxbUtil;
import demetra.workspace.util.Paths;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Philippe Charles
 */
final class LegacyIndexer implements Indexer {

    static boolean isValid(Path file) throws IOException {
        try {
            unmarshalIndex(file);
            return true;
        } catch (JAXBException ex) {
            return false;
        }
    }

    private final Path file;

    LegacyIndexer(Path file) {
        this.file = file;
    }

    @Override
    public void checkId(Index.Key key) throws IOException {
        if (WorkspaceFamily.UTIL_CAL.equals(key.getFamily())) {
            if (!key.getId().equals("Calendars")) {
                throw new IOException("Only one calendar file is allowed");
            }
        } else if (WorkspaceFamily.UTIL_VAR.equals(key.getFamily())) {
            if (!key.getId().equals("Variables")) {
                throw new IOException("Only one variable file is allowed");
            }
        }
    }

    @Override
    public Index loadIndex() throws IOException {
        try {
            return xmlToIndex(unmarshalIndex(file), Paths.changeExtension(file.toString(), null));
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void storeIndex(Index index) throws IOException {
        try {
            marshalIndex(file, indexToXml(index));
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    private static Index xmlToIndex(XmlLegacyWorkspace xml, String name) {
        Index.Builder result = Index.builder().name(name);

        JaxbUtil.forSingle(xml.calendars, pusher(result, UTIL_CAL));
        JaxbUtil.forSingle(xml.variables, pusher(result, UTIL_VAR));

        JaxbUtil.forEach(xml.saProcessing, pusher(result, SA_MULTI));
        JaxbUtil.forEach(xml.tramoseatsDocs, pusher(result, SA_DOC_TRAMOSEATS));
        JaxbUtil.forEach(xml.tramoseatsSpecs, pusher(result, SA_SPEC_TRAMOSEATS));
        JaxbUtil.forEach(xml.x12Docs, pusher(result, SA_DOC_X13));
        JaxbUtil.forEach(xml.x12Specs, pusher(result, SA_SPEC_X13));

        return result.build();
    }

    private static Consumer<XmlLegacyWorkspaceItem> pusher(Index.Builder result, WorkspaceFamily family) {
        return o -> result.item(getIndexKey(o, family), getIndexValue(o));
    }

    private static Index.Key getIndexKey(XmlLegacyWorkspaceItem xml, WorkspaceFamily family) {
        return new Index.Key(family, xml.file != null ? xml.file : xml.name);
    }

    private static Index.Value getIndexValue(XmlLegacyWorkspaceItem xml) {
        return new Index.Value(xml.name, xml.readOnly, null);
    }

    private static XmlLegacyWorkspace indexToXml(Index index) {
        XmlLegacyWorkspace result = new XmlLegacyWorkspace();

        result.calendars = toSingleItem(index, UTIL_CAL);
        result.variables = toSingleItem(index, UTIL_VAR);

        result.saProcessing = toEachItem(index, SA_MULTI);
        result.tramoseatsDocs = toEachItem(index, SA_DOC_TRAMOSEATS);
        result.tramoseatsSpecs = toEachItem(index, SA_SPEC_TRAMOSEATS);
        result.x12Docs = toEachItem(index, SA_DOC_X13);
        result.x12Specs = toEachItem(index, SA_SPEC_X13);

        return result;
    }

    private static XmlLegacyWorkspaceItem toSingleItem(Index index, WorkspaceFamily family) {
        return index.getItems().entrySet().stream()
                .filter(filterOnFamily(family))
                .map(LegacyIndexer::map)
                .findFirst()
                .orElse(null);
    }

    private static XmlLegacyWorkspaceItem[] toEachItem(Index index, WorkspaceFamily family) {
        return index.getItems().entrySet().stream()
                .filter(filterOnFamily(family))
                .map(LegacyIndexer::map)
                .toArray(XmlLegacyWorkspaceItem[]::new);
    }

    private static Predicate<Entry<Index.Key, Index.Value>> filterOnFamily(WorkspaceFamily family) {
        return o -> o.getKey().getFamily().equals(family);
    }

    private static XmlLegacyWorkspaceItem map(Entry<Index.Key, Index.Value> o) {
        XmlLegacyWorkspaceItem result = new XmlLegacyWorkspaceItem();
        result.file = o.getKey().getId();
        result.name = o.getValue().getLabel();
        result.readOnly = o.getValue().isReadOnly();
        return result;
    }

    private static XmlLegacyWorkspace unmarshalIndex(Path file) throws JAXBException, IOException {
        return (XmlLegacyWorkspace) JaxbUtil.unmarshal(file, XML_WS_CONTEXT);
    }

    private static void marshalIndex(Path file, XmlLegacyWorkspace jaxbElement) throws JAXBException, IOException {
        JaxbUtil.marshal(file, XML_WS_CONTEXT, jaxbElement, true);
    }

    private static final JAXBContext XML_WS_CONTEXT = JaxbUtil.createContext(XmlLegacyWorkspace.class);
}
