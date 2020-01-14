/*
 * Copyright 2016 National Bank of Belgium
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
/*
 */
package jdplus.ssf;

import jdplus.data.DataBlock;
import jdplus.math.matrices.MatrixStorage;
import jdplus.math.matrices.Matrix;


/**
 *
 * @author Jean Palate
 */
public class MatrixResults {

    MatrixStorage data_;
    int start_;

    /**
     *
     */
    public MatrixResults() {
    }

    /**
     *
     */
    public void clear() {
        data_ = null;
    }

    /**
     *
     * @return
     */
    public int getCurrentSize() {
        return data_.getCurrentSize();
    }

    /**
     *
     * @return
     */
    public int getDim() {
        return data_.getMatrixColumnsCount();
    }

    /**
     *
     * @param dim
     * @param start
     * @param end
     */
    public void prepare(final int dim, final int start, final int end) {
        clear();
        start_=start;
        data_ = new MatrixStorage(dim, end - start);
    }

    /**
     *
     * @param nrows
     * @param ncols
     * @param start
     * @param end
     */
    public void prepare(final int nrows, final int ncols, final int start, final int end) {
        clear();
        start_=start;
        data_ = new MatrixStorage(nrows, ncols, end - start);
    }
    /**
     *
     * @param t
     * @return
     */
    public Matrix matrix(final int t) {
        if (data_ == null || t < start_) {
            return null;
        } else {
            return data_.matrix(t - start_);
        }
    }

    public DataBlock item(int row, int col){
        return data_.item(row, col);
    }

    public void save(final int t, final Matrix P) {
        int st = t - start_;
        if (st < 0) {
            return;
        }
        int capacity=data_.getCapacity();
        if (capacity<=st){
            data_.resize(capacity<<1);
        }
        data_.save(st, P);
    }

    /**
     *
     * @return
     */
    public int getStartSaving() {
        return start_;
    }

    public void rescale(double factor) {
        data_.rescale(factor);
    }

}
