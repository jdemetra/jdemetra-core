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
package demetra.tsprovider.grid;

import internal.tsprovider.grid.GridFactoryImpl;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface GridFactory {

    @Nonnull
    GridReader getReader(@Nonnull GridImport options);

    @Nonnull
    GridWriter getWriter(@Nonnull GridExport options);

    @Nonnull
    default TsCollectionGrid read(@Nonnull GridInput input, @Nonnull GridImport options) {
        try (GridReader o = getReader(options)) {
            return o.read(input);
        }
    }

    @Nonnull
    default void write(@Nonnull TsCollectionGrid value, @Nonnull GridOutput output, @Nonnull GridExport options) {
        try (GridWriter o = getWriter(options)) {
            o.write(value, output);
        }
    }

    @Nonnull
    static GridFactory getDefault() {
        return GridFactoryImpl.INSTANCE;
    }
}
