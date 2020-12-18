/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.TramoSpec;
import demetra.util.r.Dictionary;
import jdplus.math.matrices.Matrix;
import jdplus.regsarima.regular.CheckLast;
import jdplus.tramo.TramoKernel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Terror {
    
    public MatrixType process(TsData series, String defSpec, int nback){
        TramoSpec spec=TramoSpec.fromString(defSpec);
        return process(series, spec, null, nback);
    }
    
    public MatrixType process(TsData series, TramoSpec spec, Dictionary dic, int nback){
        ModellingContext context=dic == null ? null : dic.toContext();
        TramoKernel kernel=TramoKernel.of(spec, context);
        CheckLast cl=new CheckLast(kernel, nback);
        if (! cl.check(series.cleanExtremities()))
            return null;
        Matrix R=Matrix.make(nback, 7);
        R.column(0).copy(cl.getActualValues());
        R.column(1).copy(cl.getForecastsValues());
        R.column(2).copy(cl.getAbsoluteErrors());
        R.column(3).copyFrom(cl.getScores(), 0);
        R.column(4).copy(cl.getRawValues());
        R.column(5).copyFrom(cl.getRawForecasts(), 0);
        R.column(6).copyFrom(cl.getRawForecastsStdev(), 0);
        return R;
    }
   
    
}
