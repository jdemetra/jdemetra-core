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
package ec.tstoolkit.arima.estimation;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class OneStepAheadPredictionErrorsTest {
    
    public OneStepAheadPredictionErrorsTest() {
    }

    @Test
    public void testSomeMethod() {
        PreprocessingModel process = TramoSpecification.TRfull.build().process(Data.X, null);
        RegArimaModel<SarimaModel> regArima = process.estimation.getRegArima();
        DataBlock errors = OneStepAheadPredictionErrors.errors(regArima);
//        System.out.println(errors);
        DataBlock cerrors = DataBlock.select(errors, x->Double.isFinite(x));
//        System.out.println(cerrors);
        assertEquals(cerrors.ssq(), process.estimation.getStatistics().SsqErr, 1e-6);
    }
    
}
