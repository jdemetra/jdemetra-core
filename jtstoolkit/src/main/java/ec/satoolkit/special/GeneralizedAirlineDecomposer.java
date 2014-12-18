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
import ec.tstoolkit.arima.special.GaSpecification;
import ec.tstoolkit.arima.special.GeneralizedAirlineMonitor;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author pcuser
 */
public class GeneralizedAirlineDecomposer implements IDefaultSeriesDecomposer<GeneralizedAirlineResults>{

    private GaSpecification spec_;
    private GeneralizedAirlineResults results_;

    public GeneralizedAirlineDecomposer(GaSpecification spec){
        spec_=spec;
    }

    @Override
    public boolean decompose(TsData y) {
       GeneralizedAirlineMonitor monitor=new GeneralizedAirlineMonitor();
        monitor.setSpecification(spec_);
        if (! monitor.process(y, null))
            return false;
        else{
            results_=new GeneralizedAirlineResults(y, monitor, false);
            return true;
        }
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
       TsData y=filter.getCorrectedSeries(true);
       GeneralizedAirlineMonitor monitor=new GeneralizedAirlineMonitor();
        monitor.setSpecification(spec_);
        if (! monitor.process(y, null))
            return false;
        else{
            results_=new GeneralizedAirlineResults(y, monitor,
                    model.description.getTransformation() == DefaultTransformationType.Log);
            return true;
        }
    }

    @Override
    public GeneralizedAirlineResults getDecomposition() {
        return results_;
    }

}
