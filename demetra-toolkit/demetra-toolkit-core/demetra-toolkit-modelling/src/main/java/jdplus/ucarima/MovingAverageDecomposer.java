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

package jdplus.ucarima;

import jdplus.arima.ArimaModel;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;


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
        BackFilter AR = model.getAr();
        Polynomial ar = AR.asPolynomial();
        Polynomial q = model.getMa().asPolynomial();
        if (q.degree() <= ar.degree()) {
            signal = null;
            noise = model;
        } else {
            BackFilter U = model.getNonStationaryAr();
            BackFilter P = model.getStationaryAr();
            Polynomial p = P.asPolynomial();

            Polynomial.Division div = Polynomial.divide(q, ar);
            Polynomial r = div.getQuotient();
            // normalize r...
            // q(-) = ar(-)*r(-) -> r(-) = q(-)/ar(-)
            double cq = q.get(q.degree());
            double cp = ar.get(ar.degree());
            double cr = r.get(r.degree());
            double c0 = r.get(0);

            r = r.times(cq / (cr * cp));
            BackFilter fr = new BackFilter(r);
            // compute the remainder:
            SymmetricFilter sq = SymmetricFilter.convolutionOf(new BackFilter(q));
            SymmetricFilter sr = SymmetricFilter.convolutionOf(new BackFilter(r));
            SymmetricFilter spr = SymmetricFilter.convolutionOf(AR.times(fr));
            SymmetricFilter ss = spr.minus(sq);

            signal = new ArimaModel(BackFilter.ONE, BackFilter.ONE, sr);
            noise = new ArimaModel(P, U, ss);
        }
    }
}
