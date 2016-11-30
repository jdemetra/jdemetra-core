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
package spreadsheet.xlsx.internal;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.XlsxDateSystem;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxPackage;
import spreadsheet.xlsx.XlsxParser;
import spreadsheet.xlsx.XlsxReader;
import spreadsheet.xlsx.XlsxSheetBuilder;

/**
 *
 * @author Philippe Charles
 */
public final class XlsxBook extends Book {

    @Nonnull
    public static XlsxBook create(@Nonnull XlsxPackage pkg, @Nonnull XlsxReader reader) throws IOException {
        XlsxParser parser = null;
        XlsxSheetBuilder sheetBuilder = null;

        try {
            parser = reader.getParser().create();

            WorkbookData data = parseWorkbookData(pkg::getWorkbookData, parser);
            XlsxDateSystem dateSystem = data.date1904 ? reader.getDateSystem1904() : reader.getDateSystem1900();
            IntFunction<String> sharedStrings = parseSharedStringsData(pkg::getSharedStringsData, parser);
            IntPredicate dateFormats = parseStylesData(pkg::getStylesData, parser, reader.getNumberingFormat());

            sheetBuilder = reader.getBuilder().create(dateSystem, sharedStrings, dateFormats);

            return new XlsxBook(pkg, parser, data.sheets, sheetBuilder);
        } catch (IOException ex) {
            closeAll(ex, parser, sheetBuilder);
            throw ex;
        }
    }

    private final XlsxPackage pkg;
    private final XlsxParser parser;
    private final List<SheetMeta> sheets;
    private final XlsxSheetBuilder sheetBuilder;

    private XlsxBook(XlsxPackage pkg, XlsxParser parser, List<SheetMeta> sheets, XlsxSheetBuilder sheetBuilder) {
        this.pkg = pkg;
        this.parser = parser;
        this.sheets = sheets;
        this.sheetBuilder = sheetBuilder;
    }

    @Override
    public void close() throws IOException {
        closeAll(null, pkg, parser, sheetBuilder);
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Sheet getSheet(int index) throws IOException {
        SheetMeta meta = sheets.get(index);
        return parseSheet(() -> pkg.getSheet(meta.relationId), parser, meta.name, sheetBuilder);
    }

    static void closeAll(IOException initial, Closeable... closeables) throws IOException {
        for (Closeable o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (IOException ex) {
                    if (initial == null) {
                        initial = ex;
                    } else {
                        initial.addSuppressed(ex);
                    }
                }
            }
        }
        if (initial != null) {
            throw initial;
        }
    }

    static final class WorkbookData {

        final List<SheetMeta> sheets;
        final boolean date1904;

        private WorkbookData(List<SheetMeta> sheets, boolean date1904) {
            this.sheets = sheets;
            this.date1904 = date1904;
        }
    }

    static final class SheetMeta {

        final String relationId;
        final String name;

        private SheetMeta(String relationId, String name) {
            this.relationId = relationId;
            this.name = name;
        }
    }

    static WorkbookData parseWorkbookData(SaxUtil.ByteSource byteSource, XlsxParser parser) throws IOException {
        WorkbookDataVisitorImpl result = new WorkbookDataVisitorImpl();
        try (InputStream stream = byteSource.openStream()) {
            parser.parseWorkbookData(stream, result);
        }
        return result.build();
    }

    static final class WorkbookDataVisitorImpl implements XlsxParser.WorkbookDataVisitor {

        private final List<SheetMeta> sheets = new ArrayList<>();
        private boolean date1904 = false;

        @Override
        public void onSheet(String relationId, String name) {
            sheets.add(new SheetMeta(relationId, name));
        }

        @Override
        public void onDate1904(boolean date1904) {
            this.date1904 = date1904;
        }

        WorkbookData build() {
            return new WorkbookData(sheets, date1904);
        }
    }

    static IntFunction<String> parseSharedStringsData(SaxUtil.ByteSource byteSource, XlsxParser parser) throws IOException {
        List<String> result = new ArrayList<>();
        try (InputStream stream = byteSource.openStream()) {
            parser.parseSharedStringsData(stream, result::add);
        }
        return result::get;
    }

    static IntPredicate parseStylesData(SaxUtil.ByteSource byteSource, XlsxParser parser, XlsxNumberingFormat dateFormat) throws IOException {
        StylesDataVisitorImpl result = new StylesDataVisitorImpl(dateFormat);
        try (InputStream stream = byteSource.openStream()) {
            parser.parseStylesData(stream, result);
        }
        return result.build();
    }

    static final class StylesDataVisitorImpl implements XlsxParser.StylesDataVisitor {

        private final XlsxNumberingFormat dateFormat;
        private final List<Integer> orderedListOfIds = new ArrayList<>();
        private final Map<Integer, String> numberFormats = new HashMap<>();

        StylesDataVisitorImpl(XlsxNumberingFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public void onNumberFormat(int formatId, String formatCode) {
            numberFormats.put(formatId, formatCode);
        }

        @Override
        public void onCellFormat(int formatId) {
            orderedListOfIds.add(formatId);
        }

        public IntPredicate build() {
            // Style order matters! -> accessed by index in sheets
            return orderedListOfIds.stream()
                    .map(o -> dateFormat.isExcelDateFormat(o, numberFormats.get(o)))
                    .collect(Collectors.toList())::get;
        }
    }

    static Sheet parseSheet(SaxUtil.ByteSource byteSource, XlsxParser parser, String name, XlsxSheetBuilder sheetBuilder) throws IOException {
        SheetVisitorImpl result = new SheetVisitorImpl(name, sheetBuilder);
        try (InputStream stream = byteSource.openStream()) {
            parser.parseSheet(stream, result);
        }
        return result.build();
    }

    static final class SheetVisitorImpl implements XlsxParser.SheetVisitor {

        private final String sheetName;
        private final XlsxSheetBuilder sheetBuilder;

        SheetVisitorImpl(String sheetName, XlsxSheetBuilder sheetBuilder) {
            this.sheetName = sheetName;
            this.sheetBuilder = sheetBuilder.reset(sheetName, null);
        }

        @Override
        public void onSheetData(String sheetBounds) {
            sheetBuilder.reset(sheetName, sheetBounds);
        }

        @Override
        public void onCell(String ref, CharSequence rawValue, String rawDataType, Integer rawStyleIndex) {
            sheetBuilder.put(ref, rawValue, rawDataType, rawStyleIndex);
        }

        public Sheet build() {
            return sheetBuilder.build();
        }
    }
}
