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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Philippe Charles
 */
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
        try {
            return fast.get() ? FastPoiBook.create(createXMLReader(), file) : PoiBook.create(file);
        } catch (OpenXML4JException | InvalidOperationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        try {
            return fast.get() ? FastPoiBook.create(createXMLReader(), stream) : PoiBook.create(stream);
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

    @Nonnull
    private static XMLReader createXMLReader() throws IOException {
        try {
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException ex) {
            throw new IOException("While creating XmlReader", ex);
        }
    }
}
