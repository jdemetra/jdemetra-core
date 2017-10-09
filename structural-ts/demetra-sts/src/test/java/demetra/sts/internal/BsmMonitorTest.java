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
package demetra.sts.internal;

import data.Data;
import demetra.sts.SeasonalModel;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class BsmMonitorTest {

    public BsmMonitorTest() {
    }

    @Test
    public void testProd() {
        BsmMonitor monitor = new BsmMonitor();
        BsmSpecification bspec = new BsmSpecification();
//        bspec.setOptimizer(BsmSpecification.Optimizer.LBFGS);
        bspec.getModelSpecification().setSeasonalModel(SeasonalModel.Crude);
        monitor.setSpecification(bspec);
        monitor.process(Data.P, 12);
//        System.out.println("New");
//        System.out.println(monitor.getLikelihood().legacy(true).logLikelihood());
    }

    @Test
    public void testProdLegacy() {
        ec.tstoolkit.structural.BsmMonitor monitor = new ec.tstoolkit.structural.BsmMonitor();
        ec.tstoolkit.structural.BsmSpecification bspec = new ec.tstoolkit.structural.BsmSpecification();
        bspec.getModelSpecification().setSeasonalModel(ec.tstoolkit.structural.SeasonalModel.Crude);
//        bspec.setOptimizer(ec.tstoolkit.structural.BsmSpecification.Optimizer.LBFGS);
        monitor.setSpecification(bspec);
        monitor.process(Data.P.toArray(), 12);
//        System.out.println("Legacy");
//        System.out.println(monitor.getLikelihood().getLogLikelihood());
    }

    @Test
    @Ignore
    public void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            BsmMonitor monitor = new BsmMonitor();
            BsmSpecification bspec = new BsmSpecification();
            bspec.setOptimizer(BsmSpecification.Optimizer.MinPack);
            monitor.setSpecification(bspec);
            monitor.process(Data.P, 12);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void stressTestProdLegacy() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            ec.tstoolkit.structural.BsmMonitor monitor = new ec.tstoolkit.structural.BsmMonitor();
            ec.tstoolkit.structural.BsmSpecification bspec = new ec.tstoolkit.structural.BsmSpecification();
            bspec.setOptimizer(ec.tstoolkit.structural.BsmSpecification.Optimizer.MinPack);
            monitor.setSpecification(bspec);
            monitor.process(Data.P.toArray(), 12);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
    }
}
