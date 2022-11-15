/*
 * Copyright 2017 National Bank of Belgium
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
package internal.ec.tss.tsproviders.spreadsheet;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.BookFactoryLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
public interface BookSupplier {

    @NonNull Optional<Book.Factory> getFactory(@NonNull File file);

    default boolean hasFactory(@NonNull File file) {
        return getFactory(file).isPresent();
    }

    @NonNull
    static BookSupplier usingServiceLoader() {
        return BookSupplier::getLoaderByFile;
    }

    static Optional<Book.Factory> getLoaderByFile(File file) {
        return BookFactoryLoader.get()
                .stream()
                .filter(Book.Factory::canLoad)
                .filter(factory -> factory.accept(file))
                .findFirst();
    }
}
