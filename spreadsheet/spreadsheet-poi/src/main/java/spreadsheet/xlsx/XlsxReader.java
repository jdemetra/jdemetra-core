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

import ec.util.spreadsheet.Book;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.internal.XlsxBook;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Data
public final class XlsxReader {

    XlsxPackage.Factory packager = XlsxPackage.Factory.getDefault();
    XlsxParser.Factory parser = XlsxParser.Factory.getDefault();
    XlsxNumberingFormat numberingFormat = XlsxNumberingFormat.getDefault();
    XlsxDateSystem dateSystem1900 = XlsxDateSystem.getDefault(false);
    XlsxDateSystem dateSystem1904 = XlsxDateSystem.getDefault(true);
    XlsxSheetBuilder.Factory builder = XlsxSheetBuilder.Factory.getDefault();

    @Nonnull
    public Book read(@Nonnull Path file) throws IOException {
        XlsxPackage pkg = getPackager().open(file);
        return create(pkg);
    }

    @Nonnull
    public Book read(@Nonnull InputStream stream) throws IOException {
        XlsxPackage pkg = getPackager().open(stream);
        return create(pkg);
    }

    private Book create(XlsxPackage pkg) throws IOException {
        try {
            return XlsxBook.create(pkg, this);
        } catch (IOException ex) {
            ensureClosed(ex, pkg);
            throw ex;
        }
    }

    private static void ensureClosed(IOException exception, Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException suppressed) {
            exception.addSuppressed(suppressed);
        }
    }
}
