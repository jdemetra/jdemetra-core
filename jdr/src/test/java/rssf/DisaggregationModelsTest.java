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
package rssf;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.models.AR1;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class DisaggregationModelsTest {

    public DisaggregationModelsTest() {
    }

    @Test
    public void testChowLin() {
        int m = Data.PCRA.length;
        int n = Data.IND_PCR.length;
        DataBlock edata = DataBlock.make(n);
        Matrix x = Matrix.make(n, 2);
        x.column(0).set(1);
        x.column(1).copyFrom(Data.IND_PCR, 0);
        edata.set(Double.NaN);
        edata.extract(3, m, 4).copyFrom(Data.PCRA, 0);
        SsfData sdata = new SsfData(edata);
        ISsf ar= AR1.of(.9);
        ISsf rssf = RegSsf.of(ar, x);
        ISsf dssf = DisaggregationModels.of(rssf, 4);
        DefaultSmoothingResults srslts = DkToolkit.smooth(dssf, sdata, true);
        System.out.println(srslts.getComponent(0));
    }

}
