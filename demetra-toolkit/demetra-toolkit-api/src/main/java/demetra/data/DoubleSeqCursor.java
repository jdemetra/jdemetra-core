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
import internal.data.InternalBlockCursors;
import java.util.function.DoubleUnaryOperator;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    @NonNull
    static DoubleSeqCursor of(@NonNull double[] data) {
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
    @NonNull
    static DoubleSeqCursor of(@NonNull double[] data, @NonNegative int pos, int inc) {
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

    /**
     * Cursor on sequence of mutable doubles
     */
    interface OnMutable extends DoubleSeqCursor{

        /**
         * Sets the given value at the current position and advance the cursor.
         *
         * @param newValue
         */
        void setAndNext(double newValue) throws IndexOutOfBoundsException;

        void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException;

        //<editor-fold defaultstate="collapsed" desc="Factories">
        /**
         * Creates a cell on an array of doubles
         *
         * @param data The array of doubles
         * @param pos The starting position of the cell
         * @param inc The distance between two adjacent cells (if c(t)=data[k],
         * c(t+1)=data[k+inc]).
         * @return The r/w iterator
         */
        static DoubleSeqCursor.OnMutable of(double[] data, int pos, int inc) {
            switch (inc) {
                case 1:
                    return new InternalBlockCursors.BlockP1DoubleVectorCursor(data, pos);
                case -1:
                    return new InternalBlockCursors.BlockM1DoubleVectorCursor(data, pos);
                default:
                    return new InternalBlockCursors.BlockDoubleVectorCursor(data, inc, pos);
            }
        }
        //</editor-fold>
    }
}
