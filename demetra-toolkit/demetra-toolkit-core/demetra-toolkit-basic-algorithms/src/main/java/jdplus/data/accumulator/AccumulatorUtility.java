/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.data.accumulator;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import java.util.Iterator;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.lapack.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class AccumulatorUtility {
    /**
     * Computes in a robust way the scalar product
     *
     * @param l The first DataBlock
     * @param r The second DataBlock. Cannot be smaller than l.
     * @param sum The robust accumulator. Should be correctly initialized. It
     * will contain the result on exit
     */
    public void robustDot(DataBlock l, DoubleSeq r, DoubleAccumulator sum) {
        DoubleSeqCursor lcursor = l.cursor();
        DoubleSeqCursor rcursor = r.cursor();
        int n=l.length();
        for (int i=0; i<n; ++i){
            sum.add(lcursor.getAndNext()*rcursor.getAndNext());
        }
    }
    
    
    default void robustProduct(final FastMatrix lm, final FastMatrix rm, DoubleAccumulator acc) {
        DataBlockIterator iter = columnsIterator(), riter = lm.rowsIterator(), citer = rm.columnsIterator();
        while (iter.hasNext()) {
            riter.reset();
            DataBlock cur = iter.next(), col = citer.next();
            cur.set(riter, row -> {
                acc.reset();
                col.robustDot(row, acc);
                return acc.sum();
            });
        }
    }
    public Matrix robustXtX(final FastMatrix X, DoubleAccumulator acc) {
        int n = X.getColumnsCount();
        Matrix z = Matrix.square(n);
        DataBlockIterator rows = X.columnsIterator(), columns = X.columnsIterator();
        int irow = 0;
        while (rows.hasNext()) {
            DataBlock row = rows.next();
            columns.reset(irow);
            acc.reset();
            row.robustDot(columns.next(), acc);
            z.set(irow, irow, acc.sum());
            int icol = irow;
            while (columns.hasNext()) {
                icol++;
                acc.reset();
                row.robustDot(columns.next(), acc);
                double val = acc.sum();
                z.set(irow, icol, val);
                z.set(icol, irow, val);
            }
            irow++;
        }
        return z;
    }


}
