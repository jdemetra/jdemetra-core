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
import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
@Development(status = Development.Status.Release)
public interface BaseTable<E> {

    /**
     * Returns the number of columns of the matrix.
     *
     * @return the number of <code>columns</code>s in this matrix
     */
    @NonNegative
    int getColumnsCount();

    /**
     * Returns the number of rows of the matrix.
     *
     * @return the number of <code>rows</code>s in this matrix
     */
    @NonNegative
    int getRowsCount();

    default boolean isEmpty() {
        return getColumnsCount() == 0 || getRowsCount() == 0;
    }
    
    default boolean isSquare() {
        return getRowsCount() == getColumnsCount();
    }

    default boolean isRow() {
        return 1 == getRowsCount();
    }

    default boolean isColumn() {
        return 1 == getColumnsCount();
    }

    default int size(){
        return getColumnsCount()*getRowsCount();
    }
}
