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
import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.IRobustStandardDeviationComputer;
import ec.tstoolkit.modelling.arima.ApproximateSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.ExactSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.IResidualsComputer;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ResidualsOutlierDetector;
import ec.tstoolkit.modelling.arima.x13.OutliersDetector;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectionModuleTest {

    public OutliersDetectionModuleTest() {
    }

    @Test
    @Ignore
    public void testSomeSeries() {
        ModellingContext context = new ModellingContext();
        context.automodelling = true;
        context.hasseas = true;
        context.description = new ModelDescription(Data.M2, null);
        context.description.setAirline(true);
        context.description.setTransformation(DefaultTransformationType.Log);
        OutliersDetectionModule om = new OutliersDetectionModule(new ApproximateSingleOutlierDetector());
        om.setDefault();
        om.setCriticalValue(2.8);
        om.setMethod(OutliersDetectionModule.Method.X13);
        int K = 1;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            context.description.getOutliers().clear();
            context.estimation = null;
            om.process(context);
//            for (IOutlierVariable o : context.description.getOutliers()) {
//                System.out.println(o);
//            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            context.description.getOutliers().clear();
            context.estimation = null;
            OutliersDetector om2 = new OutliersDetector();
            om2.setDefault();
            om2.setCriticalValue(2.8);
//            om2.useEML(true);
            om2.process(context);
//            for (IOutlierVariable o : context.description.getOutliers()) {
//                System.out.println(o);
//            }
        }
        System.out.println("");
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

    @Test
    @Ignore
    public void testMethods() {
        ModellingContext context = new ModellingContext();
        context.automodelling = true;
        context.hasseas = true;
        context.description = new ModelDescription(Data.M3, null);
        context.description.setAirline(true);
        context.description.setTransformation(DefaultTransformationType.Log);
        OutliersDetectionModule om = new OutliersDetectionModule(new ApproximateSingleOutlierDetector());
        om.setDefault();
        om.setCriticalValue(2.5);
        om.setMethod(OutliersDetectionModule.Method.X13);
        int K = 1;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            context.description.getOutliers().clear();
            context.estimation = null;
            om.process(context);
            for (IOutlierVariable o : context.description.getOutliers()) {
                System.out.println(o.getDescription(TsFrequency.Monthly));
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        om.setMethod(OutliersDetectionModule.Method.Tramo);
        for (int i = 0; i < K; ++i) {
            context.description.getOutliers().clear();
            context.estimation = null;
            om.process(context);
            for (IOutlierVariable o : context.description.getOutliers()) {
                System.out.println(o.getDescription(TsFrequency.Monthly));
            }
        }
        System.out.println("");
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        om = new OutliersDetectionModule(new ResidualsOutlierDetector());
        om.setDefault();
        om.setCriticalValue(2.5);
        om.setMethod(OutliersDetectionModule.Method.Demetra1);
        for (int i = 0; i < K; ++i) {
            context.description.getOutliers().clear();
            context.estimation = null;
            om.process(context);
            for (IOutlierVariable o : context.description.getOutliers()) {
                System.out.println(o.getDescription(TsFrequency.Monthly));
            }
        }
        System.out.println("");
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }
}
