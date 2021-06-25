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
package demetra.toolkit.extractors;

import demetra.arima.ArimaModel;
import demetra.arima.UcarimaModel;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class UcarimaExtractor extends InformationMapping<UcarimaModel> {

    public final static String COMPONENT = "component", MODEL = "model", REDUCEDMODEL = "reducedmodel", // Component
            SUM = "sum", // Reduced model
            SIZE = "size";  // Number of components

    public UcarimaExtractor() {
        set(SIZE, Integer.class, source -> source.size());
        set(REDUCEDMODEL, demetra.arima.ArimaModel.class, source -> source.getSum());
        delegate(SUM, ArimaModel.class, source -> source.getSum());
        delegateArray(COMPONENT, 1, 10, ArimaModel.class, (source, i)
                -> i > source.size() ? null : source.getComponent(i - 1));
    }

    @Override
    public Class getSourceClass() {
        return UcarimaModel.class;
    }

}
