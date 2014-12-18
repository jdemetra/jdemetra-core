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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;

/**
 *
 * @author pcuser
 */
public class DerivedRegressionTest implements IRegressionTest {

    private double t_;
    private double vcur_, tcur_;

    public DerivedRegressionTest(double tsig) {
        t_ = tsig;
    }

    public double getValue() {
        return vcur_;
    }

    public double getTStat() {
        return t_;
    }

    @Override
    public boolean accept(ConcentratedLikelihood ll, int nhp, int ireg, int nregs, InformationSet info) {
        vcur_ = -new DataBlock(ll.getB(), ireg, ireg + nregs, 1).sum();
        double v = ll.getBVar(nhp >= 0, nhp).subMatrix(ireg, ireg + nregs, ireg, ireg + nregs).sum();
        tcur_ = vcur_ / Math.sqrt(v);
        return Math.abs(tcur_) > t_;
    }
}
