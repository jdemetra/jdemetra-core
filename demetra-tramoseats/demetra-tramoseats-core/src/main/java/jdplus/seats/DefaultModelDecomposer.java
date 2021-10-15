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
package jdplus.seats;

import demetra.arima.SarimaOrders;
import nbbrd.design.Development;
import demetra.seats.DecompositionSpec;
import demetra.seats.DecompositionSpec.ModelApproximationMode;
import demetra.seats.SeatsSpec;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Data
public class DefaultModelDecomposer implements IModelDecomposer {

    private final DecompositionSpec spec;

    /**
     *
     * @param arima
     * @return
     */
    @Override
    public UcarimaModel decompose(SarimaModel arima) {
        try {
            int period=arima.getPeriod();
            SarimaOrders orders = arima.orders();
            TrendCycleSelector tsel = new TrendCycleSelector(spec.getTrendBoundary());
            tsel.setDefaultLowFreqThreshold(period);
            SeasonalSelector ssel = new SeasonalSelector(period, spec.getSeasTolerance());
            if (orders.getBd()>0 || orders.getBp()>0) {
                ssel.setK(spec.getSeasBoundary());
            } else {
                ssel.setK(spec.getSeasBoundaryAtPi());
            }
            
            ModelDecomposer decomposer = new ModelDecomposer();
            decomposer.add(tsel);
            decomposer.add(ssel);

            UcarimaModel ucm = decomposer.decompose(arima);
            return ucm.setVarianceMax(-1, spec.getApproximationMode()==ModelApproximationMode.Noisy);
        } catch (Exception err) {
            return null;
        }
    }

}
