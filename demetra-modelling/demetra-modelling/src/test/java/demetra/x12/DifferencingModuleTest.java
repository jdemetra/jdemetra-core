/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x12;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import ec.tstoolkit.modelling.DefaultTransformationType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DifferencingModuleTest {

    public DifferencingModuleTest() {
    }

    @Test
    public void testProd() {
        DifferencingModule test = DifferencingModule.builder().build();
        int[] diff = test.process(DoubleSequence.of(Data.PROD), new int[]{1,12}, null);
        System.out.println(diff[0]);
        System.out.println(diff[1]);
        System.out.println(test.isMeanCorrection());
    }

    @Test
    public void testX() {
        DifferencingModule test = DifferencingModule.builder().build();
        int[] diff = test.process(DoubleSequence.of(Data.EXPORTS), new int[]{1,12}, null);
        System.out.println(diff[0]);
        System.out.println(diff[1]);
        System.out.println(test.isMeanCorrection());
    }

    //@Test
    public void testProdLegacy() {

        ec.tstoolkit.modelling.arima.x13.DifferencingModule diff = new ec.tstoolkit.modelling.arima.x13.DifferencingModule();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
        desc.setTransformation(DefaultTransformationType.Auto);
        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        diff.process(s, 12);
        System.out.println(diff.getD());
        System.out.println(diff.getBD());
        System.out.println(diff.isMeanCorrection());
    }
}
