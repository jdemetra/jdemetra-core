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
package demetra.regarima;

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
public class RegArimaDocument extends TsDocument<RegArimaSpec, GeneralLinearModel<SarimaSpec>>{

    public static List<String> additionalItems=Collections.emptyList();

    private final ModellingContext context;
    
    public RegArimaDocument(){
        super(RegArimaSpec.RG4);
        context=ModellingContext.getActiveContext();
    }
    
   public RegArimaDocument(ModellingContext context){
        super(RegArimaSpec.RG4);
        this.context=context;
    }
    
    @Override
    protected GeneralLinearModel<SarimaSpec> internalProcess(RegArimaSpec spec, TsData data) {
        return RegArima.process(data, spec, context, additionalItems);
    }
    
}
