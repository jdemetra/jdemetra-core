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
package internal.workspace.file;

import ec.demetra.workspace.WorkspaceFamily;
import internal.workspace.file.xml.XmlGenericWorkspace;
import internal.workspace.file.xml.XmlGenericWorkspaceItem;
import internal.io.JaxbUtil;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author Philippe Charles
 */
final class GenericIndexer implements Indexer {

    static boolean isValid(Path file) throws IOException {
        try {
            unmarshalIndex(file);
            return true;
        } catch (FileSystemException ex) {
            throw ex;
        } catch (IOException ex) {
            return false;
        }
    }

    private final Path file;
    private final Path rootFolder;

    GenericIndexer(Path file, Path rootFolder) {
        this.file = file;
        this.rootFolder = rootFolder;
    }

    @Override
    public void checkId(Index.Key key) throws IOException {
        if (WorkspaceFamily.UTIL_CAL.equals(key.getFamily())) {
            if (!key.getId().equals("Calendars")) {
                throw new IOException("Only one calendar file is allowed");
            }
        }
    }

    @Override
    public Index loadIndex() throws IOException {
        return xmlToIndex(unmarshalIndex(file), rootFolder);
    }

    @Override
    public void storeIndex(Index index) throws IOException {
        marshalIndex(file, indexToXml(index, rootFolder));
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    private static Index xmlToIndex(XmlGenericWorkspace xml, Path rootFolder) {
        Index.Builder result = Index.builder().name(xml.name);
        JaxbUtil.forEach(xml.items, o -> result.item(getIndexKey(o), getIndexValue(o)));
        pushCalendar(rootFolder, result);
        return result.build();
    }

    private static Index.Key getIndexKey(XmlGenericWorkspaceItem xml) {
        return new Index.Key(WorkspaceFamily.parse(xml.family), xml.file);
    }

    private static Index.Value getIndexValue(XmlGenericWorkspaceItem xml) {
        return new Index.Value(xml.name, xml.readOnly, xml.comments);
    }

    private static XmlGenericWorkspace indexToXml(Index index, Path rootFolder) {
        XmlGenericWorkspace result = new XmlGenericWorkspace();
        result.name = index.getName();
        result.items = indexEntriesToXml(pullCalendar(rootFolder, index).getItems());
        return result;
    }

    private static XmlGenericWorkspaceItem[] indexEntriesToXml(Map<Index.Key, Index.Value> entries) {
        return entries.entrySet().stream()
                .map(o -> {
                    XmlGenericWorkspaceItem xml = new XmlGenericWorkspaceItem();
                    xml.family = o.getKey().getFamily().toString();
                    xml.file = o.getKey().getId();
                    xml.name = o.getValue().getLabel();
                    xml.readOnly = o.getValue().isReadOnly();
                    xml.comments = o.getValue().getComments();
                    return xml;
                })
                .toArray(XmlGenericWorkspaceItem[]::new);
    }

    private static final Xml.Parser<XmlGenericWorkspace> PARSER;
    private static final Xml.Formatter<XmlGenericWorkspace> FORMATTER;

    static {
        PARSER = Jaxb.Parser.of(XmlGenericWorkspace.class);
        FORMATTER = Jaxb.Formatter.of(XmlGenericWorkspace.class).withFormatted(true);
    }

    private static XmlGenericWorkspace unmarshalIndex(Path file) throws IOException {
        return PARSER.parsePath(file);
    }

    private static void marshalIndex(Path file, XmlGenericWorkspace jaxbElement) throws IOException {
        FORMATTER.formatPath(jaxbElement, file);
    }

    private static void pushCalendar(Path rootFolder, Index.Builder index) {
        Path calFile = rootFolder.resolve("Calendars").resolve("Calendars.xml");
        if (Files.exists(calFile) && !Files.isDirectory(calFile)) {
            index.item(SINGLE_CAL_GENERIC_KEY, SINGLE_CAL_GENERIC_VALUE);
        }
    }

    private static Index pullCalendar(Path rootFolder, Index index) {
        return index.withoutItem(SINGLE_CAL_GENERIC_KEY);
    }

    private static final Index.Key SINGLE_CAL_GENERIC_KEY = new Index.Key(WorkspaceFamily.UTIL_CAL, "Calendars");
    private static final Index.Value SINGLE_CAL_GENERIC_VALUE = new Index.Value("Calendars", false, null);
}
