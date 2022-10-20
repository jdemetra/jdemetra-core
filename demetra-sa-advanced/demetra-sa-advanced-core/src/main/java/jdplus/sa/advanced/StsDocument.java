/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.advanced;

import demetra.processing.ProcessingLog;
import demetra.sa.advanced.StsSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.regression.ModellingContext;

/**
 *
 * @author PALATEJ
 */
public class StsDocument extends AbstractTsDocument<StsSpec, StsResults>  {

    private final ModellingContext context;

    public StsDocument() {
        super(StsSpec.DEF);
        context = ModellingContext.getActiveContext();
    }

    public StsDocument(ModellingContext context) {
        super(StsSpec.DEF);
        this.context = context;
    }
    
    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected StsResults internalProcess(StsSpec spec, TsData data) {
        return StsKernel.of(spec).process(data, ProcessingLog.dummy());
    }

}
