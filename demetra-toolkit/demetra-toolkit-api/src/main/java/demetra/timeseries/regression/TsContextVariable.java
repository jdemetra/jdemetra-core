/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import java.util.List;
import nbbrd.design.Development;

/**
 * This variable should be translated to the correct implementation when the
 * context is known
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class TsContextVariable implements ITsVariable {
    
    @lombok.NonNull
    String id;
    
    int lag;
    
    public TsContextVariable(String id) {
        this.id = id;
        lag = 0;
    }
    
    /**
     * For instance, if lag = -1 we have the following variables:
     * v(t+1)
     *
     * @param id
     * @param lag (negative values = leads)
     */
    public TsContextVariable(String id, int lag) {
        this.id = id;
        this.lag = lag;
    }
    
    public TsContextVariable withId(String nid) {
        if (id.equals(nid)) {
            return this;
        } else {
            return new TsContextVariable(nid, lag);
        }
    }
    
    public TsContextVariable withLag(int nlag) {
        if (nlag == lag) {
            return this;
        } else {
            return new TsContextVariable(id, nlag);
        }
    }

    public boolean isLag() {
        return lag != 0;
    }
    
    @Override
    public int dim() {
        return 1;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return id;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        if (isLag()) {
            StringBuilder builder = new StringBuilder();
            builder.append(id);
            if (lag < 0) {
                builder.append('+').append(-lag).append(')');
            } else {
                builder.append('-').append(lag).append(')');
            }
            return builder.toString();
        } else {
            return id;
        }
    }
    
    public ITsVariable instantiateFrom(ModellingContext context, String desc) {
        UserVariable var = UserVariable.of(id, desc, context);
        if (isLag()) {
            return ModifiedTsVariable.builder()
                    .variable(var)
                    .modifier(new TsLag(lag))
                    .build();
        } else {
            return var;
        }
    }
    
    public static TsContextVariable of(ITsVariable var) {
        if (var instanceof TsContextVariable tvar) {
            return tvar;
        } else if (var instanceof UserVariable user) {
            return new TsContextVariable(user.getId(), 0);
        } else if (var instanceof ModifiedTsVariable mvar) {
            List<ModifiedTsVariable.Modifier> modifiers = mvar.getModifiers();
            if (modifiers.size() == 1 && modifiers.get(0) instanceof TsLag lag && mvar.getVariable() instanceof UserVariable user) {
                return new TsContextVariable(user.getId(), lag.getLag());
            }
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public String toString() {
        if (lag == 0) {
            return id;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('[');
        if (lag < 0) {
            builder.append('+');
        }
        builder.append(-lag);
        return builder.append(']').toString();
        
    }
}
