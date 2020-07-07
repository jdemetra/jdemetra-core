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
package ec.tss.tsproviders.spreadsheet;

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSource;
import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.spreadsheet.facade.Book.Factory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ServiceLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 */
@Deprecated
public final class SpreadSheetAccessor implements FileFilter {

    public static final SpreadSheetAccessor INSTANCE = new SpreadSheetAccessor();
    private ImmutableList<Book.Factory> factories;

    private SpreadSheetAccessor() {
        factories = null;
    }

    public synchronized ImmutableList<Factory> getFactories() {
        if (factories == null) {
            factories = ImmutableList.copyOf(ServiceLoader.load(Book.Factory.class));
        }
        return factories;
    }

    public synchronized void setFactories(ImmutableList<Factory> factories) {
        this.factories = factories;
    }

    private Book.@Nullable Factory getFactoryByFile(@NonNull File file) {
        for (Book.Factory o : getFactories()) {
            if (o.canLoad() && o.accept(file)) {
                return o;
            }
        }
        return null;
    }

    @Override
    public boolean accept(File file) {
        return getFactoryByFile(file) != null;
    }

    @NonNull
    public SpreadSheetSource load(@NonNull File file, @NonNull SpreadSheetBean bean) throws IOException {
        Book.Factory factory = getFactoryByFile(file);
        if (factory != null) {
            try (Book book = factory.load(file)) {
                return SpreadSheetSource.load(book, bean.getDataFormat(), bean.getFrequency(), bean.getAggregationType(), bean.isCleanMissing());
            }
        }
        throw new RuntimeException("File type not supported");
    }
}
