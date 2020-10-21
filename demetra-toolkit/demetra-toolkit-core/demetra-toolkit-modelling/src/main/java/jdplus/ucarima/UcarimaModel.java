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

import jdplus.arima.ArimaException;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.arima.Spectrum;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import java.util.ArrayList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents an Unobserved Components Arima model. Such a model is the sum of
 * arima models with independent innovations.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class UcarimaModel implements Cloneable {

    /**
     * Creates a new Ucarima model corresponding to a given aggregation model
     * and to a given list of models.
     *
     */
    @BuilderPattern(UcarimaModel.class)
    public static class Builder {

        private IArimaModel model;
        private ArrayList<ArimaModel> components = new ArrayList<>();
        private boolean verify = false;

        private Builder() {
        }

        public UcarimaModel build() {

            if (verify && model != null) {
                ArimaModel sum = sum();
                if (!ArimaModel.same(model, sum, EPS)) {
                    throw new UcarimaException();
                }
            }
            return new UcarimaModel(model == null ? sum() : model, components.toArray(new ArimaModel[components.size()]));
        }

        public Builder verify(boolean verify) {
            this.verify = verify;
            return this;
        }

        public Builder add(ArimaModel component) {
            this.components.add(component);
            return this;
        }

        public Builder model(IArimaModel model) {
            this.model = model;
            return this;
        }

        public Builder add(@NonNull ArimaModel... components) {
            for (int i = 0; i < components.length; ++i) {
                this.components.add(components[i]);
            }

            return this;
        }

        /**
         * Computes the aggregated model, even if it has been provided at the
         * creation of this object or if it has been created through the
         * getModel method. This method can be used to check that the
         * decomposition is coherent with the aggregated model.
         *
         * @return A new Arima model is returned.
         */
        public final ArimaModel sum() {
            if (components.isEmpty()) {
                return new ArimaModel(null, null, null, 0);
            }
            ArimaModel sum = components.get(0);

            for (int i = 1; i < components.size(); ++i) {
                sum = sum.plus(components.get(i), false);
            }
            return sum;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private static final double EPS = 1e-6;
    private final IArimaModel model;
    private final ArimaModel[] components;

    /**
     * Creates a new empty Ucarima model
     */
    UcarimaModel(final IArimaModel model, final ArimaModel[] cmps) {
        this.model = model;
        this.components = cmps;
    }

    /**
     * Compacts some components of this decomposition
     *
     * @param istart The first component being used in the compacting operation
     * @param count The number of components used in the compacting operation.
     * Should be strictly positive. Moreover, istart+count should be lesser or
     * equal to the number of components of the decomposition. At the end of the
     * operation, the number of components is decreased by count-1.
     * @return
     */
    public UcarimaModel compact(final int istart, final int count) {
        Builder builder = new Builder();
        builder.model(model);
        for (int i = 0; i < istart; ++i) {
            builder.add(components[i]);
        }
        ArimaModel sum = components[istart];
        for (int i = 1; i < count; ++i) {
            sum = sum.plus(components[istart + i], false);
        }
        builder.add(sum);
        for (int i = istart + count; i < components.length; i++) {
            builder.add(components[i]);
        }
        return builder.build();
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
        if (components.length <= 1) {
            return ArimaModel.NULL;
        }

        ArimaModel sum = null;
        for (int i = 0; i < components.length; ++i) {
            if (cmp != i) {
                ArimaModel cur = components[i];
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
            return ArimaModel.NULL;
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
        return components[idx];
    }

    /**
     * Gets the number of components
     *
     * @return The number of components. Can be 0 if the model is empty.
     */
    public int getComponentsCount() {
        return components.length;
    }

    /**
     * Gets the aggregated model.
     *
     * @return The aggregated model. If the model was not provided at the
     * creation of this object, the first call to this method will create it
     * automatically.
     */
    public IArimaModel getModel() {
        return model;
    }
    
    private ArimaModel of(ArimaModel m){
        return new ArimaModel(m.getStationaryAr(), m.getNonStationaryAr(), m.getMa(), m.getInnovationVariance());
    }

    public ArimaModel sum() {
        ArimaModel s = null;
        for (int i = 0; i < components.length; ++i) {
            if (!components[i].isNull()) {
                if (s == null) {
                    s = of(components[i]);
                } else {
                    s = s.plus(of(components[i]), false);
                }
            }
        }
        return s;
    }

    /**
     * Gets all the components of the decomposition
     *
     * @return An array with all the components is returned.
     */
    public ArimaModel[] getComponents() {
        int n = 0;
        for (ArimaModel cmp : components) {
            if (cmp != null) {
                ++n;
            }
        }
        ArimaModel[] cmps = new ArimaModel[n];
        n = 0;
        for (ArimaModel cmp : components) {
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
            for (int i = 0; i < components.length; ++i) {
                if (components[i].getInnovationVariance() < 0) {
                    return false;
                }
            }
            return true;
        } catch (ArimaException e) {
            return false;
        }
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
     * @return The new decomposition.
     */
    public UcarimaModel setVarianceMax(int ncmp, boolean adjustModel) {
        double var = 0;
        if (components.length == 0) {
            return this;
        }

        ArimaModel[] ncmps;
        int n = components.length;
        if (ncmp < 0) {
            ncmps = new ArimaModel[n + 1];
            System.arraycopy(components, 0, ncmps, 0, n);
            ncmps[n] = ArimaModel.NULL;
            ncmp = n;
        } else {
            ncmps = components.clone();
        }

        Spectrum.Minimizer min = new Spectrum.Minimizer();
        for (int i = 0; i < ncmps.length; ++i) {
            if (i != ncmp) {
                ArimaModel m = ncmps[i];
                if (m != null) {
                    min.minimize(m.getSpectrum());
                    if (min.getMinimum() != 0) {
                        var += min.getMinimum();
                        ncmps[i] = m.minus(min.getMinimum());
                    }
                }
            }
        }
        IArimaModel nmodel = model;
        if (var < 0 && adjustModel) {
            nmodel = ArimaModel.add(-var, ArimaModel.of(model));
        } else {
            ncmps[ncmp] = ncmps[ncmp].plus(var);
        }
        return new UcarimaModel(nmodel, ncmps);
    }

    /**
     * Removes any null Arima model (0 variance and no differencing).
     */
    public UcarimaModel simplify() {
        Builder builder = new Builder();
        builder.model = model;
        for (int i = 0; i < components.length; ++i) {
            if (!components[i].isNull()) {
                builder.add(components[i]);
            }
        }
        return builder.build();
    }

    /**
     * Normalizes this decomposition. A normalized decomposition is such that
     * variance of the innovations of the aggregated Arima model is equal to 1
     *
     * @return The factor used to normalize the decomposition. If it is equal to
     * 1, the decomposition has not been modified.
     */
    public UcarimaModel normalize() {
        double var = model.getInnovationVariance();
        if (Math.abs(var - 1) < EPS) {
            return this;
        } else {
            ArimaModel nmodel = ArimaModel.of(model).normalize();
            ArimaModel[] ncmps = components.clone();
            for (int i = 0; i < components.length; ++i) {
                ArimaModel cur = components[i];
                if (!cur.isNull()) {
                    ncmps[i] = ncmps[i].scaleVariance(1 / var);
                }
            }
            return new UcarimaModel(nmodel, ncmps);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Model: ").append(getModel()).append(System.lineSeparator());
        for (int i = 0, j = 0; i < components.length; ++i) {
            ArimaModel cmp = components[i];
            if (!cmp.isNull()) {
                builder.append("component").append(++j).append(": ").append(cmp).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
