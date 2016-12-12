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
package spreadsheet.xlsx;

import ec.util.spreadsheet.Sheet;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.internal.XlsxSheetBuilders;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface XlsxSheetBuilder extends Closeable {

    @Nonnull
    XlsxSheetBuilder reset(@Nonnull String sheetName, @Nullable String sheetBounds);

    @Nonnull
    XlsxSheetBuilder put(@Nullable String ref, @Nonnull CharSequence value, @Nullable String dataType, @Nullable Integer styleIndex);

    @Nonnull
    Sheet build();

    interface Factory {

        @Nonnull
        XlsxSheetBuilder create(
                @Nonnull XlsxDateSystem dateSystem,
                @Nonnull IntFunction<String> sharedStrings,
                @Nonnull IntPredicate dateFormats) throws IOException;

        static Factory getDefault() {
            return XlsxSheetBuilders::create;
        }
    }
}
