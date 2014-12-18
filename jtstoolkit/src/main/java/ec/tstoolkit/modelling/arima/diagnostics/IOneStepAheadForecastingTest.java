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

package ec.tstoolkit.modelling.arima.diagnostics;

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.stats.MeanTest;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public interface IOneStepAheadForecastingTest {
    
    boolean test(RegArimaModel<SarimaModel> regarima);
    
    int getInSampleLength();
    int getOutOfSampleLength();
    
    IReadDataBlock getInSampleResiduals();
    IReadDataBlock getOutOfSampleResiduals();
    
    MeanTest inSampleMeanTest();
    MeanTest outOfSampleMeanTest();
    StatisticalTest mseTest();
    
    /**
     * In-sample mean error 
     * @return 
     */
    double getInSampleME();
    /**
     * Out-of-sample mean error
     * @return 
     */
    double getOutOfSampleME();

    /**
     * In-sample mean square error 
     * @return 
     */
    double getInSampleMSE();
    /**
     * Out-of-sample mean square error
     * @return 
     */
    double getOutOfSampleMSE();
}
