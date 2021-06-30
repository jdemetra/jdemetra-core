/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.regarima.extractors;

import demetra.information.InformationDelegate;
import demetra.information.InformationExtractor;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import jdplus.regsarima.regular.RegSarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class GenericExtractor extends InformationDelegate<RegSarimaModel, GeneralLinearModel>{
    
    public GenericExtractor(){
        super(v->v);
    }

    @Override
    public Class<GeneralLinearModel> getDelegateClass() {
        return GeneralLinearModel.class;
    }

    @Override
    public Class<RegSarimaModel> getSourceClass() {
        return RegSarimaModel.class;
    }
    
}
