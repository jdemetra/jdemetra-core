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
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Describes an abstract sequence of elements. A sequence is an ordered
 * collection where duplicates are permitted and elements can be accessed by
 * their position.
 *
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
public interface BaseSeq {

    /**
     * Returns the length of this sequence.
     *
     * @return the number of <code>values</code>s in this sequence
     */
    @NonNegative
    int length();

    /**
     * Checks if this sequence is empty
     *
     * @return true if empty, false otherwise
     */
    default boolean isEmpty() {
        return length() == 0;
    }
    
    /**
     * Creates a new cursor at the beginning of this object.
     *
     * @return
     */
    @NonNull
    BaseSeqCursor cursor();
}
