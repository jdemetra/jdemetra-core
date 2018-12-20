/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TimeSeriesDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegressionUtility {

    public <D extends TimeSeriesDomain> void addAY(D domain, DataBlock rslt, double a, DoubleSequence c, ITsVariable... var) {
        Matrix x = Regression.matrix(domain, var);
        DoubleReader reader = c.reader();
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()) {
            rslt.addAY(a * reader.next(), columns.next());
        }
    }
   
}
