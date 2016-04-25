/*
 * Copyright 2016 National Bank of Belgium
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
package ec.util.spreadsheet;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Represents an operation that accepts a row index, a column index and an input
 * argument and returns no result.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(int,int,Object)}.
 *
 * @author Philippe Charles
 * @param <T> the type of the input to the operation
 * @since 2.2.0
 */
@FunctionalInterface
public interface SheetConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param i the zero-based row index
     * @param j the zero-based column index
     * @param t the input argument
     */
    void accept(int i, int j, T t);

    /**
     * Returns a composed {@code SheetConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation. If performing this operation throws an exception, the
     * {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code SheetConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    @Nonnull
    default SheetConsumer<T> andThen(@Nonnull SheetConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (int i, int j, T t) -> {
            accept(i, j, t);
            after.accept(i, j, t);
        };
    }
}
