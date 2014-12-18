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
package ec.util.spreadsheet.od;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 *
 * @author Philippe Charles
 */
final class OdBook extends Book {

    private final SpreadSheet book;

    public OdBook(SpreadSheet book) {
        this.book = book;
    }

    @Override
    public int getSheetCount() {
        return book.getSheetCount();
    }

    @Override
    public Sheet getSheet(int index) {
        return new OdSheet(book.getSheet(index));
    }
}
