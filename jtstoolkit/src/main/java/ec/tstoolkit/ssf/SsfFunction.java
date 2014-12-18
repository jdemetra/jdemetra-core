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

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametersDomain;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunction;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionDerivatives;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;
import ec.tstoolkit.maths.realfunctions.NumericalDerivatives;
import ec.tstoolkit.maths.realfunctions.SsqNumericalDerivatives;

/**
 * 
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfFunction<S extends ISsf> implements IFunction, ISsqFunction {
    
    private final boolean mt, sym;

    /**
     *
     */
    public final ISsfAlgorithm<S> algorithm;
    /**
     *
     */
    public final IParametricMapping<S> mapper;
    /**
     *
     */
    public final SsfModel<S> model;

    /**
     *
     * @param model
     * @param mapper
     * @param algorithm
     */
    public SsfFunction(SsfModel<S> model, IParametricMapping<S> mapper,
	    ISsfAlgorithm<S> algorithm) {
	this.model = model;
	this.mapper = mapper;
	this.algorithm = algorithm;
        this.mt=false;
        this.sym=false;
    }

    public SsfFunction(SsfModel<S> model, IParametricMapping<S> mapper,
	    ISsfAlgorithm<S> algorithm, boolean symderivatives, boolean mt) {
	this.model = model;
	this.mapper = mapper;
	this.algorithm = algorithm;
        this.mt=mt;
        this.sym=symderivatives;
    }

    @Override
    public IFunctionInstance evaluate(IReadDataBlock parameters) {
	return new SsfFunctionInstance<>(this, parameters);
    }

    @Override
    public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
	return new NumericalDerivatives(this, point, sym, mt);
    }

    @Override
    public ISsqFunctionDerivatives getDerivatives(ISsqFunctionInstance point) {
	return new SsqNumericalDerivatives(this, point, sym, mt);
    }

    /**
     *
     * @return
     */
    @Override
    public IParametersDomain getDomain() {
	return mapper;
    }

    @Override
    public ISsqFunctionInstance ssqEvaluate(IReadDataBlock parameters) {
	return new SsfFunctionInstance<>(this, parameters);
    }
}
