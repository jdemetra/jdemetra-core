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

package ec.tstoolkit.modelling.arima.tramo;

import data.Data;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.regression.TsVariableSelection.Item;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TramoProcessorTest {

    public TramoProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testOutliers() {
        TramoSpecification mySpec = TramoSpecification.TR4.clone();
        // Set a lower critical value to get more outliers
        mySpec.getOutliers().setCriticalValue(2.5);
        IPreprocessor preprocessor = mySpec.build();
        PreprocessingModel model = preprocessor.process(Data.P, null);
        // The fast way. 
        // The "unbiased" parameter is the Tramo way for computing the standard errors,
        // X13 uses ML estimates
        OutlierEstimation[] outliers = model.outliersEstimation(true, false);
        for (int i = 0; i < outliers.length; ++i) {
            System.out.println(outliers[i]);
        }
        // The long way, but more flexible.
        // This solution
        // We retrieve information on the type of the regression variables through the
        // description part of the model. We get the actual coefficients by means of its
        // estimation part.
        TsVariableSelection<IOutlierVariable> select = model.description.buildRegressionVariables().select(OutlierType.AO);
        double[] coeff=model.estimation.getLikelihood().getB(), t=model.estimation.getLikelihood().getTStats();
        // The coefficients may contain information that doesn't correspond to actual regression variable:
        // The mean effect (always the first item) and - if any - estimates of missing values (just after the mean).
        // We must take those elements into account when we want to make the mapping
        // between the regression variables and their coefficients.
        int start = model.description.getRegressionVariablesStartingPosition();
        
        System.out.println();
        Item<IOutlierVariable>[] elements = select.elements();
        for (int i=0; i<elements.length; ++i){
            System.out.print(elements[i].variable.getPosition());
            System.out.print(' ');
            System.out.print(elements[i].variable.getOutlierType());
            System.out.print(' ');
            System.out.println(coeff[start+elements[i].position]);
        }

    }
    
}
