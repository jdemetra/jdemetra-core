/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;

/**
 *
 * @author Jean Palate
 */
public class TsMatrix implements Cloneable {

    private TsPeriod start_;
    private Matrix matrix_;

    /**
     * Creates a new time series matrix
     * @param start The starting period
     * @param nperiods The number of periods
     * @param ncolumns The number of series (columns) in the object 
     */
    public TsMatrix(TsPeriod start, final int nperiods, final int ncolumns) {
        start_ = start.clone();
        matrix_ = new Matrix(nperiods, ncolumns);
    }

    /**
     * Creates a new time series matrix
     * @param start The starting period
     * @param m The matrix of data. Series should be stored in the columns.
     * @param clone Indicates if the data will be copied (true) or simply referenced (false)
     */
    public TsMatrix(TsPeriod start, final Matrix m, boolean clone) {
        start_ = start.clone();
        if (clone) {
            matrix_ = m.clone();
        }
        else {
            matrix_ = m;
        }
    }

    /**
     * Creates a new time series matrix
     * @param s The array of time series that will constitute the matrix.
     * The time domain of the different series may differ.
     */
    public TsMatrix(TsData... s) {
        TsDataTable tmp = new TsDataTable();
        for (int i = 0; i < s.length; ++i) {
            tmp.insert(-1, s[i]);
        }
        TsDomain domain = tmp.getDomain();
        start_ = domain.getStart();
        matrix_ = new Matrix(domain.getLength(), s.length);
        matrix_.set(Double.NaN);
        for (int i = 0; i < domain.getLength(); ++i) {
            for (int j = 0; j < s.length; ++j) {
                TsDataTableInfo dataInfo = tmp.getDataInfo(i, j);
                if (dataInfo == TsDataTableInfo.Valid) {
                    matrix_.set(i, j, tmp.getData(i, j));
                }
            }
        }
    }

    /**
     * The domain of the matrix.
     * @return The union of the time domain of all the series
     */
    public TsDomain getDomain() {
        return new TsDomain(start_, matrix_.getRowsCount());
    }

    /**
     * The Matrix of all the data
     * @return 
     */
    public Matrix getMatrix() {
        return matrix_;
    }

    /**
     * The number of columns (or series) in the matrix
     * @return 
     */
    public int getColumnsCount() {
        return matrix_.getColumnsCount();
    }

    /**
     * Gets the series at the specified position
     * @param pos The position of the series (in [0, getColumnsCount()[)
     * @return A cleaned series (missing values at the beginning and at the end have been removed).
     */
    public TsData series(int pos) {
        return new TsData(start_, matrix_.column(pos)).cleanExtremities();
    }

    /**
     * Gets a row of the matrix
     * @param pos The position of the row (in [0, getDomain().getLength()[)
     * @return The returned object can be use for input and for output.
     */
    public DataBlock data(int pos) {
        return matrix_.row(pos);
    }

    /**
     * Removes leading/trailing missing values
     * @param data The data to simplify
     * @return A new data block is returned (r/w).
     */
    public static DataBlock shrink(DataBlock data) {

        double[] x = data.getData();
        int beg = data.getStartPosition(), end = data.getEndPosition(), inc = data.getIncrement();
        while (beg != end) {
            if (Double.isFinite(x[beg])) {
                break;
            }
            else {
                beg += inc;
            }
        }
        if (beg == end) {
            return new DataBlock(0);
        }
        while (beg != end) {
            if (Double.isFinite(x[end - inc])) {
                break;
            }
            else {
                end -= inc;
            }
        }
        return new DataBlock(x, beg, end, inc);
    }

    /**
     * Gets a row of the matrix
     * @param pos The position of the row in the domain
     * @return The returned object can be use for input and for output. 
     * Null if it is outside the domain
     */
    public DataBlock data(TsPeriod pos) {
        int row = pos.minus(start_);
        if (row < 0 || row >= matrix_.getRowsCount()) {
            return null;
        }
        return matrix_.row(row);
    }

    /**
     * Gets a row of the matrix
     * @param pos The position of the row in the domain
     * @return The returned object can be use for input and for output. 
     * Null if it is outside the domain
     */
    public DataBlock data(Day pos) {
        int row = getDomain().search(pos);
        if (row < 0 || row >= matrix_.getRowsCount()) {
            return null;
        }
        return matrix_.row(row);
    }

    /**
     * Gets a selection in a given series (column)
     * @param selector The selector
     * @param col The column
     * @return A new TsDataBlock is returned. It gives a direct access (r/w) to the data.
     */
    public TsDataBlock series(TsPeriodSelector selector, int col) {
        TsDomain domain = getDomain();
        TsDomain sdomain = domain.select(selector);
        if (sdomain.isEmpty()) {
            return null;
        }
        int nbeg = sdomain.getStart().minus(start_);
        int nend = domain.getLength() - sdomain.getLength() - nbeg;
        return new TsDataBlock(sdomain.getStart(), matrix_.column(col).drop(nbeg, nend));
    }

    /**
     * Expands an existing matrix
     * @param nperiods The number of new periods (at the end)
     * @param ncolumns The number of new columns (at the end)
     * @return Creates a new Matrix. The new rows/columns are
     * initialised with Double.NaN.
     */
    public TsMatrix expand(final int nperiods, final int ncolumns) {
        Matrix m = new Matrix(matrix_.getRowsCount() + nperiods, matrix_.getColumnsCount() + ncolumns);
        m.set(Double.NaN);
        m.subMatrix(0, matrix_.getRowsCount(), 0, matrix_.getColumnsCount()).copy(matrix_.all());
        return new TsMatrix(start_, m, false);
    }

    /**
     * Selects a part of the matrix
     * @param selector The selector
     * @return The selection of the current object. The values are copied, not referenced
     */
    public TsMatrix select(final TsPeriodSelector selector) {
        TsDomain domain = getDomain();
        TsDomain sdomain = domain.select(selector);
        if (sdomain.isEmpty()) {
            return null;
        }
        Matrix m = new Matrix(sdomain.getLength(), matrix_.getColumnsCount());
        int r0 = sdomain.getStart().minus(start_);
        m.all().copy(matrix_.subMatrix(r0, r0 + matrix_.getRowsCount(), 0, matrix_.getColumnsCount()));
        return new TsMatrix(start_, m, false);
    }

    @Override
    public TsMatrix clone() {
        try {
            TsMatrix m = (TsMatrix) super.clone();
            m.matrix_ = matrix_.clone();
            return m;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
}
