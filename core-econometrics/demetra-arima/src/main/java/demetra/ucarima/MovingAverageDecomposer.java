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

package demetra.ucarima;

import demetra.arima.ArimaModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.polynomials.Polynomial;


/**
 *
 * @author Jean Palate
 */
public class MovingAverageDecomposer extends SimpleModelDecomposer {

    @Override
    // Q//(P*D) = S/(P*D) + R
    // Q(B) * Q(F) = S(B) * S(R) + R(B) * R(F) * P(B) * P(F) * D(B) * D(R)
    // S(B) * S(F) = R(B) * R(F) * P(B) * P(F) * D(B) * D(R) - Q(B) * Q(F)
    // R*P = Q for Q(n) > (PD(n)
    protected void calc() {
        BackFilter AR = model.getAR();
        Polynomial ar = AR.asPolynomial();
        Polynomial q = model.getMA().asPolynomial();
        if (q.getDegree() <= ar.getDegree()) {
            signal = null;
            noise = model;
        } else {
            BackFilter U = model.getNonStationaryAR();
            BackFilter P = model.getStationaryAR();
            Polynomial p = P.asPolynomial();

            Polynomial.Division div = Polynomial.divide(q, ar);
            Polynomial r = div.getQuotient();
            // normalize r...
            // q(-) = ar(-)*r(-) -> r(-) = q(-)/ar(-)
            double cq = q.get(q.getDegree());
            double cp = ar.get(ar.getDegree());
            double cr = r.get(r.getDegree());
            double c0 = r.get(0);

            r = r.times(cq / (cr * cp));
            BackFilter fr = new BackFilter(r);
            // compute the remainder:
            SymmetricFilter sq = SymmetricFilter.fromFilter(new BackFilter(q));
            SymmetricFilter sr = SymmetricFilter.fromFilter(new BackFilter(r));
            SymmetricFilter spr = SymmetricFilter.fromFilter(AR.times(fr));
            SymmetricFilter ss = spr.minus(sq);

            signal = new ArimaModel(BackFilter.ONE, BackFilter.ONE, sr);
            noise = new ArimaModel(P, U, ss);
        }
    }
}
