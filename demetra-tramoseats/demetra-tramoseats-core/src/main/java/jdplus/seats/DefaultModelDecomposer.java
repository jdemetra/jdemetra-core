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

import jdplus.arima.IArimaModel;
import demetra.design.Development;
import demetra.seats.SeatsSpec;
import demetra.seats.SeatsSpec.ApproximationMode;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultModelDecomposer implements IModelDecomposer {

    public static DefaultModelDecomposer of(SeatsSpec spec) {
        DefaultModelDecomposer decomposer = new DefaultModelDecomposer();
        decomposer.epsphi = spec.getSeasTolerance();
        decomposer.smod = spec.getSeasBoundary();
        decomposer.noisyModel=spec.getApproximationMode() == ApproximationMode.Noisy;
        return decomposer;
    }

    private double epsphi = SeatsSpec.DEF_EPSPHI;
    private double rmod = SeatsSpec.DEF_RMOD;
    private double smod = SeatsSpec.DEF_SMOD;
    private boolean noisyModel;

    /**
     *
     */
    private DefaultModelDecomposer() {
    }

    /**
     *
     * @param arima
     * @param period
     * @return
     */
    @Override
    public UcarimaModel decompose(IArimaModel arima, int period) {
        try {
            TrendCycleSelector tsel = new TrendCycleSelector(rmod);
            tsel.setDefaultLowFreqThreshold(period);
            SeasonalSelector ssel = new SeasonalSelector(period, epsphi);
            ssel.setK(smod);

            ModelDecomposer decomposer = new ModelDecomposer();
            decomposer.add(tsel);
            decomposer.add(ssel);

            UcarimaModel ucm = decomposer.decompose(arima);
            return ucm.setVarianceMax(-1, noisyModel);
        } catch (Exception err) {
            return null;
        }
    }

    /**
     * @return the epsphi
     */
    public double getEpsphi() {
        return epsphi;
    }

    /**
     * @return the rmod
     */
    public double getRmod() {
        return rmod;
    }

    /**
     * @return the smod
     */
    public double getSmod() {
        return smod;
    }

}
