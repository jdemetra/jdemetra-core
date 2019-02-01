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

package demetra.regarima.regular;

import demetra.data.Doubles;
import demetra.information.InformationSet;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class DerivedRegressionTest implements IRegressionTest {

    private final double tsig;
    private double vcur, tcur;
    private final boolean ml;

    public DerivedRegressionTest(double tsig, boolean ml) {
        this.tsig = tsig;
        this.ml=ml;
    }

    public double getValue() {
        return vcur;
    }
    
    public double getTStat() {
        return tcur;
    }

    @Override
    public boolean accept(ConcentratedLikelihoodWithMissing ll, int nhp, int ireg, int nregs, InformationSet info) {
        vcur=-Doubles.sum(ll.coefficients().extract(ireg, nregs));
        
        Matrix V=Matrix.of(ll.unscaledCovariance().extract(ireg, nregs, ireg, nregs));
        int ndf = ml ? ll.dim() : ll.degreesOfFreedom()-nhp;
        double ssq=ll.ssq();
        double v=V.sum()*ssq/ndf;
        tcur = vcur / Math.sqrt(v);
        return Math.abs(tcur) > tsig;
    }
}
