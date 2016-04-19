/*
 * Copyright 2016 National Bank of Belgium
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
package ec.util.spreadsheet;

import static ec.util.spreadsheet.Assertions.msg;
import ec.util.spreadsheet.helpers.ArrayBook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

/**
 *
 * @author Philippe Charles
 */
public class BookFactoryAssert extends AbstractAssert<BookFactoryAssert, Book.Factory> {

    public BookFactoryAssert(Book.Factory actual) {
        super(actual, BookFactoryAssert.class);
    }

    public static BookFactoryAssert assertThat(Book.Factory actual) {
        return new BookFactoryAssert(actual);
    }

    public BookFactoryAssert isCompliant(File valid, File invalid) throws IOException {
        isNotNull();
        SoftAssertions s = new SoftAssertions();
        assertCompliance(s, actual, valid, invalid);
        s.assertAll();
        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static void assertCompliance(SoftAssertions s, Book.Factory factory, File valid, File invalid) throws IOException {
        s.assertThat(factory.getName()).isNotNull();
        s.assertThat(factory.accept(valid)).isTrue();
        s.assertThat(factory.accept(invalid)).isTrue();

        if (factory.canLoad()) {
            assertLoadNull(s, factory);
            assertLoadValid(s, factory, valid);
            assertLoadInvalid(s, factory, invalid);
        } else {
            assertLoadUnsupported(s, factory, valid);
        }

        if (factory.canLoad() && factory.canStore()) {
            assertLoadStore(s, factory, valid.toURI().toURL());
        }
    }

    private static void assertLoadNull(SoftAssertions s, Book.Factory factory) {
        s.assertThatThrownBy(() -> factory.load(NULL_FILE))
                .as(msg(factory, "load(nullFile)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_INPUT_STREAM))
                .as(msg(factory, "load(nullInputStream)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_PATH))
                .as(msg(factory, "load(nullPath)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> factory.load(NULL_URL))
                .as(msg(factory, "load(nullURL)", NullPointerException.class))
                .isInstanceOf(NullPointerException.class);
    }

    private static void assertLoadValid(SoftAssertions s, Book.Factory factory, File valid) throws IOException {
        ArrayBook b;
        try (Book x = factory.load(valid)) {
            BookAssert.assertCompliance(s, x);
            b = ArrayBook.copyOf(x);
        }
        try (InputStream stream = Files.newInputStream(valid.toPath())) {
            try (Book x = factory.load(stream)) {
                BookAssert.assertContentEquals(s, b, x, true);
            }
        }
        try (Book x = factory.load(valid.toPath())) {
            BookAssert.assertContentEquals(s, b, x, true);
        }
        try (Book x = factory.load(valid.toURI().toURL())) {
            BookAssert.assertContentEquals(s, b, x, true);
        }
    }

    private static void assertLoadInvalid(SoftAssertions s, Book.Factory f, File invalidFile) throws IOException {
        // TODO
    }

    private static void assertLoadUnsupported(SoftAssertions s, Book.Factory f, File valid) throws IOException {
        s.assertThatThrownBy(() -> f.load(valid))
                .isInstanceOf(UnsupportedOperationException.class);
        try (InputStream stream = Files.newInputStream(valid.toPath())) {
            s.assertThatThrownBy(() -> f.load(stream))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
        s.assertThatThrownBy(() -> f.load(valid.toPath()))
                .isInstanceOf(UnsupportedOperationException.class);
        s.assertThatThrownBy(() -> f.load(valid.toURI().toURL()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static void assertLoadStore(SoftAssertions s, Book.Factory factory, URL sample) throws IOException {
        try (Book original = factory.load(sample)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            factory.store(outputStream, original);
            byte[] data = outputStream.toByteArray();

            try (Book result = factory.load(new ByteArrayInputStream(data))) {
                BookAssert.assertContentEquals(s, original, result, false);
            }
        }
    }

    private static final File NULL_FILE = null;
    private static final InputStream NULL_INPUT_STREAM = null;
    private static final Path NULL_PATH = null;
    private static final URL NULL_URL = null;
    //</editor-fold>
}
