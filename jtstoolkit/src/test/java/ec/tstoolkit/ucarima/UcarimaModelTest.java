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

package ec.tstoolkit.ucarima;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.ucarima.estimation.BurmanEstimatesC;
import ec.tstoolkit.ucarima.estimation.McElroyEstimates;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class UcarimaModelTest {
    
    public UcarimaModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void demoHodrickPrescott() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA5);
        TsData trend=rslt.getData(ModellingDictionary.T, TsData.class);
        
        // Create the HP decomposition
          // Model with AR=1, MA=1, D=I(2)=(1-B)*(1-B)
         Polynomial D2=UnitRoots.D1.times(UnitRoots.D1);
         ArimaModel I2=new ArimaModel(null, new BackFilter(D2), null, 1);
         // White noise, with var=120000
         ArimaModel N= new ArimaModel(120000);
         
         UcarimaModel ucm=new UcarimaModel();
         ucm.addComponent(I2);
         ucm.addComponent(N);
         ucm.normalize();
         
         // Burman's estimates
         BurmanEstimatesC burman=new BurmanEstimatesC();
         burman.setUcarimaModel(ucm);
         burman.setData(trend);
         
         // Get the second component (cycle) and create the corresponding time series
         double[]c1=burman.estimates(1, true);
         TsData C1=new TsData(trend.getStart(), c1, false);
         
         // Idem with McElroy formulae
         McElroyEstimates mcelroy=new McElroyEstimates();
         mcelroy.setUcarimaModel(ucm);
         mcelroy.setData(trend);
         double[] c2=mcelroy.getComponent(1);
         TsData C2=new TsData(trend.getStart(), c2, false);
         
         // State space smoother needs a little bit more work
         SsfData data=new SsfData(trend, null);
         SsfUcarima ssf=new SsfUcarima(ucm);
         Smoother smoother=new Smoother();
         smoother.setSsf(ssf);
         SmoothingResults sr=new SmoothingResults(true, true);
         smoother.setCalcVar(true);
         smoother.process(data, sr);
         double[] c3=sr.component(ssf.cmpPos(1));
         TsData C3=new TsData(trend.getStart(), c3, false);
         
         // Computes standard errors
         double[] e1=burman.stdevEstimates(1);
         double[] e2=mcelroy.stdevEstimates(1);
         double[] e3=sr.componentStdev(ssf.cmpPos(1));
         
         TsData E1=new TsData(trend.getStart(), e1, false);
         TsData E2=new TsData(trend.getStart(), e2, false);
         TsData E3=new TsData(trend.getStart(), e3, false);
         E1.getValues().mul(sr.getStandardError());
         E2.getValues().mul(sr.getStandardError());
         
         assertTrue(C1.distance(C3) < 1e-3 && C1.distance(C2)<1e-3 && C2.distance(C3)<1e-3);
         assertTrue(E2.distance(E3)<1e-6);
 
         TsDataTable table=new TsDataTable();
         table.insert(-1, E1);
         table.insert(-1, E2);
         table.insert(-1, E3);
         System.out.println(table);
    }
}
