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
package demetra.ucarima;

import demetra.arima.ArimaModel;
import static demetra.ucarima.UcarimaModelTest.ucmAirline;
import jdplus.maths.linearfilters.RationalFilter;
import static demetra.ucarima.UcarimaModelTest.ucm3111;
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
    public void testAirline() {
        int N=72;
        UcarimaModel ucm = ucmAirline(-.8, -.6);
        WienerKolmogorovEstimators wke = new WienerKolmogorovEstimators(ucm);
        double[][] psie = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            if (!cmp.isNull()) {
                WienerKolmogorovEstimator wk = wke.finalEstimator(i, true);
                RationalFilter filter = wk.getFilter();
                psie[i] = new double[N];
                for (int l = 0; l < psie[i].length; ++l) {
                    psie[i][l] = filter.weight(l - 12);
                }
            }
        }
        for (int l = 0; l < N; ++l) {
            double s=0;
            for (int i = 0; i < ucm.getComponentsCount(); ++i) {
                if (psie[i]!= null)
                    s+=psie[i][l];
            }
            assertEquals(s, l!=12 ? 0 : 1, 1e-12);
        }
    }

    @Test
    public void test3111() {
        int N=72;
        UcarimaModel ucm = ucm3111(new double[]{.2, -.5, .1}, -.9, -.9);
        WienerKolmogorovEstimators wke = new WienerKolmogorovEstimators(ucm);
        double[][] psie = new double[ucm.getComponentsCount()][];
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            if (!cmp.isNull()) {
                WienerKolmogorovEstimator wk = wke.finalEstimator(i, true);
                RationalFilter filter = wk.getFilter();
                psie[i] = new double[N];
                for (int l = 0; l < N; ++l) {
                    psie[i][l] = filter.weight(l - 12);
                }
            }
        }
        for (int l = 0; l < N; ++l) {
            double s=0;
            for (int i = 0; i < ucm.getComponentsCount(); ++i) {
                if (psie[i]!= null)
                    s+=psie[i][l];
            }
            assertEquals(s, l!=12 ? 0 : 1, 1e-12);
        }
    }
}
