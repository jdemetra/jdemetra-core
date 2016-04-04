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
package ec.tstoolkit.ucarima.estimation;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.seats.KalmanEstimator;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.FastArimaForecasts;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class McElroyEstimatesTest {

    public McElroyEstimatesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void test() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA4);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        TsData lin = rslt.getData(ModellingDictionary.Y_LIN, TsData.class);
        McElroyEstimates e1 = new McElroyEstimates();
        e1.setUcarimaModel(ucm);
        e1.setData(lin);
        double[][] cmps1 = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < cmps1.length; ++i) {
            cmps1[i] = e1.getComponent(i);
        }
        BurmanEstimatesC e2 = new BurmanEstimatesC();
        e2.setUcarimaModel(ucm);
        e2.setData(lin);
        double[][] cmps2 = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < cmps2.length; ++i) {
            cmps2[i] = e2.estimates(i, true);
        }
        for (int i = 0; i < cmps2.length; ++i) {
            if (cmps1[i] == null) {
                continue;
            }
            double d = new DataBlock(cmps1[i]).distance(new DataBlock(cmps2[i]));
            d /= new DataBlock(cmps1[i]).nrm2();
            assertTrue(d / lin.getLength() < 1e-9);
        }
    }

    @Test
    public void testWithMean() {
        TramoSeatsSpecification spec = TramoSeatsSpecification.RSA0.clone();
        spec.getTramoSpecification().getArima().airlineWithMean();
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, spec);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        // add ur in ucm
        ucm.simplify();
        UcarimaModel ucmm = addMean(ucm);
        TsData lin = rslt.getData(ModellingDictionary.Y_LIN, TsData.class);
        McElroyEstimates e1 = new McElroyEstimates();
        e1.setUcarimaModel(ucmm);
        e1.setData(lin);
        double[][] cmps1 = new double[ucmm.getComponentsCount()][];
        for (int i = 0; i < cmps1.length; ++i) {
            cmps1[i] = e1.getComponent(i);
        }
        BurmanEstimatesC e2 = new BurmanEstimatesC();
        e2.setUcarimaModelWithMean(ucm);
        e2.setData(lin);
        double[][] cmps2 = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < cmps2.length; ++i) {
            cmps2[i] = e2.estimates(i, true);
        }
        for (int i = 0; i < cmps2.length; ++i) {
            if (cmps1[i] == null) {
                continue;
            }
            double d = new DataBlock(cmps1[i]).distance(new DataBlock(cmps2[i]));
            d /= new DataBlock(cmps1[i]).nrm2();
            assertTrue(d / lin.getLength() < 1e-9);
        }
    }

    @Test
    public void testCmpForecasts() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA4);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        TsData lin = rslt.getData(ModellingDictionary.Y_LIN, TsData.class);
        McElroyEstimates e1 = new McElroyEstimates();
        int nf = 60;
        e1.setForecastsCount(nf);
        e1.setUcarimaModel(ucm);
        e1.setData(lin);
        double[][] cmps1 = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < cmps1.length; ++i) {
            cmps1[i] = e1.getComponent(i);
        }
        double[] t = e1.getComponent(0);
        double[] ft = e1.getForecasts(0);
        double[] et = e1.stdevEstimates(0);
        double[] eft = e1.stdevForecasts(0);
        double[] irr = e1.getComponent(3);
        double[] firr = e1.getForecasts(3);
        double[] eirr = e1.stdevEstimates(3);
        double[] efirr = e1.stdevForecasts(3);


        //        BurmanEstimatesC e2 = new BurmanEstimatesC();
        //        e2.setUcarimaModel(ucm);
        //        e2.setData(lin);
        //        double[][] cmps2 = new double[ucm.getComponentsCount()][];
        //        for (int i = 0; i < cmps2.length; ++i) {
        //            cmps2[i] = e2.estimates(i, true);
        //        }
        //        for (int i = 0; i < cmps2.length; ++i) {
        //            if (cmps1[i] == null) {
        //                continue;
        //            }
        //            double d = new DataBlock(cmps1[i]).distance(new DataBlock(cmps2[i]));
        //            d /= new DataBlock(cmps1[i]).nrm2();
        //            assertTrue(d / lin.getLength() < 1e-9);
        //        }

        // add forecasts
        ec.tstoolkit.ssf.SsfData sdata = new ec.tstoolkit.ssf.SsfData(lin.getValues().internalStorage(), null);
        ec.tstoolkit.ssf.ExtendedSsfData xsdata = new ec.tstoolkit.ssf.ExtendedSsfData(sdata);
        xsdata.setForecastsCount(nf);

        SsfUcarima ssf = new SsfUcarima(ucm);
        // compute KS
//	DisturbanceSmoother smoother = new DisturbanceSmoother();
//	Smoother smoother = new Smoother();
//	smoother.setSsf(ssf);
//	Filter<SsfUcarima> filter = new Filter<SsfUcarima>();
//	filter.setSsf(ssf);
//	DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
//	filter.process(xsdata , frslts);

//	smoother.process(xsdata, frslts);
//	ec.tstoolkit.ssf.SmoothingResults srslts = smoother
//		.calcSmoothedStates();

        ec.tstoolkit.ssf.Smoother smoother = new ec.tstoolkit.ssf.Smoother();
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);

        ec.tstoolkit.ssf.SmoothingResults srslts =
                new ec.tstoolkit.ssf.SmoothingResults();

        smoother.process(xsdata, srslts);
        double[] trend = srslts.component(ssf.cmpPos(0));
        double[] etrend = srslts.componentStdev(ssf.cmpPos(0));
        double[] irregular = srslts.component(ssf.cmpPos(3));
        double[] eirregular = srslts.componentStdev(ssf.cmpPos(3));
    }

    @Test
    public void testForecasts() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA4);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        TsData lin = rslt.getData(ModellingDictionary.Y_LIN, TsData.class);
        McElroyEstimates e1 = new McElroyEstimates();
        int nf = 60;
        e1.setForecastsCount(nf);
        e1.setUcarimaModel(ucm);
        e1.setData(lin);
        double[] f1 = e1.getForecasts();
        FastArimaForecasts farima = new FastArimaForecasts(ucm.getModel(), false);
        double[] f2 = farima.forecasts(lin, nf);
    }

    private UcarimaModel addMean(UcarimaModel ucm) {
        UcarimaModel tmp = new UcarimaModel();
        ArimaModel tm = ucm.getComponent(0);
        BackFilter ur = BackFilter.D1;
        tm = new ArimaModel(tm.getStationaryAR(), tm.getNonStationaryAR().times(ur), tm.getMA().times(ur),
                tm.getInnovationVariance());
        tmp.addComponent(tm);
        for (int i = 1; i < ucm.getComponentsCount(); ++i) {
            tmp.addComponent(ucm.getComponent(i));
        }
        return tmp;
    }
}
