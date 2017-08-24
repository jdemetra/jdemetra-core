/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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

import java.util.stream.DoubleStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface DoubleMatrix extends BaseMatrix<Double> {

    public static DoubleMatrix ofInternal(@Nonnull double[] data, @Nonnegative int nrows, @Nonnegative int ncolumns){
        if (data.length < nrows*ncolumns){
            throw new IllegalArgumentException();
        }
        return new MatrixReader(data, nrows, ncolumns);
    }
    
    public static DoubleMatrix copyOf(@Nonnull DoubleMatrix matrix){
        return new MatrixReader(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
    }
    
    /**
     * Returns the <code>double</code> value at the specified row/column.
     *
     * @param row
     * @param column
     * @return
     */
    double get(@Nonnegative int row, @Nonnegative int column) throws IndexOutOfBoundsException;

    /**
     *
     * @param irow
     * @return
     */
    DoubleSequence row(@Nonnull int irow);

    public DoubleSequence diagonal();

    public DoubleSequence subDiagonal(int pos);
    /**
     *
     * @param icolumn
     * @return
     */
    DoubleSequence column(@Nonnull int icolumn);

    default DoubleMatrix extract(@Nonnegative final int rstart, @Nonnegative final int nr,
            @Nonnegative final int cstart, @Nonnegative final int nc) {
        return new SubMatrixReader(this, rstart, nr, cstart, nc);
    }

    /**
     * Copies the data into a given buffer
     *
     * @param buffer The buffer that will receive the data.
     * @param offset The start position in the buffer for the copy. The matrix
     * will be copied in the buffer by columns at the indexes [start, start+size()[.
     * The length of the buffer is not checked (it could be larger than this
     * array.
     */
    default void copyTo(@Nonnull double[] buffer, @Nonnegative int offset) {
        int pos = offset, nr = getRowsCount(), nc = getColumnsCount();
        for (int c = 0; c < nc; ++c) {
            column(c).copyTo(buffer, pos);
            pos += nr;
        }
    }

    /**
     * @return @see DoubleStream#toArray()
     */
    @Nonnull
    default double[] toArray() {
        double[] all = new double[size()];
        int pos = 0, nr = getRowsCount(), nc = getColumnsCount();
        for (int c = 0; c < nc; ++c) {
            column(c).copyTo(all, pos);
            pos += nr;
        }
        return all;
    }

    public static String toString(DoubleMatrix matrix, String fmt) {
        StringBuilder builder = new StringBuilder();
        if (!matrix.isEmpty()) {
            DoubleSequence row = matrix.row(0);
            builder.append(DoubleSequence.toString(row, fmt));
            for (int i = 1; i < matrix.getRowsCount(); ++i) {
                builder.append(System.lineSeparator());
                row = matrix.row(i);
                builder.append(DoubleSequence.toString(row, fmt));
            }
        }
        return builder.toString();
    }

}
