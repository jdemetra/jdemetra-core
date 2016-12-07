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
package spreadsheet.xlsx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.internal.SaxXlsxParser;

/**
 * Parser for Office Open XML files.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface XlsxParser extends Closeable {

    void parseWorkbookData(InputStream stream, WorkbookDataVisitor visitor) throws IOException;

    void parseSharedStringsData(InputStream stream, SharedStringsVisitor visitor) throws IOException;

    void parseStylesData(InputStream stream, StylesDataVisitor visitor) throws IOException;

    void parseSheet(InputStream stream, SheetVisitor visitor) throws IOException;

    interface WorkbookDataVisitor {

        void onSheet(String relationId, String name);

        void onDate1904(boolean date1904);
    }

    interface SharedStringsVisitor {

        void onSharedString(String str);
    }

    interface StylesDataVisitor {

        void onNumberFormat(int formatId, String formatCode);

        void onCellFormat(int formatId);
    }

    interface SheetVisitor {

        void onSheetData(String sheetBounds);

        void onCell(@Nullable String ref, @Nonnull CharSequence rawValue, @Nullable String rawDataType, @Nullable Integer rawStyleIndex);
    }

    interface Factory {

        @Nonnull
        XlsxParser create() throws IOException;

        @Nonnull
        static Factory getDefault() {
            return SaxXlsxParser::create;
        }
    }
}
