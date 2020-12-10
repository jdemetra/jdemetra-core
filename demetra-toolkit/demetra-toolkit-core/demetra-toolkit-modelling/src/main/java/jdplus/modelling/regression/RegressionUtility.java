/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.timeseries.TimeSeriesDomain;
import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.modelling.RegressionVariables;
import jdplus.math.matrices.Matrix;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegressionUtility {

    public <D extends TimeSeriesDomain> void addAY(D domain, DataBlock rslt, double a, DoubleSeq c, ITsVariable... var) {
        Matrix x = Regression.matrix(domain, var);
        DoubleSeqCursor reader = c.cursor();
        DataBlockIterator columns = x.columnsIterator();
        while (columns.hasNext()) {
            rslt.addAY(a * reader.getAndNext(), columns.next());
        }
    }

    @ServiceProvider(RegressionVariables.Processor.class)
    public static class Processor implements RegressionVariables.Processor {

        @Override
        public <D extends TimeSeriesDomain> Matrix matrix(D d, ITsVariable... itvs) {
            return Regression.matrix(d, itvs);
        }
    }
    
    private final Processor PROCESSOR=new Processor();

}
