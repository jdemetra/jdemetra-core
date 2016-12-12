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

import ec.util.spreadsheet.SheetAssert;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import spreadsheet.xlsx.XlsxNumberingFormat;
import spreadsheet.xlsx.XlsxParser;
import spreadsheet.xlsx.XlsxSheetBuilder;
import spreadsheet.xlsx.internal.util.IOUtil;

/**
 *
 * @author Philippe Charles
 */
public class XlsxBookTest {

    private final IOUtil.ByteSource empty = IOUtil.ByteSource.empty();
    private final IOUtil.ByteSource boom = CustomIOException.asByteSource();
    private final XlsxParser emptyParser = new CustomParser();

    @Test
    public void testParseSharedStrings() throws IOException {
        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThat(XlsxBook.parseSharedStrings(empty, emptyParser)).isEmpty();

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> o.onSharedString(null))))
                .isInstanceOf(NullPointerException.class);

        assertThat(XlsxBook.parseSharedStrings(empty, parserOnSharedStrings(o -> {
            o.onSharedString("hello");
            o.onSharedString("world");
        }))).containsExactly("hello", "world");
    }

    @Test
    public void testParseSheet() throws IOException {
        XlsxValueFactory vf = new XlsxValueFactory(
                XlsxDateSystems.X1900,
                Arrays.asList("hello", "world")::get,
                Arrays.asList(false, true)::get);
        XlsxSheetBuilder builder = new XlsxSheetBuilders.Builder(vf);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onCell("A1", "hello", XlsxValueFactory.STRING_TYPE, null);
        }))).as("Must follow call order").isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onSheetData(null);
            o.onSheetData(null);
        }))).as("Must follow call order").isInstanceOf(IllegalStateException.class);

        SheetAssert.assertThat(XlsxBook.parseSheet("", builder, empty, parserOnSheet(o -> {
            o.onSheetData(null);
            o.onCell("A1", "hello", XlsxValueFactory.STRING_TYPE, null);
        }))).hasName("").hasRowCount(1).hasColumnCount(1).hasCellValue(0, 0, "hello");
    }

    @Test
    public void testParseStyles() throws IOException {
        XlsxNumberingFormat nf = DefaultXlsxNumberingFormat.INSTANCE;

        assertThatThrownBy(() -> XlsxBook.parseStyles(nf, boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThat(XlsxBook.parseStyles(nf, empty, emptyParser)).isEmpty();

        assertThatThrownBy(() -> XlsxBook.parseStyles(nf, empty, parserOnStyles(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseStyles(nf, empty, parserOnStyles(o -> {
            o.onCellFormat(14);
            o.onNumberFormat(14, null);
        }))).isInstanceOf(IllegalStateException.class);

        assertThat(XlsxBook.parseStyles(nf, empty, parserOnStyles(o -> {
            o.onNumberFormat(1000, "");
            o.onCellFormat(14);
        }))).containsExactly(true);
    }

    @Test
    public void testParseWorkbook() throws IOException {
        assertThatThrownBy(() -> XlsxBook.parseWorkbook(boom, emptyParser))
                .isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            throw new CustomIOException();
        }))).isInstanceOf(CustomIOException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onSheet(null, "");
        }))).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onSheet("", null);
        }))).isInstanceOf(NullPointerException.class);

        assertThat(XlsxBook.parseWorkbook(empty, emptyParser))
                .isEqualTo(new XlsxBook.WorkbookData(new ArrayList<>(), false));

        assertThat(XlsxBook.parseWorkbook(empty, parserOnWorkbook(o -> {
            o.onDate1904(true);
            o.onSheet("rId1", "hello");
        }))).isEqualTo(new XlsxBook.WorkbookData(new ArrayList<>(Arrays.asList(new XlsxBook.SheetMeta("rId1", "hello"))), true));
    }

    private static XlsxParser parserOnSharedStrings(ConsumerWithIO<? super XlsxParser.SharedStringsVisitor> consumer) {
        return new CustomParser() {
            @Override
            public void visitSharedStrings(InputStream s, XlsxParser.SharedStringsVisitor v) throws IOException {
                consumer.accept(v);
            }
        };
    }

    private static XlsxParser parserOnSheet(ConsumerWithIO<? super XlsxParser.SheetVisitor> consumer) {
        return new CustomParser() {
            @Override
            public void visitSheet(InputStream s, XlsxParser.SheetVisitor v) throws IOException {
                consumer.accept(v);
            }
        };
    }

    private static XlsxParser parserOnStyles(ConsumerWithIO<? super XlsxParser.StylesVisitor> consumer) {
        return new CustomParser() {
            @Override
            public void visitStyles(InputStream s, XlsxParser.StylesVisitor v) throws IOException {
                consumer.accept(v);
            }
        };
    }

    private static XlsxParser parserOnWorkbook(ConsumerWithIO<? super XlsxParser.WorkbookVisitor> consumer) {
        return new CustomParser() {
            @Override
            public void visitWorkbook(InputStream s, XlsxParser.WorkbookVisitor v) throws IOException {
                consumer.accept(v);
            }
        };
    }

    private static class CustomParser implements XlsxParser {

        @Override
        public void visitWorkbook(InputStream s, WorkbookVisitor v) throws IOException {
        }

        @Override
        public void visitSharedStrings(InputStream s, SharedStringsVisitor v) throws IOException {
        }

        @Override
        public void visitStyles(InputStream s, StylesVisitor v) throws IOException {
        }

        @Override
        public void visitSheet(InputStream s, SheetVisitor v) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    interface ConsumerWithIO<T> {

        void accept(T t) throws IOException;
    }
}
