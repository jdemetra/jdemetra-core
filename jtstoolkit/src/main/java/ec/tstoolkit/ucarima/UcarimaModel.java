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

import java.util.ArrayList;
import java.util.List;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.Spectrum;
import ec.tstoolkit.design.Development;
import java.util.Collections;

/**
 * Represents an Unobserved Components Arima model. Such a model is the sum of
 * arima models with independent innovations.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UcarimaModel implements Cloneable {

    private static final double EPS = 1e-6;
    private IArimaModel m_model;
    private ArrayList<ArimaModel> m_cmps = new ArrayList<>();

    /**
     * Creates a new empty Ucarima model
     */
    public UcarimaModel() {
    }

    /**
     * Creates a new Ucarima model corresponding to a given aggregation model
     * and to a given list of models.
     *
     * @param model The aggregation model. Can be null. In that case, the
     * aggregation model will be automatically computed.
     * @param cmps The list of the components @remark The constructor doesn't
     * check that the model and the components are compatible.
     */
    public UcarimaModel(final IArimaModel model, final ArimaModel[] cmps) {
        Collections.addAll(m_cmps, cmps);
        if (model != null) {
            m_model = model;
        }
    }

    /**
     * Creates a new Ucarima model corresponding to a given aggregation model
     * and to a given list of models.
     *
     * @param model The aggregation model. Can be null. In that case, the
     * aggregation model will be automatically computed.
     * @param cmps The list of the components @remark The constructor doesn't
     * check that the model and the components are compatible.
     */
    public UcarimaModel(final IArimaModel model, final List<ArimaModel> cmps) {
        m_cmps.addAll(cmps);
        if (model != null) {
            m_model = model;
            // verify...
//            boolean ok = ArimaModel.same(sum(), model, EPS);
//            if (!ok) {
//                throw new ArimaException(ArimaException.InvalidDecomposition);
//            }
        }
    }

    /**
     * Adds a new component to the model
     *
     * @param model The new component @remark Adding a new component
     * automatically invalidate the aggregation model.
     */
    public void addComponent(final ArimaModel model) {
        m_cmps.add(model);
        m_model = null;
    }

    /**
     * Clears the model
     */
    public void clear() {
        m_cmps.clear();
        m_model = null;
    }

    /**
     * Creates a clone of this object
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public UcarimaModel clone() {
        try {
            UcarimaModel model = (UcarimaModel) super.clone();
            model.m_cmps = (ArrayList<ArimaModel>) m_cmps.clone();
            return model;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param istart
     * @param count
     */
    public void compact(final int istart, final int count) {
        ArimaModel sum = m_cmps.get(istart);
        for (int i = 1; i < count; ++i) {
            sum = sum.plus(m_cmps.get(istart + i), false);
        }
        for (int i = istart + 1; i < istart + 1 + count - 1; i++) {
            m_cmps.remove(i);
        }
        m_cmps.set(istart, sum);
    }

    /**
     *
     * @param cmp
     * @return
     */
    public ArimaModel getComplement(final int cmp) {
        if (m_cmps.size() <= 1) {
            return new ArimaModel(null, null, null, 0);
        }
//        ArimaModel model;
//        if (m_model != null)
//            model=ArimaModel.create(m_model);
//        else
//            model=sum();
//        ArimaModel cur=m_cmps.get(cmp);
//        if (cur.isNull())
//            return model;
//        else
//            return model.minus(cur);

        ArimaModel sum = null;
        for (int i = 0; i < m_cmps.size(); ++i) {
            if (cmp != i) {
                ArimaModel cur = m_cmps.get(i);
                if (!cur.isNull()) {
                    if (sum == null) {
                        sum = cur;
                    } else {
                        sum = sum.plus(cur, false);
                    }
                }
            }
        }
        if (sum == null) {
            return new ArimaModel(null, null, null, 0);
        }
        return sum;

    }

    /**
     *
     * @param idx
     * @return
     */
    public ArimaModel getComponent(final int idx) {
        return m_cmps.get(idx);
    }

    /**
     *
     * @return
     */
    public int getComponentsCount() {
        return m_cmps.size();
    }

    /**
     *
     * @return
     */
    public IArimaModel getModel() {
        if (m_model == null) {
            m_model = sum();
        }
        return m_model;
    }

    public ArimaModel[] getComponents() {
        int n = 0;
        for (ArimaModel cmp : m_cmps) {
            if (cmp != null) {
                ++n;
            }
        }
        ArimaModel[] cmps = new ArimaModel[n];
        n = 0;
        for (ArimaModel cmp : m_cmps) {
            if (cmp != null) {
                cmps[n++] = cmp;
            }
        }
        return cmps;
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        try {
            for (int i = 0; i < m_cmps.size(); ++i) {
                if (m_cmps.get(i) != null && m_cmps.get(i).getInnovationVariance() < 0) {
                    return false;
                }
            }
            return true;
        } catch (ec.tstoolkit.arima.ArimaException e) {
            return false;
        }
    }

    public boolean setVarianceMax(int ncmp) {
        double var = setVarianceMax(ncmp, false);
        return var >= 0;
    }

    /**
     *
     * @param ncmp
     * @return
     */
    public double setVarianceMax(int ncmp, boolean adjustModel) {
        double var = 0;
        if (m_cmps.isEmpty()) {
            return 0;
        }

        if (ncmp < 0) {
            m_cmps.add(new ArimaModel(null, null, null, 0));
            ncmp = m_cmps.size() - 1;
        } else {
            for (int i = m_cmps.size(); i <= ncmp; ++i) {
                m_cmps.add(new ArimaModel(null, null, null, 0));
            }
        }

        Spectrum.Minimizer min = new Spectrum.Minimizer();
        for (int i = 0; i < m_cmps.size(); ++i) {
            if (i != ncmp) {
                ArimaModel m = m_cmps.get(i);
                if (m != null) {
                    min.minimize(m.getSpectrum());
                    if (min.getMinimum() != 0) {
                        var += min.getMinimum();
                        m_cmps.set(i, m.minus(min.getMinimum()));
                    }
                }
            }
        }
        if (var < 0 && adjustModel) {
            m_model = ArimaModel.add(-var, ArimaModel.create(m_model));
        } else {
            m_cmps.set(ncmp, m_cmps.get(ncmp).plus(var));
        }

        return var;
    }

    /**
     *
     */
    public void simplify() {
        for (int i = m_cmps.size() - 1; i >= 0; --i) {
            if (m_cmps.get(i).isNull()) {
                m_cmps.remove(i);
            }
        }
    }

    /**
     *
     * @return
     */
    public final ArimaModel sum() {
        if (m_cmps.isEmpty()) {
            return new ArimaModel(null, null, null, 0);
        }
        ArimaModel sum = m_cmps.get(0);

        for (int i = 1; i < m_cmps.size(); ++i) {
            sum = sum.plus(m_cmps.get(i), false);
        }
        return sum;
    }

    public double normalize() {
        ArimaModel sum = ArimaModel.create(this.getModel());
        double var = sum.getInnovationVariance();
        if (Math.abs(var - 1) < EPS) {
            return 1;
        } else {
            m_model = sum.normalize();
            ArrayList<ArimaModel> tmp = new ArrayList<>();
            for (int i = 0; i < m_cmps.size(); ++i) {
                ArimaModel cur = m_cmps.get(i);
                if (!cur.isNull()) {
                    tmp.add(cur.scaleVariance(1 / var));
                } else {
                    tmp.add(cur);
                }
            }
            m_cmps = tmp;
            return var;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Model: ").append(getModel()).append("\r\n");
        for (int i = 0, j = 0; i < m_cmps.size(); ++i) {
            ArimaModel cmp = m_cmps.get(i);
            if (!cmp.isNull()) {
                builder.append("component").append(++j).append(": ").append(cmp).append("\r\n");
            }
        }
        return builder.toString();
    }
}
