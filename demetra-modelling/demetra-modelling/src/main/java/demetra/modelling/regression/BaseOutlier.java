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

package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.time.Period;
import demetra.timeseries.TimeSeriesDomain;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class BaseOutlier{


    protected final LocalDateTime position;
    protected final String name;

    protected BaseOutlier(LocalDateTime pos, String name) {
        position = pos;
        this.name = name;
    }
    
    public abstract String getCode();

    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append(getCode()).append(" (");
        if (context == null) {
            builder.append(position);
        } else {
            TsPeriod p = context.get(0);
            p.withDate(position);
            builder.append(p);
        }
        builder.append(')');
        return builder.toString();
    }

}
