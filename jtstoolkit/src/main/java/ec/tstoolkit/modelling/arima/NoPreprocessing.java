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


package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class NoPreprocessing implements IPreprocessor{

    @Override
    public PreprocessingModel process(TsData originalTs, ModellingContext context) {
        ModelDescription desc=new ModelDescription(originalTs, null);
        desc.setSpecification(new SarimaSpecification(desc.getFrequency()));
        ModelEstimation estimation=new ModelEstimation(desc.buildRegArima(), 0);
        PreprocessingModel pp=new PreprocessingModel(desc, estimation);
        pp.info_=new InformationSet();
        return pp;
    }

}
