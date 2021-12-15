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
package demetra.tramo;

import demetra.modelling.implementations.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDocument;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class TramoDocument extends TsDocument<TramoSpec, GeneralLinearModel<SarimaSpec>>{

    public static List<String> additionalItems=Collections.emptyList();

    private final ModellingContext context;
    
    public TramoDocument(){
        super(TramoSpec.TRfull);
        context=ModellingContext.getActiveContext();
    }
    
   public TramoDocument(ModellingContext context){
        super(TramoSpec.TRfull);
        this.context=context;
    }
    
    @Override
    protected GeneralLinearModel<SarimaSpec> internalProcess(TramoSpec spec, TsData data) {
        return Tramo.process(data, spec, context, additionalItems);
    }
    
}
