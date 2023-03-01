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
package jdplus.sts;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;
import demetra.sts.SeasonalModel;
import ec.tstoolkit.structural.ComponentUse;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class BsmKernelTest {

    public BsmKernelTest() {
    }

    @Test
    public void testProd() {
        BsmEstimationSpec espec=BsmEstimationSpec.builder()
                .precision(1e-9)
                .build();
        BsmKernel monitor = new BsmKernel(espec);
        long t0=System.currentTimeMillis();
//        for (int i=0; i<100; ++i){
        BsmSpec mspec = BsmSpec.builder()
                .level(Parameter.undefined(), Parameter.undefined())
                .seasonal(SeasonalModel.Crude)
//                .cycle(true)
                .build();
        monitor.process(DoubleSeq.of(Data.RETAIL_BOOKSTORES), 12, mspec);
        long t1=System.currentTimeMillis();
//        System.out.println("New");
//        System.out.println(t1-t0);
//        System.out.println(monitor.getLikelihood().logLikelihood());
//        System.out.println(monitor.decompose());
//        System.out.println(monitor.getLikelihood().ser());
    }

    @Test
    public void testProdNoScaling() {
//        BsmKernel monitor = new BsmKernel();
//        BsmEstimationSpec bspec = new BsmEstimationSpec();
//        bspec.setScalingFactor(false);
//        BsmSpec mspec = new BsmSpec();
//        mspec.setLevelUse(demetra.sts.ComponentUse.Free);
//        mspec.setSlopeUse(demetra.sts.ComponentUse.Free);
////        bspec.setOptimizer(BsmSpecification.Optimizer.LBFGS);
//        mspec.setSeasonalModel(SeasonalModel.Crude);
//        monitor.setSpecifications(mspec, bspec);
//        monitor.process(DoubleSeq.of(Data.PROD), 12);
////        System.out.println("New no scaling");
////        System.out.println(monitor.getLikelihood().legacy(true).logLikelihood());
////        System.out.println(monitor.getLikelihood().ser());
//        mspec.fixComponent(5, Component.Noise);
//        monitor.setSpecifications(mspec, bspec);
//        monitor.process(DoubleSeq.of(Data.PROD), 12);
//        System.out.println("New no scaling; fixed noise var = "+mspec.getNoiseVar());
//        System.out.println(monitor.getLikelihood().logLikelihood());
////        System.out.println(monitor.getLikelihood().ser());
    }

    @Test
    public void testProdLegacy() {
        ec.tstoolkit.structural.BsmMonitor monitor = new ec.tstoolkit.structural.BsmMonitor();
        ec.tstoolkit.structural.BsmSpecification bspec = new ec.tstoolkit.structural.BsmSpecification();
        bspec.getModelSpecification().setSeasonalModel(ec.tstoolkit.structural.SeasonalModel.HarrisonStevens);
        bspec.getModelSpecification().useCycle(ComponentUse.Free);
        bspec.setPrecision(1e-9);
//        bspec.setOptimizer(ec.tstoolkit.structural.BsmSpecification.Optimizer.MinPack);
        long t0=System.currentTimeMillis();
//        for (int i=0; i<100; ++i){
        monitor.setSpecification(bspec);
        monitor.process(Data.RETAIL_MOTORDEALERS, 12);
        long t1=System.currentTimeMillis();
//        System.out.println("Legacy");
//        System.out.println(t1-t0);
//        System.out.println(monitor.getLikelihood().getLogLikelihood());
    }
    
    public static void main(String[] arg){
        stressTestProd();
        stressTestProdLegacy();
    }

    public static void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            BsmKernel monitor = new BsmKernel(null); 
            monitor.process(DoubleSeq.of(Data.PROD), 12, BsmSpec.DEFAULT);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
    }

     public static void stressTestProdLegacy() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            ec.tstoolkit.structural.BsmMonitor monitor = new ec.tstoolkit.structural.BsmMonitor();
            ec.tstoolkit.structural.BsmSpecification bspec = new ec.tstoolkit.structural.BsmSpecification();
            bspec.setOptimizer(ec.tstoolkit.structural.BsmSpecification.Optimizer.MinPack);
            monitor.setSpecification(bspec);
            monitor.process(Data.PROD, 12);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
    }
}
