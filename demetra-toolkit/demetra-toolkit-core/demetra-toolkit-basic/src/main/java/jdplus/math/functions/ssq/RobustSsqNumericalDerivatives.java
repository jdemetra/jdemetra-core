/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.functions.ssq;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import internal.jdplus.maths.functions.gsl.derivation.GslDerivation;
import java.util.function.DoubleFunction;
import jdplus.data.DataBlock;
import jdplus.math.functions.IFunction;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author PALATEJ
 */
public class RobustSsqNumericalDerivatives implements ISsqFunctionDerivatives {

    private static final int NTHREADS = Runtime.getRuntime().availableProcessors();

    private DoubleSeq[] m_de;
    private double[] m_grad;
    private FastMatrix m_h;
    private final ISsqFunction fn;
    private DoubleSeq m_pt;
    private DoubleSeq m_ecur;
    private final boolean m_sym, m_mt;
    private static int g_nsteps = 2;

    /**
     *
     * @param point
     */
    public RobustSsqNumericalDerivatives(ISsqFunctionPoint point) {
        this(point, true, true);
    }

    public RobustSsqNumericalDerivatives(ISsqFunctionPoint point, boolean sym, boolean mt) {
        m_sym = sym;
        m_mt = mt;
        fn = point.getSsqFunction();
        m_ecur = point.getE();
        m_pt = point.getParameters();
    }

    /**
     *
     * @param point
     * @param sym
     */
    public RobustSsqNumericalDerivatives(ISsqFunctionPoint point,
            boolean sym) {
        this(point, sym, false);
    }

    private void calcgrad() {
        int n = m_pt.length();
        m_grad = new double[n];
        m_de = new DoubleSeq[n];

        int ne = m_ecur.length();
        for (int i = 0; i < n; ++i) {
            double eps = fn.getDomain().epsilon(m_pt, i);
            DoubleSeq de = dfndi(i, eps);
            m_grad[i] = 2 * m_ecur.dot(de);
            m_de[i] = de;
        }
    }

    private DoubleFunction<DoubleSeq> fni(int idx) {

        return (x) -> {
            if (x == m_pt.get(idx)) {
                return m_ecur;
            }
            DataBlock pcur = DataBlock.of(m_pt);
            pcur.set(idx, x);
            ISsqFunctionPoint fn = this.fn.ssqEvaluate(pcur);
            return fn.getE();
        };
    }

    // compute the derivative for the parameter i
    private DoubleSeq dfndi(int idx, double h) {
        DoubleFunction<DoubleSeq> fni=fni(idx);
        DoubleSeq fp=fni.apply(m_pt.get(idx)+h);
        
        DoubleSeq del = DoublesMath.subtract(fp, m_ecur);
        if (del.ssq()*10>= del.length()*h*h)
            return del.times(1/h);
        
        try {
            return GslDerivation.centralDerivation(fni(idx), m_pt.get(idx), h);
        } catch (Exception err) {
        }
        try {
            return GslDerivation.forwardDerivation(fni(idx), m_pt.get(idx), h);
        } catch (Exception err) {
        }
        try {
            return GslDerivation.backwardDerivation(fni(idx), m_pt.get(idx), h);
        } catch (Exception err) {
            return DoubleSeq.onMapping(m_ecur.length(), i -> 0);
        }
    }

    private void calch() {
        if (m_grad == null) {
            calcgrad();
        }
        int n = m_grad.length;
        m_h = FastMatrix.square(n);
        // compute first the diagonal
        for (int i = 0; i < n; ++i) {
            DoubleSeq de = m_de[i];
            m_h.set(i, i, 2 * de.ssq());
        }
        // other elements
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                DoubleSeq dei = m_de[i];
                DoubleSeq dej = m_de[j];
                double z = 2 * dei.dot(dej);
                m_h.set(i, j, z);
                m_h.set(j, i, z);
            }
        }
    }

     @Override
    public IFunction getFunction() {
        return fn.asFunction();
    }

    /**
     * Computes d e(t,p)/dp(i)
     *
     * @param idx
     * @return
     */
    @Override
    public DoubleSeq dEdX(int idx) {
        if (m_de == null) {
            calcgrad();
        }
        return m_de[idx];
    }

    private DoubleSeq err(int i, double dx) {
        try {
            if (dx == 0) {
                return m_ecur;
            }
            DataBlock pcur = DataBlock.of(m_pt);
            pcur.add(i, dx);
            ISsqFunctionPoint fn = this.fn.ssqEvaluate(pcur);
            return fn.getE();
        } catch (Exception err) {
            return m_ecur;
        }
    }

    /**
     * Gets the gradient of the function. = sum(e(t,p) * d e(t,p)/dp(i))
     *
     * @return
     */
    @Override
    public DoubleSeq gradient() {
        if (m_grad == null) {
            calcgrad();
        }
        return DataBlock.of(m_grad);
    }

    /**
     *
     * @param m
     * @return
     */
    @Override
    public void jacobian(FastMatrix m) {
        if (m_de == null) {
            calcgrad();
        }
        for (int i = 0; i < m_de.length; ++i) {
            m.column(i).copy(m_de[i]);
        }
    }

    /**
     * Gets the hessian of the function. h(i,j) is approximated by
     * sum(de(t,p)/dp(i)*de(t,p)/dp(j)) We consider that d^2e/dp(i)dp(j) is
     * negligible, which is not necessary true.
     *
     * @param h
     */
    @Override
    public void hessian(FastMatrix h) {
        if (m_h == null) {
            calch();
        }
        h.copy(m_h);
    }

 }
