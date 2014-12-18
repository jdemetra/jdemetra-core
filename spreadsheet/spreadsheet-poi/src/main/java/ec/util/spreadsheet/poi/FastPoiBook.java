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
package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Philippe Charles
 */
final class FastPoiBook extends Book {

    private final OPCPackage pkg;
    private final XSSFReader reader;
    private final List<SheetMeta> sheets;
    private final SheetContext sheetContext;

    /**
     * Opens a book from a file.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws OpenXML4JException
     */
    @Nonnull
    public static FastPoiBook create(@Nonnull File file) throws IOException, OpenXML4JException {
        return new FastPoiBook(OPCPackage.open(file.getPath(), PackageAccess.READ));
    }

    /**
     * Opens a book from a stream.
     *
     * Note from OPCPackage: uses quite a bit more memory than
     * {@link #create(File)}, which doesn't need to hold the whole zip file in
     * memory, and can take advantage of native methods
     *
     * @param stream
     * @return
     * @throws IOException
     * @throws OpenXML4JException
     */
    @Nonnull
    public static FastPoiBook create(@Nonnull InputStream stream) throws IOException, OpenXML4JException {
        return new FastPoiBook(OPCPackage.open(stream));
    }

    private FastPoiBook(OPCPackage pkg) throws IOException, OpenXML4JException {
        this.pkg = pkg;
        this.reader = new XSSFReader(pkg);
        WorkbookData workbookData = new WorkbookDataSax2EventHandler().parse(newWorkBookDataSupplier(reader));
        this.sheets = workbookData.sheets;
        this.sheetContext = new SheetContext(
                new SharedStringsDataSax2EventHandler().parse(newSharedStringsDataSupplier(reader)),
                new StylesDataSax2EventHandler().parse(newStylesDataSupplier(reader)),
                workbookData.date1904
        );
    }

    @Override
    public void close() throws IOException {
        pkg.close();
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Sheet getSheet(int index) throws IOException {
        SheetMeta sheetMeta = sheets.get(index);
        return new SheetSax2EventHandler(sheetMeta.name, sheetContext).parse(newSheetSupplier(reader, sheetMeta.relationId));
    }

    //<editor-fold defaultstate="collapsed" desc="Local structures">
//    @VisibleForTesting
    @Immutable
    static final class WorkbookData {

        public final List<SheetMeta> sheets;
        public final boolean date1904;

        public WorkbookData(List<SheetMeta> sheets, boolean date1904) {
            this.sheets = sheets;
            this.date1904 = date1904;
        }
    }

//    @VisibleForTesting
    @Immutable
    static final class SheetMeta {

        public final String relationId;
        public final String name;

        public SheetMeta(String relationId, String name) {
            this.relationId = relationId;
            this.name = name;
        }
    }

//    @VisibleForTesting
    @Immutable
    static final class Style {

        public final int formatId;
        @Nullable
        public final String formatString;

        public Style(int formatId, @Nullable String formatString) {
            this.formatId = formatId;
            this.formatString = formatString;
        }
    }

//    @VisibleForTesting
    @Immutable
    static final class SheetContext {

        @Nonnull
        public final List<String> sharedStrings;
        @Nonnull
        public final List<Style> styles;
        public final boolean date1904;

        public SheetContext(List<String> sharedStrings, List<Style> styles, boolean date1904) {
            this.sharedStrings = sharedStrings;
            this.styles = styles;
            this.date1904 = date1904;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ByteSource suppliers">
//    @VisibleForTesting
    interface ByteSource {

        InputStream openStream() throws IOException;
    }

    private static ByteSource newWorkBookDataSupplier(final XSSFReader reader) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                try {
                    return reader.getWorkbookData();
                } catch (InvalidFormatException ex) {
                    throw new IOException(ex);
                }
            }
        };
    }

