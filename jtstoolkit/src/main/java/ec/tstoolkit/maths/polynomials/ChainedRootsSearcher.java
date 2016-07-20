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
package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.design.Development;
import java.util.List;
import java.util.Stack;

import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class ChainedRootsSearcher implements IRootsSolver {

    private final Stack<IRootsSolver> m_rootfinders;

    private Complex[] m_roots;

    private Polynomial m_remainder;

    /**
     * Default constructor. The base rootfinding algorithm is set to Muller
     */
    public ChainedRootsSearcher() {
        m_rootfinders = new Stack<>();
        m_rootfinders.push(new MullerNewtonSolver());
        // m_rootfinders.push(new GrantHitchinsSolver());
    }

    /**
     * The constructor takes a RootsSearcher object that will serve as the
     * "base" rootfinding algorithm. This is the algorithm that will be applied
     * after all other algorithms have had a chance to factorize the polynomial.
     *
     * @param baseFinder A RootsSearcher interface pointer.
     */
    public ChainedRootsSearcher(final IRootsSolver baseFinder) {
        m_rootfinders = new Stack<>();
        m_rootfinders.push(baseFinder);
    }

    /**
     * The method adds an algorithm to the chain. The algorithm will be the
     * first to factorize the polynomial.
     *
     * @param rf A RootsSearcher interface pointer representing a root finding
     * algorithm.
     */
    public void add(final IRootsSolver rf) {
        m_rootfinders.push(rf);
    }

    @Override
    public void clear() {
        m_roots = null;
        m_remainder = null;
        for (IRootsSolver searcher : m_rootfinders) {
            searcher.clear();
        }
    }

    /**
     * The method factorizes the polynomial p by applying all the algorithms it
     * has chained. It starts with the last algorithm added and ends with the
     * base algorithm.
     *
     * @param p A polynomial of arbitrary degree
     * @return An array with the roots of p
     */
    @Override
    public boolean factorize(final Polynomial p) {
        m_remainder = p;
        final List<Complex> al = new ArrayList<>(p.getDegree());
        // Polynomial pp = p;
        for (int i = m_rootfinders.size() - 1; (i >= 0)
                && (m_remainder.getDegree() > 0); i--) {
            if (m_rootfinders.get(i).factorize(m_remainder)) {
                final Complex[] r = m_rootfinders.get(i).roots();
                if ((r != null) && (r.length != 0)) {
                    Collections.addAll(al, r);
                }
                m_remainder = m_rootfinders.get(i).remainder();
            }
        }

        if (!al.isEmpty()) {
            m_roots = Jdk6.Collections.toArray(al, Complex.class);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The method removes the topmost algorithm.
     */
    public void pop() {
        m_rootfinders.pop();
    }

    /**
     * The method removes the topmost algorithm if it has the same type as rf.
     *
     * @param rf A RootsSearcher interface pointer
     */
    public void pop(final IRootsSolver rf) {
        IRootsSolver r = m_rootfinders.peek();
        if (r.getClass().equals(rf.getClass())) {
            m_rootfinders.pop();
        }
    }

    @Override
    public Polynomial remainder() {
        return m_remainder;
    }

    @Override
    public Complex[] roots() {
        return m_roots;
    }

    @Override
    public ChainedRootsSearcher exemplar() {
        ChainedRootsSearcher solver = new ChainedRootsSearcher(m_rootfinders.get(0).exemplar());
        for (int i = 1; i < m_rootfinders.size(); ++i) {
            solver.add(m_rootfinders.get(i).exemplar());
        }
        return solver;
    }
}

