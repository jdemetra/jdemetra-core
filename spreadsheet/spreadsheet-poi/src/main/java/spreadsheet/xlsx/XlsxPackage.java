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
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.internal.DefaultXlsxPackageFactory;

/**
 * Container for Office Open XML files.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface XlsxPackage extends Closeable {

    /**
     * Returns a stream to read the content of the main workbook.
     *
     * @return a non-null stream
     * @throws IOException if the stream is missing or something went wrong
     */
    @Nonnull
    InputStream getWorkbookData() throws IOException;

    /**
     * Returns a stream to read the content of the shared strings table.
     *
     * @return a non-null stream
     * @throws IOException if the stream is missing or something went wrong
     */
    @Nonnull
    InputStream getSharedStringsData() throws IOException;

    /**
     * Returns a stream to read the content of the styles table.
     *
     * @return a non-null stream
     * @throws IOException if the stream is missing or something went wrong
     */
    @Nonnull
    InputStream getStylesData() throws IOException;

    /**
     * Returns a stream to read the content of the specified Sheet.
     *
     * @param relationId the non-null id of a sheet
     * @return a non-null stream
     * @throws IOException if the stream is missing or something went wrong
     */
    @Nonnull
    InputStream getSheet(@Nonnull String relationId) throws IOException;

    /**
     * Factory for an XlsxPackage.
     */
    interface Factory {

        @Nonnull
        XlsxPackage open(@Nonnull InputStream stream) throws IOException;

        @Nonnull
        default XlsxPackage open(@Nonnull Path file) throws IOException {
            return open(Files.newInputStream(file));
        }

        @Nonnull
        static Factory getDefault() {
            return DefaultXlsxPackageFactory.INSTANCE;
        }
    }
}
