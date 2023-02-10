/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.mstlplus;

import demetra.processing.ProcessingLog;
import demetra.stl.MStlPlusSpec;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;

/**
 *
 * @author palatej
 */
public class MStlPlusDocument extends AbstractTsDocument<MStlPlusSpec, MStlPlusResults> {
    
    private final ModellingContext context;
    
    public MStlPlusDocument() {
        super(MStlPlusSpec.DEFAULT);
        context = ModellingContext.getActiveContext();
    }
    
    public MStlPlusDocument(ModellingContext context) {
        super(MStlPlusSpec.DEFAULT);
        this.context = context;
    }
    
     @Override
    public void set(MStlPlusSpec spec, Ts s) {
        if (s != null) {
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        } else {
            super.set(spec, s);
        }
    }

    @Override
    public void set(MStlPlusSpec spec) {
        Ts s = getInput();
        if (s != null) {
            super.set(spec.withPeriod(s.getData().getTsUnit()));
        } else {
            super.set(spec);
        }
    }

    @Override
    public void set(Ts s) {
        if (s == null) {
            set(s);
        } else {
            MStlPlusSpec spec = getSpecification();
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        }
    }

    @Override
    protected MStlPlusResults internalProcess(MStlPlusSpec spec, TsData data) {
        // modify the spec and prepare data according to the time series
        return MStlPlusKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }
    
}
