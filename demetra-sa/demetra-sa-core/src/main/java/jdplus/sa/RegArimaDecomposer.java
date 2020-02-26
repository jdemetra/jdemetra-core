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
package jdplus.sa;

import demetra.sa.ComponentType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITsVariable;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */

public class RegArimaDecomposer {
    
    private final ModelEstimation model;
    private final SaVariablesMapping mapping;
    
    public static RegArimaDecomposer of(final ModelEstimation model,
            final SaVariablesMapping mapping){
        return new RegArimaDecomposer(model, mapping);
    }
    
    private RegArimaDecomposer(final ModelEstimation model,
            final SaVariablesMapping mapping){
        this.model=model;
        this.mapping=mapping;
    }
    
    public TsData deterministicEffect(TsDomain domain, ComponentType type, boolean transformed){
        final ITsVariable[] vars = mapping.forComponentType(type);
        TsData f = getModel().deterministicEffect(domain, v->{
            for (int i=0; i<vars.length; ++i)
                if (v == vars[i])
                    return true;
            return false;
        });
        if (! transformed){
            f=getModel().backTransform(f, type == ComponentType.CalendarEffect);
        }
        return f;
    }

    /**
     * @return the model
     */
    public ModelEstimation getModel() {
        return model;
    }
}
