/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.modelling.arima.demetra;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSelectionModuleTest {
    
    public TradingDaysSelectionModuleTest() {
    }

    @Test
    public void testProd() {
        TradingDaysSelectionModule tdm=new TradingDaysSelectionModule(.01, .01);
        ModellingContext context=new ModellingContext();
        context.automodelling=true;
        context.hasseas=true;
        context.description=new ModelDescription(Data.X, null);
        context.description.setAirline(true);
        context.description.setTransformation(DefaultTransformationType.Log);
        tdm.process(context);
//        System.out.println(new DataBlock(tdm.getPdel()));
//        System.out.println(new DataBlock(tdm.getPtd()));
//        System.out.println(tdm.getChoice());
    }
    
}
