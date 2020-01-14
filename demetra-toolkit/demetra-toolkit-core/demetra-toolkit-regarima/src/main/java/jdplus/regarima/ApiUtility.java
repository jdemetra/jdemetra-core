/*
* Copyright 2020 National Bank of Belgium
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
package jdplus.regarima;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import java.util.List;
import java.util.function.Function;
import jdplus.arima.IArimaModel;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ApiUtility {

    public <S extends IArimaModel, T>  demetra.regarima.RegArimaModel<T> toApi(RegArimaModel<S> regarima, Function<S, T> fn) {
        double[] y = regarima.getY().toArray();
        int[] missing = regarima.missing();
        for (int i = 0; i < missing.length; ++i) {
            y[missing[i]] = Double.NaN;
        }
        List<DoubleSeq> x = regarima.getX();
        if (x.isEmpty()) {
            return new demetra.regarima.RegArimaModel<>(
                    y, regarima.isMean(), null, fn.apply(regarima.arima()));
        } else {
            double[] all = new double[y.length * x.size()];
            int pos = 0;
            for (DoubleSeq xcur : x) {
                xcur.copyTo(all, pos);
                pos += y.length;
            }
            return new demetra.regarima.RegArimaModel<>(
                    y, regarima.isMean(), MatrixType.of(all, y.length, x.size()), fn.apply(regarima.arima()));
        }
    }
    
}
