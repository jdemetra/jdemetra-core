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
package demetra.workspace.file;

import demetra.workspace.io.IoUtil;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
interface Indexer extends Closeable {

    void checkId(@Nonnull Index.Key id) throws IOException;

    @Nonnull
    Index loadIndex() throws IOException;

    void storeIndex(@Nonnull Index index) throws IOException;

    @Nonnull
    default Indexer memoize() {
        Indexer delegate = this;
        return new Indexer() {
            private Index latest;
            private boolean storeRequired;

            @Override
            public void checkId(Index.Key id) throws IOException {
                delegate.checkId(id);
            }

            @Override
            public Index loadIndex() throws IOException {
                if (latest == null) {
                    latest = delegate.loadIndex();
                    storeRequired = false;
                }
                return latest;
            }

            @Override
            public void storeIndex(Index index) throws IOException {
                latest = index;
                storeRequired = true;
            }

            @Override
            public void close() throws IOException {
                IoUtil.closeAll(this::flushIndex, delegate::close);
            }

            private void flushIndex() throws IOException {
                if (latest != null && storeRequired) {
                    delegate.storeIndex(latest);
                }
            }
        };
    }
}
