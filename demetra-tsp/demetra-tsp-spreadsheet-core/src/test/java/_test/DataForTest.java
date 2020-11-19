/*
 * Copyright 2018 National Bank of Belgium
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
package _test;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DataForTest {

    public static final LocalDateTime JAN_2010 = LocalDate.of(2010, 1, 1).atStartOfDay();
    public static final LocalDateTime FEB_2010 = LocalDate.of(2010, 2, 1).atStartOfDay();
    public static final LocalDateTime MAR_2010 = LocalDate.of(2010, 3, 1).atStartOfDay();

    public static final Object[][] DATA = {
        {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
        {JAN_2010, 1.01, 2.01, 3.01, null},
        {FEB_2010, null, null, 3.02, 4.02},
        {MAR_2010, 1.03, null, null, 4.03}
    };

    public final static Book.Factory FACTORY = new Book.Factory() {
        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Book load(File file) throws IOException {
            return ArrayBook
                    .builder()
                    .sheet(ArraySheet.copyOf("s1", DATA))
                    .sheet(ArraySheet.copyOf("s2", new Object[][]{}))
                    .build();
        }

        @Override
        public Book load(InputStream stream) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void store(OutputStream stream, Book book) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean accept(File pathname) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
}
