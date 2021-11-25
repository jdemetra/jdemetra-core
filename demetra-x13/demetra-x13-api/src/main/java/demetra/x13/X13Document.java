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
package demetra.x13;

import demetra.timeseries.TsData;
import demetra.timeseries.TsDocument;
import demetra.timeseries.regression.ModellingContext;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class X13Document extends TsDocument<X13Spec, X13Results>{
    
    public static List<String> additionalItems=Collections.emptyList();

    private final ModellingContext context;
    
    public X13Document(){
        super(X13Spec.RSA4);
        context=ModellingContext.getActiveContext();
    }
    
   public X13Document(ModellingContext context){
        super(X13Spec.RSA4);
        this.context=context;
    }
    

    @Override
    protected X13Results internalProcess(X13Spec spec, TsData data) {
        return X13.process(data, spec, context, additionalItems);
    }
    
}
