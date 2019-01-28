/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.decomposition;

/**
 *
 * @author palatej
 */
public class JHouseholder {
    
}

//    internal class JHouseholder
//    {
//        // fails if ...
//        public bool Triangularize(Matrix m, int ISsf, int maxRow)
//        {
//            int nr = m.RowsCount, nc = m.ColumnsCount;
//            int n = Math.Min(nr, nc);
//            if (n > maxRow)
//                n = maxRow;
//            if (n == nr)
//                --n;
//            double[] v = new double[nc];
//            for (int r = 0; r < n; ++r)
//            {
//                // Compute 2-norm of k-th row .
//                double jsigma = 0, sigma = 0;
//                for (int c = r + 1; c < nc; ++c)
//                {
//                    double tmp = m[r, c];
//                    if (c < ISsf)
//                        jsigma += tmp * tmp;
//                    else
//                        jsigma -= tmp * tmp;
//                    sigma += tmp * tmp;
//                    v[c] = tmp;
//                }
//
//                // 
//                if (sigma <= m_eps)
//                    continue;
//                double d = m[r, r];
//                double xJx = jsigma + d * d;
//                if (xJx <= 0)
//                    return false;
//
//                double jnrm = Math.Sqrt(xJx);
//                if (d < 0)
//                    v[r] = d - jnrm;
//                else
//                    v[r] = -jsigma / (d + jnrm);
//
//                m[r, r] = jnrm;
//                for (int c = r + 1; c < nc; ++c)
//                    m[r, c] = 0;
//
//                double beta = 2 / (jsigma + v[r] * v[r]);
//
//                // updating the remaining rows:
//                // A P = A - w v', with w = b * A J v
//
//                for (int k = r + 1; k < nr; ++k)
//                {
//                    double s = 0;
//                    for (int c = r; c < nc; ++c)
//                        if (v[c] != 0)
//                            if (c < ISsf)
//                                s += m[k, c] * v[c];
//                            else
//                                s -= m[k, c] * v[c];
//
//                    s *= beta;
//                    for (int c = r; c < nc; ++c)
//                        if (v[c] != 0)
//                            m[k, c] -= s * v[c];
//                }
//            }
//            return true;
//        }
//
//        private double m_eps = 1e-15;
//    }
//
//    /// <summary>
//    /// We consider the matrix 
//    /// | R Z |
//    /// | K L | 
//    /// </summary>
//    internal class UMatrix
//    {
//        internal UMatrix()
//        {
//        }
//
//        internal double R, Z;
//        internal double[] K, L;
//        // fails if ...
//
//        internal bool Triangularize()
//        {
//            // Compute 2-norm of k-th row .
//            double jsigma = -Z * Z;
//            // 
//            if (-jsigma <= m_eps)
//                return true;
//            double xJx = jsigma + R * R;
//            if (xJx <= 0)
//                return false;
//
//            double jnrm = Math.Sqrt(xJx);
//            double v = -jsigma / (R + jnrm), w = Z;
//
//            R = jnrm;
//            Z = 0;
//
//            double beta = 2 / (jsigma + v * v);
//
//            // updating the remaining rows:
//            // A P = A - w v', with w = b * A J v
//
//            int nr = K.Length;
//            for (int k = 0; k < nr; ++k)
//            {
//                double s = (K[k] * v - L[k] * w) * beta;
//                K[k] -= s * v;
//                L[k] -= s * w;
//            }
//            return true;
//        }
//
//        internal double m_eps = 1e-15;
//    }
//
//    public class FastArrayFilter<F>
//        where F : ISsf
//    {
//        public FastArrayFilter()
//        {
//        }
//
//        public IFastArrayInitializer<F> Initializer
//        {
//            get { return m_initializer; }
//            set { m_initializer = value; }
//        }
//
//        public F Ssf
//        {
//            get { return m_ssf; }
//            set { m_ssf = value; }
//        }
//
//        public bool Process(ISsfData data, IFastArrayFilteringResults rslts)
//        {
//            if (m_ssf == null)
//                return false;
//            m_data = data;
//            m_dim = m_ssf.StateDim;
//            m_pos = 0;
//            m_end = m_data.Count;
//            if (!Initialize(rslts))
//                return false;
//            if (rslts != null)
//                rslts.Prepare(m_ssf, data);
//            if (m_pos < m_end)
//            {
//                do
//                {
//                    UpdateE();
//                    if (rslts != null)
//                        rslts.Save(m_pos, m_state);
//                    UpdateA();
//                    //
//                    if (!m_steady)
//                    {
//                        PreArray();
//                        if (!m_matrix.Triangularize())
//                            return false;
//                        //checksteady();
//                    }
//                }
//                while (++m_pos < m_end);
//            }
//            if (rslts != null)
//                rslts.Close();
//
//            return true;
//        }
//
//        public FastArrayState State
//        {
//            get { return m_state; }
//        }
//
//        private bool Initialize(IFastArrayFilteringResults rslts)
//        {
//            m_state = new FastArrayState(m_dim, m_data.HasData);
//            if (m_initializer != null)
//                m_ndiffuse = m_initializer.Initialize(m_ssf, m_data, m_state, rslts);
//            else
//            {
//                IFastArrayInitializer<ISsf> initializer = new FastInitializer();
//                m_ndiffuse = initializer.Initialize(m_ssf, m_data, m_state, rslts);
//            }
//            if (m_ndiffuse < 0)
//                return false;
//
//            m_matrix.K = m_state.K.Data;
//            m_matrix.L = m_state.L.Data;
//            m_pos = m_ndiffuse;
//            m_matrix.R = m_state.r;
//
//            return true;
//        }
//
//        private void PreArray()
//        {
//            //if (m_pos != m_ndiffuse)
//            //{
//            RC l = new RC(m_matrix.L);
//            m_matrix.Z = m_ssf.ZX(m_pos, l);
//            m_ssf.TX(m_pos, l);
//            //}
//        }
//
//        private void UpdateE()
//        {
//            double y = m_data[m_pos];
//            m_state.e = y - m_ssf.ZX(m_pos, m_state.A);
//            m_state.r = m_matrix.R;
//        }
//
//        private void UpdateA()
//        {
//            m_ssf.TX(m_pos, m_state.A);
//            double es = m_state.e / m_state.r;
//            m_state.A.AddAY(es, m_state.K);
//        }
//
//        private void checksteady()
//        {
//            m_steady = false;
//            for (int i = 0; i < m_dim; ++i)
//                if (Math.Abs(m_matrix.L[i]) > m_epsilon)
//                    return;
//            m_steady = true;
//        }
//
//        private int m_pos, m_end, m_dim, m_ndiffuse;
//        private bool m_steady;
//        private UMatrix m_matrix = new UMatrix();
//        private FastArrayState m_state;
//        private IFastArrayInitializer<F> m_initializer;
//        private F m_ssf;
//        private ISsfData m_data;
//        private double m_epsilon = 0;// 1e-9;
//    }
