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
package ec.tss.sa.processors;

import data.Data;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class X13ProcessorTest {
    
    public X13ProcessorTest() {
    }
    
    @Test
    @Ignore
    public void testOutputDictionary() {
        Map<String, Class> dic = new X13Processor().getOutputDictionary(true);
        dic.forEach((s, c) -> {
            System.out.print(s);
            System.out.print('\t');
            System.out.println(c.getCanonicalName());
        });
        System.out.println("");
        dic = new X13Processor().getOutputDictionary(false);
        dic.forEach((s, c) -> {
            System.out.print(s);
            System.out.print('\t');
            System.out.println(c.getCanonicalName());
        });
    }
    
    @Test
    public void testOutliers() {
        X13Specification xspec = X13Specification.RSA4.clone();
        TsPeriodSelector sel=new TsPeriodSelector();
        sel.excluding(0, 24);
 //       sel.all();
        xspec.getRegArimaSpecification().getBasic().setSpan(sel);
        xspec.getRegArimaSpecification().getRegression().add(new OutlierDefinition(new Day(1993, Month.May, 0), "AO"));
        xspec.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.5);
        X13Processor processor = new X13Processor();
        CompositeResults rslt = processor.generateProcessing(xspec, null).process(Data.P);
        PreprocessingModel model = rslt.get("preprocessing", PreprocessingModel.class);
        assertTrue(model.description.getOutliers().size()>1);
    }
}
