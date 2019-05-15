/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import jd.data.DataBlock;
import jd.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.timeseries.TimeSeriesDomain;
import demetra.data.DoubleSeq;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegressionUtility {

    public <D extends TimeSeriesDomain> void addAY(D domain, DataBlock rslt, double a, DoubleSeq c, ITsVariable... var) {
        FastMatrix x = Regression.matrix(domain, var);
        DoubleSeqCursor reader = c.cursor();
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()) {
            rslt.addAY(a * reader.getAndNext(), columns.next());
        }
    }
   
}
