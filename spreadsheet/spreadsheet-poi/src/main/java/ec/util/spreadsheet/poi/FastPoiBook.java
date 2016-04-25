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
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Philippe Charles
 */
final class FastPoiBook extends Book {

    /**
     * Opens a book from a file.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws OpenXML4JException
     */
    @Nonnull
    public static FastPoiBook create(@Nonnull XMLReader xmlReader, @Nonnull File file) throws IOException, OpenXML4JException {
        return create(xmlReader, OPCPackage.open(file.getPath(), PackageAccess.READ));
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
    public static FastPoiBook create(@Nonnull XMLReader xmlReader, @Nonnull InputStream stream) throws IOException, OpenXML4JException {
        return create(xmlReader, OPCPackage.open(stream));
    }

    @Nonnull
    private static FastPoiBook create(@Nonnull XMLReader xmlReader, @Nonnull OPCPackage pkg) throws IOException, OpenXML4JException {
        XSSFReader xssfReader = new XSSFReader(pkg);
        WorkbookData workbookData = new WorkbookDataSax2EventHandler().parse(xmlReader, xssfReader::getWorkbookData);
        FastPoiContext sheetContext = new FastPoiContext(
                new SharedStringsDataSax2EventHandler().parse(xmlReader, xssfReader::getSharedStringsData),
                new StylesDataSax2EventHandler().parse(xmlReader, xssfReader::getStylesData),
                workbookData.date1904
        );
        return new FastPoiBook(pkg, xmlReader, xssfReader, workbookData.sheets, sheetContext);
    }

    private final OPCPackage pkg;
    private final XMLReader xmlReader;
    private final XSSFReader xssfReader;
    private final List<SheetMeta> sheets;
    private final FastPoiContext sheetContext;

    private FastPoiBook(OPCPackage pkg, XMLReader xmlReader, XSSFReader xssfReader, List<SheetMeta> sheets, FastPoiContext sheetContext) {
        this.pkg = pkg;
        this.xssfReader = xssfReader;
        this.xmlReader = xmlReader;
        this.sheets = sheets;
        this.sheetContext = sheetContext;
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
        return new SheetSax2EventHandler(sheetMeta.name, sheetContext).parse(xmlReader, () -> xssfReader.getSheet(sheetMeta.relationId));
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Sax2 event handlers">
//    @VisibleForTesting
    static abstract class FluentHandler<T> extends DefaultHandler /*implements IBuilder<T>*/ {

        public final T parse(XMLReader reader, Callable<? extends InputStream> byteSource) throws IOException {
            try (InputStream stream = open(byteSource)) {
                if (stream != null) {
                    parse(reader, stream, this);
                }
                return build();
            }
        }

        private static InputStream open(Callable<? extends InputStream> byteSource) throws IOException {
            try {
                return byteSource.call();
            } catch (Exception ex) {
                if (ex instanceof IOException) {
                    throw (IOException) ex;
                }
                throw new IOException("While opening xml", ex);
            }
        }

        private static void parse(XMLReader reader, InputStream stream, ContentHandler handler) throws IOException {
            try {
                reader.setContentHandler(handler);
                reader.parse(new InputSource(stream));
            } catch (SAXException ex) {
                throw new IOException("While parsing xml", ex);
            }
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
        private final FastPoiContext sheetContext;
        private final SaxStringBuilder stringBuilder;
        //
        private String sheetBounds;
        private FastPoiSheetBuilder sheetBuilder;
        private String ref;
        private String rawDataType;
        private String rawStyleIndex;

        public SheetSax2EventHandler(String sheetName, FastPoiContext sheetContext) {
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
            return sheetBuilder != null ? sheetBuilder.build() : ArraySheet.copyOf(sheetName, new Object[][]{});
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
    static final class SharedStringsDataSax2EventHandler extends FluentHandler<String[]> {

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
        public String[] build() {
            return sharedStrings.toArray(new String[sharedStrings.size()]);
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
    static final class StylesDataSax2EventHandler extends FluentHandler<boolean[]> {

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
        public boolean[] build() {
            boolean[] result = new boolean[orderedListOfIds.size()];
            // Style order matters! -> accessed by index in sheets
            for (int i = 0; i < result.length; i++) {
                String formatId = orderedListOfIds.get(i);
                result[i] = DateUtil.isADateFormat(Integer.parseInt(formatId), numberFormats.get(formatId));
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
