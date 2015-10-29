/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.tstoolkit.ssf.ucarima;

import data.Data;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.util.Random;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaTest {

    public SsfUcarimaTest() {
    }

    @Test
    public void testSomeMethod() {
        int N=150;
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        tsel.setDefaultLowFreqThreshold(12);
        SeasonalSelector ssel = new SeasonalSelector(12, 3);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        TsData x = Data.X.clone();
        int[] missing=new int[N];
        Random rng=new Random();
        for (int i=0; i<N; ++i){
            missing[i]=rng.nextInt(x.getLength());
        }
        SarimaModel arima = new SarimaModelBuilder().createAirlineModel(12, -.8, -.9);
        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(arima));
        ucm.setVarianceMax(-1);
        ucm.simplify();

        for (int i=0; i<N; ++i){
            x.setMissing(missing[i]);
        }
        
        SsfUcarima ssf=new SsfUcarima(ucm);
        Smoother smoother=new Smoother();
        SsfData data=new SsfData(x,null);
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);
        SmoothingResults sr=new SmoothingResults(true, true);
        smoother.process(data, sr);
        DataBlock t=new DataBlock(sr.component(0));
        DataBlock et=new DataBlock(sr.componentStdev(0));
        System.out.println(t);
        System.out.println(et);
        
        SsfArima xssf=new SsfArima(arima);
        smoother.setSsf(xssf);
        SmoothingResults xsr=new SmoothingResults(true, true);
        smoother.process(data, xsr);
        DataBlock n=new DataBlock(xsr.component(0));
        DataBlock en=new DataBlock(xsr.componentStdev(0));
        
        for (int i=0; i<N; ++i){
            int cur=missing[i];
            x.set(cur, n.get(cur));
        }
        
        data=new SsfData(x,null);
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);
        sr=new SmoothingResults(true, true);
        smoother.process(data, sr);
        t=new DataBlock(sr.component(0));
        et=new DataBlock(sr.componentStdev(0));
        System.out.println(t);
        System.out.println(et);
        
    }

}
