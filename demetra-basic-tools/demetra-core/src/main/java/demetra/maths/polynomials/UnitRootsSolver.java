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
package demetra.maths.polynomials;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.IntUtility;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UnitRootsSolver implements IRootsSolver {

    private static double pnorm(final Polynomial p, final int n) {
        if (n == 1) {
            double x = p.evaluateAt(1);
            return Math.abs(x);
        } else if (n == 2) {
            double x = p.evaluateAt(-1);
            return Math.abs(x);
        } else {
            double x = Math.PI * 2 / n;
            final Complex c = Complex.cart(Math.cos(x), Math.sin(x));
            Complex val = p.evaluateAt(c);
            return val.abs();
        }
    }

    private Polynomial m_remainder;

    private UnitRoots m_roots;

    private int m_start = 12;

    private final static double EPS = 1e-12;

    /**
     *
     */
    public UnitRootsSolver() {
    }

    /**
     *
     * @param start
     */
    public UnitRootsSolver(final int start) {
        m_start = start;
    }

    @Override
    public void clear() {
        m_roots = null;
        m_remainder = null;
    }

    @Override
    public boolean factorize(final Polynomial p) {
        m_roots = new UnitRoots();
        m_remainder = process(p, m_roots);
        return m_roots.getRootsCount() != 0;
    }

    /**
     *
     * @return
     */
    public UnitRoots getUnitRoots() {
        return m_roots;

    }

    private Polynomial process(final Polynomial p, final UnitRoots ur) {
        // return p;
        int num = m_start == 0 ? (p.getDegree() * 2) + 1 : m_start; //

        int[] divs = new int[num];
        UnitRoots tmp = new UnitRoots();

        Polynomial cur = p;

        while (num >= 1) {
            if (pnorm(cur, num) < EPS) // should be a root
            {
                tmp.add(num);
                // try it
                int ndiv = IntUtility.divisors(num, divs);
                for (int cdiv = ndiv - 1; cdiv >= 0; --cdiv) {
                    if (pnorm(cur, divs[cdiv]) > EPS) // remove it, and
                    // all their
                    // Divisors
                    {
                        tmp.removeOnly(divs, cdiv);

                        // if (tmp.IsValid)
                    }
                }
                {
                    Polynomial q = UnitRoots.divide(cur, tmp);
                    if (q != null) {
                        cur = q;
                        ur.add(tmp);
                    } else {
                        --num;
                    }
                }
		// else
                // --m_num;
                tmp.clear();
            } else {
                --num;
            }
        }

        return cur;
    }

    @Override
    public Polynomial remainder() {
        return m_remainder;
    }

    @Override
    public Complex[] roots() {
        return m_roots == null ? null : m_roots.roots();

    }

}
