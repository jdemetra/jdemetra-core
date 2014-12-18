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
import java.util.Locale;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Philippe Charles
 */
public class ExcelBookFactory extends Book.Factory {

    private boolean fast;

    public ExcelBookFactory() {
        this.fast = true;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setFast(boolean fast) {
        this.fast = fast;
    }

    public boolean isFast() {
        return fast;
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
        try {
            return fast ? FastPoiBook.create(file) : PoiBook.create(file);
        } catch (InvalidFormatException ex) {
            throw new IOException(ex);
        } catch (OpenXML4JException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        try {
            return fast ? FastPoiBook.create(stream) : PoiBook.create(stream);
        } catch (InvalidFormatException ex) {
            throw new IOException(ex);
        } catch (OpenXML4JException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        XSSFWorkbook target = new XSSFWorkbook();
        PoiBookWriter.copy(book, target);
        target.write(stream);
    }
}
