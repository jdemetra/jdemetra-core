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
package _util.tsproviders;

import demetra.tsprovider.cursor.TsCursor;
import java.io.IOException;
import java.util.function.Consumer;
import demetra.design.MightBePromoted;

/**
 *
 * @author Philippe Charles
 */
@MightBePromoted(packagePattern = "_util(\\.\\w+)+")
public final class TsCursorUtil {

    public static int readAll(TsCursor<?> cursor) throws IOException {
        int result = 0;
        cursor.getMetaData();
        while (cursor.nextSeries()) {
            cursor.getSeriesId();
            cursor.getSeriesData();
            cursor.getSeriesMetaData();
            result++;
        }
        return result;
    }

    public static int readAllAndClose(TsCursor<?> cursor) throws IOException {
        try (TsCursor<?> closeable = cursor) {
            return readAll(cursor);
        }
    }

    public static <ID> void forEachId(TsCursor<ID> cursor, Consumer<? super ID> consumer) throws IOException {
        while (cursor.nextSeries()) {
            consumer.accept(cursor.getSeriesId());
        }
    }
}
