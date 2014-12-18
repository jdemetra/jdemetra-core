/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.spreadsheet.facade.utils;

import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.spreadsheet.facade.Cell;
import ec.tss.tsproviders.spreadsheet.facade.Sheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

/**
 *
 * @author Philippe Charles
 */
public class BookFactoryAdapter extends Book.Factory {

    protected final ec.util.spreadsheet.Book.Factory adaptee;

    public BookFactoryAdapter(ec.util.spreadsheet.Book.Factory adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public String getName() {
        return adaptee.getName();
    }

    @Override
    public boolean canLoad() {
        return adaptee.canLoad();
    }

    @Override
    public Book load(File file) throws IOException {
        return toBook(adaptee.load(file), adaptee.getName());
    }

    @Override
    public Book load(URL url) throws IOException {
        return toBook(adaptee.load(url), adaptee.getName());
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        return toBook(adaptee.load(stream), adaptee.getName());
    }

    @Override
    public boolean canStore() {
        return adaptee.canStore();
    }

    @Override
    public void store(File file, Book book) throws IOException {
        adaptee.store(file, fromBook(book));
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        adaptee.store(stream, fromBook(book));
    }

    @Override
    public boolean accept(File pathname) {
        return adaptee.accept(pathname);
    }

    static Book toBook(final ec.util.spreadsheet.Book adaptee, final String factoryName) {
        return new Book() {
            @Override
            public int getSheetCount() {
                return adaptee.getSheetCount();
            }

            @Override
            public Sheet getSheet(int index) throws IOException, IndexOutOfBoundsException {
                return toSheet(adaptee.getSheet(index));
            }

            @Override
            public String getFactoryName() {
                return factoryName;
            }

            @Override
            public void close() throws IOException {
                adaptee.close();
            }
        };
    }

    static ec.util.spreadsheet.Book fromBook(final Book adaptee) {
        return new ec.util.spreadsheet.Book() {

            @Override
            public int getSheetCount() {
                return adaptee.getSheetCount();
            }

            @Override
            public ec.util.spreadsheet.Sheet getSheet(int index) throws IOException, IndexOutOfBoundsException {
                return fromSheet(adaptee.getSheet(index));
            }

            @Override
            public void close() throws IOException {
                adaptee.close();
            }
        };
    }

    static Sheet toSheet(final ec.util.spreadsheet.Sheet adaptee) {
        return new Sheet() {

            private final ToCellAdapter flyweightCell = new ToCellAdapter();

            @Override
            public int getRowCount() {
                return adaptee.getRowCount();
            }

            @Override
            public int getColumnCount() {
                return adaptee.getColumnCount();
            }

            @Override
            public Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
                ec.util.spreadsheet.Cell cell = adaptee.getCell(rowIdx, columnIdx);
                return cell != null ? flyweightCell.withCell(cell) : null;
            }

            @Override
            public String getName() {
                return adaptee.getName();
            }

            @Override
            public Sheet memoize() {
                return this;
            }
        };
    }

    static ec.util.spreadsheet.Sheet fromSheet(final Sheet adaptee) {
        return new ec.util.spreadsheet.Sheet() {

            private final FromCellAdapter flyweightCell = new FromCellAdapter();

            @Override
            public int getRowCount() {
                return adaptee.getRowCount();
            }

            @Override
            public int getColumnCount() {
                return adaptee.getColumnCount();
            }

            @Override
            public ec.util.spreadsheet.Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
                Cell cell = adaptee.getCell(rowIdx, columnIdx);
                return cell != null ? flyweightCell.withCell(cell) : null;
            }

            @Override
            public String getName() {
                return adaptee.getName();
            }
        };
    }

    private static final class ToCellAdapter extends Cell {

        private ec.util.spreadsheet.Cell adaptee = null;

        ToCellAdapter withCell(ec.util.spreadsheet.Cell adaptee) {
            this.adaptee = adaptee;
            return this;
        }

        @Override
        public String getString() throws UnsupportedOperationException {
            return adaptee.getString();
        }

        @Override
        public Date getDate() throws UnsupportedOperationException {
            return adaptee.getDate();
        }

        @Override
        public Number getNumber() throws UnsupportedOperationException {
            return adaptee.getNumber();
        }

        @Override
        public boolean isNumber() {
            return adaptee.isNumber();
        }

        @Override
        public boolean isString() {
            return adaptee.isString();
        }

        @Override
        public boolean isDate() {
            return adaptee.isDate();
        }
    }

    private static final class FromCellAdapter extends ec.util.spreadsheet.Cell {

        private Cell adaptee = null;

        FromCellAdapter withCell(Cell adaptee) {
            this.adaptee = adaptee;
            return this;
        }

        @Override
        public String getString() throws UnsupportedOperationException {
            return adaptee.getString();
        }

        @Override
        public Date getDate() throws UnsupportedOperationException {
            return adaptee.getDate();
        }

        @Override
        public Number getNumber() throws UnsupportedOperationException {
            return adaptee.getNumber();
        }

        @Override
        public boolean isNumber() {
            return adaptee.isNumber();
        }

        @Override
        public boolean isString() {
            return adaptee.isString();
        }

        @Override
        public boolean isDate() {
            return adaptee.isDate();
        }
    }
}
