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

package ec.benchmarking.ssf;

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.ssf.ISsf;

/**
 * 
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfDisaggregationMapper<S extends ISsf> implements
        IParametricMapping<SsfDisaggregation<S>> {

    /**
     *
     */
    public final IParametricMapping<S> internalMapper;
    /**
     *
     */
    public final int conversion;

    /**
     * 
     * @param mapper
     * @param conv
     */
    public SsfDisaggregationMapper(IParametricMapping<S> mapper, int conv) {
        internalMapper = mapper;
        this.conversion = conv;
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return internalMapper.checkBoundaries(inparams);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return internalMapper.epsilon(inparams, idx);
    }

    @Override
    public int getDim() {
        return internalMapper.getDim();
    }

    @Override
    public double lbound(int idx) {
        return internalMapper.lbound(idx);
    }

    @Override
    public IReadDataBlock map(SsfDisaggregation<S> t) {
        return internalMapper.map(t.getInternalSsf());
    }

    @Override
    public SsfDisaggregation<S> map(IReadDataBlock p) {
        S ssf = internalMapper.map(p);
        return new SsfDisaggregation<>(conversion, ssf);
    }

    @Override
    public double ubound(int idx) {
        return internalMapper.ubound(idx);
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        return internalMapper.validate(ioparams);
    }

    @Override
    public String getDescription(int idx) {
        return internalMapper.getDescription(idx); //To change body of generated methods, choose Tools | Templates.
    }
}
