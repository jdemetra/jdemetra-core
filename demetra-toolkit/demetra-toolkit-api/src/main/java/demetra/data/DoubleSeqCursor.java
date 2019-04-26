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

import demetra.design.Development;
import internal.data.InternalBlockCursors;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Describes a double cursor.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface DoubleSeqCursor extends BaseSeqCursor {

    /**
     * Returns the current element and advances the cursor.
     *
     * @return
     */
    double getAndNext() throws IndexOutOfBoundsException;

    //<editor-fold defaultstate="collapsed" desc="Factories">
    /**
     * Reader of an array of doubles. All data a read. The starting position is
     * the first element of the array
     *
     * @param data The underlying array
     * @return
     */
    @Nonnull
    static DoubleSeqCursor of(@Nonnull double[] data) {
        return of(data, 0, 1);
    }

    /**
     * Reader of an array of doubles. The starting position and the increment
     * between two successive elements are given.
     *
     * @param data The underlying array
     * @param pos The starting position
     * @param inc The increment between two successive items. Can be negative.
     * @return
     */
    @Nonnull
    static DoubleSeqCursor of(@Nonnull double[] data, @Nonnegative int pos, int inc) {
        switch (inc) {
            case 1:
                return new InternalBlockCursors.BlockP1DoubleSeqCursor(data, pos);
            case -1:
                return new InternalBlockCursors.BlockM1DoubleSeqCursor(data, pos);
            default:
                return new InternalBlockCursors.BlockDoubleSeqCursor(data, inc, pos);
        }
    }
    //</editor-fold>
}
