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

import ec.tstoolkit.maths.linearfilters.RationalFilter;
import static ec.tstoolkit.ucarima.WienerKolmogorovPreliminaryEstimatorPropertiesTest.ucmAirline;
import java.text.DecimalFormat;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class WienerKolmogorovEstimatorsTest {

    public WienerKolmogorovEstimatorsTest() {
    }

    @Test
    public void testSRevisions() {
        int n = 4;
        double h = -.2;
        double u = -.2;
        double[][] vars = new double[n * n][];
        double[][] psie = new double[n * n][];
        String[] s = new String[n * n];
        DecimalFormat df2 = new DecimalFormat("0.00");
        for (int i = 0, k = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j, ++k) {
                StringBuilder txt = new StringBuilder();
                txt.append("Airline(").append(df2.format(u + h * i)).append(',').append(df2.format(u + h * j)).append(')');
                s[k] = txt.toString();
                UcarimaModel ucm = ucmAirline(u + h * i, u + h * j);
                WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
                vars[k] = wk.revisionVariance(1, true, -12, 72);
                RationalFilter filter = wk.finalEstimator(1, true).getFilter();
                psie[k]=new double[72];
                for (int l=0; l<psie[k].length; ++l){
                    psie[k][l]=filter.getWeight(l-12);
                }
                
            }
        }

        for (int k = 0; k < s.length; ++k) {
            System.out.print('\t');
            System.out.print(s[k]);
        }
        System.out.println();
        for (int i = 0; i < 72; ++i) {
            System.out.print(i);
            for (int k = 0; k < vars.length; ++k) {
                System.out.print('\t');
                System.out.print(Math.sqrt(vars[k][i]));
            }
            System.out.println();
        }
       for (int k = 0; k < s.length; ++k) {
            System.out.print('\t');
            System.out.print(s[k]);
        }
        System.out.println();
        for (int i = 0; i < 72; ++i) {
            System.out.print(i);
            for (int k = 0; k < psie.length; ++k) {
                System.out.print('\t');
                System.out.print(psie[k][i]);
            }
            System.out.println();
        }

    }

}
