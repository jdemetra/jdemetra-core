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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Book.Factory.class)
public class ExcelClassicBookFactory extends Book.Factory {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase(Locale.ROOT).endsWith(".xls");
    }

    @Override
    public String getName() {
        return "Excel Classic";
    }

    @Override
    public Book load(File file) throws IOException {
        return PoiBook.createClassic(file);
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        return PoiBook.createClassic(stream);
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        HSSFWorkbook target = new HSSFWorkbook();
        PoiBookWriter.copy(book, target);
        target.write(stream);
    }
}
