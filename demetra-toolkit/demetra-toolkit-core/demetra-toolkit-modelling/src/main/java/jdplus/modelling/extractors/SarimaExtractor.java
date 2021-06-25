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
package jdplus.modelling.extractors;

import demetra.arima.ISarimaModel;
import demetra.information.InformationDelegate;
import demetra.information.InformationExtractor;
import jdplus.sarima.SarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class SarimaExtractor extends InformationDelegate<SarimaModel, ISarimaModel>{
    
    public SarimaExtractor(){
        super(v->v);
    }

    @Override
    public Class<ISarimaModel> getDelegateClass() {
        return ISarimaModel.class;
    }

    @Override
    public Class<SarimaModel> getSourceClass() {
        return SarimaModel.class;
    }
    
}
