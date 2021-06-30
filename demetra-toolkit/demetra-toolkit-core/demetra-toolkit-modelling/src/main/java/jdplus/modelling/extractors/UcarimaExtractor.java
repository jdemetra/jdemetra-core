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

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import jdplus.arima.IArimaModel;
import jdplus.modelling.ApiUtility;
import jdplus.ucarima.UcarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class UcarimaExtractor extends InformationMapping<UcarimaModel>{

    public final static String COMPONENT="component", COMPLEMENT="complement", MODEL="model", MODELC="modelc", REDUCEDMODEL="reducedmodel", // Component
            SUM="sum",  // Reduced model
            SIZE="size";  // Number of components

    public UcarimaExtractor(){
        set(SIZE, Integer.class, source->source.getComponentsCount());
        set(REDUCEDMODEL, demetra.arima.ArimaModel.class, source->ApiUtility.toApi(source.getModel(), "reducedmodel"));
        delegate(SUM, IArimaModel.class, source->source.getModel());
        delegateArray(COMPONENT, 1, 10, IArimaModel.class, (source, i)
                -> i>source.getComponentsCount()? null : source.getComponent(i-1));
        setArray(MODEL, 1, 10, demetra.arima.ArimaModel.class, (source, i)
                -> i>source.getComponentsCount() ? null : ApiUtility.toApi(source.getComponent(i-1),"cmp"+(i+1)));
        delegateArray(COMPLEMENT, 1, 10, IArimaModel.class, (source, i)
                -> i>source.getComponentsCount()? null : source.getComplement(i-1));
        setArray(MODELC, 1, 10, demetra.arima.ArimaModel.class, (source, i)
                -> i>source.getComponentsCount() ? null : ApiUtility.toApi(source.getComplement(i-1),"cmpc"+(i+1)));
    }

    @Override
    public Class<UcarimaModel> getSourceClass() {
        return UcarimaModel.class;
    }
    
}
