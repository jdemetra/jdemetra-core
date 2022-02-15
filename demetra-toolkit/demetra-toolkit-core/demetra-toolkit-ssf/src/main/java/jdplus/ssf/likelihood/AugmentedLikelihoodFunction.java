/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.ssf.likelihood;

import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.IParametricMapping;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfBuilder;
import jdplus.ssf.univariate.ISsfData;
import nbbrd.design.BuilderPattern;
import demetra.data.DoubleSeq;
import jdplus.likelihood.LikelihoodFunction;

/**
 *
 * @author Jean Palate
 * @param <S> Type of the underlying object
 * @param <F> Ssf representation of objects of type S
 */
public class AugmentedLikelihoodFunction<S, F extends ISsf> implements LikelihoodFunction<DiffuseLikelihood> {

    @BuilderPattern(AugmentedLikelihoodFunction.class)
    public static class Builder<S, F extends ISsf> {

        private final IParametricMapping<S> mapping;
        private final ISsfBuilder<S, F> builder;
        private final ISsfData data;
        private boolean ml = true, log = false, fast = false, mt = false, sym = false, scalingFactor = true, res = true, collapsing=true, robust=false;

        private Builder(final ISsfData data, final IParametricMapping<S> mapping, final ISsfBuilder<S, F> builder) {
            this.data = data;
            this.builder = builder;
            this.mapping = mapping;
        }

        public Builder useParallelProcessing(boolean mt) {
            this.mt = mt;
            return this;
        }

        public Builder useMaximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        public Builder useLog(boolean log) {
            this.log = log;
            return this;
        }

        public Builder useFastAlgorithm(boolean fast) {
            this.fast = fast;
            return this;
        }

        public Builder useSymmetricNumericalDerivatives(boolean sym) {
            this.sym = sym;
            return this;
        }

        public Builder useScalingFactor(boolean scalingFactor) {
            this.scalingFactor = scalingFactor;
            if (!scalingFactor) {
                this.log = true;
            } else {
                res = true;
            }
            return this;
        }

        public Builder useCollapsing(boolean collapsing) {
            this.collapsing = collapsing;
            if (collapsing)
                robust=false;
            return this;
        }

        public Builder robust(boolean robust) {
            this.robust = robust;
            if (robust)
                collapsing=false;
            return this;
        }

        public Builder residuals(boolean res) {
            this.res = res;
            return this;
        }

        public AugmentedLikelihoodFunction<S, F> build() {
            return new AugmentedLikelihoodFunction(data, mapping, builder, ml, log, fast, 
                    mt, sym, scalingFactor, res, collapsing, robust);
        }
    }

    public static <S, F extends ISsf> Builder builder(ISsfData data, IParametricMapping<S> mapping, ISsfBuilder<S, F> builder) {
        return new Builder(data, mapping, builder);
    }

    private final IParametricMapping<S> mapping; // mapping from an array of double to an object S
    private final ISsfBuilder<S, F> builder; // mapping from an object S to a given ssf
    private final ISsfData data;
    private final boolean missing;
    private final boolean ml, log, fast, mt, sym, scaling, res, collapsing, robust;

    private AugmentedLikelihoodFunction(ISsfData data, IParametricMapping<S> mapper, ISsfBuilder<S, F> builder,
            final boolean ml, final boolean log, final boolean fast, final boolean mt, 
            final boolean sym, final boolean scaling, final boolean res, final boolean collapsing, final boolean robust) {
        this.data = data;
        this.mapping = mapper;
        this.builder = builder;
        missing = data.hasMissingValues();
        this.ml = ml;
        this.fast = fast;
        this.log = log;
        this.mt = mt;
        this.sym = sym;
        this.scaling = scaling;
        this.res=res;
        this.collapsing=collapsing;
        this.robust=robust;
    }

    public IParametricMapping<S> getMapping() {
        return mapping;
    }

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public boolean isLog() {
        return log;
    }

    public boolean isFast() {
        return fast;
    }

    public boolean isScalingFactor() {
        return scaling;
    }

    public boolean isResiduals() {
        return res;
    }

    public boolean isCollapsing() {
        return collapsing;
    }
    
    public boolean isRobust(){
        return robust;
    }

    @Override
    public AugmentedLikelihoodFunctionPoint<S, F> evaluate(DoubleSeq parameters) {
        return new AugmentedLikelihoodFunctionPoint<>(this, parameters);
    }

    /**
     *
     * @return
     */
    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public AugmentedLikelihoodFunctionPoint<S, F> ssqEvaluate(DoubleSeq parameters) {
        return new AugmentedLikelihoodFunctionPoint<>(this, parameters);
    }

    /**
     * @return the builder
     */
    public ISsfBuilder<S, F> getBuilder() {
        return builder;
    }

    /**
     * @return the data
     */
    public ISsfData getData() {
        return data;
    }

    /**
     * @return the missing
     */
    public boolean isMissing() {
        return missing;
    }

    /**
     * @return the ml
     */
    public boolean isMl() {
        return ml;
    }

    /**
     * @return the mt
     */
    public boolean isMultiThreaded() {
        return mt;
    }

    /**
     * @return the sym
     */
    public boolean isSymmetric() {
        return sym;
    }

}
