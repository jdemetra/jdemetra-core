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
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;

/**
 *
 * @author palatej
 */
public class MStlPlusDocument extends AbstractTsDocument<MStlPlusSpec, MStlPlusResults> {

    private final ModellingContext context;

    public MStlPlusDocument() {
        super(MStlPlusSpec.FULL);
        context = ModellingContext.getActiveContext();
    }

    public MStlPlusDocument(ModellingContext context) {
        super(MStlPlusSpec.FULL);
        this.context = context;
    }

    @Override
    protected MStlPlusResults internalProcess(MStlPlusSpec spec, TsData data) {
        return MStlPlusKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

}
