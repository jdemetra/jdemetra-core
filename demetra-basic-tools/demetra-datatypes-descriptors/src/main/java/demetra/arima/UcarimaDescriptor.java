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
package demetra.arima;

import demetra.arima.ArimaType;
import demetra.arima.UcarimaType;
import demetra.design.Development;
import demetra.information.InformationMapping;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class UcarimaDescriptor {

    final static String COMPONENT="component", MODEL="model", REDUCEDMODEL="reducedmodel", // Component
            SUM="sum",  // Reduced model
            SIZE="size";  // Number of components

    static final InformationMapping<UcarimaType> MAPPING = new InformationMapping<>(UcarimaType.class);

    static {
        MAPPING.set(SIZE, Integer.class, source->source.size());
        MAPPING.set(REDUCEDMODEL, ArimaType.class, source->source.getSum());
        MAPPING.delegate(SUM, ArimaDescriptor.getMapping(), source->source.getSum());
        MAPPING.delegateArray(COMPONENT, 1, 10, ArimaDescriptor.getMapping(), (source, i)
                -> i>source.size() ? null : source.getComponent(i-1));
        MAPPING.setArray(MODEL, 1, 10, ArimaType.class, (source, i)
                -> i>source.size() ? null : source.getComponent(i-1));
    }

    public InformationMapping<UcarimaType> getMapping() {
        return MAPPING;
    }
    
}