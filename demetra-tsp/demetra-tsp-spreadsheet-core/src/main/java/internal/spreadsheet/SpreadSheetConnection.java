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
package internal.spreadsheet;

import demetra.timeseries.TsCollection;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public interface SpreadSheetConnection extends Closeable {

    @NonNull
    Optional<TsCollection> getSheetByName(@NonNull String name) throws IOException;

    @NonNull
    List<String> getSheetNames() throws IOException;

    @NonNull
    List<TsCollection> getSheets() throws IOException;
}
