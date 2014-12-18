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

package ec.tss.sa.processors;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsProcessorTest {
    
    public TramoSeatsProcessorTest() {
    }

    @Test
    public void testContext(){
 /////////////////
       ProcessingContext context = new ProcessingContext();
        TsData refdata = new TsData(TsFrequency.Monthly, 1980, 0, 240);
        Random rnd = new Random();
        for (int i = 0; i < refdata.getLength(); ++i) {
            refdata.set(i, rnd.nextDouble());
        }
        TsData xdata = new TsData(TsFrequency.Monthly, 1980, 0, 240);
        double prev=0;
        for (int i = 0; i < xdata.getLength(); ++i) {
            double cur=rnd.nextDouble();
            xdata.set(i, cur+prev);
            prev=cur;
        }
        int selectionLag=1;
/////////////////////

        TsVariables vars = new TsVariables();
        vars.set("var1", new TsVariable(refdata));
        context.getTsVariableManagers().set("tmp", vars);
        // The full name of the variable is tmp.var1
        
        TramoSpecification spec=new TramoSpecification();
        spec.setUsingAutoModel(true);
        TsVariableDescriptor dvar=new TsVariableDescriptor();
        dvar.setLags(selectionLag, selectionLag);
        dvar.setName("tmp.var1");
        spec.getRegression().add(dvar);
        PreprocessingModel rslt = spec.build(context).process(xdata, null);
        
        TsData res=rslt.getFullResiduals();
        
        Ts mts=TsFactory.instance.createTs("model", null,xdata.minus(res));
        Ts rts=TsFactory.instance.createTs("res",null, res);
        Ts fts=TsFactory.instance.createTs("fcasts", null, rslt.forecast(1, false));
        
        assertTrue(fts != null && rts != null && mts != null);
//        System.out.println(mts.getTsData());
//        System.out.println(fts.getTsData());
   }
}