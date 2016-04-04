/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package ec.util.spreadsheet.od;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 *
 * @author Philippe Charles
 */
public class OpenDocumentBookFactory extends Book.Factory {

    @Override
    public String getName() {
        return "Open Document";
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".ods");
    }

    @Override
    public Book load(File file) throws IOException {
        return new OdBook(SpreadSheet.create(new ODPackage(file)));
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        return new OdBook(SpreadSheet.create(new ODPackage(stream)));
    }

    @Override
    public boolean canStore() {
        return true;
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        toOdSpreadSheet(book).getPackage().save(stream);
    }

    @Override
    public void store(File file, Book book) throws IOException {
        toOdSpreadSheet(book).saveAs(file);
    }

    private static SpreadSheet toOdSpreadSheet(Book book) throws IOException {
        SpreadSheet result = SpreadSheet.createEmpty(new DefaultTableModel());
        for (int s = 0; s < book.getSheetCount(); s++) {
            Sheet sheet = book.getSheet(s);
            org.jopendocument.dom.spreadsheet.Sheet odSheet = result.addSheet(sheet.getName());
            odSheet.setRowCount(sheet.getRowCount());
            odSheet.setColumnCount(sheet.getColumnCount());
            for (int i = 0; i < sheet.getRowCount(); i++) {
                for (int j = 0; j < sheet.getColumnCount(); j++) {
                    odSheet.setValueAt(sheet.getCellValue(i, j), j, i);
                }
            }
        }
        result.getSheet(0).detach();
        return result;
    }
}
