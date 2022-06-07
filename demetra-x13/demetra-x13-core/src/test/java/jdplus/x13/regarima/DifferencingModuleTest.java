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
package jdplus.x13.regarima;

import demetra.data.Data;
import ec.tstoolkit.modelling.DefaultTransformationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import demetra.timeseries.TsData;
import ec.tstoolkit.data.ReadDataBlock;

/**
 *
 * @author Jean Palate
 */
public class DifferencingModuleTest {

    public DifferencingModuleTest() {
    }


    @Test
    public void testInsee() {
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {

            DifferencingModule diff = DifferencingModule.builder()
                    .build();
            
            ec.tstoolkit.modelling.arima.x13.DifferencingModule odiff = new ec.tstoolkit.modelling.arima.x13.DifferencingModule();
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(convert(insee[i]), null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
            desc.setTransformation(DefaultTransformationType.Auto);
            desc.setAirline(true);
            context.description = desc;
            context.hasseas = true;
            odiff.process(new ReadDataBlock(insee[i].getValues().toArray()), 12);
            diff.process(insee[i].getValues(), 12);
            assertEquals(diff.getD(), odiff.getD());
            assertEquals(diff.getBd(), odiff.getBD());
            assertTrue(diff.isMeanCorrection()==odiff.isMeanCorrection());
        }
    }

    @Test
    public void testInseeLog() {
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {

            DifferencingModule diff = DifferencingModule.builder()
                    .build();
            ec.tstoolkit.modelling.arima.x13.DifferencingModule odiff = new ec.tstoolkit.modelling.arima.x13.DifferencingModule();
            odiff.process(new ReadDataBlock(insee[i].getValues().log().toArray()), 12);
            diff.process(insee[i].getValues().log(), 12);
            assertEquals(diff.getD(), odiff.getD());
            assertEquals(diff.getBd(), odiff.getBD());
            assertTrue(diff.isMeanCorrection()==odiff.isMeanCorrection());
        }
    }

    private static ec.tstoolkit.timeseries.simplets.TsData convert(TsData s) {
        int period = s.getAnnualFrequency();
        int year = s.getStart().year(), pos = s.getStart().annualPosition();
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(period),
                year, pos, s.getValues().toArray(), false);
    }

}
