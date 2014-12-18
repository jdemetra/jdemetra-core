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
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultTimeInvariantSsf extends DefaultSsf {

    public static DefaultTimeInvariantSsf of(ISsf ssf) {
        if (!ssf.isTimeInvariant()) {
            return null;
        }
        DefaultTimeInvariantSsf nssf = new DefaultTimeInvariantSsf();
        int dim = ssf.getStateDim(), nd = ssf.getNonStationaryDim(),
                nr = ssf.getTransitionResCount(), rdim = ssf.getTransitionResDim();
        nssf.initialize(dim, nr, rdim);
        // measurement
        ssf.Z(0, new DataBlock(nssf.m_Z));
        // transition
        ssf.T(0, nssf.m_T.subMatrix());
        // innovations
        if (nr < dim) {
            ssf.R(0, SubArrayOfInt.create(nssf.m_R));
        }
        if (ssf.hasW()) {
            ssf.W(0, nssf.m_W.subMatrix());
        }
        ssf.Q(0, nssf.m_Q.subMatrix());
        // initialization
        if (nd > 0) {
            nssf.m_B0 = new Matrix(dim, nd);
            ssf.diffuseConstraints(nssf.m_B0.subMatrix());
        }
        nssf.m_Pf0 = new Matrix(dim, dim);
        ssf.Pf0(nssf.m_Pf0.subMatrix());
        return nssf;
    }

    /**
     *
     */
    public DefaultTimeInvariantSsf() {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasW() {
        return m_W != null;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return true;
    }

    /**
     *
     * @param pos
     * @param q
     * @return
     */
    @Override
    protected boolean loadQ(final int pos, final Matrix q) {
        return true;
    }

    /**
     *
     * @param pos
     * @param r
     * @return
     */
    @Override
    protected boolean loadR(final int pos, final int[] r) {
        return true;
    }

    /**
     *
     * @param pos
     * @param t
     * @return
     */
    @Override
    protected boolean loadT(final int pos, final Matrix t) {
        return true;
    }

    /**
     *
     * @param pos
     * @param w
     * @return
     */
    @Override
    protected boolean loadW(final int pos, final Matrix w) {
        return true;
    }

    /**
     *
     * @param pos
     * @param z
     * @return
     */
    @Override
    protected boolean loadZ(final int pos, final double[] z) {
        return true;
    }

    /**
     *
     * @param q
     */
    public void setQ(final Matrix q) {
        m_Q = q;
    }

    /**
     *
     * @param r
     */
    public void setR(final int[] r) {
        m_R = (r == null) ? null : r.clone();
    }

    /**
     *
     * @param t
     */
    public void setT(final Matrix t) {
        m_T = t;
    }

    /**
     *
     * @param w
     */
    public void setW(final Matrix w) {
        m_W = w;
    }

    // Initialisation
    /**
     *
     * @param z
     */
    public void setZ(final double[] z) {
        m_Z = z.clone();
    }
}
