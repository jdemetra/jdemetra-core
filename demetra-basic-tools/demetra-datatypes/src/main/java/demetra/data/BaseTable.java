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
package demetra.data;

import javax.annotation.Nonnegative;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface BaseTable<E> {

    /**
     * Returns the number of columns of the matrix.
     *
     * @return the number of <code>columns</code>s in this matrix
     */
    @Nonnegative
    int getColumnsCount();

    /**
     * Returns the number of rows of the matrix.
     *
     * @return the number of <code>rows</code>s in this matrix
     */
    @Nonnegative
    int getRowsCount();

    default boolean isEmpty() {
        return getColumnsCount() == 0 || getRowsCount() == 0;
    }
    
    default int size(){
        return getColumnsCount()*getRowsCount();
    }
}
