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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openide.util.lookup.ServiceProvider;
import spreadsheet.xlsx.XlsxPackage;
import spreadsheet.xlsx.XlsxReader;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Book.Factory.class)
public class ExcelBookFactory extends Book.Factory {

    private final AtomicBoolean fast;

    public ExcelBookFactory() {
        this.fast = new AtomicBoolean(true);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setFast(boolean fast) {
        this.fast.set(fast);
    }

    public boolean isFast() {
        return fast.get();
    }
    //</editor-fold>

    @Override
    public String getName() {
        return "Excel";
    }

    @Override
    public boolean accept(File pathname) {
        String tmp = pathname.getName().toLowerCase(Locale.ROOT);
        return tmp.endsWith(".xlsx") || tmp.endsWith(".xlsm");
    }

    @Override
    public Book load(File file) throws IOException {
        checkFile(file);
        if (fast.get()) {
            return newReader().read(file.toPath());
        }
        try {
            return PoiBook.create(file);
        } catch (OpenXML4JException | InvalidOperationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        if (fast.get()) {
            return newReader().read(stream);
        }
        try {
            return PoiBook.create(stream);
        } catch (OpenXML4JException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        // Currenty, inline string is not supported in FastPoiBook -> use of shared strings table
        SXSSFWorkbook target = new SXSSFWorkbook(null, 100, false, true);
        try {
            PoiBookWriter.copy(book, target);
            target.write(stream);
        } finally {
            // dispose of temporary files backing this workbook on disk
            target.dispose();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @Nonnull
    private static File checkFile(@Nonnull File file) throws FileSystemException {
        if (!file.exists() || file.isDirectory()) {
            throw new NoSuchFileException(file.getPath());
        }
        if (!file.canRead()) {
            throw new AccessDeniedException(file.getPath());
        }
        return file;
    }

    private static XlsxReader newReader() {
        XlsxReader result = new XlsxReader();
        result.setPackager(CustomFactory.INSTANCE);
        return result;
    }

    static final class CustomFactory implements XlsxPackage.Factory {

        static final CustomFactory INSTANCE = new CustomFactory();

        @Override
        public XlsxPackage open(InputStream stream) throws IOException {
            try {
                return openOPC(OPCPackage.open(stream));
            } catch (OpenXML4JException | InvalidOperationException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public XlsxPackage open(Path file) throws IOException {
            try {
                return open(file.toFile());
            } catch (UnsupportedOperationException ex) {
                return XlsxPackage.Factory.super.open(file);
            }
        }

        private XlsxPackage open(File file) throws IOException {
            try {
                return openOPC(OPCPackage.open(file.getPath(), PackageAccess.READ));
            } catch (OpenXML4JException | InvalidOperationException ex) {
                throw new IOException(ex);
            }
        }

        private static XlsxPackage openOPC(OPCPackage pkg) throws IOException, OpenXML4JException {
            XSSFReader reader = new XSSFReader(pkg);
            return new XlsxPackage() {
                @Override
                public InputStream getWorkbookData() throws IOException {
                    try {
                        return reader.getWorkbookData();
                    } catch (InvalidFormatException ex) {
                        throw new IOException("While opening xml", ex);
                    }
                }

                @Override
                public InputStream getSharedStringsData() throws IOException {
                    try {
                        return reader.getSharedStringsData();
                    } catch (InvalidFormatException ex) {
                        throw new IOException("While opening xml", ex);
                    }
                }

                @Override
                public InputStream getStylesData() throws IOException {
                    try {
                        return reader.getStylesData();
                    } catch (InvalidFormatException ex) {
                        throw new IOException("While opening xml", ex);
                    }
                }

                @Override
                public InputStream getSheet(String relationId) throws IOException {
                    try {
                        return reader.getSheet(relationId);
                    } catch (InvalidFormatException ex) {
                        throw new IOException("While opening xml", ex);
                    }
                }

                @Override
                public void close() throws IOException {
                    pkg.close();
                }
            };
        }
    }
    //</editor-fold>
}
