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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SeparateRegressionTest implements IRegressionTest {

    private double tlow_, thigh_;
    private int nsig_;

    public SeparateRegressionTest(double tlow, double thigh) {
        tlow_ = tlow;
        thigh_ = thigh;
        nsig_ = 2;
    }

    public SeparateRegressionTest(double tsig) {
        tlow_ = tsig;
        thigh_ = tsig;
        nsig_ = 1;
    }

    @Override
    public boolean accept(ConcentratedLikelihood ll, int nhp, int ireg, int nregs, InformationSet info) {
        double[] t = ll.getTStats(nhp >= 0, nhp);
        int nlow = 0, nhigh = 0;
        for (int i = 0; i < nregs; ++i) {
            double ct = Math.abs(t[ireg + i]);
            if (ct >= thigh_) {
                ++nhigh;
            } else if (ct >= tlow_) {
                ++nlow;
            }
        }
        return nhigh > 0 || nlow >= Math.min(nsig_, nregs);

    }
}
