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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.ssf.DiffusePredictionErrorDecomposition;
import ec.tstoolkit.ssf.DiffuseSquareRootInitializer;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.RegSsf;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;

/**
 *
 * @author Jean Palate
 */
public class OneStepAheadPredictionErrors {

    public static DataBlock errors(RegArimaModel regarima) {
        IArimaModel arima = regarima.getArima();
        if (regarima.isMeanCorrection()) {
            // increase differencing order and ma polynomial
            arima = new ArimaModel(arima.getStationaryAR(), arima.getNonStationaryAR().times(BackFilter.D1),
                    arima.getMA().times(BackFilter.D1), arima.getInnovationVariance());
        }
        ISsf ssf;
        SsfArima ssfarima = new SsfArima(arima);
        DataBlock y = regarima.getY().deepClone();
        int nx = regarima.getXCount();
        if (nx > 0) {
            Matrix x = new Matrix(y.getLength(), nx);
            for (int i = 0; i < nx; ++i) {
                x.column(i).copy(regarima.X(i));
            }
            ssf = new RegSsf(ssfarima, x.all());
        } else {
            ssf = ssfarima;
        }
        Filter filter = new Filter();
        filter.setSsf(ssf);
        filter.setInitializer(new DiffuseSquareRootInitializer());

        int[] missings = regarima.getMissings();
        if (missings != null) {
            for (int i = 0; i < missings.length; ++i) {
                y.set(missings[i], Double.NaN);
            }
        }
        DiffusePredictionErrorDecomposition ped=new DiffusePredictionErrorDecomposition(true);
        
        filter.process(new SsfData(y, null), ped);
        return new DataBlock(ped.residuals(false));
    }
}
