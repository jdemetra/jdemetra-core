/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data;

import nbbrd.design.Development;
import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Describes an abstract cursor of elements. A cursor is an iterator with an
 * adjustable position. It is a low-level iterator that allows fast access to
 * internal structures. It is also not thread-safe.
 *
 * @author Philippe Charles
 */
@NotThreadSafe
@Development(status = Development.Status.Release)
public interface BaseSeqCursor {

    /**
     * Sets the cursor at a given position. The next call to "get" should return
     * the element at that position
     *
     * @param index
     */
    void moveTo(@NonNegative int index);

    /**
     * Skips n data (advances the cursor by n positions).
     *
     * @param n
     */
    void skip(@NonNegative int n);
}
