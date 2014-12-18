/*
 * Copyright 2013-2014 National Bank of Belgium
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

package ec.tstoolkit.ssf;

import data.Data;
import ec.benchmarking.ssf.SsfDisaggregation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AkfAlgorithmTest {
    
    public AkfAlgorithmTest() {
    }

    @Test
    public void testDiffuse() {
        TsData X=Data.X;
        double[] x=new double[X.getLength()];
        X.copyTo(x, 0);
        double[] x0=x.clone();
        x[2]=Double.NaN;
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, -.6, -.8);
        SsfArima ssf = new SsfArima(sarima);
        SsfModel<SsfArima> model = new SsfModel<>(ssf, new SsfData(x, null), null, null);
        AkfAlgorithm<SsfArima> alg0=new AkfAlgorithm<>();
        alg0.useDiffuseInitialization(true);
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell0 = alg0.evaluate(model);
        DiffuseConcentratedLikelihood likelihood0 = ell0.getLikelihood();
        SsfAlgorithm<SsfArima> alg1=new SsfAlgorithm<>();
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> ell1 = alg1.evaluate(model);
        DiffuseConcentratedLikelihood likelihood1 = ell1.getLikelihood();
        
        RegArimaModel<SarimaModel> reg=new RegArimaModel<>(sarima, new DataBlock(x0));
        reg.setMissings(new int[]{2});
        ConcentratedLikelihood computeLikelihood = reg.computeLikelihood();
        
        assertTrue(Math.abs(likelihood0.getLogLikelihood()-likelihood1.getLogLikelihood())<1e-7);
    }
    
}
