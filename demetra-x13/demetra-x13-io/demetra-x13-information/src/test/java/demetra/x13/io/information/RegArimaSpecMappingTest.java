/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.x13.io.information;

import demetra.data.Data;
import demetra.information.InformationSet;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionTestSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.x13.regarima.RegArimaFactory;
import jdplus.x13.regarima.RegArimaKernel;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class RegArimaSpecMappingTest {
    
    public RegArimaSpecMappingTest() {
    }

    @Test
    public void testAll() {
        test(RegArimaSpec.RG0);
        test(RegArimaSpec.RG1);
        test(RegArimaSpec.RG2);
        test(RegArimaSpec.RG3);
        test(RegArimaSpec.RG4);
        test(RegArimaSpec.RG5);
    }

    private void test(RegArimaSpec spec) {
        InformationSet info = RegArimaSpecMapping.write(spec, null, true);
        RegArimaSpec nspec = RegArimaSpecMapping.read(info, null);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = RegArimaSpecMapping.write(spec, null, false);
        nspec = RegArimaSpecMapping.read(info, null);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }

    @Test
    public void testAllLegacy() {
        testLegacy(RegArimaSpec.RG0);
        testLegacy(RegArimaSpec.RG1);
        testLegacy(RegArimaSpec.RG2);
        testLegacy(RegArimaSpec.RG3);
        testLegacy(RegArimaSpec.RG4);
        testLegacy(RegArimaSpec.RG5);
    }

    private void testLegacy(RegArimaSpec spec) {
        InformationSet info = RegArimaSpecMapping.writeLegacy(spec, null, true);
        RegArimaSpec nspec = RegArimaSpecMapping.readLegacy(info, null);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = RegArimaSpecMapping.writeLegacy(spec, null, false);
        nspec = RegArimaSpecMapping.readLegacy(info, null);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }
    
    @Test
    public void testSpecific() {
        RegArimaKernel kernel = RegArimaKernel.of(RegArimaSpec.RG5, null);
        RegSarimaModel rslt = kernel.process(Data.TS_PROD, null);
        RegArimaSpec pspec = RegArimaFactory.INSTANCE.generateSpec(RegArimaSpec.RG5, rslt.getDescription());
        test(pspec);        
        testLegacy(pspec);        
   }

    @Test
    public void testHoliday() {
        RegArimaSpec spec =RegArimaSpec.RG5;
        TradingDaysSpec td = TradingDaysSpec.holidays("test", TradingDaysType.TD7, LengthOfPeriodType.None, RegressionTestSpec.Remove, true);
        spec=spec.toBuilder()
                .regression(spec.getRegression().toBuilder()
                .tradingDays(td)
                .build())
                .build();
        test(spec);        
        testLegacy(spec);        
   }
}
