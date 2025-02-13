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
package ec.tss.tsproviders.spreadsheet.facade;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.AbstractList;
import java.util.List;

/**
 * Facade that represents <b>a book in a spreadsheet</b>. It is created by a
 * factory.
 * <br>Note that you should not store directly a sheet since some
 * implementations may use the flyweight pattern.
 * <br>This facade might also lock some resources. Therefore it is recommended
 * to {@link #close() close} it after use.
 *
 * @see Factory
 * @see Sheet
 * @see Cell
 * @author Philippe Charles
 */
//@FacadePattern
@Deprecated
public abstract class Book implements Closeable {

    /**
     * Returns the number of sheets contained in this book.
     *
     * @return a sheet count
     */
    abstract public int getSheetCount();

    /**
     * Returns a sheet based on its index in this book.
     *
     * @param index a zero-based index
     * @return a non-null sheet
     * @throws IOException if something goes wrong during loading
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * (0<=index<sheetCount)
     */
    @NonNull
    abstract public Sheet getSheet(int index) throws IOException, IndexOutOfBoundsException;

    /**
     * Returns the name of the factory used to create this book.
     *
     * @return non-null factory identifier.
     */
    @NonNull
    abstract public String getFactoryName();

    /**
     * Closes this book and releases any resources associated with it.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Create a list of sheets.
     *
     * @return a new list.
     * @deprecated use {@link #getSheet(int)} instead to avoid swallowing of
     * exceptions
     */
    @NonNull
    @Deprecated
    public List<Sheet> getSheets() {
        return new AbstractList<Sheet>() {
            @Override
            public Sheet get(int index) {
                try {
                    return getSheet(index);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public int size() {
                return getSheetCount();
            }
        };
    }

    /**
     * Factory used to store/load books in/from spreadsheets.
     */
    public abstract static class Factory implements FileFilter {

        /**
         * Returns a unique identifier of this factory.
         *
         * @return a non-null identifier
         */
        @NonNull
        abstract public String getName();

        //<editor-fold defaultstate="collapsed" desc="Loading methods">
        /**
         * Checks if this factory can load a book from a spreadsheet.
         *
         * @return true if loading is supported; false otherwise
         */
        public boolean canLoad() {
            return true;
        }

        /**
         * Loads a book from a File.
         *
         * @param file a non-null spreadsheet file
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        public Book load(@NonNull File file) throws IOException {
            try (InputStream stream = Files.newInputStream(file.toPath())) {
                return load(stream);
            }
        }

        /**
         * Loads a book from an URL.
         *
         * @param url a non-null spreadsheet URL
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        public Book load(@NonNull URL url) throws IOException {
            try (InputStream stream = url.openStream()) {
                return load(stream);
            }
        }

        /**
         * Loads a book from a supplied InputStream.<br>This method
         * <u>automatically closes</u> the stream after use.
         *
         * @param supplier a non-null stream supplier
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        @Deprecated
        public Book load(@NonNull InputSupplier<? extends InputStream> supplier) throws IOException {
            try (InputStream stream = supplier.getInput()) {
                return load(stream);
            }
        }

        /**
         * Loads a book from an InputStream.<br>This method <u>does not
         * close</u> the stream after use.
         *
         * @param stream a non-null spreadsheet stream
         * @return a non-null book
         * @throws IOException if something goes wrong during the loading.
         */
        @NonNull
        abstract public Book load(@NonNull InputStream stream) throws IOException;
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Storing methods">
        /**
         * Checks if this factory can store a book in a spreadsheet.
         *
         * @return true if storing is supported; false otherwise
         */
        public boolean canStore() {
            return true;
        }

        /**
         * Stores a book in a file.
         *
         * @param file a non-null spreadsheet file
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        public void store(@NonNull File file, @NonNull Book book) throws IOException {
            try (OutputStream stream = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE)) {
                store(stream, book);
            }
        }

        /**
         * Stores a book in a supplied OutputStream.<br>This method
         * <u>automatically closes</u> the stream after use.
         *
         * @param supplier a non-null stream supplier
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        @Deprecated
        public void store(@NonNull OutputSupplier<? extends OutputStream> supplier, @NonNull Book book) throws IOException {
            try (OutputStream stream = supplier.getOutput()) {
                store(stream, book);
            }
        }

        /**
         * Stores a book in an OutputStream.<br>This method <u>does not
         * close</u> the stream after use.
         *
         * @param stream a non-null spreadsheet stream
         * @param book the data to be stored
         * @throws IOException if something goes wrong during the storing.
         */
        abstract public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException;
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Convenient methods">
        /**
         * Creates a convenient InputSupplier for a book from a file.
         *
         * @param file
         * @return
         */
        @NonNull
        @Deprecated
        public InputSupplier<Book> newBookSupplier(@NonNull final File file) {
            return new InputSupplier<Book>() {
                @Override
                public Book getInput() throws IOException {
                    return load(file);
                }
            };
        }

        /**
         * Creates a convenient InputSupplier for a book from an URL.
         *
         * @param url
         * @return
         */
        @NonNull
        @Deprecated
        public InputSupplier<Book> newBookSupplier(@NonNull final URL url) {
            return new InputSupplier<Book>() {
                @Override
                public Book getInput() throws IOException {
                    return load(url);
                }
            };
        }

        /**
         *
         * @param stream
         * @return
         * @deprecated use {@link #newBookSupplier(java.net.URL) } instead.
         */
        @NonNull
        @Deprecated
        public InputSupplier<Book> newBookSupplier(@NonNull final InputStream stream) {
            return new InputSupplier<Book>() {
                @Override
                public Book getInput() throws IOException {
                    return load(stream);
                }
            };
        }
        //</editor-fold>
    }
}
