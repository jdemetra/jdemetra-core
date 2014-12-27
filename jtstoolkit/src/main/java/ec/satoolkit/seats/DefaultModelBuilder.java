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


package ec.satoolkit.seats;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class DefaultModelBuilder implements IModelBuilder{

    private SarimaComponent arima;

    public DefaultModelBuilder(SarimaComponent arima){
        this.arima=arima;
    }

    @Override
    public SeatsModel build(TsData y, InformationSet info, SeatsContext context) {
        //TsData y=context.isLogTransformed() ? s.log() : s;
        int ifreq=y.getFrequency().intValue();
        SarimaModel sarima=null;
        if (arima != null && arima.isDefined() ){
            arima.setS(ifreq);
            sarima=arima.getModel();
            return new SeatsModel(y, sarima,arima.isMean());
       }else{
            SarimaSpecification spec=null;
            boolean mean=false;
            if (arima == null){
                spec=new SarimaSpecification(y.getFrequency().intValue());
                spec.airline();                        
            }else{
                spec=arima.getSpecification();
                spec.setFrequency(ifreq);
                mean=arima.isMean();
            }
            SeatsModel model=new SeatsModel(y, new SarimaModel(spec), mean);
            context.getEstimator().estimate(true, model, info);
            return model;
        }
    }

}
