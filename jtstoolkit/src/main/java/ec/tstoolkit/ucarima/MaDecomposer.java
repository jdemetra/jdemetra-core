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

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 *
 * @author Jean Palate
 */
public class MaDecomposer extends SimpleModelDecomposer {

    @Override
    // Q//(P*D) = S/(P*D) + R
    // Q(B) * Q(F) = S(B) * S(R) + R(B) * R(F) * P(B) * P(F) * D(B) * D(R)
    // S(B) * S(F) = R(B) * R(F) * P(B) * P(F) * D(B) * D(R) - Q(B) * Q(F)
    // R*P = Q for Q(n) > (PD(n)
    protected void calc() {
        BackFilter AR = m_model.getAR();
        Polynomial ar = AR.getPolynomial();
        Polynomial q = m_model.getMA().getPolynomial();
        if (q.getDegree() <= ar.getDegree()) {
            m_s = null;
            m_n = m_model;
        } else {
            BackFilter U = m_model.getNonStationaryAR();
            BackFilter P = m_model.getStationaryAR();
            Polynomial p = P.getPolynomial();

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
            SymmetricFilter sq = SymmetricFilter.createFromFilter(new BackFilter(q));
            SymmetricFilter sr = SymmetricFilter.createFromFilter(new BackFilter(r));
            SymmetricFilter spr = SymmetricFilter.createFromFilter(AR.times(fr));
            SymmetricFilter ss = spr.minus(sq);

            m_s = new ArimaModel(null, null, sr);
            m_n = new ArimaModel(P, U, ss);
        }
    }
}
