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

package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
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
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ArmaFunction<S extends IArimaModel> implements ISsqFunction, IFunction {

    public final RegModel dmodel;
    public final int[] missings;
    public final IParametricMapping<S> mapper;
    public final int d;
    public IArmaFilter filter;
    public boolean ml = true, llog=false;

    public ArmaFunction(RegModel dmodel, int d, int[] missings, IParametricMapping<S> mapper) {
	this.d=d;
        this.dmodel = dmodel;
	this.missings = missings;
	this.mapper = mapper;
        this.filter = new KalmanFilter(dmodel.getVarsCount()>0);
    }

    @Override
    public ArmaEvaluation<S> evaluate(IReadDataBlock parameters) {
	S tmp = mapper.map(parameters);
	if (tmp == null)
	    return null;
	return new ArmaEvaluation<>(this,
		(S) tmp.stationaryTransformation().stationaryModel);

    }

    @Override
    public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
	return new NumericalDerivatives(this, point, false);
    }

    @Override
    public ISsqFunctionDerivatives getDerivatives(ISsqFunctionInstance point) {
	return new SsqNumericalDerivatives(this, point, false);
    }

    @Override
    public IParametersDomain getDomain() {
	return mapper;
    }

    @Override
    public ISsqFunctionInstance ssqEvaluate(IReadDataBlock parameters) {
	S tmp = mapper.map(parameters);
	if (tmp == null)
	    return null;
	return new ArmaEvaluation<>(this,
		(S) tmp.stationaryTransformation().stationaryModel);
    }

}
