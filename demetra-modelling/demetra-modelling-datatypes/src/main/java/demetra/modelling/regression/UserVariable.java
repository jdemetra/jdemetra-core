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
package demetra.modelling.regression;

import demetra.design.Development;
import static demetra.modelling.regression.TsVariable.data;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class UserVariable extends TsVariable implements IUserTsVariable {

    public static UserVariable of(String id, ModellingContext context) {
       TsData data = data(id, context);
        if (data == null) {
            return null;
        } else {
            return new UserVariable(id, data);
        }
    }

    private UserVariable(String id, TsData data){
        super(id, data);
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj instanceof UserVariable){
            return equals((TsVariable)obj);
        }else
            return false;
    }

    @Override
    public int hashCode() {
        return hash();
    }
}
