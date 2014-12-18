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
package ec.tstoolkit.timeseries.simplets.chainlinking;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;

/**
 *
 * @author Mats Maggi
 */
public class AnnualOverlapTest {

    @Test
    public void testAnnualOverlap() {
        double[] qa = new double[]{56.4, 62.4, 65.7, 66.5, 67.4, 69.4, 71.5, 73.7, 76, 78.3, 80.6, 83.1, 85.5, 88.2, 90.8, 93.5};
        double[] qb = new double[]{57.6, 58, 59, 61.4, 57.6, 57.1, 56.5, 55.8, 55.4, 54.8, 54.2, 53.6, 53.2, 52.7, 52.1, 52};
        double[] pa = new double[]{6.2, 6.5, 7.6, 7.7, 6.1, 5.7, 5.3, 5, 4.5, 4.3, 3.8, 3.5, 3.4, 3.1, 2.8, 2.7};
        double[] pb = new double[]{5.4, 5.6, 6.2, 6.8, 8.0, 8.6, 9.4, 10.0, 10.7, 11.5, 11.7, 12.1, 12.5, 13, 13.8, 14.7};
        
        TsData QAq = new TsData(TsFrequency.Quarterly, 1997, 0, qa, true);
        TsData QBq = new TsData(TsFrequency.Quarterly, 1997, 0, qb, true);
        TsData PAq = new TsData(TsFrequency.Quarterly, 1997, 0, pa, true);
        TsData PBq = new TsData(TsFrequency.Quarterly, 1997, 0, pb, true);
        
        AnnualOverlap o = new AnnualOverlap();
        o.setRefYear(1997);
        
        o.addProduct("Product A", QAq, PAq);
        o.addProduct("Product B", QBq, PBq);
        
        o.process();
    }
}
