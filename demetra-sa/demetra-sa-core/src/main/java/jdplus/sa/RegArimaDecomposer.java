/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        TsData f = model.deterministicEffect(domain, v->{
            for (int i=0; i<vars.length; ++i)
                if (v == vars[i])
                    return true;
            return false;
        });
        if (! transformed){
            f=model.backTransform(f, type == ComponentType.CalendarEffect);
        }
        return f;
    }
}
