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
package ec.tstoolkit.structural;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * The basic structural model is defined as follows l(t+1) = l(t) + n(t) + u(t)
 * n(t+1) = n(t) + v(t) S(B) s(t) = M(B) w(t) y(t) = l(t) + s(t) + e(t)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BasicStructuralModel implements ISsf, Cloneable {

    private static Matrix _tsvar(int freq) {
        int n = freq - 1;
        Matrix M = new Matrix(n, freq);
        M.diagonal().set(1);
        M.column(n).set(-1);
        Matrix O = SymmetricMatrix.XXt(M);
        Householder qr = new Householder(false);
        qr.decompose(O);
        Matrix Q = qr.solve(M);
        Matrix H = new Matrix(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }

        return SymmetricMatrix.XXt(Q.times(H));
    }

    private static ComponentUse getUse(double var) {
        if (var < 0) {
            return ComponentUse.Unused;
        } else if (var == 0) {
            return ComponentUse.Fixed;
        } else {
            return ComponentUse.Free;
        }
    }

    private static double getVar(ComponentUse use) {
        if (use == ComponentUse.Unused) {
            return -1;
        } else if (use == ComponentUse.Fixed) {
            return 0;
        } else {
            return 1;
        }
    }

    private static double getVar(SeasonalModel use) {
        if (use == SeasonalModel.Unused) {
            return -1;
        } else if (use == SeasonalModel.Fixed) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     *
     * @param freq
     * @param sm
     * @param Q
     */
    public static void initQSeas(int freq, SeasonalModel sm, SubMatrix Q) {
        // Dummy
        if (sm == SeasonalModel.Dummy) {
            Q.set(0);
            Q.set(freq - 2, freq - 2, 1);
        } else if (sm == SeasonalModel.Crude) {
            Q.set(1);
        } else if (sm == SeasonalModel.HarrisonStevens) {
            // HarrisonStevens
            double v = 1.0 / freq;
            Q.set(-v);
            Q.diagonal().add(1);
        } else {
            Q.copy(tsVar(freq).subMatrix());
        }
    }

    private static void svar(int freq, SubMatrix O) {
        int n = freq - 1;
        Matrix H = new Matrix(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }

        SymmetricMatrix.XXt(H.subMatrix(), O);
    }

    /**
     *
     * @param freq
     * @return
     */
    public static Matrix tsVar(int freq) {
        if (freq == 12) {
            if (g_VTS12 == null) {
                g_VTS12 = _tsvar(12);
            }
            return g_VTS12;
        } else if (freq == 4) {
            if (g_VTS4 == null) {
                g_VTS4 = _tsvar(4);
            }
            return g_VTS4;
        } else if (freq == 2) {
            if (g_VTS2 == null) {
                g_VTS2 = _tsvar(2);
            }
            return g_VTS2;
        } else if (freq == 3) {
            if (g_VTS3 == null) {
                g_VTS3 = _tsvar(3);
            }
            return g_VTS3;
        } else if (freq == 6) {
            if (g_VTS6 == null) {
                g_VTS6 = _tsvar(6);
            }
            return g_VTS6;
        } else {
            return _tsvar(freq);
        }
    }
    /**
     *
     */
    public final int freq;
    private int[] m_cmps;
    private Matrix m_tsvar;
    double lVar, sVar, seasVar, cVar, nVar;
    double cDump, cPeriod;
    private double ccos, csin;
    SeasonalModel seasModel;
    private static Matrix g_VTS2, g_VTS3, g_VTS4, g_VTS6, g_VTS12;

    /**
     *
     * @param spec
     * @param freq
     */
    public BasicStructuralModel(ModelSpecification spec, int freq) {
        this.freq = freq;
        seasModel = spec.getSeasonalModel();
        seasVar = getVar(seasModel);
        lVar = getVar(spec.lUse);
        sVar = getVar(spec.sUse);
        cVar = getVar(spec.cUse);
        nVar = getVar(spec.nUse);
        if (spec.cUse != ComponentUse.Unused){
            cycle(.5, freq*2);
        }
    }

    public UcarimaModel computeReducedModel(boolean normalized) {
        UcarimaModel ucm = new UcarimaModel();
        // trend.
        BackFilter D = BackFilter.D1;
        if (lVar >= 0 && sVar >= 0) {
            if (lVar == 0 && sVar == 0) {
                ucm.addComponent(new ArimaModel(null, D.times(D), null, 0));
            } else if (lVar == 0) {
                ucm.addComponent(new ArimaModel(null, D.times(D), null, sVar));
            } else if (sVar == 0) {
                ucm.addComponent(new ArimaModel(null, D.times(D), D, lVar));

            } else {
                ArimaModel ml = new ArimaModel(null, null, D, lVar);
                ml = ml.plus(sVar);
                ucm.addComponent(new ArimaModel(null, D.times(D), ml.sma()));
            }
        } else if (lVar >= 0) {// sVar < 0
            ucm.addComponent(new ArimaModel(null, D, null, lVar));
        } else {
            ucm.addComponent(new ArimaModel(null, null, null, 0)); // null model
        }
        //seasonal
        if (seasVar >= 0) {
            BackFilter S = new BackFilter(UnitRoots.S(freq, 1));
            if (seasVar > 0) {
                SymmetricFilter sma;
                if (seasModel != SeasonalModel.Dummy) {
                    // ma is the first row of the v/c innovations
                    Matrix O = new Matrix(freq, freq);
                    switch (seasModel) {
                        case Crude:
                            O.subMatrix().set(1);
                            break;

                        case HarrisonStevens:
                            O.subMatrix().set(-1.0 / freq);
                            O.diagonal().add(1);
                            break;
                        case Trigonometric:
                            svar(freq, O.subMatrix());
                            break;
                        default:
                            break;
                    }

                    double[] w = new double[freq - 1];
                    for (int i = 0; i <= freq - 1; ++i) {
                        for (int j = 1; j <= freq - 1 - i; ++j) {
                            SubMatrix s = O.subMatrix(0, j, 0, j + i);
                            w[i] += s.sum();
                        }
                    }
                    sma = SymmetricFilter.of(w);
                    sma = sma.times(seasVar);
                } else {
                    sma = SymmetricFilter.of(new double[]{seasVar});
                }
                ucm.addComponent(new ArimaModel(null, S, sma));
            } else {
                ucm.addComponent(new ArimaModel(null, S, null, 0));

            }
        } else {
            ucm.addComponent(new ArimaModel(null, null, null, 0));
        }

        if (nVar > 0) {
            ucm.addComponent(new ArimaModel(null, null, null, nVar));
        } else {
            ucm.addComponent(new ArimaModel(null, null, null, 0));
        }
        if (normalized) {
            ucm.normalize();
        }
        return ucm;
    }

    /**
     *
     */
    protected final void calcCmpsIndexes() {
        int n = 0;
        if (nVar > 0) {
            ++n;
        }
        if (cVar >= 0) {
            ++n;
        }
        if (lVar >= 0) {
            ++n;
        }
        if (seasVar >= 0) {
            ++n;
        }
        m_cmps = new int[n];
        int i = 0, j = 0;
        if (nVar > 0) {
            m_cmps[i++] = j++;
        }
        if (cVar >= 0) {
            m_cmps[i++] = j;
            j += 2;
        }
        if (lVar >= 0) {
            m_cmps[i++] = j++;
        }
        if (sVar >= 0) {
            ++j;
        }
        if (seasVar >= 0) {
            m_cmps[i++] = j;
        }
    }

    @Override
    public BasicStructuralModel clone() {
        try {
            BasicStructuralModel bsm = (BasicStructuralModel) super.clone();
            return bsm;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param p
     */
    @Override
    public void diffuseConstraints(SubMatrix p) {
        int sdim = getStateDim();
        int istart = nVar > 0 ? 1 : 0;
        int iend = sdim;
        for (int i = istart, j = 0; i < iend; ++i, ++j) {
            p.set(i, j, 1);
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public Component fixMaxVariance(double val) {
        Component max = getMaxVariance();
        if (max != Component.Undefined) {
            double vmax = getVariance(max);
            if (vmax != val) {
                scaleVariances(val / vmax);
            }
        }
        return max;
    }

    /**
     *
     * @param eps
     * @return
     */
    public boolean fixSmallVariance(double eps) {
        Component min = getMinVariance();
        if (min != Component.Undefined && getVariance(min) < eps) {
            setVariance(min, 0);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param pos
     * @param q
     */
    @Override
    public void fullQ(int pos, SubMatrix q) {
        int i = 0;
        if (nVar > 0) {
            q.set(i, i, nVar);
            ++i;
        }
        if (cVar >= 0) {
            q.set(i, i, cVar);
            ++i;
            q.set(i, i, cVar);
            ++i;
        }
        if (lVar >= 0) {
            if (lVar != 0) {
                q.set(i, i, lVar);
            }
            ++i;
        }
        if (sVar >= 0) {
            if (sVar != 0) {
                q.set(i, i, sVar);
            }
            ++i;
        }
        if (seasVar > 0) {
            initQSeas(q.extract(i, i + freq - 1, i, i + freq - 1), seasVar);
        }
    }

    /**
     *
     * @return
     */
    public int[] getCmpPositions() {
        int[] cmp = new int[getCmpsCount()];
        int idx = 0, i = 0;
        if (nVar > 0) {
            cmp[idx++] = i++;
        }
        if (cVar >= 0) {
            cmp[idx++] = i;
            i += 2;
        }
        if (lVar >= 0) {
            cmp[idx++] = i++;
            if (sVar >= 0) {
                cmp[idx++] = i++;
            }
        }
        if (seasVar >= 0) {
            cmp[idx] = i;
        }

        return cmp;
    }

    /**
     *
     * @return
     */
    public int getCmpsCount() {
        int n = 0;
        if (nVar > 0) {
            ++n;
        }
        if (cVar >= 0) {
            ++n;
        }
        if (lVar >= 0) {
            ++n;
            if (sVar >= 0) {
                ++n;
            }
        }
        if (seasVar >= 0) {
            ++n;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public Component[] getComponents() {
        Component[] cmp = new Component[getCmpsCount()];
        int idx = 0;
        if (nVar > 0) {
            cmp[idx++] = Component.Noise;
        }
        if (cVar >= 0) {
            cmp[idx++] = Component.Cycle;
        }
        if (lVar >= 0) {
            cmp[idx++] = Component.Level;
            if (sVar >= 0) {
                cmp[idx++] = Component.Slope;
            }
        }
        if (seasVar >= 0) {
            cmp[idx] = Component.Seasonal;
        }

        return cmp;
    }

    /**
     *
     * @return
     */
    public Component getMaxVariance() {
        Component cmp = Component.Undefined;
        double vmax = 0;
        if (lVar > vmax) {
            vmax = lVar;
            cmp = Component.Level;
        }
        if (sVar > vmax) {
            vmax = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > vmax) {
            vmax = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > vmax) {
            vmax = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > vmax) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    /**
     *
     * @return
     */
    public Component getMinVariance() {
        Component cmp = Component.Undefined;
        double vmin = Double.MAX_VALUE;
        if (lVar > 0 && lVar < vmin) {
            vmin = lVar;
            cmp = Component.Level;
        }
        if (sVar > 0 && sVar < vmin) {
            vmin = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > 0 && seasVar < vmin) {
            vmin = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > 0 && cVar < vmin) {
            vmin = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > 0 && nVar < vmin) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        int r = 0;
        if (lVar >= 0) {
            ++r;
        }
        if (sVar >= 0) {
            ++r;
        }
        if (seasVar >= 0) {
            r += freq - 1;
        }
        return r;
    }

    private SeasonalModel getSeas() {
        if (seasVar < 0) {
            return SeasonalModel.Unused;
        } else if (seasVar == 0) {
            return SeasonalModel.Fixed;
        } else {
            return seasModel;
        }
    }

    /**
     *
     * @return
     */
    public ModelSpecification getSpecification() {
        ModelSpecification spec = new ModelSpecification();
        spec.setSeasonalModel(getSeas());
        spec.useLevel(getUse(lVar));
        spec.useSlope(getUse(sVar));
        spec.useCycle(getUse(cVar));
        spec.useNoise(nVar <= 0 ? ComponentUse.Unused : ComponentUse.Free);
        return spec;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        int r = 0;
        if (nVar > 0) {
            ++r;
        }
        if (cVar >= 0) {
            r += 2;
        }
        if (lVar >= 0) {
            ++r;
        }
        if (sVar >= 0) {
            ++r;
        }
        if (seasVar >= 0) {
            r += freq - 1;
        }
        return r;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        int nr = 0;
        if (seasVar > 0) {
            if (seasModel == SeasonalModel.Dummy) {
                ++nr;
            } else {
                nr += freq - 1;
            }
        }
        if (nVar > 0) {
            ++nr;
        }
        if (cVar > 0) {
            nr += 2;
        }
        if (lVar > 0) {
            ++nr;
        }
        if (sVar > 0) {
            ++nr;
        }
        return nr;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        int nr = 0;
        if (seasVar > 0) {
            if (seasModel == SeasonalModel.Dummy
                    || seasModel == SeasonalModel.Crude) {
                ++nr;
            } else {
                nr += freq - 1;
            }
        }
        if (nVar > 0) {
            ++nr;
        }
        if (cVar > 0) {
            nr+=2;
        }
        if (lVar > 0) {
            ++nr;
        }
        if (sVar > 0) {
            ++nr;
        }
        return nr;
    }

    /**
     *
     * @param cmp
     * @return
     */
    public double getVariance(Component cmp) {
        switch (cmp) {
            case Noise:
                return nVar;
            case Cycle:
                return cVar;
            case Level:
                return lVar;
            case Slope:
                return sVar;
            case Seasonal:
                return seasVar;
        }
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        if (lVar == 0) {
            return true;
        }
        if (sVar == 0) {
            return true;
        }
        if (cVar >= 0) {
            return true;
        }
        return seasVar == 0 || seasModel == SeasonalModel.Dummy;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasW() {
        return seasModel == SeasonalModel.Crude;
    }

    /**
     *
     * @param Q
     * @param var
     */
    protected void initQSeas(SubMatrix Q, double var) {
        // Dummy
        if (seasModel == SeasonalModel.Dummy) {
            Q.set(0, 0, var);
        } else if (seasModel == SeasonalModel.Crude) {
            Q.set(var);
        } else if (seasModel == SeasonalModel.HarrisonStevens) {
            double v = var / freq;
            Q.set(-v);
            Q.diagonal().add(var);
        } else {
            // Trigonometric
            initTSVar();
            Q.copy(m_tsvar.subMatrix());
            Q.mul(var);
        }
        SymmetricMatrix.fromLower(Q);
    }

    /**
     *
     */
    protected void initTSVar() {
        if (m_tsvar != null && m_tsvar.getRowsCount() == freq - 1) {
            return;
        }
        m_tsvar = tsVar(freq);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
        return isValid();
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
     * @return
     */
    @Override
    public boolean isValid() {
        if (freq == 1 && seasVar >= 0) {
            return false;
        }
        return lVar >= 0 || sVar >= 0 || cVar >= 0 || nVar >= 0;
    }

    /**
     *
     * @param pos
     * @param k
     * @param l
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix l) {
        T(pos, l);
        // Z = (1, [[0], 1], .... C'Z null except for some first columns
        if (m_cmps == null) {
            calcCmpsIndexes();
        }
        int n = m_cmps.length;

        for (int j = 0; j < n; ++j) {
            l.column(m_cmps[j]).sub(k);
        }
    }

    /**
     *
     * @param p
     */
    @Override
    public void Pf0(SubMatrix p) {
        // FullQ(p);
        int i = 0;
        if (nVar > 0) {
            p.set(0, 0, nVar);
            ++i;
        }
        if (cVar > 0) {
            double q = cVar / (1 - cDump * cDump);
            p.set(i, i, q);
            ++i;
            p.set(i, i, q);
        }
    }

    /**
     *
     * @param p
     */
    @Override
    public void Pi0(SubMatrix p) {
        int sdim = getStateDim();
        int istart = nVar > 0 ? 1 : 0;
        if (cVar >= 0) {
            istart += 2;
        }
        int iend = sdim;
        for (int i = istart; i < iend; ++i) {
            p.set(i, i, 1);
        }
    }

    /**
     *
     * @param pos
     * @param q
     */
    @Override
    public void Q(int pos, SubMatrix q) {
        int i = 0;
        if (nVar > 0) {
            q.set(i, i, nVar);
            ++i;
        }
        if (cVar > 0) {
            q.set(i, i, cVar);
            ++i;
            q.set(i, i, cVar);
            ++i;
        }
        if (lVar > 0) {
            q.set(i, i, lVar);
            ++i;
        }
        if (sVar > 0) {
            q.set(i, i, sVar);
            ++i;
        }
        if (seasVar > 0) {
            if (seasModel == SeasonalModel.Dummy
                    || seasModel == SeasonalModel.Crude) {
                q.set(i, i, seasVar);
            } else {
                initQSeas(q.extract(i, i + freq - 1, i, i + freq - 1), seasVar);
            }
        }
    }

    /**
     *
     * @param pos
     * @param r
     */
    @Override
    public void R(int pos, SubArrayOfInt r) {
        int i = 0, j = 0;
        if (nVar > 0) {
            r.set(i++, j);
            ++j;
        }
        if (cVar >= 0) {
            if (cVar != 0) {
                r.set(i++, j);
                r.set(i++, j+1);
            }
            j+=2;
        }
        if (lVar >= 0) {
            if (lVar != 0) {
                r.set(i++, j);
            }
            ++j;
            if (sVar >= 0) {
                if (sVar != 0) {
                    r.set(i++, j);
                }
                ++j;
            }
        }
        if (seasVar > 0) {
            if (seasModel == SeasonalModel.Dummy) {
                r.set(i, j);
            } else {
                for (int k = 1; k < freq; ++k) {
                    r.set(i++, j++);
                }
            }
        }
    }

    /**
     *
     * @param factor
     */
    public void scaleVariances(double factor) {
        if (lVar > 0) {
            lVar *= factor;
        }
        if (cVar > 0) {
            cVar *= factor;
        }
        if (sVar > 0) {
            sVar *= factor;
        }
        if (seasVar > 0) {
            seasVar *= factor;
        }
        if (nVar > 0) {
            nVar *= factor;
        }
        if (factor == 0) {
            m_cmps = null;
        }
    }

    /**
     *
     * @param cmp
     * @param var
     */
    public void setVariance(Component cmp, double var) {
        if (var == 0) {
            m_cmps = null;
        }
        switch (cmp) {
            case Noise:
                if (nVar > 0) {
                    nVar = var;
                }
                return;
            case Cycle:
                if (cVar >= 0) {
                    cVar = var;
                }
                return;
            case Level:
                if (lVar >= 0) {
                    lVar = var;
                }
                return;
            case Slope:
                if (sVar >= 0) {
                    sVar = var;
                }
                return;
            case Seasonal:
                if (seasVar >= 0) {
                    seasVar = var;
                    if (var == 0) {
                        seasModel = SeasonalModel.Fixed;
                    }
                }
        }
    }
    
    public void setCycle(double cro, double cperiod){
        cycle(cro, cperiod);
    }
    
     private void cycle(double cro, double cperiod){
       cDump=cro;
        cPeriod=cperiod;
        double q=Math.PI*2/cperiod;
        ccos=cDump*Math.cos(q);
        csin=cDump*Math.sin(q);
    }
    
    public double getCyclicalDumpingFactor(){
        return cDump;
    }
    
    public double getCyclicalPeriod(){
        return cPeriod;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        int i = 0;
        if (nVar > 0) {
            ++i;
        }
        if (cVar >= 0) {
            tr.set(i,i,ccos);
            tr.set(i+1,i+1,ccos);
            tr.set(i,i+1,csin);
            tr.set(i+1,i,-csin);
            i+=2;
        }
        if (lVar >= 0) {
            tr.set(i, i, 1);
            if (sVar >= 0) {
                tr.set(i, i + 1, 1);
                ++i;
                tr.set(i, i, 1);
            }
            ++i;
        }
        if (seasVar >= 0) {
            SubMatrix seas = tr.extract(i, i + freq - 1, i, i + freq - 1);
            seas.row(0).set(-1);
            seas.subDiagonal(-1).set(1);
        }
    }

    /**
     *
     * @param pos
     * @param V
     */
    @Override
    public void TVT(int pos, SubMatrix V) {
        DataBlockIterator cols = V.columns();
        DataBlock col = cols.getData();
        do {
            TX(pos, col);
        } while (cols.next());

        DataBlockIterator rows = V.rows();
        DataBlock row = rows.getData();
        do {
            TX(pos, row);
        } while (rows.next());

    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        int i0 = 0;
        if (nVar > 0) {
            x.set(0, 0);
            ++i0;
        }
        if (cVar >= 0) {
            double a=x.get(i0), b=x.get(i0+1);
            x.set(i0, a*ccos+b*csin);
            x.set(i0+1, -a*csin+b*ccos);
            i0+=2;
        }
        if (lVar >= 0) {
            if (sVar >= 0) {
                x.add(i0, x.get(i0 + 1));
                i0 += 2;
            } else {
                ++i0;
            }
        }
        if (seasVar >= 0) {
            DataBlock ex = x.extract(i0, freq - 1, 1);
            ex.fshift(DataBlock.ShiftOption.NegSum);
        }
    }

    /**
     *
     */
    protected void updateStructure() {
        calcCmpsIndexes();
    }

    /**
     *
     * @param pos
     * @param V
     * @param d
     */
    @Override
    public void VpZdZ(int pos, SubMatrix V, double d) {
        if (m_cmps == null) {
            calcCmpsIndexes();
        }
        int n = m_cmps.length;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                V.add(m_cmps[i], m_cmps[j], d);
            }
        }
    }

    /**
     *
     * @param pos
     * @param w
     */
    @Override
    public void W(int pos, SubMatrix w) {
        if (seasModel == SeasonalModel.Crude) {
            int nr = 0, nc=0;
            if (nVar > 0) {
                w.set(nr, nc, 1);
                ++nr;
                ++nc;
            }
            if (cVar > 0) {
                w.set(nr, nc, 1);
                ++nr;
                ++nc;
                w.set(nr, nc, 1);
                ++nr;
                ++nc;
            }
            if (lVar > 0) {
                w.set(nr, nc, 1);
                ++nr;
                ++nc;
            }
            if (sVar > 0) {
                w.set(nr, nc, 1);
                ++nr;
                ++nc;
           }
            if (seasVar > 0 && seasModel == SeasonalModel.Crude) {
                for (int i = 0; i < freq - 1; ++i) {
                    w.set(nr + i, nc, 1);
                }
            }else{
                w.set(nr, nc, 1);
            }
        }
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        if (m_cmps == null) {
            calcCmpsIndexes();
        }
        int n = m_cmps.length;
        for (int i = 0; i < n; ++i) {
            x.add(m_cmps[i], d);
        }
    }

    // backwards operation
    /**
     *
     * @param pos
     * @param xin
     */
    @Override
    public void XT(int pos, DataBlock xin) {
        int i0 = 0;
        if (nVar > 0) {
            xin.set(0, 0);
            ++i0;
        }
        if (cVar>=0){
            double a=xin.get(i0), b=xin.get(i0+1);
            xin.set(i0, a*ccos-b*csin);
            xin.set(i0+1, a*csin+b*ccos);
            i0+=2;
            
        }
        if (lVar >= 0) {
            if (sVar >= 0) {
                xin.add(i0 + 1, xin.get(i0));
                i0 += 2;
            } else {
                ++i0;
            }
        }
        if (seasVar >= 0) {
            int imax = i0 + freq - 2;
            double x0 = xin.get(i0);
            for (int i = i0; i < imax; ++i) {
                xin.set(i, xin.get(i + 1) - x0);
            }
            xin.set(imax, -x0);
        }
    }

    /**
     *
     * @param pos
     * @param z
     */
    @Override
    public void Z(int pos, DataBlock z) {
        int i = 0;
        if (nVar > 0) {
            z.set(i++, 1);
        }
        if (cVar > 0) {
            z.set(i, 1);
            i+=2;
        }
        if (lVar >= 0) {
            z.set(i++, 1);
            if (sVar >= 0) {
                ++i;
            }
        }
        if (seasVar >= 0) {
            z.set(i, 1);
        }
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        DataBlockIterator cols = m.columns();
        DataBlock col = cols.getData();
        int i = 0;
        do {
            x.set(i++, ZX(pos, col));
        } while (cols.next());
    }

    /**
     *
     * @param pos
     * @param V
     * @return
     */
    @Override
    public double ZVZ(int pos, SubMatrix V) {
        double d = 0;
        if (m_cmps == null) {
            calcCmpsIndexes();
        }
        int n = m_cmps.length;
        for (int i = 0; i < n; ++i) {
            d += V.get(m_cmps[i], m_cmps[i]);
            for (int j = 0; j < i; ++j) {
                d += 2 * V.get(m_cmps[i], m_cmps[j]);
            }
        }
        return d;
    }

    /**
     *
     * @param pos
     * @param xin
     * @return
     */
    @Override
    public double ZX(int pos, DataBlock xin) {
        if (m_cmps == null) {
            calcCmpsIndexes();
        }
        int n = m_cmps.length;
        double d = xin.get(m_cmps[0]);
        for (int i = 1; i < n; ++i) {
            d += xin.get(m_cmps[i]);
        }
        return d;
    }
}
