/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.regarima;

import demetra.arima.IArimaModel;
import jd.data.DataBlock;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 * @param <M>
 */
public class RegArimaMapping<M extends IArimaModel> implements IParametricMapping<RegArimaModel<M>> {

    private final IParametricMapping<M> mapping;
    private final RegArimaModel.Builder<M> builder;

    public RegArimaMapping(final IParametricMapping<M> mapping, final RegArimaModel<M> model) {
        this.mapping = mapping;
        this.builder = model.toBuilder();
    }

    @Override
    public RegArimaModel<M> map(DoubleSeq p) {
        return builder.arima(mapping.map(p)).build();
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        return mapping.checkBoundaries(inparams);
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        return mapping.epsilon(inparams, idx);
    }

    @Override
    public int getDim() {
        return mapping.getDim();
    }

    @Override
    public double lbound(int idx) {
        return mapping.lbound(idx);
    }

    @Override
    public double ubound(int idx) {
        return mapping.ubound(idx);
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        return mapping.validate(ioparams);
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        return mapping.getDefaultParameters();
    }

}
