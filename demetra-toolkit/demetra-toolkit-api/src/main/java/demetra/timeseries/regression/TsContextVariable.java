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

    int firstLag, lastLag;

    public TsContextVariable(String id) {
        this.id = id;
        firstLag = 0;
        lastLag = 0;
    }

    public TsContextVariable(String id, int lag) {
        this.id = id;
        firstLag = lag;
        lastLag = lag;
    }

    /**
     * first &le last !!
     *
     * For instance, if first = -1 and last = 2
     * we have the following variables:
     * v(t+1), v(t), v(t-1), v(t-2)
     *
     * @param id
     * @param first First lag (negative values = leads)
     * @param last Last lag (negative values = leads)
     */
    public TsContextVariable(String id, int first, int last) {
        this.id = id;
        firstLag = first;
        lastLag = last;
    }

    public boolean isLag() {
        return firstLag != 0 || lastLag != 0;
    }

    @Override
    public int dim() {
        return lastLag - firstLag + 1;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return id;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        if (isLag()) {
             StringBuilder builder = new StringBuilder();
            int lag = firstLag + idx;
            builder.append(id);
            if (lag < 0) {
                builder.append("+").append(-lag).append(')');
            } else {
                builder.append("-").append(lag).append(')');
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
                    .modifier(new TsLags(firstLag, lastLag))
                    .build();
        } else {
            return var;
        }
    }
}
