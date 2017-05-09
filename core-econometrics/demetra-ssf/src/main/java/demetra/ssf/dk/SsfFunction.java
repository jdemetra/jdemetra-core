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
package demetra.ssf.dk;

import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfBuilder;
import demetra.ssf.univariate.ISsfData;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <F>
 */
public class SsfFunction<S, F extends ISsf> implements IFunction, ISsqFunction {

    private final IParametricMapping<S> mapping; // mapping from an array of double to an object S
    private final ISsfBuilder<S, F> builder; // mapping from an object S to a given ssf
    private final ISsfData data;
    private final boolean missing;
    private final Matrix X;
    private final int[] diffuseX;
    private boolean ml = true, log = false, fast = false, mt, sym;

    /**
     *
     * @param ssf
     * @param data
     * @param symderivatives
     * @param mt
     * @param mapper
     */
    public SsfFunction(ISsfData data, IParametricMapping<S> mapper, ISsfBuilder<S, F> builder) {
        this(data, null, null, mapper, builder);
    }

    public SsfFunction(ISsfData data, Matrix X, int[] diffuseX, IParametricMapping<S> mapper, ISsfBuilder<S, F> builder) {
        this.data = data;
        this.mapping = mapper;
        this.builder=builder;
        this.mt = mt;
        this.X = X;
        this.diffuseX = diffuseX;
        missing = data.hasMissingValues();
    }

    public IParametricMapping<S> getMapping() {
        return mapping;
    }

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public void setMaximumLikelihood(boolean ml) {
        this.ml = ml;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }

    @Override
    public IFunctionPoint evaluate(Doubles parameters) {
        return new SsfFunctionInstance<>(this, parameters);
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
    public ISsqFunctionPoint ssqEvaluate(Doubles parameters) {
        return new SsfFunctionInstance<>(this, parameters);
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
     * @return the X
     */
    public Matrix getX() {
        return X;
    }

    /**
     * @return the diffuseX
     */
    public int[] getDiffuseX() {
        return diffuseX;
    }

    /**
     * @return the ml
     */
    public boolean isMl() {
        return ml;
    }

    /**
     * @param ml the ml to set
     */
    public void setMl(boolean ml) {
        this.ml = ml;
    }

    /**
     * @return the mt
     */
    public boolean isMtisMultiThtreaded() {
        return mt;
    }

    /**
     * @param mt the mt to set
     */
    public void setMultiThreaded(boolean mt) {
        this.mt = mt;
    }

    /**
     * @return the sym
     */
    public boolean isSymmetric() {
        return sym;
    }

    /**
     * @param sym the sym to set
     */
    public void setSymmetric(boolean sym) {
        this.sym = sym;
    }
}