    private static ByteSource newSharedStringsDataSupplier(final XSSFReader reader) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                try {
                    return reader.getSharedStringsData();
                } catch (InvalidFormatException ex) {
                    throw new IOException(ex);
                }
            }
        };
    }

    private static ByteSource newStylesDataSupplier(final XSSFReader reader) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                try {
                    return reader.getStylesData();
                } catch (InvalidFormatException ex) {
                    throw new IOException(ex);
                }
            }
        };
    }

    private static ByteSource newSheetSupplier(final XSSFReader reader, final String relationId) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                try {
                    return reader.getSheet(relationId);
                } catch (InvalidFormatException ex) {
                    throw new IOException(ex);
                }
            }
        };
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Sax2 event handlers">
//    @VisibleForTesting
    static abstract class FluentHandler<T> extends DefaultHandler /*implements IBuilder<T>*/ {

        public final T parse(ByteSource byteSource) throws IOException {
            try (InputStream stream = byteSource.openStream()) {
                if (stream != null) {
                    try {
                        XMLReader reader = XMLReaderFactory.createXMLReader();
                        reader.setContentHandler(this);
                        reader.parse(new InputSource(stream));
                    } catch (SAXException ex) {
                        throw new RuntimeException("While parsing xml", ex);
                    }
                }
            }
            return build();
        }

        abstract public T build();
    }

    private static final class SaxStringBuilder {

        private boolean enabled = false;
        private StringBuilder content = new StringBuilder();

        public SaxStringBuilder clear() {
            content = new StringBuilder();
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public SaxStringBuilder enable() {
            this.enabled = true;
            return this;
        }

        public SaxStringBuilder disable() {
            this.enabled = false;
            return this;
        }

        public CharSequence build() {
            // we defer CharSequence@toString()
            return content;
        }

        public SaxStringBuilder appendIfNeeded(char[] ch, int start, int length) {
            if (isEnabled()) {
                content.append(ch, start, length);
            }
            return this;
        }
    }

    /**
     * FIXME: missing support of inline string <is><t>hello</t></is>
     */
//    @VisibleForTesting
    static final class SheetSax2EventHandler extends FluentHandler<Sheet> {

        private static final String CELL_TAG = "c";
        private static final String REFERENCE_ATTRIBUTE = "r";
        private static final String STYLE_INDEX_ATTRIBUTE = "s";
        private static final String CELL_DATA_TYPE_ATTRIBUTE = "t";
        private static final String CELL_VALUE_TAG = "v";
        private static final String SHEET_DIMENSIONS_TAG = "dimension";
        private static final String SHEET_BOUNDS_ATTRIBUTE = "ref";
        private static final String SHEET_DATA_TAG = "sheetData";
        //private static final String INLINE_STRING_TAG = "is";
        //
        private final String sheetName;
        private final SheetContext sheetContext;
        private final SaxStringBuilder stringBuilder;
        //
        private String sheetBounds;
        private FastPoiSheetBuilder sheetBuilder;
        private String ref;
        private String rawDataType;
        private String rawStyleIndex;

        public SheetSax2EventHandler(String sheetName, SheetContext sheetContext) {
            this.sheetName = sheetName;
            this.sheetContext = sheetContext;
            this.stringBuilder = new SaxStringBuilder();
            //
            this.sheetBounds = null;
            this.sheetBuilder = null;
            this.ref = null;
            this.rawDataType = null;
            this.rawStyleIndex = null;
        }

        @Override
        public Sheet build() {
            return sheetBuilder.build();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            switch (name) {
                case CELL_TAG:
                    ref = attributes.getValue(REFERENCE_ATTRIBUTE);
                    rawDataType = attributes.getValue(CELL_DATA_TYPE_ATTRIBUTE);
                    rawStyleIndex = attributes.getValue(STYLE_INDEX_ATTRIBUTE);
                    break;
                case CELL_VALUE_TAG:
                    stringBuilder.enable().clear();
                    break;
                case SHEET_DIMENSIONS_TAG:
                    sheetBounds = attributes.getValue(SHEET_BOUNDS_ATTRIBUTE);
                    break;
                case SHEET_DATA_TAG:
                    sheetBuilder = FastPoiSheetBuilder.create(sheetName, sheetContext, sheetBounds);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (stringBuilder.isEnabled() && (name.equals(CELL_VALUE_TAG) /*|| name.equals(INLINE_STRING_TAG)*/)) {
                sheetBuilder.put(ref, stringBuilder.disable().build(), rawDataType, rawStyleIndex);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.appendIfNeeded(ch, start, length);
        }
    }

    /**
     * http://msdn.microsoft.com/en-us/library/office/documentformat.openxml.spreadsheet.aspx
     */
//    @VisibleForTesting
    static final class WorkbookDataSax2EventHandler extends FluentHandler<WorkbookData> {

        private static final String SHEET_TAG = "sheet";
        private static final String WORKBOOK_PROPERTIES_TAG = "workbookPr";
        private static final String DATE1904_ATTRIBUTE = "date1904";
        private static final String SHEET_TAB_ID_ATTRIBUTE = "r:id";
        private static final String SHEET_NAME_ATTRIBUTE = "name";
        //
        private final List<SheetMeta> sheets;
        private boolean date1904;

        public WorkbookDataSax2EventHandler() {
            this.sheets = new ArrayList<>();
            this.date1904 = false;
        }

        @Override
        public WorkbookData build() {
            return new WorkbookData(new ArrayList<>(sheets), date1904);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case SHEET_TAG:
                    sheets.add(new SheetMeta(attributes.getValue(SHEET_TAB_ID_ATTRIBUTE), attributes.getValue(SHEET_NAME_ATTRIBUTE)));
                    break;
                case WORKBOOK_PROPERTIES_TAG:
                    date1904 = Boolean.parseBoolean(attributes.getValue(DATE1904_ATTRIBUTE));
                    break;
            }
        }
    }

    /**
     * http://msdn.microsoft.com/en-us/library/office/gg278314.aspx
     */
//    @VisibleForTesting
    static final class SharedStringsDataSax2EventHandler extends FluentHandler<List<String>> {

        private static final String SHARED_STRING_ITEM_TAG = "si";
        private static final String TEXT_TAG = "t";
        //
        private final List<String> sharedStrings;
        private final SaxStringBuilder stringBuilder;

        public SharedStringsDataSax2EventHandler() {
            this.sharedStrings = new ArrayList<>();
            this.stringBuilder = new SaxStringBuilder();
        }

        @Override
        public List<String> build() {
            return new ArrayList<>(sharedStrings);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case SHARED_STRING_ITEM_TAG:
                    stringBuilder.clear();
                    break;
                case TEXT_TAG:
                    stringBuilder.enable();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case TEXT_TAG:
                    stringBuilder.disable();
                    break;
                case SHARED_STRING_ITEM_TAG:
                    sharedStrings.add(stringBuilder.build().toString());
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.appendIfNeeded(ch, start, length);
        }
    }

//    @VisibleForTesting
    static final class StylesDataSax2EventHandler extends FluentHandler<List<Style>> {

        private static final String CELL_FORMAT_TAG = "xf";
        private static final String CELL_FORMATS_TAG = "cellXfs";
        private static final String NUMBER_FORMAT_TAG = "numFmt";
        private static final String NUMBER_FORMAT_ID_ATTRIBUTE = "numFmtId";
        private static final String NUMBER_FORMAT_CODE_ATTRIBUTE = "formatCode";
        //
        private final List<String> orderedListOfIds;
        private final Map<String, String> numberFormats;
        private boolean insideGroupTag;

        public StylesDataSax2EventHandler() {
            this.orderedListOfIds = new ArrayList<>();
            this.numberFormats = new HashMap<>();
            this.insideGroupTag = false;
        }

        @Override
        public List<Style> build() {
            List<Style> result = new ArrayList<>();
            // Style order matters! -> accessed by index in sheets
            for (String formatId : orderedListOfIds) {
                result.add(new Style(Integer.parseInt(formatId), numberFormats.get(formatId)));
            }
            return result;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case CELL_FORMATS_TAG:
                    insideGroupTag = true;
                    break;
                case CELL_FORMAT_TAG:
                    if (insideGroupTag) {
                        orderedListOfIds.add(attributes.getValue(NUMBER_FORMAT_ID_ATTRIBUTE));
                    }
                    break;
                case NUMBER_FORMAT_TAG:
                    numberFormats.put(
                            attributes.getValue(NUMBER_FORMAT_ID_ATTRIBUTE),
                            attributes.getValue(NUMBER_FORMAT_CODE_ATTRIBUTE));
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(CELL_FORMATS_TAG)) {
                insideGroupTag = false;
            }
        }
    }
    //</editor-fold>
}
