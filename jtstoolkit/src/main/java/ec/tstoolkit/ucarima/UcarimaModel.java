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
@Development(status = Development.Status.Beta)
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
     * @param cmps The list of the components. The constructor doesn't check
     * that the model and the components are compatible.
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
     * Clears the model (by removing all the components)
     */
    public void clear() {
        m_cmps.clear();
        m_model = null;
    }

    /**
     * Creates a clone of this object
     *
     * @return A new object is returned
     */
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
     * Compacts some components of this decomposition
     *
     * @param istart The first component being used in the compacting operation
     * @param count The number of components used in the compacting operation.
     * Should be strictly positive. Moreover, istart+count should be lesser or
     * equal to the number of components of the decomposition. At the end of the
     * operation, the number of components is decreased by count-1.
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
     * Gets the complement of a given component. The complement is the
     * difference between this component and the aggregated model.
     *
     * @param cmp The 0-based index of the considered component.
     * @return A new Arima model is returned. We have that complement(i) +
     * component(i) = sum(). The current implementation computes the complement
     * by making the sum of all the other components. That solution is usually
     * more stable than computing the complement by difference with the
     * aggregated model.
     */
    public ArimaModel getComplement(final int cmp) {
        if (m_cmps.size() <= 1) {
            return new ArimaModel(null, null, null, 0);
        }

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
     * Gets the model of a given component
     *
     * @param idx The 0-based position of the component.
     * @return The (immutable) Arima model
     */
    public ArimaModel getComponent(final int idx) {
        return m_cmps.get(idx);
    }

    /**
     * Gets the number of components
     *
     * @return The number of components. Can be 0 if the model is empty.
     */
    public int getComponentsCount() {
        return m_cmps.size();
    }

    /**
     * Gets the aggregated model.
     *
     * @return The aggregated model. If the model was not provided at the
     * creation of this object, the first call to this method will create it
     * automatically.
     */
    public IArimaModel getModel() {
        if (m_model == null) {
            m_model = sum();
        }
        return m_model;
    }

    /**
     * Gets all the components of the decomposition
     *
     * @return An array with all the components is returned.
     */
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
     * Check that the current decomposition is valid (which means that all the
     * models have a positive innovation variance.
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
     * Makes the decomposition canonical. All the components (except the
     * component at position ncmp, if ncmp is greater than 0) are made non
     * invertible, which means that the maximum noise is removed from them. The
     * noises removed from all the components are then added to the component
     * ncmp. If ncmp is strictly negative, a new one containing all the removed
     * noises is created.
     *
     * @param ncmp The component that will contain the noises. It should be
     * either lesser then the number of the components in the model or strictly
     * negative (-1 by default).
     * @param adjustModel If the sum of the removed noises is negative and the
     * adjustModel parameter is true, the aggregated model is increased by the
     * opposite of that "negative noise". Otherwise, an exception is thrown.
     * @return The sum of the removed noises (which is added to the component
     * ncmp). May be negative if adjustModel is set to true.
     */
    public double setVarianceMax(int ncmp, boolean adjustModel) {
        return setVarianceMax(ncmp, adjustModel, true);
    }

    @Deprecated
    public double setVarianceMax(int ncmp, boolean adjustModel, boolean def) {
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
                    if (def) {
                        min.minimize(m.getSpectrum());
                    } else {
                        min.minimize2(m.getSpectrum());
                    }
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
     * Removes any null Arima model (0 variance and no differencing).
     */
    public void simplify() {
        for (int i = m_cmps.size() - 1; i >= 0; --i) {
            if (m_cmps.get(i).isNull()) {
                m_cmps.remove(i);
            }
        }
    }

    /**
     * Computes the aggregated model, even if it has been provided at the
     * creation of this object or if it has been created through the getModel
     * method. This method can be used to check that the decomposition is
     * coherent with the aggregated model.
     *
     * @return A new Arima model is returned.
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

    /**
     * Normalizes this decomposition. A normalized decomposition is such that
     * variance of the innovations of the aggregated Arima model is equal to 1
     *
     * @return The factor used to normalize the decomposition. If it is equal to
     * 1, the decomposition has not been modified.
     */
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
