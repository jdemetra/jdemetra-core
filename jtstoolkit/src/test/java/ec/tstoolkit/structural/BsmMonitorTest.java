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

package ec.tstoolkit.structural;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class BsmMonitorTest {
    
    public BsmMonitorTest() {
    }

    @Test
    public void testCycle() {
        BsmMonitor monitor=new BsmMonitor();
        ModelSpecification mspec=new ModelSpecification();
        mspec.cUse=ComponentUse.Free;
       // mspec.sUse=ComponentUse.Unused;
        mspec.seasModel=SeasonalModel.Crude;
        monitor.setSpecification(mspec);
        double[]y=data.Data.X.getValues().internalStorage();
        boolean ok = monitor.process(y, 12);
        BasicStructuralModel model = monitor.getResult();
        Smoother smoother = new Smoother();
        smoother.setSsf(model);
        SsfData data = new SsfData(y, null);
        SmoothingResults srslts = new SmoothingResults();
        smoother.process(data, srslts);
        double[] cmp = srslts.component(1);
        System.out.println(new DataBlock(cmp));
   }
    
}
