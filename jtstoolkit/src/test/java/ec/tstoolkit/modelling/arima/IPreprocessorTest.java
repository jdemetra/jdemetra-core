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

import data.Data;
import ec.tstoolkit.modelling.arima.x13.X13Preprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class IPreprocessorTest {

    public IPreprocessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test
    public void demoHybrid() {
        // Default AMI of X13
        IPreprocessor processor = RegArimaSpecification.RG4.build();
        // Tramo outliers detection
        ec.tstoolkit.modelling.arima.tramo.OutliersDetector xtramo =
                new ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        // Detects all outliers type
        xtramo.setDefault();
         // Change the outliers detection module of X13
        ((X13Preprocessor) processor).outliers = xtramo;
        PreprocessingModel model = processor.process(Data.X, null);
        // Print the outliers
        OutlierEstimation[] outliers = model.outliersEstimation(true, false);
        for (int i = 0; i < outliers.length; ++i) {
            System.out.println(outliers[i]);
        }
    }
}
