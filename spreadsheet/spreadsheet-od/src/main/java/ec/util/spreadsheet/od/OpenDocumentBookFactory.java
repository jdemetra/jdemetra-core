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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
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
        return false;
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        throw new IOException("Not supported yet.");
    }
}
