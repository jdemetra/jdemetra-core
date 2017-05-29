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

package ec.benchmarking.cholette;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.DoubleList;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Ar1Variance implements IVariance {

    private static int gMin_ = 100;
    private double ro_;
    private DoubleList powRo_ = new DoubleList(gMin_);

    /**
     * 
     * @param ro
     */
    public Ar1Variance(double ro) {
        ro_ = ro;
        powRo_.add(1);
        double cur = ro_;
        powRo_.add(cur);
        do {
            cur *= ro_;
            powRo_.add(cur);
        } while (powRo_.size() < gMin_);
    }

    private void extendtop(int p) {
        double cur = powRo_.get(powRo_.size() - 1);
        do {
            cur *= ro_;
            powRo_.add(cur);
        } while (powRo_.size() <= p);

    }

    private double rop(int p) {
        if (p >= powRo_.size()) {
            extendtop(p);
        }
        return powRo_.get(p);
    }

    /**
     * 
     * @param i
     * @param j
     * @return
     */
    public double var(int i, int j) {
        return rop(Math.abs(i - j));
    }
}
