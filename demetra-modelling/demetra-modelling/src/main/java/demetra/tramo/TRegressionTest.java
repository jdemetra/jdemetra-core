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

package demetra.tramo;

import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.likelihood.ConcentratedLikelihood;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TRegressionTest implements IRegressionTest {

    private final double tlow, thigh;
    private final int nsig;

    public TRegressionTest(double tlow, double thigh) {
        this.tlow = tlow;
        this.thigh = thigh;
        nsig = 2;
    }

    public TRegressionTest(double tsig) {
        tlow = tsig;
        thigh = tsig;
        nsig = 1;
    }

    @Override
    public boolean accept(ConcentratedLikelihood ll, int nhp, int ireg, int nregs, InformationSet info) {
        double[] t = ll.tstats(nhp, true);
        int nlow = 0, nhigh = 0;
        for (int i = 0; i < nregs; ++i) {
            double ct = Math.abs(t[ireg + i]);
            if (ct >= thigh) {
                ++nhigh;
            } else if (ct >= tlow) {
                ++nlow;
            }
        }
        return nhigh > 0 || nlow >= Math.min(nsig, nregs);
    }
}
