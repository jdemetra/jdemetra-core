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

import ec.tstoolkit.timeseries.regression.SwitchOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import data.Data;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class IPreprocessingModuleTest {
    
    public IPreprocessingModuleTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void demoOutliersDetection() {
        ModelDescription desc=new ModelDescription(Data.X, null);
        desc.setAirline(true);
        ModellingContext context=new ModellingContext();
        context.description=desc.clone();
        
        ec.tstoolkit.modelling.arima.tramo.OutliersDetector xtramo=new 
                ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        xtramo.setAll();   
        xtramo.addOutlierFactory(new SwitchOutlierFactory());
        xtramo.setCriticalValue(3);
        xtramo.process(context);
        
        System.out.println("Tramo");
        for (IOutlierVariable var : context.description.getOutliers()){
            System.out.print(var.getPosition());
            System.out.print(' ');
            System.out.println(var.getOutlierType());
        }
        context.description=desc.clone();
        ec.tstoolkit.modelling.arima.x13.OutliersDetector xx13=new 
                ec.tstoolkit.modelling.arima.x13.OutliersDetector();
        xx13.setAll();      
        xx13.addOutlierFactory(new SwitchOutlierFactory());
        xx13.setCriticalValue(3);
        xx13.process(context);
        
        System.out.println();
        System.out.println("X13");
        for (IOutlierVariable var : context.description.getOutliers()){
            System.out.print(var.getPosition());
            System.out.print(' ');
            System.out.println(var.getOutlierType());
        }
    }
}
