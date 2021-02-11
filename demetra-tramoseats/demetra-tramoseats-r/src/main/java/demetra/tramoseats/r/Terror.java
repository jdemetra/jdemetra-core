/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
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
