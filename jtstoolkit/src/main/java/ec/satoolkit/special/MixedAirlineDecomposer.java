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

package ec.satoolkit.special;

import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.IPreprocessingFilter;
import ec.tstoolkit.arima.special.MaSpecification;
import ec.tstoolkit.arima.special.MixedAirlineMonitor;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class MixedAirlineDecomposer implements IDefaultSeriesDecomposer<MixedAirlineResults> {

    private final MaSpecification spec_;
    private MixedAirlineResults results_;

    public MixedAirlineDecomposer(MaSpecification decompositionSpec) {
        spec_ = decompositionSpec;
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
        MixedAirlineMonitor monitor = new MixedAirlineMonitor();
        MaSpecification spec = spec_.clone();
        if (model.estimation != null && model.estimation.getArima().isAirline(true)) {
            spec.airline = model.estimation.getArima();
        }
        TsData y = filter.getCorrectedSeries(true);
        if (!monitor.process(y, spec)) {
            return false;
        }
        else {
            results_ = new MixedAirlineResults(y, monitor, model.description.getTransformation() == DefaultTransformationType.Log);
            return true;
        }
    }

    @Override
    public boolean decompose(TsData y) {
        MixedAirlineMonitor monitor = new MixedAirlineMonitor();
        MaSpecification spec = spec_.clone();
        if (!monitor.process(y, spec)) {
            return false;
        }
        else {
            results_ = new MixedAirlineResults(y, monitor, false);
            return true;
        }
    }

    @Override
    public MixedAirlineResults getDecomposition() {
        return results_;
    }
}
